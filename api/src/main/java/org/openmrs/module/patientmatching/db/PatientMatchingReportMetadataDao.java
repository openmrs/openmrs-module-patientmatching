package org.openmrs.module.patientmatching.db;
import java.util.List;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
import org.openmrs.module.patientmatching.Report;


public interface PatientMatchingReportMetadataDao {
	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration);
    public void savePatientMatchingReport(Report report);
	public void deletePatientMatchingConfigurationByName(String name);
	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name);
	public List<PatientMatchingConfiguration> getBlockingRuns();
	public List<PatientMatchingConfiguration> getMatchingConfigs();
	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId);
	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration);
	public long getCustomCount(String query);
    public List<String> getReportNames();
}

