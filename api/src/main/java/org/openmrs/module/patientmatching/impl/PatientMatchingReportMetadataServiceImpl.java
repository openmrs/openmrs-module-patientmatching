package org.openmrs.module.patientmatching.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.openmrs.module.patientmatching.Report;
import org.openmrs.module.patientmatching.db.PatientMatchingReportMetadataDao;

/**
 * The Implementation of the service class PatientMatchingReportMetadataService
 */
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

	@Override
	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration) {
		dao.savePatientMatchingConfiguration(patientMatchingConfiguration);
	}

	@Override
	public void deletePatientMatchingConfigurationByName(String name) {
		dao.deletePatientMatchingConfigurationByName(name);
	}

	@Override
	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(
			String name) {
		return dao.findPatientMatchingConfigurationByName(name);
	}

	@Override
	public List<PatientMatchingConfiguration> getMatchingConfigs() {
		return dao.getMatchingConfigs();
	}

	@Override
	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId) {
		return dao.getPatientMatchingConfiguration(configurationId);
	}

	@Override
	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration) {
		dao.deletePatientMatchingConfiguration(configuration);
	}

	@Override
	public long getCustomCount(String query) {
		return dao.getCustomCount(query);
	}

    @Override
	public java.util.List<String> getReportNames() {
        return dao.getReportNames();
    }

    @Override
	public Report getReportByName(String reportName) {
        return dao.getReportByName(reportName);
    }

    @Override
	public void deleteReport(Report report) {
        dao.deleteReport(report);
    }

    @Override
	public void savePatientMatchingReport(Report report) {
        dao.savePatientMatchingReport(report);
    }
	
	@Override
	public Cohort getAllPatients() {
		return dao.getAllPatients();
	}
}
