package org.openmrs.module.patientmatching;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.util.OpenmrsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class ReportMigrationUtils {
    public static void migrateFlatFilesToDB(){
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        for(File file : configFileFolder.listFiles()){
            if(file.isFile() && file.getName().startsWith("dedup")){
                Report report = flatFileToReport(file);
                if(report!=null){
                    Context.getService(PatientMatchingReportMetadataService.class).savePatientMatchingReport(report);
                }
            }
        }
    }

    public static Report flatFileToReport(File reportFile){
        if(reportFile.exists()&&reportFile.isFile()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(reportFile));
                Report report = new Report();
                report.setReportName(reportFile.getName());
                //Process first line containing header
                String lineRead = reader.readLine();
                List<String> usedFields = new ArrayList<String>();
                if(lineRead!=null && !lineRead.equals("")){
                    String[] headerItems = lineRead.split("\\|");
                    if(headerItems.length>2){
                        for(int i=2;i<headerItems.length;i++){
                            usedFields.add(headerItems[i]);
                        }
                    }
                }
                Set<MatchingRecord> matchingRecordSet = new TreeSet<MatchingRecord>();
                while((lineRead=reader.readLine())!=null){
                    MatchingRecord matchingRecord = new MatchingRecord();
                    String[] matchItemAttributes = lineRead.split("\\|");
                    matchingRecord.setGroupId(Integer.parseInt(matchItemAttributes[0]));
                    int uniqueId = Integer.parseInt(matchItemAttributes[1]);
                    Patient patient = Context.getPatientService().getPatient(uniqueId);
                    if(patient==null){
                        continue;
                    }
                    matchingRecord.setPatient(patient);
                    matchingRecord.setReport(report);
                    if(!patient.isVoided()){
                        matchingRecord.setState("MERGED");
                    } else{
                        matchingRecord.setState("PENDING");
                    }
                    Set<MatchingRecordAttribute> matchingRecordAttributes = new TreeSet<MatchingRecordAttribute>();
                    for(int i = 0; i<usedFields.size();i++){
                        MatchingRecordAttribute matchingRecordAttribute = new MatchingRecordAttribute();
                        matchingRecordAttribute.setFieldName(usedFields.get(i));
                        matchingRecordAttribute.setFieldValue(matchItemAttributes[i+2]);
                        matchingRecordAttribute.setMatchingRecord(matchingRecord);
                        matchingRecordAttributes.add(matchingRecordAttribute);
                    }
                    matchingRecord.setMatchingRecordAttributeSet(matchingRecordAttributes);
                    matchingRecordSet.add(matchingRecord);
                }
                report.setMatchingRecordSet(matchingRecordSet);
                assignOldReportMetadata(report);
                return report;
            } catch (FileNotFoundException e) {
                //TODO log and handle
            } catch (IOException e) {
                //TODO log and handle
            }
        }
        return null;
    }

    private static void assignOldReportMetadata(Report report){
        String reportName = report.getReportName();

        HibernateSessionFactoryBean bean = new HibernateSessionFactoryBean();
        Configuration cfg = bean.newConfiguration();
        Properties c = cfg.getProperties();

        String url = c.getProperty("hibernate.connection.url");
        String user = c.getProperty("hibernate.connection.username");
        String passwd = c.getProperty("hibernate.connection.password");
        String driver = c.getProperty("hibernate.connection.driver_class");
        Connection databaseConnection;
        try {
            Class.forName(driver);
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    url, user, passwd);
            databaseConnection = connectionFactory.createConnection();
            Statement statement = databaseConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM persistreportdata WHERE report_name='" + reportName+"'");

            String usedStrategies = "";
            String processTimes = "";
            String dateCreated = "";
            if(resultSet.next()){
                usedStrategies = resultSet.getString("strategies_used");
                processTimes = resultSet.getString("process_name_time");
                dateCreated = resultSet.getString("datecreated");
            }
            String[] splittedStrategyList = usedStrategies.split(",");
            Set<PatientMatchingConfiguration> usedStrategySet = new TreeSet<PatientMatchingConfiguration>();
            for(String strategy : splittedStrategyList){
                try {
                    PatientMatchingConfiguration configuration = Context.getService(PatientMatchingReportMetadataService.class).findPatientMatchingConfigurationByName(strategy);
                    if (configuration!=null){
                        usedStrategySet.add(configuration);
                    }
                } catch (Exception e) {
                    //Strategy does not exist
                }
            }
            report.setUsedConfigurationList(usedStrategySet);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
            Date createdOn = format.parse(dateCreated);
            report.setCreatedOn(createdOn);
            String[] processStepTimes = processTimes.split(",");
            Set<ReportGenerationStep> reportGenerationSteps = new TreeSet<ReportGenerationStep>();
            //There are 11 entries in the process_name_time column, first and last are not steps and are always 0,
            // also one before last is also static at 96 ms, so 3 of those steps are ignored and steps from 1-8
            //are converted to db format
            for(int i=1;i<processStepTimes.length-2;i++){
                ReportGenerationStep reportGenerationStep = new ReportGenerationStep();
                reportGenerationStep.setReport(report);
                reportGenerationStep.setSequenceNo(i);
                reportGenerationStep.setProcessName(MatchingReportUtils.REPORT_GEN_STAGES[i-1]);
                reportGenerationStep.setTimeTaken(Integer.parseInt(processStepTimes[i].substring(0,processStepTimes[i].length()-2)));
                reportGenerationSteps.add(reportGenerationStep);
            }
            report.setReportGenerationSteps(reportGenerationSteps);
        } catch (ClassNotFoundException e) {
            //TODO log and handle
        } catch (SQLException e) {
            //TODO log and handle
        } catch (ParseException e) {
            //TODO log and handle
        }
    }
}
