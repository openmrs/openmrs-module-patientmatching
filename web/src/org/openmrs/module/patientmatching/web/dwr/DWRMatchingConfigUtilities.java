package org.openmrs.module.patientmatching.web.dwr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.MatchingConfigUtilities;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportReader;
import org.openmrs.util.OpenmrsUtil;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;

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
    
    @SuppressWarnings("unchecked")
    public List<List<String>> getNextPage() {
        WebContext context = WebContextFactory.get();
        HttpSession session = context.getSession();
        String filename = (String) session.getAttribute("reportFilename");
        List<Long> pagePos = (List<Long>) session.getAttribute("reportPagePosition");
        int currentPage = (Integer) session.getAttribute("reportCurrentPage");
        boolean eof = (Boolean) session.getAttribute("isReportEOF");
        
        List<List<String>> currentContent = new ArrayList<List<String>>();
        // init with error message
        List<String> s = new ArrayList<String>();
        s.add("Unable to get the report data, please retry or re-open the report page");
        currentContent.add(s);
        
        MatchingReportReader reader = new MatchingReportReader(currentPage, eof, pagePos, filename);
        try {
            if(eof) {
                reader.fetchContent(currentPage);
            } else {
                reader.fetchContent(currentPage + 1);
            }
            // only update the value when succeed
            session.setAttribute("reportPagePosition", reader.getPagePos());
            session.setAttribute("reportCurrentPage", reader.getCurrentPage());
            session.setAttribute("isReportEOF", reader.isEof());
            // this will replace error message if the process is done
            currentContent = reader.getCurrentContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentContent;
    }
    
    @SuppressWarnings("unchecked")
    public List<List<String>> getPrevPage() {
        WebContext context = WebContextFactory.get();
        HttpSession session = context.getSession();
        String filename = (String) session.getAttribute("reportFilename");
        List<Long> pagePos = (List<Long>) session.getAttribute("reportPagePosition");
        int currentPage = (Integer) session.getAttribute("reportCurrentPage");
        // pressing previous button always makes the eof false
        boolean eof = false;
        
        List<List<String>> currentContent = new ArrayList<List<String>>();
        // init with error message
        List<String> s = new ArrayList<String>();
        s.add("Unable to get the report data, please retry or re-open the report page");
        currentContent.add(s);
        
        MatchingReportReader reader = new MatchingReportReader(currentPage, eof, pagePos, filename);
        try {
            if(currentPage > 1) {
                reader.fetchContent(currentPage - 1);
            } else {
                reader.fetchContent(currentPage);
            }
            // only update the value when succeed
            session.setAttribute("reportPagePosition", reader.getPagePos());
            session.setAttribute("reportCurrentPage", reader.getCurrentPage());
            session.setAttribute("isReportEOF", reader.isEof());
            // this will replace error message if the process is done
            currentContent = reader.getCurrentContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentContent;
    }
}
