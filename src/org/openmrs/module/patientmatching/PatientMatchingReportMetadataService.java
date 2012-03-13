package org.openmrs.module.patientmatching;
import java.util.List;
import java.util.Map;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
public interface PatientMatchingReportMetadataService {
	
    public void saveReportDetails(PatientMatchingReportMetadata pri);
    public Map<String, Object> showReportDetails(String reportName);
    public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration);
    public void deletePatientMatchingConfigurationByName(String name);
    public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name);
    public List<PatientMatchingConfiguration> getBlockingRuns();
    public List<PatientMatchingConfiguration> getMatchingConfigs();
}
