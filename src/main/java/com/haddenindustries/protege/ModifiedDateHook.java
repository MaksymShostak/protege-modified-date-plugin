package com.haddenindustries.protege;

import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifiedDateHook extends EditorKitHook {
    
    private static final Logger log = LoggerFactory.getLogger(ModifiedDateHook.class);
    
    private ModifiedDateListener listener;
    private OWLModelManager modelManager;

    public ModifiedDateHook() {
        super();
    }

    @Override
    public void initialise() throws Exception {
        log.debug(">>> ModifiedDateHook INITIALISING! <<<");
        
        try {
            OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
            this.modelManager = editorKit.getOWLModelManager();
            
            // Attach directly to the core OWLOntologyManager to intercept raw API events
            this.listener = new ModifiedDateListener(modelManager.getOWLOntologyManager());
            this.modelManager.getOWLOntologyManager().addOntologyChangeListener(listener);
            
            log.debug(">>> ModifiedDateListener successfully attached to OWLOntologyManager! <<<");
        } catch (Throwable t) {
            // Catch EVERYTHING (including standard exceptions and silent system Errors like NoClassDefFoundError)
            log.error(">>>>>>>>>> FATAL ERROR IN ModifiedDateHook INITIALISE: " + t.getMessage(), t);
        }
    }

    @Override
    public void dispose() throws Exception {
        if (this.modelManager != null && this.listener != null) {
            this.modelManager.getOWLOntologyManager().removeOntologyChangeListener(this.listener);
            log.debug(">>> ModifiedDateListener detached. <<<");
        }
    }
}