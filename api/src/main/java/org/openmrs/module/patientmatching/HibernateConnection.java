package org.openmrs.module.patientmatching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.DbSessionFactory;

/**
 * Class retrieves a Hibernate SessionFactory to give to the Reader object
 * in order to create Record objects from OpenMRS Patients
 * @author jegg
 *
 */

public class HibernateConnection {
	private static DbSessionFactory sessionFactory;
	protected final Log log = LogFactory.getLog(getClass());
	
	public HibernateConnection(){};

	public DbSessionFactory getSessionFactory() {
		return HibernateConnection.sessionFactory;
	}
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		//log.warn("set session factory called with object : " + sessionFactory);
		HibernateConnection.sessionFactory = sessionFactory;
	}
	
	//public static void setSessionFactory(SessionFactory sessionFactory) {
	//	HibernateConnection.sessionFactory = sessionFactory;
	//}
	
	
}
