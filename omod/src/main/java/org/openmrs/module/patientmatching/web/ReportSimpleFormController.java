package org.openmrs.module.patientmatching.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ReportSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected String formBackingObject(HttpServletRequest request) throws Exception {
		return "Not used";
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest req) throws Exception {
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("reportParam", MatchingConstants.PARAM_REPORT);
        requestMap.put("blockingRuns", MatchingConfigurationUtils.listAvailableBlockingRuns_db());
        requestMap.put("reportResults", MatchingReportUtils.listAvailableReport());
        requestMap.put("defaultStatus", MatchingReportUtils.NO_PROCESS);
        requestMap.put("premStatus", MatchingReportUtils.PREM_PROCESS);
        requestMap.put("endStatus", MatchingReportUtils.END_PROCESS);
        requestMap.put("stepList", MatchingReportUtils.listSteps());
		return requestMap;
	}

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        log.info("Creating new patient matching report");
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("reportParam", MatchingConstants.PARAM_REPORT);
        model.put("blockingRuns", MatchingConfigurationUtils.listAvailableBlockingRuns());
        model.put("reportResults", MatchingReportUtils.listAvailableReport());
        model.put("defaultStatus", MatchingReportUtils.NO_PROCESS);
        model.put("premStatus", MatchingReportUtils.PREM_PROCESS);
        model.put("endStatus", MatchingReportUtils.END_PROCESS);
        model.put("stepList", MatchingReportUtils.listSteps());
        return new ModelAndView(getSuccessView(), model);
    }
}
