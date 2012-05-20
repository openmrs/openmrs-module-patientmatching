package org.openmrs.module.patientmatching.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.openmrs.module.patientmatching.MatchingRunData;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ReportMetadataSimpleFormController extends SimpleFormController {

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
		String text = "Not used";
		return text;
	}

	/**
	 * provide report information for viewing on page.
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		Map<String, Object> map = showReportDetails(MatchingRunData.getInstance().getRptname());
		map.put("reportParam", MatchingConstants.PARAM_REPORT);
		map.put("stepList", MatchingReportUtils.listSteps());
		return map;
	}

	/**
	 * moved from PatientMatchingReportMetadataService since it was only used in this class.
	 *
	 * @param reportName
	 * @return
	 */
	private Map<String, Object> showReportDetails(String reportName) {

		HibernateSessionFactoryBean bean = new HibernateSessionFactoryBean();
		Configuration cfg = bean.newConfiguration();
		Properties c = cfg.getProperties();
		String url = c.getProperty("hibernate.connection.url");
		String user = c.getProperty("hibernate.connection.username");
		String passwd = c.getProperty("hibernate.connection.password");
		String driver = c.getProperty("hibernate.connection.driver_class");
		Map<String, Object> reportMap = new HashMap<String, Object>();
		List<String> strategyList = new ArrayList<String>();
		List<String> processList = new ArrayList<String>();

		try {
			Class.forName(driver);

			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
					url, user, passwd);
			Connection databaseConnection = connectionFactory.createConnection();

			PreparedStatement ps = databaseConnection.prepareStatement("SELECT * FROM persistreportdata WHERE report_name =?");
			ps.setString(1, reportName);
			ps.execute();
			
			while (ps.getResultSet().next()) {
				reportMap.put("reportName", MatchingRunData.getInstance().getRptname());
				reportMap.put("createdBy", ps.getResultSet().getString("createdby"));
				reportMap.put("date", ps.getResultSet().getString("datecreated"));
				
				String strats = ps.getResultSet().getString("strategies_used");
				String pInfo = ps.getResultSet().getString("process_name_time");
				
				String[] strategy = strats.split(",");
				String[] process = pInfo.split(",");
				
				strategyList.addAll(Arrays.asList(strategy));
				reportMap.put("strategylist", strategyList);
				
				processList.addAll(Arrays.asList(process));
				reportMap.put("processlist", processList);
			}

		} catch (Exception e) {
			log.warn("error while showing report details", e);
		}
		
		return reportMap;
	}
}
