package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
		
		log.debug("Returning text: " + text);
		
		return text;
		
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		String filename = req.getParameter(MatchingConstants.PARAM_REPORT);
        
        MatchingReportReader reader = new MatchingReportReader(filename);
        reader.fetchInitialContent();
        
        HttpSession session = req.getSession();
        
        session.setAttribute("reportFilename", filename);
        
        session.setAttribute("reportPagePosition", reader.getPagePos());
        session.setAttribute("reportCurrentPage", reader.getCurrentPage());
        session.setAttribute("isReportEOF", reader.isEof());
		
		map.put("report", reader.getCurrentContent());
		map.put("useMinimalHeader", true);
		return map;
		
	}
	
}
