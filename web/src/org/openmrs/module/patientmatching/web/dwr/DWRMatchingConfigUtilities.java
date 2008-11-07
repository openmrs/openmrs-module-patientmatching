package org.openmrs.module.patientmatching.web.dwr;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.MatchingConfigUtilities;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.util.OpenmrsUtil;

public class DWRMatchingConfigUtilities {

    protected final Log log = LogFactory.getLog(getClass());
    
    public List<String> getAllBlockingRuns() {
        return MatchingConfigUtilities.listAvailableBlockingRuns();
    }
    
    public List<String> getAllReports() {
        return MatchingConfigUtilities.listAvailableReport();
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
		} catch (Exception e) {
			LinkDBConnections.getInstance().releaseLock();
			MatchingConfigUtilities.setStatus(MatchingConfigUtilities.PREM_PROCESS);
			log.info("Exception caught during the analysis process ...");
			e.printStackTrace();
		} catch (Throwable t) {
			LinkDBConnections.getInstance().releaseLock();
			MatchingConfigUtilities.setStatus(MatchingConfigUtilities.PREM_PROCESS);
			log.info("Throwable object caught during the analysis process ...");
			t.printStackTrace();
		}
    }
    
    public void deleteReportFile(String filename) {
        log.info("DWRMatchingConfigUtilities: deleting file " + filename);
		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File reportFile = new File(configFileFolder, filename);
        log.info("Report file to be deleted: " + reportFile.getAbsolutePath());
        boolean deleted = reportFile.delete();
        if (deleted) {
            log.info("Config file deleted.");
        }
    }
    
    public String getStatus() {
    	return MatchingConfigUtilities.getStatus();
    }
    
    public void setStatus(String status) {
    	MatchingConfigUtilities.setStatus(status);
    }
}
