package org.openmrs.module.patientmatching.db.hibernate;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.Report;
import org.openmrs.module.patientmatching.db.PatientMatchingReportMetadataDao;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HibernatePatientMatchingReportMetadataDAO implements PatientMatchingReportMetadataDao {

	private SessionFactory sessionFactory;
	protected final Log log = LogFactory.getLog(this.getClass());

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public HibernatePatientMatchingReportMetadataDAO() {
		super();
	}

	@Override
	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration) {
		getCurrentSession().saveOrUpdate(patientMatchingConfiguration);
	}

    @Override
	public void savePatientMatchingReport(Report report) {
		getCurrentSession().saveOrUpdate(report);
    }

    @Override
	public void deletePatientMatchingConfigurationByName(String name) {
		PatientMatchingConfiguration pmc = findPatientMatchingConfigurationByName(name);
		getCurrentSession().delete(pmc);
	}

	@Override
	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name) {
		Criteria criteria = getCurrentSession().createCriteria(PatientMatchingConfiguration.class);
		criteria.add(Restrictions.eq("configurationName", name));
		return (PatientMatchingConfiguration) criteria.uniqueResult();
	}

	@Override
	public List<PatientMatchingConfiguration> getMatchingConfigs() {
		Criteria criteria = getCurrentSession().createCriteria(PatientMatchingConfiguration.class);
		return criteria.list();
	}

	@Override
	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId) {
		return (PatientMatchingConfiguration) getCurrentSession().get(PatientMatchingConfiguration.class, configurationId);
	}

	@Override
	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration) {
		getCurrentSession().delete(configuration);
	}

	@Override
	public long getCustomCount(String query) {
		Query countQuery = getCurrentSession().createQuery(query);
		Long count = (Long)countQuery.uniqueResult();
		return count;
	}

    @Override
	public java.util.List<String> getReportNames() {
		Criteria criteria = getCurrentSession().createCriteria(Report.class)
		        .setProjection(Projections.property("reportName"));
        return criteria.list();
    }

    @Override
	public Report getReportByName(String name) {
		Criteria criteria = getCurrentSession().createCriteria(Report.class);
        criteria.add(Restrictions.eq("reportName", name));
        return (Report) criteria.uniqueResult();
    }

    @Override
	public void deleteReport(Report report) {
		getCurrentSession().delete(report);
    }
	
	@SuppressWarnings("unchecked")
	public Cohort getAllPatients() {
		
		Query query = getCurrentSession().createQuery("select patientId from Patient p where p.voided = '0'");
		
		Set<Integer> ids = new HashSet<Integer>();
		ids.addAll(query.list());
		
		return new Cohort("All patients", "", ids);
	}
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 * 
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session) method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
}
