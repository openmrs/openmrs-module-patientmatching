package org.regenstrief.linkage.io;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.module.patientmatching.HibernateConnection;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;

public class OpenMRSReader implements DataSourceReader {
    @SuppressWarnings("unchecked")
    private List patients;
    
    private int pageNumber = 0;

    private final int PAGING_SIZE = 100;

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
    	patients = createCriteria(PAGING_SIZE, pageNumber).list();

    	log.info("Getting data for patient records ...");
    	LinkDBConnections.getInstance().syncRecordDemogrpahics();
    	
    	log.info("Finish intialization ...");
    }

    private Criteria createCriteria(int size, int pageNumber){
    	session.flush();
    	session.clear();
    	criteria = session.createCriteria(Patient.class);
    	//        criteria.setFetchMode("identifiers", FetchMode.JOIN);
    	//        criteria.setFetchMode("addresses", FetchMode.JOIN);
    	//        criteria.setFetchMode("names", FetchMode.JOIN);
    	//        criteria.setFetchMode("attributes", FetchMode.JOIN);

    	criteria.setMaxResults(size);
    	criteria.setFirstResult(pageNumber * size);
    	return criteria;
    }
    
    private Session createHibernateSession() {
        HibernateConnection connection = new HibernateConnection();
        log.info("Hibernate connection null? " + (connection == null));
        
        SessionFactory sessionFactory = connection.getSessionFactory();
        log.info("Session factory null? " + (sessionFactory == null));
        
        return sessionFactory.getCurrentSession();
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
    		patients = createCriteria(PAGING_SIZE, pageNumber).list();
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
        List patients = createCriteria(PAGING_SIZE, 0).list();
        
        return (patients != null);
    }

}
