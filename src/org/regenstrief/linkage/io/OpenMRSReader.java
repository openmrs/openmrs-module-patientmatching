package org.regenstrief.linkage.io;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.module.patientmatching.HibernateConnection;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;

public class OpenMRSReader implements DataSourceReader {
    @SuppressWarnings("unchecked")
    private Iterator patientsIterator;
    protected final Log log = LogFactory.getLog(this.getClass());
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public OpenMRSReader() {
        Session session = createHibernateSession();
        //TODO: possibility of out of memory exception because all record will be
        // loaded to memory immediately
        List patients = session.createCriteria(Patient.class).list();
        patientsIterator = patients.iterator();
        
        LinkDBConnections.getInstance().syncRecordDemogrpahics();
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
        patientsIterator = null;
        return (patientsIterator == null);
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
        return patientsIterator.hasNext();
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#nextRecord()
     */
    public Record nextRecord() {
        Record r = null;
        if(patientsIterator != null && hasNextRecord()) {
            Patient p = (Patient) patientsIterator.next();
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
        Session session = createHibernateSession();
        
        List patients = session.createCriteria(Patient.class).list();
        patientsIterator = patients.iterator();
        
        LinkDBConnections.getInstance().syncRecordDemogrpahics();
        
        return (patientsIterator != null);
    }

}
