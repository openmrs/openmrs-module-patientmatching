package org.openmrs.module.patientmatching.web;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.openmrs.module.patientmatching.web.dwr.DWRMatchingConfigUtilities;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class ReportMetadataSimpleFormController extends SimpleFormController {
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
        String text="Not used";
		return text;
		
	}

	

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
			Map<String, Object> map = new HashMap<String, Object>();
		  PatientMatchingReportMetadataService pmrms = Context.getService(PatientMatchingReportMetadataService.class);
		  map=pmrms.showReportDetails(DWRMatchingConfigUtilities.rptname);
		  map.put("reportParam", MatchingConstants.PARAM_REPORT);
		  map.put("stepList", MatchingReportUtils.listSteps());
		  //map.put("i", MatchingReportUtils.i);

		  return map;
		
	}

}
