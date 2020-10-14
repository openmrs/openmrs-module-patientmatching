package org.openmrs.module.patientmatching.db;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.Report;

/**
 * The DAO class to access database objects
 */
public interface PatientMatchingReportMetadataDao {
	
	/**
	 * Persists the given configuration to the database
	 * 
	 * @param patientMatchingConfiguration The configuration to be persisted
	 */
	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration);
	
	/**
	 * Persist a patient mactching report to the database
	 * 
	 * @param report The report object to be persisted
	 */
	public void savePatientMatchingReport(Report report);
	
	/**
	 * Delete the PatientMatching configuration which has the given name
	 * 
	 * @param name The name of the configuration to be deleted
	 */
	public void deletePatientMatchingConfigurationByName(String name);
	
	/**
	 * Find and return the configuration with the given name if exists in the database
	 * 
	 * @param name The name of the configuration to be loaded
	 * @return The PatientMatchingConfiguration object with the given name
	 */
	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name);
	
	/**
	 * Get the list of all the configurations in the database
	 * 
	 * @return The list of all the configurations in the database
	 */
	public List<PatientMatchingConfiguration> getMatchingConfigs();
	
	/**
	 * Get the configuration with the given id
	 * 
	 * @param configurationId The id of the configuration to be retrieved
	 * @return The Configuration with the given id
	 */
	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId);
	
	/**
	 * Delete the given configuration from the database
	 * 
	 * @param configuration The configuration to be deleted
	 */
	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration);
	
	/**
	 * Runs the given HQL (count) query on the database and return its result
	 * 
	 * @param query The HQL count query to be executed
	 * @return The result of the query
	 */
	public long getCustomCount(String query);
	
	/**
	 * Get the list of the names of all the reports in the database
	 * 
	 * @return List of names of the reports in the database
	 */
	public List<String> getReportNames();
	
	/**
	 * Get the report with the given name from the database
	 * 
	 * @param name Name of the report to be retrieved
	 * @return The report with the given name
	 */
	public Report getReportByName(String name);
	
	/**
	 * Delete the given report from the database
	 * 
	 * @param report the report file to be deleted
	 */
	public void deleteReport(Report report);
	
	public Cohort getAllPatients();
}
