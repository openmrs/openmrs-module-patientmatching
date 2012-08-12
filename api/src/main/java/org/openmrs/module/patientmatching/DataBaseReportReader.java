package org.openmrs.module.patientmatching;

import org.openmrs.api.context.Context;

import java.io.IOException;
import java.util.*;

/**
 * Class to work with reports saved in database
 */
public class DataBaseReportReader {

    private static final int GROUPS_PER_PAGE = 20;

    private Report report;
    private Set<String> includedFields;
    private Map<Integer,Integer> paginationMap;
    private int lastPage;

    public DataBaseReportReader(String reportName){
        this(reportName, new HashMap<Integer, Integer>());
        buildCache();
    }

    public DataBaseReportReader(String reportName, Map<Integer,Integer> paginationMap){
        setReport(reportName);
        setIncludedFields();
        this.paginationMap = paginationMap;
        this.lastPage = paginationMap.size() - 1;   //No of entries minus 0 th entry
    }

    private void buildCache(){
        int groupCount = 0;
        int recordCount = 0;
        int lastGroup = 0;
        int lastPage = 0;
        paginationMap.put(0, 0);
        for(MatchingRecord record : report.getMatchingRecordSet()){
            recordCount ++;
            if(record.getGroupId()!=lastGroup){
                groupCount ++;
                lastGroup = record.getGroupId();
                if(groupCount % GROUPS_PER_PAGE == 0){
                    lastPage++;
                    paginationMap.put(lastPage, recordCount + 1);
                }
            }
        }
        if (!paginationMap.containsValue(recordCount +1)){
            lastPage++;
            paginationMap.put(lastPage,recordCount);
        }
        this.lastPage = lastPage;
    }

    /**
     * Set Report name to fetch from Database
     * @param reportName name of the report to read
     */
    public void setReport(String reportName) {
        report = Context.getService(PatientMatchingReportMetadataService.class).getReportByName(reportName);
    }

    public int getLastPage() {
        return lastPage;
    }

    public Map<Integer,Integer> getPaginationMap() {
        return paginationMap;
    }

    public List<String> getHeader(){
        setIncludedFields();
        List<String> header = new ArrayList<String>();
        header.add("Group Id");
        header.add("Unique Id");
        header.addAll(includedFields);
        header.add("Action");
        return header;
    }

    private void setIncludedFields(){
        includedFields = MatchingReportUtils.getAllFieldsUsed(report);
    }

    public List<List<String>> fetchContent(int page) throws IOException {
        if(page>lastPage){
            page = lastPage;
        }

        if(page<=0){
            page=1;
        }

        int start = paginationMap.get(page-1);
        int end = paginationMap.get(page);
        List<MatchingRecord> records = new ArrayList<MatchingRecord>(report.getMatchingRecordSet()).subList(start,end);
        List<List<String>> content = new ArrayList<List<String>>();
        for(MatchingRecord record : records){
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
}
