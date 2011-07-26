package org.openmrs.module.patientmatching.impl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;

import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.openmrs.module.patientmatching.db.PatientMatchingReportMetadataDao;
import org.openmrs.module.patientmatching.web.dwr.DWRMatchingConfigUtilities;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
public class PatientMatchingReportMetadataServiceImpl implements PatientMatchingReportMetadataService
{
	private PatientMatchingReportMetadataDao dao;
	private final Log log = LogFactory.getLog(this.getClass());
	public PatientMatchingReportMetadataServiceImpl()
	{
		/**
		 * default constructor
		 */
	}

	/**
	 * setter for PatientMatchingReportMetadataDAO
	 */
	public void setPatientMatchingReportMetadataDao(PatientMatchingReportMetadataDao dao)
	{
		this.dao = dao;
	}
	/**
	 * getter for PatientMatchingReportMetadataDAO
	 */
	public PatientMatchingReportMetadataDao getPatientMatchingReportMetadataDao()
	{
		return dao;
	}
	public void saveReportDetails(PatientMatchingReportMetadata pri)
	{
	 dao.saveReportDetails(pri);
	}
	public Map<String, Object> showReportDetails(String reportName)
	{

		HibernateSessionFactoryBean bean = new HibernateSessionFactoryBean();
		Configuration cfg = bean.newConfiguration();
		Properties c = cfg.getProperties();
		String url = c.getProperty("hibernate.connection.url");
		String user = c.getProperty("hibernate.connection.username");
		String passwd = c.getProperty("hibernate.connection.password");
		String driver = c.getProperty("hibernate.connection.driver_class");
        Connection databaseConnection = null;
		Map<String, Object> reportMap = new HashMap<String, Object>();
		List<String> strategyList =new ArrayList<String>();
		List<String> processList =new ArrayList<String>();


			try {
				Class.forName(driver);
			
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
					url, user, passwd);
			databaseConnection = connectionFactory.createConnection();
			
             PreparedStatement ps=null;
			ps = databaseConnection.prepareStatement("SELECT * FROM openmrs.persistreportdata WHERE report_name =?");
			ps.setString(1,reportName);
			ps.execute();
				while (ps.getResultSet().next()) {
			    reportMap.put("reportName",DWRMatchingConfigUtilities.rptname);
				reportMap.put("createdBy",ps.getResultSet().getString("createdby"));
				reportMap.put("date",ps.getResultSet().getString("datecreated"));
				String strats=ps.getResultSet().getString("strategies_used");
				String pInfo=ps.getResultSet().getString("process_name_time");
				String[] strategy=strats.split(",");
				String[] process=pInfo.split(",");
				for(int i=0;i<strategy.length;i++)
				{		
			    strategyList.add(strategy[i]);
				}
				reportMap.put("strategylist",strategyList);
				for(int i=0;i<process.length;i++)
				{    
					processList.add(process[i]);
				}
				reportMap.put("processlist",processList);

				}

			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return reportMap;
	}
	
}
