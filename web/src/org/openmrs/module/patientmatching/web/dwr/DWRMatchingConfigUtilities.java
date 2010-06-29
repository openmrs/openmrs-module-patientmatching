/**
 * 
 */
package org.openmrs.module.patientmatching.web.dwr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.proxy.dwr.Util;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportReader;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.util.MatchingConfig;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;


/**
 * Utility class that will be available to the DWR javascript call from the
 * module web page. All methods in this class must be registered in module
 * config file to make it available as javascript call.
 */
public class DWRMatchingConfigUtilities {

	protected final Log log = LogFactory.getLog(getClass());
    private ServerContext sctx;
    private String currentPage;
	static Map objects;
	static Calendar calendar;
	static Boolean processStarted = false;
	static String previousProcessTime = "0,0";
	static List<Long> proTimeList;
	static Long time;
	static int reset;
	static int index = 1;
	static int currentStep;
	static int size = 0;
	
	/**
	 * Constructor
	 */
	
	public DWRMatchingConfigUtilities(){
		ServletContext servletContext = org.directwebremoting.WebContextFactory.get().getServletContext();
        sctx = ServerContextFactory.get(servletContext);
        org.directwebremoting.WebContext wctx = org.directwebremoting.WebContextFactory.get();
        currentPage = wctx.getCurrentPage();
	}
	
	/**
	 * @see MatchingConfigurationUtils#listAvailableBlockingRuns()
	 */
	public List<String> getAllBlockingRuns() {
		return MatchingConfigurationUtils.listAvailableBlockingRuns();
	}

	/**
	 * @see MatchingReportUtils#listAvailableReport()
	 */
	public List<String> getAllReports() {
		return MatchingReportUtils.listAvailableReport();
	}

	/**
	 * @see MatchingConfigurationUtils#deleteBlockingRun(String)
	 */
	public void deleteBlockingRun(String name) {
		log.info("DWRMatchingConfigUtilities: deleting blocking run");
		MatchingConfigurationUtils.deleteBlockingRun(name);
	}
	
	/**
	 * @see MatchingReportUtils#resetStep()
	 */
	public void resetStep() {
		log.info("DWRMatchingConfigUtilities: resetting to first step");
		reset = -1;
		currentStep = 0;
		Collection sessions = sctx.getScriptSessionsByPage(currentPage);
        Util pages = new Util(sessions);
        pages.addFunctionCall("reset");
	}
	
	/**
	 * @see MatchingReportUtils#getStep()
	 */
	public String getStep() {
		int step = currentStep;
		log.info("DWRMatchingConfigUtilities: returning step " + new Integer(step));
		return (new Integer(step).toString())+","+processStarted.toString();
	}

	//New Method Declaration
	public List<Long> previousProcessStatus(){
		return proTimeList;
	}

	public void getCurrentProcessStatus(int nextStep){
		
		time = calendar.getInstance().getTimeInMillis();
		try{	
			switch(nextStep){
			case 2:
				objects = new HashMap<String, Object>();
				objects = MatchingReportUtils.ReadConfigFile(objects);
				break;

			case 3:
				objects = MatchingReportUtils.InitScratchTable(objects);
				size =  ((List<MatchingConfig>)objects.get("matchingConfigLists")).size();
				break;

			case 4:
				objects = MatchingReportUtils.CreRanSamAnalyzer(objects);
				break;

			case 5:
				objects = MatchingReportUtils.CreAnalFormPairs(objects);
				break;

			case 6:
				objects = MatchingReportUtils.CrePairdataSourAnalyzer(objects);
				break;

			case 7:
				objects = MatchingReportUtils.CreEMAnalyzer(objects);
				break;

			case 8:
				objects = MatchingReportUtils.AnalyzingData(objects);
				break;

			case 9:
				objects = MatchingReportUtils.ScoringData(objects);
				break;

			case 10:
				objects = MatchingReportUtils.CreatingReport(objects);
				break;

			}

		} catch (Exception e) {
			LinkDBConnections.getInstance().releaseLock();
			log.info("Exception caught during the analysis process ...");
			log.error(e.getStackTrace().toString());
			reset = -1;
		} catch (Throwable t) {
			LinkDBConnections.getInstance().releaseLock();
			log.info("Throwable object caught during the analysis process ...");
			log.error(t.getStackTrace().toString());
			reset = -1;
		}
		
		time = (calendar.getInstance().getTimeInMillis()-time);
	}

