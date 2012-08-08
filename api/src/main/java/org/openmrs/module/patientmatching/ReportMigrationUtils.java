package org.openmrs.module.patientmatching;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;

import java.io.BufferedReader;
import java.io.File;
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
    protected static final Log log = LogFactory.getLog(ReportMigrationUtils.class);

    public static void migrateFlatFilesToDB(){
        migrateConfigurations();
        migrateReports();
    }

    public static void migrateReports() {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        for (File file : configFileFolder.listFiles()) {
            if (file.isFile() && file.getName().startsWith("dedup")) {
                Report report = flatFileToReport(file);
                if (report != null) {
                    Context.getService(PatientMatchingReportMetadataService.class).savePatientMatchingReport(report);
                }

                //Backup report file
                String backupFileName = "backup-" + file.getName();
                file.renameTo(new File(configFileFolder, backupFileName));
                log.info("Report migrated to database. File backed up as " + backupFileName);
            }
        }
    }

    public static Report flatFileToReport(File reportFile){
        if(reportFile.exists()&&reportFile.isFile()){
            try {
                log.info("Moving " + reportFile.getName() + " to database");
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
                        log.warn("Patient with id " + uniqueId + " does not exist in the database");
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
            } catch (IOException e) {
                log.error("IO Error ", e);
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
            log.error("MySQL driver not found", e);
        } catch (SQLException e) {
            log.error("SQL error", e);
        } catch (ParseException e) {
            log.error("Invalid date format ", e);
        }
    }

    public static void migrateConfigurations() {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

        RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
        List<MatchingConfig> matchConf = recMatchConfig.getMatchingConfigs();

        PatientMatchingReportMetadataService metadataService = Context.getService(PatientMatchingReportMetadataService.class);

        List<String> existingConfigList = MatchingConfigurationUtils.listAvailableBlockingRuns_db();

        for (MatchingConfig conf : matchConf) {
            if (existingConfigList.contains(conf.getName())) {
                log.warn("There exists a configuration in the database with same name as " + conf.getName() + ". The configuration Ignored.");
                continue;
            }
            PatientMatchingConfiguration configuration = new PatientMatchingConfiguration();
            List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();
            configuration.setConfigurationName(conf.getName());
            Set<ConfigurationEntry> configurationEntries = new TreeSet<ConfigurationEntry>();
            for (MatchingConfigRow row : conf.getMatchingConfigRows()) {
                if (row.isIncluded()) {
                    ConfigurationEntry entry = new ConfigurationEntry();
                    entry.setFieldName(row.getName());
                    entry.setSET(row.getSetID());
                    entry.setPatientMatchingConfiguration(configuration);

                    //set name and view name
                    if (row.getName().startsWith("org.openmrs")) {
                        entry.setFieldViewName("patientmatching." + row.getName());
                    } else {
                        entry.setFieldViewName(row.getName());
                    }

                    //Check whether must match field or should match field
                    if (row.getBlockOrder() > 0) {
                        entry.setBlocking();
                        entry.setBlockOrder(row.getBlockOrder());
                    } else {
                        entry.setIncluded();
                    }
                    configurationEntries.add(entry);
                }
            }
            configuration.setConfigurationEntries(configurationEntries);
            configuration.setUsingRandomSample(conf.isUsingRandomSampling());
            configuration.setRandomSampleSize(conf.getRandomSampleSize());
            metadataService.savePatientMatchingConfiguration(configuration);
            log.info("Configuration " + conf.getName() + " successfully migrated to database");
        }
    }

    public static PatientMatchingConfiguration matchingConfToPMConfiguration(MatchingConfig conf){
        PatientMatchingConfiguration configuration = new PatientMatchingConfiguration();
        List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();
        configuration.setConfigurationName(conf.getName());
        Set<ConfigurationEntry> configurationEntries = new TreeSet<ConfigurationEntry>();
        for (MatchingConfigRow row : conf.getMatchingConfigRows()) {
            if (row.isIncluded()) {
                ConfigurationEntry entry = new ConfigurationEntry();
                entry.setFieldName(row.getName());
                entry.setSET(row.getSetID());
                entry.setPatientMatchingConfiguration(configuration);

                //set name and view name
                if (row.getName().startsWith("org.openmrs")) {
                    entry.setFieldViewName("patientmatching." + row.getName());
                } else {
                    entry.setFieldViewName(row.getName());
                }

                //Check whether must match field or should match field
                if (row.getBlockOrder() > 0) {
                    entry.setBlocking();
                    entry.setBlockOrder(row.getBlockOrder());
                } else {
                    entry.setIncluded();
                }
                configurationEntries.add(entry);
            }
        }
        configuration.setConfigurationEntries(configurationEntries);
        configuration.setUsingRandomSample(conf.isUsingRandomSampling());
        configuration.setRandomSampleSize(conf.getRandomSampleSize());
        return configuration;
    }

    public static MatchingConfig ptConfigurationToMatchingConfig(PatientMatchingConfiguration configuration){
        List<MatchingConfigRow> matchingConfigRows = new ArrayList<MatchingConfigRow>();
        int blockOrderCount = 1;
        for (ConfigurationEntry entry : configuration.getConfigurationEntries()){
            MatchingConfigRow row = new MatchingConfigRow(entry.getFieldName());
            if(entry.isBlocking()){
                row.setInclude(true);
                row.setBlockOrder(blockOrderCount);
                blockOrderCount++;
            }else if( entry.isIncluded()){
                row.setInclude(true);
            }else {
                row.setInclude(false);
            }
            row.setBlockChars(40);
            row.setAlgorithm(MatchingConfig.EXACT_MATCH);
            row.setAgreement(0.9);
            row.setNonAgreement(0.1);
            matchingConfigRows.add(row);
        }
        MatchingConfig matchingConfig = new MatchingConfig(configuration.getConfigurationName(),
                matchingConfigRows.toArray(new MatchingConfigRow[matchingConfigRows.size()]));
        return matchingConfig;

    }
}
