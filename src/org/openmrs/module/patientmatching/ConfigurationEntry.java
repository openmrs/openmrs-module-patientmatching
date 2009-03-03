/**
 *
 */
package org.openmrs.module.patientmatching;

/**
 * A class to represent a single configuration elements in the web page. A single
 * instance of this class represent one "row" tag in the actual configuration file
 * that is saved in the file system.
 * 
 * This class has two (almost) similar field. The <code>fieldViewName</code> will be used to
 * display the field name. While the <code>fieldName</code> will be used internally to
 * get reference to the Hibernate mapping. The reason for this separation is OpenMRS add a
 * prefix "<module-id>." for the Spring message resource properties entry for module.
 * 
 * Ex:
 *    Creating entry message property <code>birthDate=Birth Date</code> will be stored as
 *    <code>patientmatching.birthDate=Birth Date</code> in the OpenMRS message resource
 *    properties. So, in order to correctly display "Birth Date" in the web page, we need
 *    to request the message property <code>patientmatching.birthDate</code> to Spring
 *    rather than just <code>birthDate</code>.
 *    
 * In order to avoid confussion on what to use, we divide this to two separate <code>fieldViewName</code>
 * and <code>fieldName</code>.
 * 
 */
public class ConfigurationEntry implements Comparable<ConfigurationEntry> {
    
    /**
     * String that will contains modified version of the field name. This string
     * mainly serve as the OpenMRS message properties rule
     */
    private String fieldViewName;
    
    /**
     * String that will contains the actual field name. This string will be
     * stored and read from the configuration file.
     */
    private String fieldName;
    
    /**
     * Flag to define whether this particular field will be included in the
     * analysis for matching process or not
     */
    private boolean included;
    
    /**
     * Flag to define whether this particular field will be the blocking field
     * or not
     */
    private boolean blocking;

    /**
     * Default no argument constructor
     */
    public ConfigurationEntry() {
        super();
    }
    
    /**
     * Return whether this particular field is a blocking field. Correspond to
     * the "BlockOrder" element of the "row" element.
     * 
     * @return the blocking
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Change the current field blocking flag status. Correspond to the 
     * "BlockOrder" element of the "row" element.
     * 
     * @param blocking the blocking to set
     */
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * Get the current field's actual name. This string will be stored and
     * read from the configuration file.
     * 
     * ex: <code>org.openmrs.Patient.birthDate</code>
     *     <code>org.openmrs.Patient.gender</code>
     * 
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Change the current field's actual name. This string will be stored and
     * read from the configuration file.
     * 
     * @see ConfigurationEntry#getFieldName()
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Return flag to determine whether this field will be included in the
     * matching analysis process.
     * 
     * @return the selected
     */
    public boolean isIncluded() {
        return included;
    }

    /**
     * Change the included flag for the current field.
     * 
     * @see ConfigurationEntry#isIncluded()
     * @param selected the selected to set
     */
    public void setIncluded(boolean included) {
        this.included = included;
    }

    /**
     * Get the current field's for-the-web-page name. This is a slightly
     * modified string value from the actual name.
     * 
     * ex: <code>patientmatching.org.openmrs.Patient.birthDate</code>
     *     <code>patientmatching.org.openmrs.Patient.gender</code>
     *
     * @return the fieldViewName
     */
    public String getFieldViewName() {
        return fieldViewName;
    }

    /**
     * Change the current field field's for-the-web-page name.
     * 
     * @see ConfigurationEntry#getFieldViewName()
     * @param fieldViewName the fieldViewName to set
     */
    public void setFieldViewName(String fieldViewName) {
        this.fieldViewName = fieldViewName;
    }

    /**
     * Implementation of a method inherited the <code>Comparable</code>interface.
     * This implementation will ensure the <code>ConfigurationEntry</code> sorted by
     * the field name.
     * 
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(ConfigurationEntry o) {
        return fieldName.compareToIgnoreCase(o.getFieldName());
    }
}
