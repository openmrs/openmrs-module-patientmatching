package org.openmrs.module.patientmatching.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.openmrs.module.patientmatching.db.PatientMatchingReportMetadataDao;

public class PatientMatchingReportMetadataServiceImpl implements PatientMatchingReportMetadataService {

	private PatientMatchingReportMetadataDao dao;
	private final Log log = LogFactory.getLog(this.getClass());

	public PatientMatchingReportMetadataServiceImpl() {
		// default constructor
	}

	/**
	 * setter for PatientMatchingReportMetadataDAO
	 */
	public void setPatientMatchingReportMetadataDao(PatientMatchingReportMetadataDao dao) {
		this.dao = dao;
	}

	/**
	 * getter for PatientMatchingReportMetadataDAO
	 */
	public PatientMatchingReportMetadataDao getPatientMatchingReportMetadataDao() {
		return dao;
	}

	public void saveReportDetails(PatientMatchingReportMetadata pri) {
		dao.saveReportDetails(pri);
	}

	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration) {
		dao.savePatientMatchingConfiguration(patientMatchingConfiguration);
	}

	public void deletePatientMatchingConfigurationByName(String name) {
		dao.deletePatientMatchingConfigurationByName(name);
	}

	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(
			String name) {
		return dao.findPatientMatchingConfigurationByName(name);
	}

	public List<PatientMatchingConfiguration> getBlockingRuns() {
		return dao.getBlockingRuns();
	}

	@Override
	public List<PatientMatchingConfiguration> getMatchingConfigs() {
		return dao.getMatchingConfigs();
	}
}
