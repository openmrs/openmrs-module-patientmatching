package org.openmrs.module.patientmatching;

import java.util.List;

public interface PatientMatchingReportMetadataService {

	public void saveReportDetails(PatientMatchingReportMetadata pri);

	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration);

	public void deletePatientMatchingConfigurationByName(String name);

	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name);

	public List<PatientMatchingConfiguration> getBlockingRuns();

	public List<PatientMatchingConfiguration> getMatchingConfigs();
}
