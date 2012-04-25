package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigurationListSimpleFormController extends SimpleFormController {

	// Logger for this class and subclasses
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected String formBackingObject(HttpServletRequest request)
			throws Exception {
		return "Not used";
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request)
			throws Exception {
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("configurations", service.getMatchingConfigs());
		requestMap.put("parameter", MatchingConstants.PARAM_NAME);
		return requestMap;
	}
}
