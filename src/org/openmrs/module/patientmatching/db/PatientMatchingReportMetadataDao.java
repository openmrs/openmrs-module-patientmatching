package org.openmrs.module.patientmatching.db;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;


public interface PatientMatchingReportMetadataDao {
	public void saveReportDetails(PatientMatchingReportMetadata pri) throws DAOException;
	//public void showReportDetails(String reportName) throws DAOException;

	

}

