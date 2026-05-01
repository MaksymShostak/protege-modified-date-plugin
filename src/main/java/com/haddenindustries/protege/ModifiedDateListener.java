package com.haddenindustries.protege;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.SwingUtilities;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModifiedDateListener implements OWLOntologyChangeListener {
    
    private static final Logger log = LoggerFactory.getLogger(ModifiedDateListener.class);
    
    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;
    
    // CRITICAL: A thread-safe flag to prevent the listener from triggering itself
    private final AtomicBoolean isUpdating = new AtomicBoolean(false); 

    public ModifiedDateListener(OWLOntologyManager manager) {
        this.manager = manager;
        this.factory = manager.getOWLDataFactory();
    }

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
        // 1. The Infinite Loop Trap
        // Atomically check and set the flag. If already updating, skip to avoid recursion.
        if (!isUpdating.compareAndSet(false, true)) {
            log.debug("Skipping ontologiesChanged: isUpdating flag is currently set.");
            return; 
        }

        log.debug("Received {} ontology changes for processing.", changes.size());

        boolean shouldUnlockImmediately = true;

        try {
            List<OWLOntologyChange> annotationChanges = new ArrayList<>();
            
            // Track IRIs processed in this event batch to prevent redundant axiom generation
            // Tracking by IRI prevents double-processing if an entity uses punning (Class and Individual sharing an IRI)
            Set<IRI> processedIRIs = new HashSet<>();
            
            // Pre-pass: Find newly created entities (entities being declared) to exclude them from 'modified' dates
            Set<OWLEntity> newlyCreatedEntities = new HashSet<>();
            for (OWLOntologyChange change : changes) {
                if (change instanceof AddAxiom && change.getAxiom() instanceof OWLDeclarationAxiom) {
                    newlyCreatedEntities.add(((OWLDeclarationAxiom) change.getAxiom()).getEntity());
                }
            }
            
            if (!newlyCreatedEntities.isEmpty()) {
                log.debug("Identified {} newly created entities in this batch.", newlyCreatedEntities.size());
            }
            
            // --- DYNAMIC PREFERENCES RESOLUTION ---
            ModifiedDatePreferences prefs = ModifiedDatePreferences.getInstance();
            
            String timestampStr;
            if (prefs.isUseSystemDate()) {
                if (prefs.isUseUTC()) {
                    timestampStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
                } else {
                    timestampStr = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
            } else {
                timestampStr = prefs.getCustomDateText();
            }
            
            OWLAnnotationProperty modifiedProp = factory.getOWLAnnotationProperty(IRI.create(prefs.getAnnotationPropertyIRI()));
            
            OWLLiteral timestamp = factory.getOWLLiteral(timestampStr, OWL2Datatype.XSD_DATE_TIME);
            OWLAnnotation newAnnotation = factory.getOWLAnnotation(modifiedProp, timestamp);

            boolean applyToClasses = prefs.isApplyToClasses();
            boolean applyToIndividuals = prefs.isApplyToIndividuals();

            // 2. Intercept and Analyze
            for (OWLOntologyChange change : changes) {
                
                // Only care about axiom additions or removals
                if (change.isAxiomChange()) {
                    OWLAxiom axiom = change.getAxiom();
                    OWLOntology ontology = change.getOntology();

                    log.debug("Evaluating axiom change ({}) for axiom: {}", change.getClass().getSimpleName(), axiom);

                    Set<OWLEntity> entitiesToProcess = new HashSet<>(axiom.getSignature());

                    // CRITICAL FIX: Annotations do not explicitly put their subject entities into the axiom signature.
                    // Bypass getEntitiesInSignature cache quirks by explicitly constructing and testing the entities for the IRI.
                    if (axiom instanceof OWLAnnotationAssertionAxiom) {
                        OWLAnnotationSubject subject = ((OWLAnnotationAssertionAxiom) axiom).getSubject();
                        log.debug("Axiom is an Annotation Assertion. Subject: {}", subject);
                        if (subject instanceof IRI) {
                            IRI subjectIRI = (IRI) subject;
                            entitiesToProcess.add(factory.getOWLClass(subjectIRI));
                            entitiesToProcess.add(factory.getOWLNamedIndividual(subjectIRI));
                            log.debug("Added Class and NamedIndividual permutations for IRI: {}", subjectIRI);
                        }
                    }

                    // 3. Find the modified entities (Classes and Named Individuals)
                    for (OWLEntity entity : entitiesToProcess) {
                        log.debug("Checking entity: {}", entity);
                        
                        boolean isClass = entity.isOWLClass();
                        boolean isIndividual = entity.isOWLNamedIndividual();
                        
                        // Check entity types against preferences
                        if (!isClass && !isIndividual) {
                            log.debug("Skipped: Entity is not a Class or NamedIndividual.");
                            continue;
                        }
                        
                        if (isClass && !applyToClasses) {
                            log.debug("Skipped: Preferences dictate skipping Classes.");
                            continue;
                        }
                        
                        if (isIndividual && !applyToIndividuals) {
                            log.debug("Skipped: Preferences dictate skipping Named Individuals.");
                            continue;
                        }

                        // QUIRK FIX: Prevent timestamping built-in foundational entities (like owl:Thing)
                        if (entity.isBuiltIn()) {
                            log.debug("Skipped: Entity is built-in (e.g., owl:Thing).");
                            continue;
                        }

                        // Skip if the entity was just created in this batch of changes
                        if (newlyCreatedEntities.contains(entity)) {
                            log.debug("Skipped: Entity was just declared/created in this batch.");
                            continue;
                        }

                        // Skip if we already updated this IRI in this batch of changes
                        if (processedIRIs.contains(entity.getIRI())) {
                            log.debug("Skipped: Entity IRI already processed in this batch.");
                            continue;
                        }

                        // CRITICAL FIX: In OWL API, an entity might only exist as the subject of annotations.
                        // In that case, containsEntityInSignature() might return false. Check both structural signature and annotations.
                        boolean hasStructuralAxioms = ontology.containsEntityInSignature(entity);
                        boolean hasAnnotations = !ontology.getAnnotationAssertionAxioms(entity.getIRI()).isEmpty();
                        
                        log.debug("Entity state - hasStructuralAxioms: {}, hasAnnotations: {}", hasStructuralAxioms, hasAnnotations);
                        
                        if (!hasStructuralAxioms && !hasAnnotations) {
                            log.debug("Skipped: Entity has no structural axioms or annotations (dangling reference).");
                            continue; 
                        }
                        
                        // 3a. Find and remove any existing dcterms:modified annotations
                        for (OWLAnnotationAssertionAxiom existingAnnotation : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
                            if (existingAnnotation.getProperty().equals(modifiedProp)) {
                                log.debug("Found existing dcterms:modified. Queueing removal.");
                                annotationChanges.add(new RemoveAxiom(ontology, existingAnnotation));
                            }
                        }
                        
                        // 3b. Create the axiom to assert the new annotation onto the entity
                        OWLAnnotationAssertionAxiom annotationAxiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), newAnnotation);
                        
                        // Queue the new change
                        log.debug("Queueing addition of new dcterms:modified timestamp.");
                        annotationChanges.add(new AddAxiom(ontology, annotationAxiom));
                        processedIRIs.add(entity.getIRI());
                    }
                } else {
                    log.debug("Skipped change: Not an axiom change (is {})", change.getClass().getSimpleName());
                }
            }

            // 4. Apply the updates safely
            if (!annotationChanges.isEmpty()) {
                log.debug("Dispatching {} annotation changes to SwingUtilities.", annotationChanges.size());
                shouldUnlockImmediately = false; // Transfer responsibility to the Swing thread
                
                // QUIRK FIX: Protégé is a Swing application. Applying ontology changes *during* // an active listener broadcast will cause a ConcurrentModificationException. 
                // We defer the execution to the end of the Event Dispatch Thread queue.
                SwingUtilities.invokeLater(() -> {
                    try {
                        manager.applyChanges(annotationChanges);
                        for (IRI iri : processedIRIs) {
                            log.debug("Updated dcterms:modified date for: {}", iri);
                        }
                    } catch (Exception e) {
                        log.error("Failed to apply modified date changes automatically", e);
                    } finally {
                        // ALWAYS unlock the listener when the delayed task completes
                        log.debug("Releasing isUpdating flag from Swing thread.");
                        isUpdating.set(false);
                    }
                });
            } else {
                log.debug("No annotation changes were queued during this execution.");
            }
        } finally {
            // Unlock immediately if no changes were queued, or if an exception was thrown during analysis
            if (shouldUnlockImmediately) {
                log.debug("Releasing isUpdating flag immediately.");
                isUpdating.set(false);
            }
        }
    }
}