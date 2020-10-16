/**
 *
 */
package org.openmrs.module.patientmatching.web.dwr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.proxy.dwr.Util;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.DataBaseReportReader;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.MatchingConfigurationUtils;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.openmrs.module.patientmatching.MatchingRunData;
import org.openmrs.module.patientmatching.PatientMatchingConfiguration;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadataService;
import org.openmrs.module.patientmatching.Report;
import org.openmrs.module.patientmatching.StrategyHolder;
import org.regenstrief.linkage.util.MatchingConfig;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;

/**
 * Utility class that will be available to the DWR javascript call from the module web page. All
 * methods in this class must be registered in module config file to make it available as javascript
 * call.
 */
public class DWRMatchingConfigUtilities {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static ServerContext sctx;
	
	private static String currentPage;
	
	private static Map objects;
	
	private static Boolean processStarted = false;
	
	private static String previousProcessTime = "0,0";
	
	private static Long time;
	
	private static int reset;
	
	private static int index = 1;
	
	private static int currentStep;
	
	private static int size = 0;
	
	private static String[] selectedStrat;
	
	/**
	 * Constructor
	 */
	public DWRMatchingConfigUtilities() {
		ServletContext servletContext = org.directwebremoting.WebContextFactory.get().getServletContext();
		sctx = ServerContextFactory.get(servletContext);
		org.directwebremoting.WebContext wctx = org.directwebremoting.WebContextFactory.get();
		currentPage = wctx.getCurrentPage();
	}
	
	/**
	 * @see org.openmrs.module.patientmatching.MatchingConfigurationUtils#listAvailableBlockingRuns_db()
	 */
	public List<String> getAllBlockingRuns() {
		return MatchingConfigurationUtils.listAvailableBlockingRuns_db();
	}
	
	/**
	 * returns a list of [id, name] arrays for all patient matching configurations.
	 */
	public List<Object[]> getAllPatientMatchingConfigurations() {
		// get all configs
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		List<PatientMatchingConfiguration> configs = service.getMatchingConfigs();
		
		// convert to arrays of [id, name]
		List<Object[]> results = new ArrayList<Object[]>();
		for (PatientMatchingConfiguration config : configs)
			results.add(new Object[] { config.getConfigurationId(), config.getConfigurationName() });
		
		// send them back
		return results;
	}
	
	/**
	 * @see MatchingReportUtils#listAvailableReportNamesInDB()
	 */
	public List<String> getAllReports() {
		return MatchingReportUtils.listAvailableReportNamesInDB();
	}
	
	/**
	 * @see PatientMatchingReportMetadataService#deletePatientMatchingConfiguration(org.openmrs.module.patientmatching.PatientMatchingConfiguration)
	 */
	public void deleteBlockingRun(Long id) {
		log.info("DWRMatchingConfigUtilities: deleting blocking run #" + id);
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		PatientMatchingConfiguration configuration = service.getPatientMatchingConfiguration(id.intValue());
		if (configuration != null)
			service.deletePatientMatchingConfiguration(configuration);
	}
	
	public void resetStep() {
		log.info("DWRMatchingConfigUtilities: resetting to first step");
		reset = -1;
		currentStep = 0;
		Collection sessions = sctx.getScriptSessionsByPage(currentPage);
		Util pages = new Util(sessions);
		pages.addFunctionCall("reset");
	}
	
	public String getStep() {
		boolean timerTaskStarted = MatchingRunData.getInstance().isTimerTaskStarted();
		
		int step = currentStep;
		String activeReverseAjaxEnabled = "true";
		ServletConfig sc = org.directwebremoting.WebContextFactory.get().getServletConfig();
		if (sc.getInitParameter("activeReverseAjaxEnabled") == null
		        || sc.getInitParameter("activeReverseAjaxEnabled").equals("false")) {
			activeReverseAjaxEnabled = "false," + reset + "," + timerTaskStarted;
		}
		if (timerTaskStarted) {
			Collection sessions3 = sctx.getScriptSessionsByPage(currentPage);
			Util pages3 = new Util(sessions3);
			pages3.addFunctionCall("scheduledTaskRunning");
		}
		log.debug("DWRMatchingConfigUtilities: returning step " + new Integer(step));
		return (new Integer(step).toString()) + "," + processStarted.toString() + "," + activeReverseAjaxEnabled;
	}
	
