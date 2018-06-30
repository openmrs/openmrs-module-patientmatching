package org.openmrs.module.patientmatching.db.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openmrs.module.patientmatching.RejectedPatientPair;
import org.openmrs.module.patientmatching.db.RejectedPatientPairDAO;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link RejectedPatientPairDAO}
 */
public class HibernateRejectedPatientPairDAO implements RejectedPatientPairDAO {

    private static SessionFactory sessionFactory;

    public HibernateRejectedPatientPairDAO() {}

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        HibernateRejectedPatientPairDAO.sessionFactory = sessionFactory;
    }

    @Override
    public void saveRejectedPatientPair(RejectedPatientPair pair) {
        Session session = getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(pair);
        transaction.commit();
        session.flush();
    }

    @Override
    public void saveRejectedPatientPairList(List<RejectedPatientPair> pairs) {
        Session session = getCurrentSession();
        Transaction transaction = session.beginTransaction();

        for (RejectedPatientPair p : pairs) {
            session.saveOrUpdate(p);
        }

        transaction.commit();
        session.flush();
    }

    @Override
    public List<RejectedPatientPair> getRejectedPatientPairs() {
        List list = getCurrentSession().createCriteria(RejectedPatientPair.class).list();
        ArrayList<RejectedPatientPair> rejectedPatientPairs = new ArrayList<RejectedPatientPair>();

        for (Object obj : list) {
            rejectedPatientPairs.add((RejectedPatientPair) obj);
        }

        return rejectedPatientPairs;
    }

    private Session getCurrentSession() {
        if (sessionFactory != null) {
            return sessionFactory.openSession();

        } else {
            try {
                Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
                return (org.hibernate.Session) method.invoke(sessionFactory, null);

            } catch (Exception e) {
                throw new RuntimeException("Failed to get the current hibernate session", e);
            }
        }
    }
}
