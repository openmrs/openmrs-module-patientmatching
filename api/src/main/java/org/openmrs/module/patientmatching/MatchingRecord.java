package org.openmrs.module.patientmatching;

import org.openmrs.Patient;

import java.util.Set;

public class MatchingRecord implements Comparable<MatchingRecord> {
	private int setId;
	private int groupId;
    private String state;
	private Patient patient;
    private Report report;
    private Set<MatchingRecordAttribute> matchingRecordAttributeSet;
	
	public int getSetId() {
		return setId;
	}
	public void setSetId(int setId) {
		this.setId = setId;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int compareTo(MatchingRecord o) {
        //Sort by group first and then by patient
        if(groupId == o.groupId){
            return patient.getId() - o.patient.getId();
        }
        return groupId - o.groupId;
    }

    public Set<MatchingRecordAttribute> getMatchingRecordAttributeSet() {
        return matchingRecordAttributeSet;
    }

    public void setMatchingRecordAttributeSet(Set<MatchingRecordAttribute> matchingRecordAttributeSet) {
        this.matchingRecordAttributeSet = matchingRecordAttributeSet;
    }
}