	public List<Object> getPatient(String patientId) {
		List<Object> patientDetails = new ArrayList<Object>();
		int pId = Integer.valueOf(patientId);
		Patient patient = Context.getPatientService().getPatient(pId);
		Set<PatientIdentifier> patientIdentifier = Context.getPatientService().getPatient(pId).getIdentifiers();
		Set<PersonAddress> patientAddress = Context.getPatientService().getPatient(pId).getAddresses();
		patientDetails.add(patient);
		patientDetails.add(patientIdentifier);
		patientDetails.add(patientAddress);
		
		return patientDetails;
	}
	
	public List<Long> previousProcessStatus() {
		return MatchingRunData.getInstance().getProTimeList();
	}
	
	public void selStrategy(String selected) {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		session.removeAttribute("selStrategy");
		session.setAttribute("selStrategy", selected);
	}
	
	public void getCurrentProcessStatus(int nextStep) {
		
		time = Calendar.getInstance().getTimeInMillis();
		boolean isProbabilistic = Context.getRegisteredComponent("patientMatchingStrategyHolder", StrategyHolder.class)
		        .isProbabilistic();
		
		try {
			switch (nextStep) {
				case 2:
					objects = new HashMap<String, Object>();
					objects.put(MatchingConstants.IS_PROBABILISTIC, isProbabilistic);
					MatchingReportUtils.ReadConfigFile(objects, selectedStrat);
					break;
				
				case 3:
					MatchingReportUtils.InitScratchTable(objects);
					size = ((List<MatchingConfig>) objects.get("matchingConfigLists")).size();
					break;
				
				case 4:
					if (isProbabilistic) {
						MatchingReportUtils.CreRanSamAnalyzer(objects);
					}
					break;
				
				case 5:
					if (isProbabilistic) {
						MatchingReportUtils.CreAnalFormPairs(objects);
					}
					break;
				
				case 6:
					if (isProbabilistic) {
						MatchingReportUtils.CrePairdataSourAnalyzer(objects);
					}
					break;
				
				case 7:
					if (isProbabilistic) {
						MatchingReportUtils.CreEMAnalyzer(objects);
					}
					break;
				
				case 8:
					if (isProbabilistic) {
						MatchingReportUtils.AnalyzingData(objects);
					}
					break;
				
				case 9:
					MatchingReportUtils.ScoringData(objects);
					break;
				
				case 10:
					MatchingReportUtils.CreatingReport(objects);
					break;
			}
			
		}
		catch (Exception e) {
			LinkDBConnections.getInstance().releaseLock();
			log.warn("Exception caught during the analysis process", e);
			reset = -1;
		}
		catch (Throwable t) {
			LinkDBConnections.getInstance().releaseLock();
			log.warn("Throwable object caught during the analysis process", t);
			reset = -1;
		}
		
		time = Calendar.getInstance().getTimeInMillis() - time;
	}
	
	public void doAnalysis(String selectedStrategies) {
		MatchingRunData.getInstance().setFileStrat(selectedStrategies);
		if (!MatchingRunData.getInstance().isTimerTaskStarted()) {
			selectedStrat = selectedStrategies.split(",");
			MatchingRunData.getInstance().setProTimeList(new ArrayList<Long>());
			Collection sessions = sctx.getScriptSessionsByPage(currentPage);
			Util pages = new Util(sessions);
			pages.addFunctionCall("reportProcessStarted");
			reset = 0;
			for (int i = 2; i < 11; i++) {
				currentStep = i;
				processStarted = true;
				getCurrentProcessStatus(i);
				processStarted = false;
				if (reset == -1) {
					Collection sessions1 = sctx.getScriptSessionsByPage(currentPage);
					Util pages1 = new Util(sessions1);
					pages1.addFunctionCall("enableGenReport");
					break;
				}
				
				if (size > 1 && index > 1 && i >= 4 && i <= 9) {
					time = (time + MatchingRunData.getInstance().getProTimeList().get(i - 2));
					if (i == 9 && size != index) {
						previousProcessTime = "3," + time + "p";
					} else {
						previousProcessTime = i + "," + time;
					}
					MatchingRunData.getInstance().getProTimeList().set((i - 2), time);
				} else {
					if (size > 1 && i == 9) {
						previousProcessTime = "3," + time + "p";
					} else {
						previousProcessTime = i + "," + time;
					}
					MatchingRunData.getInstance().getProTimeList().add(time);
				}
				
				if (i == 9 && size != index && size != 0) {
					objects.put("matchingConfig", ((List<MatchingConfig>) objects.get("matchingConfigLists")).get(index));
					index++;
					i = 3;
				}
				
				if (reset != -1) {
					Collection sessions2 = sctx.getScriptSessionsByPage(currentPage);
					Util pages2 = new Util(sessions2);
					pages2.addFunctionCall("updateChecklist", previousProcessTime);
					
				} else {
					MatchingRunData.getInstance().setProTimeList(null);
					currentStep = 0;
				}
			}
			
			processStarted = false;
			currentStep = 0;
			index = 1;
			size = 0;
		} else {
			Collection sessions3 = sctx.getScriptSessionsByPage(currentPage);
			Util pages3 = new Util(sessions3);
			pages3.addFunctionCall("scheduledTaskRunning");
		}
	}
	
