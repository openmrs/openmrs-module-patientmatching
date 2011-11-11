package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportReader;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class ReportFormSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    @Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
		
		//String text = Context.getService(HelloWorldService.class);

		String text = "Not used";
		
		log.debug("************ " +text);
		
		return text;
		
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		String filename = req.getParameter(MatchingConstants.PARAM_REPORT);
        
        MatchingReportReader reader = new MatchingReportReader(filename);
        
        // must call getHeader first to skip first line
        map.put("reportHeader", reader.getHeader());
        
        // then get the first page content
        reader.fetchContent(reader.getCurrentPage());
        map.put("report", reader.getCurrentContent());
        
        map.put("useMinimalHeader", true);
        AdministrationService adminService = Context.getAdministrationService();
        String contextPath = req.getContextPath();
        String prodServerUrl= adminService.getGlobalProperty("patientmatching.urlProductionServer", contextPath +"/admin/patients/mergePatients.form");
        map.put("productionServerUrl",prodServerUrl);
        // then store all values to session to be used in the future
        HttpSession session = req.getSession();
        session.setAttribute("reportFilename", filename);
        session.setAttribute("reportPagePosition", reader.getPagePos());
        session.setAttribute("reportCurrentPage", reader.getCurrentPage());
        session.setAttribute("isReportEOF", reader.isEof());
        session.removeAttribute("endPage");
        
		return map;
		
	}
	
}
