package org.openmrs.module.patientmatching.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.Report;
import org.openmrs.module.patientmatching.db.PatientMatchingReportMetadataDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

	public void savePatientMatchingConfiguration(PatientMatchingConfiguration patientMatchingConfiguration) {
		sessionFactory.getCurrentSession().saveOrUpdate(patientMatchingConfiguration);
	}

    public void savePatientMatchingReport(Report report) {
        sessionFactory.getCurrentSession().saveOrUpdate(report);
    }

    public void deletePatientMatchingConfigurationByName(String name) {
		PatientMatchingConfiguration pmc = findPatientMatchingConfigurationByName(name);
		sessionFactory.getCurrentSession().delete(pmc);
	}

	public PatientMatchingConfiguration findPatientMatchingConfigurationByName(String name) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientMatchingConfiguration.class);
		criteria.add(Restrictions.eq("configurationName", name));
		return (PatientMatchingConfiguration) criteria.uniqueResult();
	}

	public List<PatientMatchingConfiguration> getBlockingRuns() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientMatchingConfiguration.class);
		return criteria.list();
	}

	public List<PatientMatchingConfiguration> getMatchingConfigs() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientMatchingConfiguration.class);
		return criteria.list();
	}

	public PatientMatchingConfiguration getPatientMatchingConfiguration(int configurationId) {
		return (PatientMatchingConfiguration) sessionFactory.getCurrentSession().get(PatientMatchingConfiguration.class, configurationId);
	}

	public void deletePatientMatchingConfiguration(PatientMatchingConfiguration configuration) {
		sessionFactory.getCurrentSession().delete(configuration);
	}

	public long getCustomCount(String query) {
		Query countQuery = sessionFactory.getCurrentSession().createQuery(query);
		Long count = (Long)countQuery.uniqueResult();
		return count;
	}

    public java.util.List<String> getReportNames() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Report.class).setProjection(Projections.property("reportName"));
        return criteria.list();
    }
}
