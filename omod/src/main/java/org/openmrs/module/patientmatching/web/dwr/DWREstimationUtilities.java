package org.openmrs.module.patientmatching.web.dwr;

import java.util.List;

import org.openmrs.module.patientmatching.Estimator;

public class DWREstimationUtilities {
	
	private Estimator estimator = new Estimator();
	
	private boolean estimationRan = false;
	
	private final static double MAX_FACTOR = 10.0;
	
	private final static double MIN_FACTOR = 0.01;
	
	public String getEstimationInfomation(List<String> blockingFields) {
		runEstimationProcess(blockingFields);
		long estimatedComparisions = getEstimatedPairs();
		long totalRecords = getTotalRecords();
		String information = "";
		if (estimatedComparisions > MAX_FACTOR * totalRecords) {
			information = "The Strategy results in more record pairs than acceptable level";
		} else if (estimatedComparisions < MIN_FACTOR * totalRecords) {
			information = "The Strategy results in less record pairs than acceptable level";
		} else {
			information = "The strategy is at acceptable level";
		}
		information += ";" + estimatedComparisions + ";" + getEstimatedTimeToRun() + ";" + totalRecords;
		return information;
	}
	
	public long getEstimatedPairs() {
		return estimator.getEstimatedComparisons();
	}
	
	public long getEstimatedTimeToRun() {
		return estimator.getEstimatedTimeToRun();
	}
	
	public long getTotalRecords() {
		return estimator.getTotalRecords();
	}
	
	private void runEstimationProcess(List<String> entryNames) {
		if (!estimationRan) {
			estimator.doEstimationsWithBlockingFields(entryNames);
			estimationRan = true;
		}
	}
}
