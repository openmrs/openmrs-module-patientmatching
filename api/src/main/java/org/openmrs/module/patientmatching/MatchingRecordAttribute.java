package org.openmrs.module.patientmatching;

public class MatchingRecordAttribute implements Comparable<MatchingRecordAttribute> {
    private int recordAttributeId;
    private MatchingRecord matchingRecord;
    private String fieldName;
    private String fieldValue;

    public int getRecordAttributeId() {
        return recordAttributeId;
    }

    public void setRecordAttributeId(int recordAttributeId) {
        this.recordAttributeId = recordAttributeId;
    }

    public MatchingRecord getMatchingRecord() {
        return matchingRecord;
    }

    public void setMatchingRecord(MatchingRecord matchingRecord) {
        this.matchingRecord = matchingRecord;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public int compareTo(MatchingRecordAttribute o) {
        return 1;
    }
}
