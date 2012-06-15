package org.openmrs.module.patientmatching.web.dwr;

import java.util.List;

import org.openmrs.module.patientmatching.Estimator;

import freemarker.core.ReturnInstruction;

public class DWREstimationUtilities {

	private Estimator estimator;
	private boolean estimationRan;
	private final double MAX_FACTOR = 10.0;
	private final double MIN_FACTOR = 0.01;

	public DWREstimationUtilities() {
		estimator = new Estimator();
		estimationRan = false;
	}

	public String getEstimationInfomation(List<String> blockingFields) {
		System.out.println("in estimation info with "+ blockingFields);
		runEstimationProcess(blockingFields);
		long estimatedComparisions = estimator.getEstimatedComparisons();
		long totalRecords = estimator.getTotalRecords();
		String information = "";
		if (estimatedComparisions > MAX_FACTOR * totalRecords) {
			information = "The Strategy results in more record pairs than acceptable level";
		} else if(estimatedComparisions < MIN_FACTOR * totalRecords){
			information = "The Strategy results in less record pairs than acceptable level";
		} else{
			information = "The stratergy is at acceptable level";
		}
		information += ";"+getEstimatedPairs()+";"+ getEstimatedTimeToRun()+";"+getTotalRecords();
		return information;
	}

	public long getEstimatedPairs(){
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
