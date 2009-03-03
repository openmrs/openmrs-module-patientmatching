package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigurationListSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String formBackingObject(HttpServletRequest request) throws Exception {
        
        String text = "Not used";
        
        log.debug("Returning hello world text: " + text);
        
        return text;
    }

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("files", MatchingConfigurationUtils.listAvailableBlockingRuns());
        requestMap.put("parameter", MatchingConstants.PARAM_NAME);
        return requestMap;
    }
}
