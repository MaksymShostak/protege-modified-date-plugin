package com.haddenindustries.protege;

import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import javax.swing.*;
import java.awt.*;

public class ModifiedDatePreferencesPanel extends OWLPreferencesPanel {

    private JRadioButton useSystemDateRadio;
    private JRadioButton useCustomDateRadio;
    private JCheckBox useUtcCheckBox;
    private JTextField customDateTextField;
    private JTextField annotationPropertyTextField;
    private JCheckBox applyToClassesCheckBox;
    private JCheckBox applyToIndividualsCheckBox;
    private JCheckBox applyToObjectPropertiesCheckBox;
    private JCheckBox applyToDataPropertiesCheckBox;

    @Override
    public void initialise() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // --- Date Configuration ---
        add(new JLabel("<html><b>Date & Time Configuration</b></html>"), gbc);
        gbc.gridy++;
        
        useSystemDateRadio = new JRadioButton("Use Current System Date");
        useCustomDateRadio = new JRadioButton("Use Custom Date/Time");
        ButtonGroup dateGroup = new ButtonGroup();
        dateGroup.add(useSystemDateRadio);
        dateGroup.add(useCustomDateRadio);
        
        add(useSystemDateRadio, gbc);
        gbc.gridy++;
        
        useUtcCheckBox = new JCheckBox("as of UTC");
        gbc.insets.left = 25; // Indent
        add(useUtcCheckBox, gbc);
        gbc.insets.left = 5; // Reset indent
        gbc.gridy++;
        
        add(useCustomDateRadio, gbc);
        gbc.gridy++;
        
        customDateTextField = new JTextField(20);
        JPanel customDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        customDatePanel.add(new JLabel("Date Text: "));
        customDatePanel.add(customDateTextField);
        gbc.insets.left = 25; // Indent
        add(customDatePanel, gbc);
        gbc.insets.left = 5; // Reset indent
        gbc.gridy++;

        // Radio button toggle logic
        useSystemDateRadio.addChangeListener(e -> updateComponentStates());
        useCustomDateRadio.addChangeListener(e -> updateComponentStates());

        add(new JSeparator(), gbc);
        gbc.gridy++;

        // --- Annotation Configuration ---
        add(new JLabel("<html><b>Annotation Property Configuration</b></html>"), gbc);
        gbc.gridy++;
        
        annotationPropertyTextField = new JTextField(30);
        JPanel iriPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iriPanel.add(new JLabel("Property IRI: "));
        iriPanel.add(annotationPropertyTextField);
        add(iriPanel, gbc);
        gbc.gridy++;
        
        add(new JSeparator(), gbc);
        gbc.gridy++;

        // --- Target Entities ---
        add(new JLabel("<html><b>Target Entities Configuration</b></html>"), gbc);
        gbc.gridy++;
        
        applyToClassesCheckBox = new JCheckBox("Apply to Classes");
        add(applyToClassesCheckBox, gbc);
        gbc.gridy++;
        
        applyToIndividualsCheckBox = new JCheckBox("Apply to Named Individuals");
        add(applyToIndividualsCheckBox, gbc);
        gbc.gridy++;
        
        applyToObjectPropertiesCheckBox = new JCheckBox("Apply to Object Properties");
        add(applyToObjectPropertiesCheckBox, gbc);
        gbc.gridy++;
        
        applyToDataPropertiesCheckBox = new JCheckBox("Apply to Data Properties");
        add(applyToDataPropertiesCheckBox, gbc);

        // --- Push layout to top ---
        gbc.gridy++;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);

        // Load saved values into UI
        ModifiedDatePreferences prefs = ModifiedDatePreferences.getInstance();
        useSystemDateRadio.setSelected(prefs.isUseSystemDate());
        useCustomDateRadio.setSelected(!prefs.isUseSystemDate());
        useUtcCheckBox.setSelected(prefs.isUseUTC());
        customDateTextField.setText(prefs.getCustomDateText());
        annotationPropertyTextField.setText(prefs.getAnnotationPropertyIRI());
        applyToClassesCheckBox.setSelected(prefs.isApplyToClasses());
        applyToIndividualsCheckBox.setSelected(prefs.isApplyToIndividuals());
        applyToObjectPropertiesCheckBox.setSelected(prefs.isApplyToObjectProperties());
        applyToDataPropertiesCheckBox.setSelected(prefs.isApplyToDataProperties());

        updateComponentStates();
    }

    private void updateComponentStates() {
        useUtcCheckBox.setEnabled(useSystemDateRadio.isSelected());
        customDateTextField.setEnabled(useCustomDateRadio.isSelected());
    }

    @Override
    public void applyChanges() {
        ModifiedDatePreferences prefs = ModifiedDatePreferences.getInstance();
        prefs.setUseSystemDate(useSystemDateRadio.isSelected());
        prefs.setUseUTC(useUtcCheckBox.isSelected());
        prefs.setCustomDateText(customDateTextField.getText());
        prefs.setAnnotationPropertyIRI(annotationPropertyTextField.getText());
        prefs.setApplyToClasses(applyToClassesCheckBox.isSelected());
        prefs.setApplyToIndividuals(applyToIndividualsCheckBox.isSelected());
        prefs.setApplyToObjectProperties(applyToObjectPropertiesCheckBox.isSelected());
        prefs.setApplyToDataProperties(applyToDataPropertiesCheckBox.isSelected());
    }

    @Override
    public void dispose() {
        // Nothing requires explicit disposal
    }
}