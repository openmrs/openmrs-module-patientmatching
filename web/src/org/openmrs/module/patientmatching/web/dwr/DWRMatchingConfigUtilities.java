package org.openmrs.module.patientmatching.web.dwr;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.web.MatchingConfigUtilities;

public class DWRMatchingConfigUtilities {

    protected final Log log = LogFactory.getLog(getClass());
    
    public List<String> getAllBlockingRuns() {
        return MatchingConfigUtilities.listAvailableBlockingRuns();
    }
    
    public void deleteBlockingRun(String name) {
        log.info("DWRMatchingConfigUtilities: deleting blocking run");
        MatchingConfigUtilities.deleteBlockingRun(name);
    }
    
    public void doAnalysis() {
        log.info("DWRMatchingConfigUtilities: running analysis process");
        try {
        	if(LinkDBConnections.getInstance().getLock()) {
        		MatchingConfigUtilities.doAnalysis();
        		LinkDBConnections.getInstance().releaseLock();
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public String getStatus() {
    	return MatchingConfigUtilities.getStatus();
    }
}
