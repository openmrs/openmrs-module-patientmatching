package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ReportSimpleFormController extends SimpleFormController {
	
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
	    
        List<Map<String, String>> analysisResult = MatchingConfigUtilities.doAnalysis();
		
		Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("blockingRuns", MatchingConfigUtilities.listAvailableBlockingRuns());
        requestMap.put("analysisResults", analysisResult);
		
		return requestMap;
		
	}

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        // TODO Auto-generated method stub
        log.info("Creating new patient matching report");
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("blockingRuns", MatchingConfigUtilities.listAvailableBlockingRuns());
        return new ModelAndView(getSuccessView(), model);
    }
	
	
	
}
