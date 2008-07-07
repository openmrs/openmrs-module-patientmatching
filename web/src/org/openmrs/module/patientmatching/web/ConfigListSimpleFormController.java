package org.openmrs.module.patientmatching.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigListSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected String formBackingObject(HttpServletRequest request) throws Exception {
        
        //String text = Context.getService(HelloWorldService.class);
        
        String text = "Not used";
        
        log.debug("Returning hello world text: " + text);
        
        return text;
    }

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Map<String, List<String>> referenceData(HttpServletRequest request) throws Exception {
        // TODO Auto-generated method stub
        File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory("patient-matching");
        List<String> fileNamesList = new ArrayList<String>();
        for (File file : folder.listFiles()) {
            fileNamesList.add(file.getName());
            log.info("Found file: " + file.getAbsolutePath());
        }
        Map<String, List<String>> requestMap = new HashMap<String, List<String>>();
        requestMap.put("files", fileNamesList);
        return requestMap;
    }
}
