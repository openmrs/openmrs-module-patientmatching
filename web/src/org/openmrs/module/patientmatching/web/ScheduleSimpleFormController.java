package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ScheduleSimpleFormController extends SimpleFormController {
	
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
		
		return map;
		
	}
	
}
