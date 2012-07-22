package org.openmrs.module.patientmatching;

import org.openmrs.api.context.Context;

import java.io.IOException;
import java.util.*;

/**
 * Implementation of ReportReader class to work with reports saved in database
 */
public class DataBaseReportReader implements ReportReader {

    private Report report;
    private Set<String> includedFields;

    /**
     * Set Report name to fetch from Database
     * @param reportName name of the report to read
     */
    public void setReport(String reportName) {
        report = Context.getService(PatientMatchingReportMetadataService.class).getReportByName(reportName);
    }

    public boolean isEof() {
        return false;       //TODO : To be implemented
    }

    public void setEof(boolean eof) {
        //TODO : To be implemented
    }

    public List<Long> getPagePos() {
        return new ArrayList<Long>(Arrays.asList(new Long[]{0L}));
        //TODO : To be completed
    }

    public void setPagePos(List<Long> pagePos) {
        //TODO : To be implemented
    }

    public int getCurrentPage() {
        return 0;  //TODO : To be implemented
    }

    public void setCurrentPage(int currentPage) {
        //TODO : To be implemented
    }

    public List<String> getHeader() throws IOException {
        List<String> header = new ArrayList<String>();
        header.add("Group Id");
        header.add("Unique Id");
        includedFields = MatchingReportUtils.getAllFieldsUsed(report);
        header.addAll(includedFields);
        header.add("Action");
        return header;
    }

    public List<List<String>> getCurrentContent() {
        List<List<String>> content = new ArrayList<List<String>>();
        for(MatchingRecord record : report.getMatchingRecordSet()){
            List<String> recordData = new ArrayList<String>();
            recordData.add(record.getPatient().getVoided().toString());
            recordData.add(record.getPatient().getPatientId().toString());
            recordData.add(String.valueOf(record.getGroupId()));
            recordData.add(record.getPatient().getPatientId().toString());
            Map<String,String> attribMap = new HashMap<String, String>();
            for(MatchingRecordAttribute attribute: record.getMatchingRecordAttributeSet()){
                attribMap.put(attribute.getFieldName(),attribute.getFieldValue());
            }
            for(String field:includedFields){
                recordData.add(attribMap.get(field));
            }
            content.add(recordData);
        }
        return content;
    }

    public void fetchContent(int page) throws IOException {
        //TODO : To be implemented
    }
}
