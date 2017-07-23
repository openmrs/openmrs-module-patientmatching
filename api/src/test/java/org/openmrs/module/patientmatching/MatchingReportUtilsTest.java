package org.openmrs.module.patientmatching;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

import java.io.IOException;
import java.util.*;

public class MatchingReportUtilsTest extends BaseModuleContextSensitiveTest {

    @Before
    public void setup() throws Exception {
        executeDataSet("PatientMatchingTestDataSet.xml");
    }

    @Test
    public void persistIncrementalReportToDB_shouldCreateNewReportAndUpdateReport() throws IOException {
        // Report name cannot be changed per a particular strategy
        final String reportName = "Test_Report";
        final String selectedStrategy = "family_name block";
        Set<String> includeColumns = new HashSet<String>(Arrays.asList(
                "org.openmrs.PersonName.familyName",
                "org.openmrs.Patient.patientId"
        ));
        Record rec1 = new Record(2, "foo");
        Record rec2 = new Record(6, "foo");
        Record rec3 = new Record(432, "foo");
        Record rec4 = new Record(502, "foo");
        Record rec5 = new Record(999, "foo");
        Record rec6 = new Record(7, "foo");
        Record rec7 = new Record(8, "foo");

        rec1.addDemographic("org.openmrs.PersonName.familyName","Hornblower");
        rec1.addDemographic("org.openmrs.Patient.patientId","2");
        rec2.addDemographic("org.openmrs.PersonName.familyName","Doe");
        rec2.addDemographic("org.openmrs.Patient.patientId","6");
        rec3.addDemographic("org.openmrs.PersonName.familyName","of Cos");
        rec3.addDemographic("org.openmrs.Patient.patientId","432");
        rec4.addDemographic("org.openmrs.PersonName.familyName","of Cos");
        rec4.addDemographic("org.openmrs.Patient.patientId","502");
        rec5.addDemographic("org.openmrs.PersonName.familyName","of Cos");
        rec5.addDemographic("org.openmrs.Patient.patientId","999");
        rec6.addDemographic("org.openmrs.PersonName.familyName","Chebaskwony");
        rec6.addDemographic("org.openmrs.Patient.patientId","7");
        rec7.addDemographic("org.openmrs.PersonName.familyName","Oloo");
        rec7.addDemographic("org.openmrs.Patient.patientId","8");

        RecordSerializer.serialize(rec1);
        RecordSerializer.serialize(rec2);
        RecordSerializer.serialize(rec3);
        RecordSerializer.serialize(rec4);
        RecordSerializer.serialize(rec5);
        RecordSerializer.serialize(rec6);
        RecordSerializer.serialize(rec7);

        PatientMatchingReportMetadataService reportMetadataService = Context.getService(PatientMatchingReportMetadataService.class);

        // Get the Configuration
        List<PatientMatchingConfiguration> configList = Context.getService(PatientMatchingReportMetadataService.class).getMatchingConfigs();
        List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();
        for (PatientMatchingConfiguration config : configList) {
            if (OpenmrsUtil.nullSafeEquals(config.getConfigurationName(), selectedStrategy)) {
                matchingConfigLists.add(ReportMigrationUtils.ptConfigurationToMatchingConfig(config));
            }
        }

        // Case 1: When the Report has not been saved in the database
        Set<Long> matchingPair1 = new HashSet<Long>(Arrays.asList(2L, 6L));
        Set<Long> matchingPair2 = new HashSet<Long>(Arrays.asList(432L, 502L));
        List<Set<Long>> matchingPairsSet1= new ArrayList<Set<Long>>();
        matchingPairsSet1.add(matchingPair1);
        matchingPairsSet1.add(matchingPair2);
        // Call the method
        MatchingReportUtils.persistIncrementalReportToDB(reportName,matchingPairsSet1,includeColumns,matchingConfigLists);
        Report report1 = reportMetadataService.getReportByName(reportName);
        // Get the matching record set from the saved report
        Set<MatchingRecord> matchingRecordSet1 = report1.getMatchingRecordSet();

        // A hash map for the record set 1. Key: PatientId, Value: GroupId
        HashMap<Integer,Integer> mapRecordSet1 = new HashMap<Integer,Integer>();
        for(MatchingRecord mr1 : matchingRecordSet1){
            mapRecordSet1.put(mr1.getPatient().getPatientId(),mr1.getGroupId());
        }

        // Asserts
        Assert.assertNotNull(report1);
        Assert.assertEquals(reportName,report1.getReportName());
        Assert.assertNotNull(report1.getMatchingRecordSet());
        Assert.assertNotEquals(report1.getUsedConfigurationList().size(),0);
        Assert.assertNotEquals(matchingRecordSet1.size(),0);

        // Case 2: When there is already a report in the database, and new patients show matching to the existing groups
        // Two new patient ids have showed a matching property(ids are 2, and 8) with already existing matching pairs in the database
        Set<Long> matchingPair3 = new HashSet<Long>(Arrays.asList(6L, 2L));
        Set<Long> matchingPair4 = new HashSet<Long>(Arrays.asList(432L, 502L, 8L));
        List<Set<Long>> matchingPairsSet2= new ArrayList<Set<Long>>();
        matchingPairsSet2.add(matchingPair3);
        matchingPairsSet2.add(matchingPair4);
        // Call the method
        MatchingReportUtils.persistIncrementalReportToDB(reportName,matchingPairsSet2,includeColumns,matchingConfigLists);
        Report report2 = reportMetadataService.getReportByName(reportName);
        // Get the matching record set from the updated report
        Set<MatchingRecord> matchingRecordSet2 = report2.getMatchingRecordSet();

        // A hash map for the record set 2. Key: PatientId, Value: GroupId
        HashMap<Integer,Integer> mapRecordSet2 = new HashMap<Integer,Integer>();
        for(MatchingRecord mr2 : matchingRecordSet2){
            mapRecordSet2.put(mr2.getPatient().getPatientId(),mr2.getGroupId());
        }

        // Asserts
        Assert.assertNotEquals(report2.getUsedConfigurationList().size(),0);
        Assert.assertNotEquals(matchingRecordSet2.size(),0);
        Assert.assertNotEquals(mapRecordSet1.keySet().size(),mapRecordSet2.keySet().size());

        // Since no additional group ID has been created, group ids of both the sets should be equal
        Set<Integer> valuesSet1 = new HashSet<Integer>(mapRecordSet1.values());
        Set<Integer> valuesSet2 = new HashSet<Integer>(mapRecordSet2.values());
        Assert.assertEquals(valuesSet1.size(),valuesSet2.size());

        // Case 3: When there is already a report in the database, and new patients do not show match to the existing groups
        // Two new patient ids have showed a matching property(ids are 2, and 8) with already existing matching pairs in the database
        Set<Long> matchingPair5 = new HashSet<Long>(Arrays.asList(999L, 7L));
        List<Set<Long>> matchingPairsSet3= new ArrayList<Set<Long>>();
        matchingPairsSet3.add(matchingPair5);
        // Call the method
        MatchingReportUtils.persistIncrementalReportToDB(reportName,matchingPairsSet3,includeColumns,matchingConfigLists);
        Report report3 = reportMetadataService.getReportByName(reportName);
        // Get the matching record set from the updated report
        Set<MatchingRecord> matchingRecordSet3 = report3.getMatchingRecordSet();

        // A hash map for the record set 2. Key: PatientId, Value: GroupId
        HashMap<Integer,Integer> mapRecordSet3 = new HashMap<Integer,Integer>();
        for(MatchingRecord mr3 : matchingRecordSet3){
            mapRecordSet3.put(mr3.getPatient().getPatientId(),mr3.getGroupId());
        }

        // Asserts
        Assert.assertNotEquals(report3.getUsedConfigurationList().size(),0);
        Assert.assertNotEquals(matchingRecordSet3.size(),0);
        Assert.assertNotEquals(mapRecordSet3.keySet().size(),mapRecordSet2.keySet().size());

        // Since a new group ID has been added, group ids should not be equal
        Set<Integer> valuesSet3 = new HashSet<Integer>(mapRecordSet3.values());
        Assert.assertNotEquals(valuesSet1.size(),valuesSet3.size());
    }
}
