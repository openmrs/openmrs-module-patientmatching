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
 * In order to avoid confusion on what to use, we divide this to two separate <code>fieldViewName</code>
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
     * Whether this particular field will be ignored, included, or blocking in the
     * analysis for matching process
     */
    private String inclusion;
    private final String IGNORED = "IGNORED";
    private final String INCLUDED = "INCLUDED";
    private final String BLOCKING = "BLOCKING";

    /**
     * Default no argument constructor
     */
    public ConfigurationEntry() {
        super();
    }
    
    /**
	 * @return the inclusion level
	 */
	public String getInclusion() {
		return inclusion;
	}

	/**
	 * @param level the inclusion level to set
	 */
	public void setInclusion(String level) {
		this.inclusion = level;
	}

	/**
     * Return whether this particular field is to be ignored. Correspond to the "BlockOrder" element of the "row" element.
     * 
     * @return the ignored
     */
    public boolean isIgnored() {
    	return inclusion.compareTo(IGNORED) == 0;
    }
    
    /**
     * Mark as ignored field. Correspond to the "BlockOrder" element of the "row" element.
     * 
     * @see ConfigurationEntry#isIgnored()
     */
    public void setIgnored() {
    	this.inclusion = IGNORED;
    }
    
    /**
     * Return whether this particular field is a blocking field. Correspond to
     * the "BlockOrder" element of the "row" element.
     * 
     * @return the blocking
     */
    public boolean isBlocking() {
        return inclusion.compareTo(BLOCKING) == 0;
    }

    /**
     * Mark as blocking field. Correspond to the 
     * "BlockOrder" element of the "row" element.
     * 
     * @see ConfigurationEntry#isIncluded()
     */
    public void setBlocking() {
    	this.inclusion = BLOCKING;
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
     * Whether or not this field will be included in the
     * matching analysis process.
     * 
     * @return the selected
     */
    public boolean isIncluded() {
        return inclusion.compareTo(INCLUDED) == 0;
    }

    /**
     * Mark as included field.
     * 
     * @see ConfigurationEntry#isIncluded()
     */
    public void setIncluded() {
        this.inclusion = INCLUDED;
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
