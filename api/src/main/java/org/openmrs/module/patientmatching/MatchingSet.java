package org.openmrs.module.patientmatching;

import org.openmrs.Patient;

public class MatchingSet implements Comparable<MatchingSet> {
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

    public int compareTo(MatchingSet o) {
        if(o instanceof MatchingSet){
            MatchingSet newSet = (MatchingSet) o;
            if(report.getReportId()== newSet.report.getReportId() && groupId==newSet.groupId &&
                    patient.getId()==newSet.patient.getId()){
                return 0;
            }
        }
        return 1;
    }
}
