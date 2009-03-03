package org.openmrs.module.patientmatching.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigurationSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected PatientMatchingConfiguration formBackingObject(HttpServletRequest request) throws Exception {
	    String name = request.getParameter(MatchingConstants.PARAM_NAME);
	    
        AdministrationService adminService = Context.getAdministrationService();
        String excludedProperties = adminService.getGlobalProperty(MatchingConstants.CONFIG_EXCLUDE_PROPERTIES);
        List<String> listExcludedProperties = Arrays.asList(excludedProperties.split(",", -1));
        
        log.info("Excluded Property: " + excludedProperties);
        
        PatientMatchingConfiguration config = null;
        
	    if (name != null) {
            config = MatchingConfigurationUtils.createPatientMatchingConfig(name, listExcludedProperties);
	    } else {
	        config = MatchingConfigurationUtils.createPatientMatchingConfig(listExcludedProperties);
	    }
	    log.info("Config Name: " + config.toString());
		return config;
		
	}

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        PatientMatchingConfiguration patientMatchingConfig = (PatientMatchingConfiguration) command;
        MatchingConfigurationUtils.savePatientMatchingConfig(patientMatchingConfig);
        
        Map<String, String> model = new HashMap<String, String>();
        return new ModelAndView(getSuccessView(), model);
    }
	
}
