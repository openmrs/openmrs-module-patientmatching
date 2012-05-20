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
	static Map objects;
	static Boolean processStarted = false;
	static String previousProcessTime = "0,0";
	static List<Long> proTimeList;
	static Long time;
	static int reset;
	static int index = 1;
	static int currentStep;
	static int size = 0;
	static String[] selectedStrat;

	@Override
	public void execute() {

		Context.openSession();
		if (!Context.isAuthenticated()) {
			authenticate();
		}

		MatchingRunData.getInstance().setTimerTaskStarted(true);
		doAnalysis(taskDefinition.getProperty("blockList"));
		MatchingRunData.getInstance().setTimerTaskStarted(false);
		Context.closeSession();
	}

	public void getCurrentProcessStatus(int nextStep) {

		time = Calendar.getInstance().getTimeInMillis();
		try {
			switch (nextStep) {
				case 2:
					objects = new HashMap<String, Object>();
					objects = MatchingReportUtils.ReadConfigFile(objects, selectedStrat);
					break;

				case 3:
					objects = MatchingReportUtils.InitScratchTable(objects);
					size = ((List<MatchingConfig>) objects.get("matchingConfigLists")).size();
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
			log.error("Exception caught during the analysis process ...", e);
			reset = -1;
		} catch (Throwable t) {
			LinkDBConnections.getInstance().releaseLock();
			log.error("Throwable object caught during the analysis process ...", t);
			reset = -1;
		}

		time = (Calendar.getInstance().getTimeInMillis() - time);
	}

	/**
	 * @see MatchingReportUtils#doAnalysis()
	 */
	public void doAnalysis(String selectedStrategies) {
		selectedStrat = selectedStrategies.split(",");
		proTimeList = new ArrayList<Long>();
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
