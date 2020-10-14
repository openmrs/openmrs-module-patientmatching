package org.openmrs.module.patientmatching.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.*;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigurationSimpleFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected PatientMatchingConfiguration formBackingObject(HttpServletRequest request) throws Exception {
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		
		// pull excluded properties from the global property
		AdministrationService adminService = Context.getAdministrationService();
		String excludedProperties = adminService.getGlobalProperty(MatchingConstants.CONFIG_EXCLUDE_PROPERTIES);
		List<String> listExcludedProperties = Arrays.asList(excludedProperties.split(",", -1));
		log.info("Excluded Properties: " + excludedProperties);
		
		// get the existing config if the configuration id is available and properly formatted
		PatientMatchingConfiguration configuration = null;
		String configIdString = request.getParameter(MatchingConstants.PARAM_NAME);
		
		if (!StringUtils.isEmpty(configIdString) && StringUtils.isNumeric(configIdString)) {
			try {
				int configurationId = Integer.parseInt(configIdString);
				configuration = service.getPatientMatchingConfiguration(configurationId);
			}
			catch (NumberFormatException ex) {
				log.error("could not convert '" + configIdString + "' to a long", ex);
			}
		}
		
		// create a new one if not found
		if (configuration == null) {
			log.warn("creating new configuration");
			configuration = new PatientMatchingConfiguration();
		}
		
		log.warn("Configuration: " + configuration);
		
		// refresh the config with properties from current data model
		MatchingConfigurationUtils.refreshPatientMatchingConfig(configuration, listExcludedProperties);
		log.info("Config Name: " + configuration.toString());
		
		return configuration;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		PatientMatchingConfiguration patientMatchingConfig = (PatientMatchingConfiguration) command;
		
		Map<String, String> model = new HashMap<String, String>();
		
		if (patientMatchingConfig.getConfigurationName() == null
		        || "".equals(patientMatchingConfig.getConfigurationName())) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "patientmatching.config.new.noNameErrorMessage");
			return showForm(request, response, errors);
		}
		
		try {
			Set<ConfigurationEntry> entries = patientMatchingConfig.getConfigurationEntries();
			Set<ConfigurationEntry> newEntries = new TreeSet();
			
			for (ConfigurationEntry ce : entries) {
				ce.setPatientMatchingConfiguration(patientMatchingConfig);
				newEntries.add(ce);
			}
			
			patientMatchingConfig.getConfigurationEntries().clear();
			patientMatchingConfig.setConfigurationEntries(newEntries);
			MatchingConfigurationUtils.savePatientMatchingConfig(patientMatchingConfig);
		}
		catch (ConstraintViolationException e) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "patientmatching.strategy.duplicate");
			return showForm(request, response, errors);
		}
		return new ModelAndView(getSuccessView(), model);
	}
	
}
