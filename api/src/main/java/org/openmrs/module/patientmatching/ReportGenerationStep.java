package org.openmrs.module.patientmatching;

public class ReportGenerationStep implements Comparable<ReportGenerationStep> {
	
	private int stepId;
	
	private Report report;
	
	private String processName;
	
	private int sequenceNo;
	
	private int timeTaken; //in milliseconds
	
	public int getStepId() {
		return stepId;
	}
	
	public Report getReport() {
		return report;
	}
	
	public void setReport(Report report) {
		this.report = report;
	}
	
	public String getProcessName() {
		return processName;
	}
	
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public int getSequenceNo() {
		return sequenceNo;
	}
	
	public void setStepId(int stepId) {
		this.stepId = stepId;
	}
	
	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}
	
	public int getTimeTaken() {
		return timeTaken;
	}
	
	public void setTimeTaken(int timeTaken) {
		this.timeTaken = timeTaken;
	}
	
	public int compareTo(ReportGenerationStep o) {
		return 1; //Ignored for now
	}
}
