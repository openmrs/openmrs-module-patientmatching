/**
 *
 */
package org.openmrs.module.patientmatching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to represent a single configuration elements in the web page. A
 * single instance of this class represents one "row" tag in the actual
 * configuration file that is saved in the file system.
 * 
 * This class has two slightly similar fields. The <code>fieldViewName</code>
 * is used to display the field name, while the <code>fieldName</code> is
 * used internally to get reference to the Hibernate mapping. The reason for
 * this separation is OpenMRS add a prefix "<module-id>." for the Spring message
 * resource properties entry for module.
 * 
 * Ex: Creating entry message property <code>birthDate=Birth Date</code> will be
 * stored as <code>patientmatching.birthDate=Birth Date</code> in the OpenMRS
 * message resource properties. So, in order to correctly display "Birth Date"
 * in the web page, we need to request the message property
 * <code>patientmatching.birthDate</code> to Spring rather than just
 * <code>birthDate</code>.
 * 
 * In order to avoid confusion on what to use, we divide this to two separate
 * <code>fieldViewName</code> and <code>fieldName</code>.
 * 
 */

public class ConfigurationEntry implements Comparable<ConfigurationEntry> {
	
	/**
	 * A unique entryId to identify configuration entries. 
	 * Added during modifications to store ConfigurationEntries in a database
	 */
	private int entryId;
	/**
	 * String that will contain the modified version of the field name. This string
	 * mainly serves as the OpenMRS message properties rule.
	 */
	private String fieldViewName;

	/**
	 * String that will contains the actual field name. This string will be
	 * stored and read from the configuration file.
	 */
	private String fieldName;

	/**
	 * Whether this particular field will be ignored, included, or blocking(blocked)
	 * in the analysis for matching process
	 */
	private String inclusion;
	
	/**
     * blockOrder variable added during modifications to store ConfigurationEntries in a database
	 */
	private int blockOrder;
	
	/**
	 * Whether a particular field can be interchanged or not
	 */
	private String flag;
	
	private PatientMatchingConfiguration patientMatchingConfiguration;

	// public static String flag1="0";
	private final String IGNORED = "IGNORED";
	private final String INCLUDED = "INCLUDED";
	private final String BLOCKING = "BLOCKING";

	private final String SET = "0";

	// logger
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Default no argument constructor
	 */
	public ConfigurationEntry() {
		super();
	}
	
	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public int getBlockOrder() {
		return blockOrder;
	}

	public void setBlockOrder(int blockOrder) {
		this.blockOrder = blockOrder;
	}
	
	/**
	 * @return the inclusion level
	 */
	public String getInclusion() {
		return inclusion;
	}

	/**
	 * @param level
	 *            the inclusion level to set
	 */
	public void setInclusion(String level) {
		this.inclusion = level;
	}

	/**
	 * to get the Interchangeable set value(flag)
	 * 
	 * @return
	 */
	public String getFlag() {
		if (log.isDebugEnabled())
			log.debug("Getflag() called from ConfigurationEntry Entity with value in it :" + flag);

		return flag;
	}

	/**
	 * to set the Interchangeable set value(flag)
	 * 
	 * @param flag
	 */

	public void setFlag(String flag) {
		if (log.isDebugEnabled())
			log.debug("Setflag() called from ConfigurationEntry Entity with value in it :" + flag);
		
		this.flag = flag;
	}

	/**
	 * To set interchangeable(SET) value
	 * 
	 * @param sET
	 */
	public void setSET(String setvalue) {
		if (log.isDebugEnabled())
			log.debug("setSET() called from ConfigurationEntry Entity with value in it :" + SET);

		this.flag = setvalue;
	}

	/**
	 * returns whether this particular field is interchangeable or not
	 * 
	 * @return
	 */
	public String getSET() {
        return flag;
	}

	/**
	 * Return whether this particular field is to be ignored. Corresponds to the
	 * "BlockOrder" element of the "row" element.
	 * 
	 * @return boolean stating whether this is to be ignored
	 */
	public boolean isIgnored() {
		return inclusion.compareTo(IGNORED) == 0;
	}

	/**
	 * Mark as ignored field. Corresponds to the "BlockOrder" element of the
	 * "row" element.
	 * 
	 * @see ConfigurationEntry#isIgnored()
	 */
	public void setIgnored() {
		this.inclusion = IGNORED;
	}

	/**
	 * Return whether this particular field is a blocking field. Corresponds to
	 * the "BlockOrder" element of the "row" element.
	 * 
	 * @return the blocking
	 */
	public boolean isBlocking() {
		return inclusion.compareTo(BLOCKING) == 0;
	}

	/**
	 * Mark as blocking field. Corresponds to the "BlockOrder" element of the
	 * "row" element.
	 * 
	 * @see ConfigurationEntry#isIncluded()
	 */
	public void setBlocking() {
		this.inclusion = BLOCKING;
	}

	// public boolean isTransposed() {
	// return transposedSet.compareTo("SET") == 0;
	// }
	//
	// public void setTransposed() {
	// this.transposedSet = SET;
	// }
	//
	
	/**
	 * Get the current field's actual name. This string will be stored and read
	 * from the configuration file.
	 * 
	 * ex: <code>org.openmrs.Patient.birthDate</code>
	 * <code>org.openmrs.Patient.gender</code>
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
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Whether or not this field will be included in the matching analysis.
	 * 
	 * @return true if selected, false if not
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
	 * <code>patientmatching.org.openmrs.Patient.gender</code>
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
	 * @param fieldViewName
	 *            the fieldViewName to set
	 */
	public void setFieldViewName(String fieldViewName) {
		this.fieldViewName = fieldViewName;
	}

	/**
	 * Implementation of a method inherited the <code>Comparable</code>
	 * interface. This implementation will ensure the
	 * <code>ConfigurationEntry</code> sorted by the field name.
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(ConfigurationEntry o) {
		return fieldName.compareToIgnoreCase(o.getFieldName());
	}
	
	public PatientMatchingConfiguration getPatientMatchingConfiguration() {
		return patientMatchingConfiguration;
	}

	public void setPatientMatchingConfiguration(
			PatientMatchingConfiguration patientMatchingConfiguration) {
		this.patientMatchingConfiguration = patientMatchingConfiguration;
	}
}
