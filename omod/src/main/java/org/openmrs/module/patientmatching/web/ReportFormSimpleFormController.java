package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.patientmatching.DataBaseReportReader;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class ReportFormSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    @Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
		return "Not used";
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		String reportName = req.getParameter(MatchingConstants.PARAM_REPORT);
        DataBaseReportReader reader = new DataBaseReportReader(reportName);
        reader.setReport(reportName);

        // must call getHeader first to skip first line
        map.put("reportHeader", reader.getHeader());
        
        // then get the first page content
        map.put("report", reader.fetchContent(1));
        
        map.put("useMinimalHeader", true);
        AdministrationService adminService = Context.getAdministrationService();
        String contextPath = req.getContextPath();
        String prodServerUrl= adminService.getGlobalProperty("patientmatching.urlProductionServer", contextPath +"/admin/patients/mergePatients.form");
        map.put("productionServerUrl",prodServerUrl);
        // then store all values to session to be used in the future
        HttpSession session = req.getSession();
        session.setAttribute("reportFilename", reportName);
        session.setAttribute("reportPagePosition", reader.getPaginationMap());
        session.setAttribute("reportCurrentPage", 1);
		return map;
		
	}
	
}
