package org.openmrs.module.patientmatching;

import org.openmrs.Patient;

public class MatchingRecord implements Comparable<MatchingRecord> {
	private int setId;
	private int groupId;
    private String state;
	private Patient patient;
    private Report report;
	
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
        return 1;   //Ignored
    }
}
