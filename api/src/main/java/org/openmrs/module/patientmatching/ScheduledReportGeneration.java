package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.regenstrief.linkage.util.MatchingConfig;

public class ScheduledReportGeneration extends AbstractTask {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Map objects;
	
	private Boolean processStarted = false;
	
	private String previousProcessTime = "0,0";
	
	private List<Long> proTimeList;
	
	private Long time;
	
	private int reset;
	
	private int index = 1;
	
	private int currentStep;
	
	private int size = 0;
	
	private String[] selectedStrat;
	
	private MatchingRunData matchingRunData;
	
	@Override
	public void execute() {
		
		Context.openSession();
		
		matchingRunData = MatchingRunData.createInstance();
		matchingRunData.setTimerTaskStarted(true);
		matchingRunData.setFileStrat(taskDefinition.getProperty("blockList"));
		matchingRunData.setRunName(getTaskDefinition().getName());
		
		MatchingRunData.addTask(getTaskDefinition().getName());
		try {
			doAnalysis();
		}
		finally {
			MatchingRunData.removeTask(getTaskDefinition().getName());
		}
		
		matchingRunData.setTimerTaskStarted(false);
		Context.closeSession();
	}
	
	@Override
	public void shutdown() {
		MatchingRunData.removeTask(getTaskDefinition().getName());
		super.shutdown();
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
					MatchingReportUtils.ScoringData(objects, matchingRunData);
					break;
				
				case 10:
					MatchingReportUtils.CreatingReport(objects, matchingRunData);
					break;
			}
			
		}
		catch (Exception e) {
			LinkDBConnections.getInstance().releaseLock();
			log.error("Exception caught during the analysis process ...", e);
			reset = -1;
		}
		catch (Throwable t) {
			LinkDBConnections.getInstance().releaseLock();
			log.error("Throwable object caught during the analysis process ...", t);
			reset = -1;
		}
		
		time = (Calendar.getInstance().getTimeInMillis() - time);
	}
	
	public void doAnalysis() {
		selectedStrat = matchingRunData.getFileStrat().split(",");
		proTimeList = new ArrayList();
		matchingRunData.setProTimeList(proTimeList);
		reset = 0;
		for (int i = 2; i < 11; i++) {
			currentStep = i;
			processStarted = true;
			getCurrentProcessStatus(i);
			processStarted = false;
			if (reset == -1) {
				break;
			}
			
			if (size > 1 && index > 1 && i >= 4 && i <= 9) {
				int j = i;
				time = (time + proTimeList.get(i - 2));
				if (i == 9 && size != index) {
					j = 3;
					previousProcessTime = j + "," + time + "p";
				} else {
					previousProcessTime = j + "," + time;
				}
				proTimeList.set((i - 2), time);
			} else {
				int j = i;
				if (size > 1 && i == 9) {
					j = 3;
					previousProcessTime = j + "," + time + "p";
				} else {
					previousProcessTime = i + "," + time;
				}
				proTimeList.add(time);
			}
			
			if (i == 9 && size != index && size != 0) {
				objects.put("matchingConfig", ((List<MatchingConfig>) objects.get("matchingConfigLists")).get(index));
				index++;
				i = 3;
			}
			
			if (reset == -1) {
				proTimeList = null;
				currentStep = 0;
			}
		}
		processStarted = false;
		currentStep = 0;
		index = 1;
		size = 0;
	}
}
