/**
 * 
 */
package org.openmrs.module.patientmatching;

/**
 * Interface to define some of the constants that will be used throughout
 * the patient matching process.
 * 
 */
public interface MatchingConstants {
    /**
     * Folder name to store the configuration file and temporary output of the
     * patient de-duplication process
     */
    public static final String CONFIG_FOLDER_NAME = "patient-matching";
    
    /**
     * Folder name for the output of <code>Record</code> serialization process
     */
    public static final String SERIAL_FOLDER_NAME = "patient-matching/serial";
    
    /**
     * Default name for the configuration file name
     */
    public static final String CONFIG_FILE_NAME = "config.xml";
    
    /**
     * Default key for the excluded properties of the OpenMRS's object that will
     * be used in the patient de-duplication process for example are <code>creator</code>
     * property of the <code>Patient</code> object.
     * 
     * This is the key to the global property entry in the main OpenMRS global
     * property table.
     */
    public static final String CONFIG_EXCLUDE_PROPERTIES = "patientmatching.excludedProperties";
    
    /**
     * List of parameters used in the patient de-duplication web page
     */
    public static final String PARAM_NAME = "configurationId";
    public static final String PARAM_REPORT = "report";
    
    /**
     * Multiple-value delimiter
     */
    public static final String MULTI_FIELD_DELIMITER = ",";
}
