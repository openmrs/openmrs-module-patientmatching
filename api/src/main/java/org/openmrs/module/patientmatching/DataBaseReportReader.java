package org.openmrs.module.patientmatching;

import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to read the reports saved in database. If the report has too many pairs, this will divide them into pages
 * And will show the defined number of pages per each group
 */
public class DataBaseReportReader {

    /**
     * Number of matching record groups to show in a single page.
     * The page will display roughly twice records as this number
     * as page breaks are defined by groups instead of pairs.
     * This will build up a map with details of
     * where the pages should be divided and store it in the session,
     * That map will be used in the next reads and it will avoid recalculating where the pages are "breaked"
     * and what should be displayed in any given page.
     */
    private static final int GROUPS_PER_PAGE = 20;

    private Report report;
    private Set<String> includedFields;
    private Map<Integer,Integer> paginationMap;
    private int lastPage;

    /**
     * Constructor that is called when the report is first displayed. Calculate and build the mapping for the next pages
     * @param reportName name of the report to read
     */
    public DataBaseReportReader(String reportName){
        this(reportName, new HashMap<Integer, Integer>());
        buildCache();
    }

    /**
     * Constructor that is called when navigating through the pages.
     * @param reportName name of the report to read
     * @param paginationMap The mapping of records to the pages. This is calculated in the other constructor and reused
     *                      having stored in the session
     */
    public DataBaseReportReader(String reportName, Map<Integer,Integer> paginationMap){
        setReport(reportName);
        setIncludedFields();
        this.paginationMap = paginationMap;
        this.lastPage = paginationMap.size() - 1;   //Number of counting the 0th entry
    }

    /**
     * build the mapping of records to the pages they should be in
     */
    private void buildCache(){
        int groupCount = 0;
        int recordCount = 0;
        int lastGroup = -1;
        int lastPage = 0;
        paginationMap.put(0, 0); //first page starts from the 0th record
        for(MatchingRecord record : report.getMatchingRecordSet()){ //as the set is a sorted, the iteration happens starting from group 0
            recordCount ++;
            if(record.getGroupId()!=lastGroup){
                //the record is from a new group
                groupCount ++;
                lastGroup = record.getGroupId();
                if(groupCount % GROUPS_PER_PAGE == 0){
                    //should start a new page
                    lastPage++;
                    paginationMap.put(lastPage, recordCount + 1);
                }
            }
        }
        if (!paginationMap.containsValue(recordCount + 1)){
            //adds the index of the last record if not already there
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

    /**
     * Get the last page number that the report has.
     * This depends on the number of groups to display and the groups identified by the report
     * @return
     */
    public int getLastPage() {
        return lastPage;
    }

    /**
     * Get the indexing map of record to page
     * @return
     */
    public Map<Integer,Integer> getPaginationMap() {
        return paginationMap;
    }

    /**
     * Get the header to display in the report. This contains the field specified, the strategies used,
     * the group, and patient ids
     * @return
     */
    public List<String> getHeader(){
        setIncludedFields();
        List<String> header = new ArrayList<String>();
        header.add("Group Id");
        header.add("Unique Id");
        header.addAll(includedFields);
        header.add("Action");
        return header;
    }

    /**
     * get the fields used by the configurations specified in the report
     */
    private void setIncludedFields(){
        includedFields = MatchingReportUtils.getAllFieldsUsed(report);
    }

    /**
     * Get the data of the records to display in for the given page
     * @param page the page number to display (1 is the first page)
     * @return
     */
    public List<List<String>> fetchContent(int page) {
        if(page>lastPage){
            page = lastPage;
        }

        if(page<=0){
            page=1;
        }

        int start = paginationMap.get(page-1);  //get the index for the starting and ending record
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
