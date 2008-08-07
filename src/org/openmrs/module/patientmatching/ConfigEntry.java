/**
 * Auto generated file comment
 */
package org.openmrs.module.patientmatching;

/**
 *
 */
public class ConfigEntry implements Comparable<ConfigEntry> {
    
    private String fieldViewName;
    
    private String fieldName;
    
    private boolean included;
    
    private boolean blocking;

    /**
     * @return the blocking
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * @param blocking the blocking to set
     */
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

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
    public boolean isIncluded() {
        return included;
    }

    /**
     * @param selected the selected to set
     */
    public void setIncluded(boolean included) {
        this.included = included;
    }

    /**
     * @return the fieldViewName
     */
    public String getFieldViewName() {
        return fieldViewName;
    }

    /**
     * @param fieldViewName the fieldViewName to set
     */
    public void setFieldViewName(String fieldViewName) {
        this.fieldViewName = fieldViewName;
    }

    public int compareTo(ConfigEntry o) {
        // TODO Auto-generated method stub
        return fieldName.compareToIgnoreCase(o.getFieldName());
    }
}
