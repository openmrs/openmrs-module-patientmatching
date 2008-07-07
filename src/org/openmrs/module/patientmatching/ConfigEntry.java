/**
 * Auto generated file comment
 */
package org.openmrs.module.patientmatching;

/**
 *
 */
public class ConfigEntry implements Comparable<ConfigEntry> {
    
    private String fieldName;
    
    private boolean selected;

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the selected
     */
    public boolean getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int compareTo(ConfigEntry o) {
        // TODO Auto-generated method stub
        return fieldName.compareToIgnoreCase(o.getFieldName());
    }
}
