package com.haddenindustries.protege;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

public class ModifiedDatePreferences {
    
    private static final String SET_ID = "com.haddenindustries.protege.modifieddate";
    private static ModifiedDatePreferences instance;
    
    public static synchronized ModifiedDatePreferences getInstance() {
        if (instance == null) {
            instance = new ModifiedDatePreferences();
        }
        return instance;
    }
    
    private Preferences getPrefs() {
        return PreferencesManager.getInstance().getPreferencesForSet(SET_ID, "options");
    }
    
    public boolean isUseSystemDate() { return getPrefs().getBoolean("USE_SYSTEM_DATE", true); }
    public void setUseSystemDate(boolean val) { getPrefs().putBoolean("USE_SYSTEM_DATE", val); }
    
    public boolean isUseUTC() { return getPrefs().getBoolean("USE_UTC", true); }
    public void setUseUTC(boolean val) { getPrefs().putBoolean("USE_UTC", val); }
    
    public String getCustomDateText() { return getPrefs().getString("CUSTOM_DATE_TEXT", "2026-01-01T12:00:00Z"); }
    public void setCustomDateText(String val) { getPrefs().putString("CUSTOM_DATE_TEXT", val); }
    
    public String getAnnotationPropertyIRI() { return getPrefs().getString("ANNOTATION_IRI", "http://purl.org/dc/terms/modified"); }
    public void setAnnotationPropertyIRI(String val) { getPrefs().putString("ANNOTATION_IRI", val); }
    
    public boolean isApplyToClasses() { return getPrefs().getBoolean("APPLY_CLASSES", true); }
    public void setApplyToClasses(boolean val) { getPrefs().putBoolean("APPLY_CLASSES", val); }
    
    public boolean isApplyToIndividuals() { return getPrefs().getBoolean("APPLY_INDIVIDUALS", true); }
    public void setApplyToIndividuals(boolean val) { getPrefs().putBoolean("APPLY_INDIVIDUALS", val); }
    
    public boolean isApplyToObjectProperties() { return getPrefs().getBoolean("APPLY_OBJ_PROP", false); }
    public void setApplyToObjectProperties(boolean val) { getPrefs().putBoolean("APPLY_OBJ_PROP", val); }
    
    public boolean isApplyToDataProperties() { return getPrefs().getBoolean("APPLY_DATA_PROP", false); }
    public void setApplyToDataProperties(boolean val) { getPrefs().putBoolean("APPLY_DATA_PROP", val); }
}