	/**
	 * Delete a particular report file from the server using DWR call
	 *
	 * @param filename report file that will be deleted
	 */
	public void deleteReportFile(String filename) {
		log.info("DWRMatchingConfigUtilities: deleting report " + filename);
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		Report report = service.getReportByName(filename);
		
		if (report != null) {
			service.deleteReport(report);
			log.info("Report Deleted from database");
		}
	}
	
	/**
	 * Accessing report name using DWR call
	 */
	public static void setReportName(String filename) {
		MatchingRunData.getInstance().setRptname(filename);
	}
	
	/**
	 * Get the report block for a particular page out of the report file using DWR call.
	 *
	 * @return content of the next page in the report file
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> getNextPage() {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		String filename = (String) session.getAttribute("reportFilename");
		Map<Integer, Integer> pageCache = (Map<Integer, Integer>) session.getAttribute("reportPagePosition");
		int thisPage = (Integer) session.getAttribute("reportCurrentPage");
		
		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);
		DataBaseReportReader reader = new DataBaseReportReader(filename, pageCache);
		if (thisPage < reader.getLastPage()) {
			session.setAttribute("reportCurrentPage", thisPage + 1);
			currentContent = reader.fetchContent(thisPage + 1);
		} else {
			currentContent = reader.fetchContent(thisPage);
		}
		return currentContent;
	}
	
	/**
	 * Get the report block for a particular page out of the report file using DWR call.
	 *
	 * @return content of the previous page in the report file
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> getPrevPage() {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		String filename = (String) session.getAttribute("reportFilename");
		Map<Integer, Integer> pageCache = (Map<Integer, Integer>) session.getAttribute("reportPagePosition");
		int thisPage = (Integer) session.getAttribute("reportCurrentPage");
		
		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);
		
		DataBaseReportReader reader = new DataBaseReportReader(filename, pageCache);
		if (thisPage > 1) {
			session.setAttribute("reportCurrentPage", thisPage - 1);
			currentContent = reader.fetchContent(thisPage - 1);
		} else {
			currentContent = reader.fetchContent(thisPage);
		}
		return currentContent;
	}
	
	/**
	 * Get the data of the last page of the report
	 * 
	 * @return
	 */
	public List<List<String>> getStartPage() {
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		
		String filename = (String) session.getAttribute("reportFilename");
		Map<Integer, Integer> pageCache = (Map<Integer, Integer>) session.getAttribute("reportPagePosition");
		int thisPage = 1;
		
		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);
		
		DataBaseReportReader reader = new DataBaseReportReader(filename, pageCache);
		
		reader.fetchContent(thisPage);
		
		// only update the value when succeed
		session.setAttribute("reportPagePosition", reader.getPaginationMap());
		session.setAttribute("reportCurrentPage", 1);
		
		// this will replace error message if the process is done
		currentContent = reader.fetchContent(1);
		
		return currentContent;
	}
	
	/**
	 * Get the data of the last page of the report
	 * 
	 * @return
	 */
	public List<List<String>> getEndPage() {
		List<List<String>> currentContent = new ArrayList<List<String>>();
		// init with error message
		List<String> s = new ArrayList<String>();
		s.add("Unable to get the report data, please retry or re-open the report page");
		currentContent.add(s);
		
		WebContext context = WebContextFactory.get();
		HttpSession session = context.getSession();
		
		String filename = (String) session.getAttribute("reportFilename");
		Map<Integer, Integer> pageCache = (Map<Integer, Integer>) session.getAttribute("reportPagePosition");
		
		DataBaseReportReader reader = new DataBaseReportReader(filename, pageCache);
		int currentPage = reader.getLastPage();
		session.setAttribute("reportCurrentPage", currentPage);
		
		currentContent = reader.fetchContent(currentPage);
		return currentContent;
	}
}
