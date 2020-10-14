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
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.module.patientmatching.*;
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
		return map;
	}
	
	/**
	 * moved from PatientMatchingReportMetadataService since it was only used in this class.
	 *
	 * @param reportName
	 * @return
	 */
	private Map<String, Object> showReportDetails(String reportName) {
		
		Map<String, Object> reportMap = new HashMap<String, Object>();
		Report report = Context.getService(PatientMatchingReportMetadataService.class).getReportByName(reportName);
		List<String> strategyList = new ArrayList<String>();
		for (PatientMatchingConfiguration configuration : report.getUsedConfigurationList()) {
			strategyList.add(configuration.getConfigurationName());
		}
		String createdBy;
		if (report.getCreatedBy() == null) {
			createdBy = "Unknown user";
		} else {
			createdBy = report.getCreatedBy().getUsername();
		}
		reportMap.put("reportName", report.getReportName());
		reportMap.put("strategylist", strategyList);
		reportMap.put("createdBy", createdBy);
		reportMap.put("date", report.getCreatedOn().toString());
		List<String> processList = new ArrayList<String>();
		List<String> stepList = new ArrayList<String>();
		for (ReportGenerationStep step : report.getReportGenerationSteps()) {
			processList.add(step.getTimeTaken() + " ms");
			stepList.add(step.getProcessName());
		}
		reportMap.put("processlist", processList);
		reportMap.put("stepList", stepList);
		return reportMap;
	}
}
