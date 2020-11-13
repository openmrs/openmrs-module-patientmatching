/**
 * 
 */
package org.openmrs.module.patientmatching;

import java.io.File;

/**
 * Interface to define some of the constants that will be used throughout the patient matching
 * process.
 */
public interface MatchingConstants {
	
	/**
	 * Folder name to store the configuration file and temporary output of the patient de-duplication
	 * process and serialized data
	 */
	String GP_CONFIG_DIR = "patientmatching.configDirectory";
	
	String CONFIG_DIR_DEFAULT = "patientmatching";
	
	String GP_CONFIG_FILE = "patientmatching.configFile";
	
	String CONFIG_FILENAME_DEFAULT = "link_config.xml";
	
	String CONFIG_FILE_DEFAULT = CONFIG_DIR_DEFAULT + File.separator + CONFIG_FILENAME_DEFAULT;
	
	/**
	 * Folder name for the output of <code>Record</code> serialization process
	 */
	String GP_SERIAL_DIR = "patientmatching.serializationDirectory";
	
	String SERIAL_DIRNAME_DEFAULT = "serial";
	
	String SERIAL_DIR_DEFAULT = CONFIG_DIR_DEFAULT + File.separator + SERIAL_DIRNAME_DEFAULT;
	
	/**
	 * Default key for the excluded properties of the OpenMRS's object that will be used in the patient
	 * de-duplication process for example are <code>creator</code> property of the <code>Patient</code>
	 * object. This is the key to the global property entry in the main OpenMRS global property table.
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
	
	String GP_STRATEGY = "patientmatching.strategy";
	
	String STRATEGY_PROBABILISTIC = "probabilistic";
	
	String STRATEGY_DETERMINISTIC = "deterministic";
	
	String IS_PROBABILISTIC = "isProbabilistic";
	
	String CONCATENATED_FIELD_PREFIX = "concat";
}
