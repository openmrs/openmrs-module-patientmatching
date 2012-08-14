package org.openmrs.module.patientmatching.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	public List<PatientMatchingConfiguration> getMatchingConfigs() {
		return dao.getMatchingConfigs();
	}

	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId) {
		return dao.getPatientMatchingConfiguration(configurationId);
	}

	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration) {
		dao.deletePatientMatchingConfiguration(configuration);
	}

	public long getCustomCount(String query) {
		return dao.getCustomCount(query);
	}

    public java.util.List<String> getReportNames() {
        return dao.getReportNames();
    }

    public Report getReportByName(String reportName) {
        return dao.getReportByName(reportName);
    }

    public void deleteReport(Report report) {
        dao.deleteReport(report);
    }

    public void savePatientMatchingReport(Report report) {
        dao.savePatientMatchingReport(report);
    }
}
