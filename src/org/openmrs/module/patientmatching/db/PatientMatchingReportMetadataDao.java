package org.openmrs.module.patientmatching.db;
import java.util.List;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;


public interface PatientMatchingReportMetadataDao {
	public void saveReportDetails(PatientMatchingReportMetadata pri) throws DAOException;
	//public void showReportDetails(String reportName) throws DAOException;

	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration);
	public void deletePatientMatchingConfigurationByName(String name);
	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name);
	public List<PatientMatchingConfiguration> getBlockingRuns();
	public List<PatientMatchingConfiguration> getMatchingConfigs();
}

