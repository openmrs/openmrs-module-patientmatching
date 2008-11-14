package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.openmrs.Patient;
import org.openmrs.module.patientmatching.HibernateConnection;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;

public class OpenMRSReader implements DataSourceReader {
    @SuppressWarnings("unchecked")
    private List patients;
    
    private int pageNumber = 0;

    private final int PAGING_SIZE = 10000;

    protected final Log log = LogFactory.getLog(this.getClass());

    private Criteria criteria;

    private Session session;

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public OpenMRSReader() {
    	session = createHibernateSession();
        //TODO: possibility of out of memory exception because all record will be
        // loaded to memory immediately
    	log.info("Getting all patient records ...");
    	updatePatientList();

    	log.info("Getting data for patient records ...");
    	LinkDBConnections.getInstance().syncRecordDemographics();
    	
    	log.info("Finish intialization ...");
    }
    
    private Criteria createCriteria(){
    	session.flush();
    	session.clear();
    	criteria = session.createCriteria(Patient.class);

    	criteria.setMaxResults(PAGING_SIZE);
    	criteria.setFirstResult(pageNumber * PAGING_SIZE);
    	return criteria;
    }
    
    private Session createHibernateSession() {
        HibernateConnection connection = new HibernateConnection();
        log.info("Hibernate connection null? " + (connection == null));
        
        SessionFactory sessionFactory = connection.getSessionFactory();
        log.info("Session factory null? " + (sessionFactory == null));
        
        return sessionFactory.getCurrentSession();
    }
    
    private void updatePatientList() {
        if (patients == null) {
            patients = new ArrayList();
        }
    	
        try {
            patients = createCriteria().list();
        } catch (Exception e) {
            log.info("Exception caught during fetching OpenMRS data ...");
            log.info("Root cause: " + e.getMessage());
            log.info("Falling back to alternative plan ...");
            session.flush();
            session.clear();
            patients.clear();

            Integer count = (Integer) session
                                .createCriteria(Patient.class)
                                .setProjection(Projections.projectionList()
                                        .add(Projections.count("patientId")))
                                .uniqueResult();

            if (count > PAGING_SIZE) {
                count = PAGING_SIZE;
            }

            for (int i = 0; i < count; i++) {
                try {
                    Patient p = (Patient) session
                                    .createCriteria(Patient.class)
                                    .setMaxResults(1)
                                    .setFirstResult(pageNumber * PAGING_SIZE + i)
                                    .uniqueResult();
                    patients.add(p);
                } catch (Exception ex) {
                    Integer id = (Integer) session
                                    .createCriteria(Patient.class)
                                    .setMaxResults(1)
                                    .setFirstResult(pageNumber * PAGING_SIZE + i)
                                    .setProjection(Projections.projectionList()
                                            .add(Projections.property("patientId")))
                                    .uniqueResult();
                    log.info("Fail to load patient with id " + id + "...");
                    log.info("Root cause: " + ex.getMessage());
                    log.info("Skipping to the next patient ...");
                }
            }
            log.info("Fall back plan success ...");
        }
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#close()
     */
    public boolean close() {
    	session.flush();
    	session.clear();
    	patients = null;
    	return (patients == null);
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#getRecordSize()
     */
    public int getRecordSize() {
        return -999;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#hasNextRecord()
     */
    public boolean hasNextRecord() {
    	if(patients.size() == 0) {
    		pageNumber ++;
    		updatePatientList();
    	}

    	return (patients.size() > 0);
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#nextRecord()
     */
    public Record nextRecord() {
        Record r = null;
        if(patients != null && hasNextRecord()) {
        	Patient p = (Patient) patients.remove(0);
            r = LinkDBConnections.getInstance().patientToRecord(p);
        }
        return r;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#reset()
     */
    @SuppressWarnings("unchecked")
    public boolean reset() {
    	pageNumber = 0;
    	
    	updatePatientList();
        
        return (patients != null);
    }

}