	//New Method Declaration End
	
	
	
	/**
	 * @see MatchingReportUtils#doAnalysis()
	 */
	public void doAnalysis() {
		
		proTimeList = new ArrayList<Long>();
		Collection sessions = sctx.getScriptSessionsByPage(currentPage);
        Util pages = new Util(sessions);
        pages.addFunctionCall("reportProcessStarted");
		reset = 0;
		for(int i=2;i<11;i++){
			currentStep = i;
			processStarted = true;
			getCurrentProcessStatus(i);
			processStarted = false;
			if(reset == -1){
				Collection sessions1 = sctx.getScriptSessionsByPage(currentPage);
		        Util pages1 = new Util(sessions1);
		        pages1.addFunctionCall("enableGenReport");
				break;
			}
						
			if(size >1 && index >1 && i>=4 && i<=9){
				int j = i;
				time = (time+proTimeList.get(i-2));
				if(i == 9 && size != index){
					j = 3;
					previousProcessTime = j+","+time+"p";
				}else{
				previousProcessTime = j+","+time;
				}
				proTimeList.set((i-2),time);
			}else{
				int j = i;
				if(size >1 && i==9){
					j = 3;
					previousProcessTime = j+","+time+"p";
				}else {
				previousProcessTime = i+","+time;
				}
				proTimeList.add(time);
			}
			
			if(i==9 && size != index && size != 0){
				objects.put("matchingConfig",((List<MatchingConfig>)objects.get("matchingConfigLists")).get(index));
				index++;
				i = 3;
			}
			
			if(reset != -1){
				Collection sessions1 = sctx.getScriptSessionsByPage(currentPage);
	            Util pages1 = new Util(sessions);
	            pages.addFunctionCall("updateChecklist",previousProcessTime);
				
			}else {
				proTimeList = null;
				currentStep = 0;
			}
		}
		processStarted = false;
		currentStep = 0;
		index = 1;
		size = 0;
	}

	/**
	 * Delete a particular report file from the server using DWR call
	 * 
	 * @param filename
	 *            report file that will be deleted
	 */
	public void deleteReportFile(String filename) {
		log.info("DWRMatchingConfigUtilities: deleting file " + filename);
		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil
				.getDirectoryInApplicationDataDirectory(configLocation);
		File reportFile = new File(configFileFolder, filename);
		log.info("Report file to be deleted: " + reportFile.getAbsolutePath());
		boolean deleted = reportFile.delete();
		if (deleted) {
			log.info("Config file deleted.");
		}
	}

	/**
	 * Get the report block for a particular page out of the report file using
	 * DWR call.
	 * 
	 * @return content of the next page in the report file
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> getNextPage() {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		String filename = (String) session.getAttribute("reportFilename");
		List<Long> pagePos = (List<Long>) session
				.getAttribute("reportPagePosition");
		int currentPage = (Integer) session.getAttribute("reportCurrentPage");
		boolean eof = (Boolean) session.getAttribute("isReportEOF");

		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s
				.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);

		MatchingReportReader reader = new MatchingReportReader(currentPage,
				eof, pagePos, filename);
		try {
			if (eof) {
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

	/**
	 * Get the report block for a particular page out of the report file using
	 * DWR call.
	 * 
	 * @return content of the previous page in the report file
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> getPrevPage() {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		String filename = (String) session.getAttribute("reportFilename");
		List<Long> pagePos = (List<Long>) session
				.getAttribute("reportPagePosition");
		int currentPage = (Integer) session.getAttribute("reportCurrentPage");
		// pressing previous button always makes the eof false
		boolean eof = false;

		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s
				.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);

		MatchingReportReader reader = new MatchingReportReader(currentPage,
				eof, pagePos, filename);
		try {
			if (currentPage > 1) {
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
