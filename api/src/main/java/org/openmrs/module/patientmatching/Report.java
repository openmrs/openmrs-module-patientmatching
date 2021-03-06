package org.openmrs.module.patientmatching;

/**
 * A class to represent reports generated by the PatientMatching module.
 * Initially, reports were stored as xml files in the users machine.
 * 
 * However, reports are being moved into the database as part of plans
 * to move all external flat files into a database.
 */

import org.openmrs.User;

import java.util.Date;
import java.util.Set;

public class Report {
	
	private int reportId;
	
	private String reportName;
	
	private User createdBy;
	
	private Date createdOn;
	
	private Set<MatchingRecord> matchingRecordSet;
	
	private Set<PatientMatchingConfiguration> usedConfigurationList;
	
	private Set<ReportGenerationStep> reportGenerationSteps;
	
	public int getReportId() {
		return reportId;
	}
	
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	
	public String getReportName() {
		return reportName;
	}
	
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public Set<MatchingRecord> getMatchingRecordSet() {
		return matchingRecordSet;
	}
	
	public Set<ReportGenerationStep> getReportGenerationSteps() {
		return reportGenerationSteps;
	}
	
	public void setMatchingRecordSet(Set<MatchingRecord> matchingRecordSet) {
		this.matchingRecordSet = matchingRecordSet;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	public Date getCreatedOn() {
		return createdOn;
	}
	
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	public Set<PatientMatchingConfiguration> getUsedConfigurationList() {
		return usedConfigurationList;
	}
	
	public void setUsedConfigurationList(Set<PatientMatchingConfiguration> usedConfigurationList) {
		this.usedConfigurationList = usedConfigurationList;
	}
	
	public void setReportGenerationSteps(Set<ReportGenerationStep> reportGenerationSteps) {
		this.reportGenerationSteps = reportGenerationSteps;
	}
}
