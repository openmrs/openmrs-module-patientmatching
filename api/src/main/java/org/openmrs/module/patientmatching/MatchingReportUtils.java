package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.io.DataBaseRecordStore;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OpenMRSReader;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.matchresult.DedupMatchResultList;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;

/**
 * Utility class to perform various task related to the creating report for all
 * available blocking runs in the configuration file.
 */
public class MatchingReportUtils {

	protected final static Log log = LogFactory.getLog(MatchingReportUtils.class);

	/**
	 * Initial status of creating report process. The following are three main
	 * status of the process.
	 */
	public static final String NO_PROCESS = "No running process";
	public static final String END_PROCESS = "Report finished";
	public static final String PREM_PROCESS = "Prematurely terminated process";

    /**
     * List of steps in the reporting process.
     */
    public static final String[] REPORT_GEN_STAGES = {
            "Read configuration file",
            "Create scratch table",
            "Create random sample analyzer",
            "Create analyzer form pairs",
            "Create pair data source analyzer",
            "Create expectation maximization (EM) analyzer",
            "Analyze pairs",
            "Score pairs",
    };
	
	/**
	 * Fields that need to be checked to see the current status of the creating
	 * report process
	 */
	public static String status = NO_PROCESS;
	public static int i;

    /**
     * Constant name is given for a report in the incremental patient matching process
     */
	private static final String REPORT_NAME = "dedup-incremental-report-";

	/**
	 * 
	 * Get the list of steps (statuses) that the analysis has to go through
	 * 
	 * @return list of steps
	 */
	public static List<String> listSteps() {
		List<String> stepList = new ArrayList<String>();
        stepList.add(NO_PROCESS);
        stepList.addAll(Arrays.asList(REPORT_GEN_STAGES));
        stepList.add(END_PROCESS);
		return stepList;
	}
	
	//New Method1 2
	public static Map<String, Object> ReadConfigFile(Map<String,Object> objects, String[] selectedStrategies){
		log.info("Starting generate report process sequence");
		
		// open the config.xml file
		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
		File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

		log.info("Reading matching config file from " + configFile.getAbsolutePath());
		
		RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
		List<PatientMatchingConfiguration> configList = Context.getService(PatientMatchingReportMetadataService.class).getMatchingConfigs();
        List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();

		for (String selectedStrat : selectedStrategies) {
			for (PatientMatchingConfiguration config : configList) {
				if (OpenmrsUtil.nullSafeEquals(config.getConfigurationName(), selectedStrat)) {
					matchingConfigLists.add(ReportMigrationUtils.ptConfigurationToMatchingConfig(config));
				}
			}
		}
		
		DedupMatchResultList handler = new DedupMatchResultList();

		Properties c = Context.getRuntimeProperties();

		String url = c.getProperty("connection.url");
		String user = c.getProperty("connection.username");
		String passwd = c.getProperty("connection.password");
		String driver = c.getProperty("connection.driver_class");
		log.info("URL: " + url);

		Connection databaseConnection = null;
		try {
			Class.forName(driver);
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
					url, user, passwd);
			databaseConnection = connectionFactory.createConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		objects.put("databaseConnection", databaseConnection);
		objects.put("recMatchConfig", recMatchConfig);
		objects.put("driver", driver);
		objects.put("url", url);
		objects.put("user", user);
		objects.put("passwd", passwd);
		objects.put("matchingConfigLists", matchingConfigLists);
		objects.put("configFileFolder", configFileFolder);
		objects.put("handler", handler);
		return objects;
	}
	//New Method1 End 2
	
	//New Method2 3
	public static Map<String, Object> InitScratchTable(Map<String,Object> objects){
		Connection databaseConnection=(Connection) objects.get("databaseConnection");
		RecMatchConfig recMatchConfig=(RecMatchConfig) objects.get("recMatchConfig");
		List<MatchingConfig> matchingConfigList = (List<MatchingConfig>)objects.get("matchingConfigLists");
		MatchingConfig matchingConfig = matchingConfigList.get(0);
		String driver=(String) objects.get("driver");
		String url = (String) objects.get("url");
		String user = (String) objects.get("user");
		String passwd = (String) objects.get("passwd");
		
		log.info("Initiating scratch table");
		List<String> blockingColumns = Arrays.asList(matchingConfig.getBlockingColumns());
		Set<String> globalIncludeColumns = new TreeSet<String>();
		globalIncludeColumns.addAll(blockingColumns);
		List<String> includeColumns = Arrays.asList(matchingConfig.getIncludedColumnsNames());
		globalIncludeColumns.addAll(includeColumns);
		objects.put("globalIncludeColumns", globalIncludeColumns);
		DataBaseRecordStore recordStore = new DataBaseRecordStore(
				databaseConnection, recMatchConfig.getLinkDataSource1(),
				driver, url, user, passwd);
		recordStore.clearRecords();

		// If only a single strategy has been selected
		Report report = null;
		if(matchingConfigList.size() == 1){
			String reportName = REPORT_NAME + matchingConfig.getName();
			report = Context.getService(PatientMatchingReportMetadataService.class).getReportByName(reportName);
		}
		OpenMRSReader reader = new OpenMRSReader(globalIncludeColumns, report != null ? report.getCreatedOn() : null);
		while (reader.hasNextRecord()) {
			recordStore.storeRecord(reader.nextRecord());
		}
		recordStore.close();
		reader.close();
		
		ReaderProvider rp = ReaderProvider.getInstance();

		objects.put("recordStore", recordStore);
		objects.put("rp", rp);
		objects.put("recMatchConfig", recMatchConfig);
		objects.put("matchingConfig", matchingConfig);
		return objects;
	}
	//New Method2 End 3
	
	//New Method3 End 4
	public static Map<String, Object> CreRanSamAnalyzer(Map<String,Object> objects){
		RecMatchConfig recMatchConfig=(RecMatchConfig) objects.get("recMatchConfig");
		log.info("Creating random sample analyzer");
		OrderedDataSourceReader databaseReaderRandom = ((ReaderProvider) objects.get("rp")).getReader(
				((DataBaseRecordStore) objects.get("recordStore")).getRecordStoreLinkDataSource(), 
				(MatchingConfig)objects.get("matchingConfig"));
		DedupOrderedDataSourceFormPairs formPairsRandom = new DedupOrderedDataSourceFormPairs(
				databaseReaderRandom, (MatchingConfig)objects.get("matchingConfig"), recMatchConfig
						.getLinkDataSource1().getTypeTable());
		RandomSampleAnalyzer rsa = new RandomSampleAnalyzer((MatchingConfig)objects.get("matchingConfig"),
				formPairsRandom);
		databaseReaderRandom.close();
		objects.put("recMatchConfig", recMatchConfig);

		objects.put("rsa", rsa);
		return objects;
	}
	//New Method3 End 4
	
	//New Method4 5
	public static Map<String, Object> CreAnalFormPairs(Map<String,Object> objects){
		RecMatchConfig recMatchConfig=(RecMatchConfig) objects.get("recMatchConfig");
		log.info("Creating analyzer form pairs");
		OrderedDataSourceReader databaseReader = ((ReaderProvider) objects.get("rp")).getReader(
				((DataBaseRecordStore) objects.get("recordStore")).getRecordStoreLinkDataSource(), 
				(MatchingConfig)objects.get("matchingConfig"));
		DedupOrderedDataSourceFormPairs formPairs = new DedupOrderedDataSourceFormPairs(
				databaseReader, (MatchingConfig)objects.get("matchingConfig"), recMatchConfig
						.getLinkDataSource1().getTypeTable());
		
		objects.put("databaseReader", databaseReader);
		objects.put("formPairs", formPairs);
		return objects;
	}
	//New Method4 End 5
	
	//New Method5 6
	public static Map<String, Object> CrePairdataSourAnalyzer(Map<String,Object> objects){
		RandomSampleAnalyzer rsa= (RandomSampleAnalyzer) objects.get("rsa");
		DedupOrderedDataSourceFormPairs formPairs= (DedupOrderedDataSourceFormPairs) objects.get("formPairs");
		log.info("Creating pair data source analyzer");
		PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(formPairs);

		log.info("Adding random sample analyzer");
		pdsa.addAnalyzer(rsa);
		
		objects.put("pdsa", pdsa);
		return objects;
	}
	//New Method5 End 6
	
	//New Method6 7
	public static Map<String, Object> CreEMAnalyzer(Map<String,Object> objects){
		PairDataSourceAnalysis pdsa=(PairDataSourceAnalysis)objects.get("pdsa");
		log.info("Creating EM analyzer");
		EMAnalyzer ema = new EMAnalyzer((MatchingConfig)objects.get("matchingConfig"));

		log.info("Adding EM analyzer");
		pdsa.addAnalyzer(ema);
		objects.put("pdsa", pdsa);
		return objects;
	}
	//New Method6 End 7

	//New Method7 8
	public static Map<String, Object> AnalyzingData(Map<String,Object> objects){
		OrderedDataSourceReader databaseReader=(OrderedDataSourceReader)objects.get("databaseReader");
		PairDataSourceAnalysis pdsa=(PairDataSourceAnalysis)objects.get("pdsa");
		log.info("Analyzing data");
		pdsa.analyzeData();
		int n = pdsa.getRecordPairCount();
		log.info("patientmatching:  analyzed " + n + " pairs of records");
		objects.put("pdsa", pdsa);
		databaseReader.close();
		return objects;
	}	
	//New Method7 End 8
	
	//New Method8 9
	public static Map<String, Object> ScoringData(Map<String, Object> objects) throws IOException {
		DedupMatchResultList handler = (DedupMatchResultList) objects.get("handler");
		RecMatchConfig recMatchConfig = (RecMatchConfig) objects.get("recMatchConfig");
		MatchingConfig matchingConfig = (MatchingConfig) objects.get("matchingConfig");
		log.info("Scoring data");
		OrderedDataSourceReader databaseReaderScore = ((ReaderProvider) objects.get("rp")).getReader(
				((DataBaseRecordStore) objects.get("recordStore")).getRecordStoreLinkDataSource(), 
				matchingConfig);
		DedupOrderedDataSourceFormPairs formPairsScoring = new DedupOrderedDataSourceFormPairs(
				databaseReaderScore, matchingConfig, recMatchConfig.getLinkDataSource1().getTypeTable());
		ScorePair sp = new ScorePair(matchingConfig);

		Record[] pair;
		MatchResult mr;

		while ((pair = formPairsScoring.getNextRecordPair()) != null) {
			mr = sp.scorePair(pair[0], pair[1]);
			handler.acceptMatchResult(mr);
		}

		databaseReaderScore.close();

		objects.put("handler", handler);
		return objects;
	}
	//New Method8 End 9

	//New Method9 10
	public static Map<String, Object> CreatingReport(Map<String,Object> objects) throws IOException{
		log.info("Creating report");

		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		String dateString = format.format(new Date());
        Set<String> globalIncludeColumns = (Set<String>)objects.get("globalIncludeColumns");
		
		String configString = new String();
		try{
			List<MatchingConfig> mcs = (List<MatchingConfig>)objects.get("matchingConfigLists");
            for(MatchingConfig mc : mcs){
                configString += mc.getName() + "-";
            }
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}

        String reportName = REPORT_NAME + configString+dateString;
		DedupMatchResultList handler = (DedupMatchResultList)objects.get("handler");

		handler.flattenPairIdList();
		List<Set<Long>> matchingPairs = handler.getFlattenedPairIds();
		persistReportToDB(reportName, matchingPairs, globalIncludeColumns);
		return objects;
	}	
	//New Method9 End 10

	/**
	 * Method to get the list of the available report for display. The method
	 * will return all report found in the designated folder in the server.
	 * 
	 * @return all available report in the server
	 */
    @Deprecated
	public static List<String> listAvailableReport() {
		log.info("Listing all available report");
		List<String> reports = new ArrayList<String>();

		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil
				.getDirectoryInApplicationDataDirectory(configLocation);

		File[] files = configFileFolder.listFiles();
		for (File file : files) {
			if (file.getName().startsWith("dedup")) {
				reports.add(file.getName());
			}
		}

		Collections.sort(reports);

		return reports;
	}

    public static List<String> listAvailableReportNamesInDB(){
        return Context.getService(PatientMatchingReportMetadataService.class).getReportNames();
    }

    /**
     * Method to persist the report to the database
     * @param rptName The report name of the new report
     * @param matchingPairs A list of the matching pair sets
     */
	public static void persistReportToDB(String rptName, List<Set<Long>> matchingPairs, Set<String> includeColumns) throws FileNotFoundException {
        Report report = new Report();
        report.setCreatedBy(Context.getAuthenticatedUser());
        report.setReportName(rptName);
        report.setCreatedOn(new Date());
        String selectedStrategies = MatchingRunData.getInstance().getFileStrat();
        String[] selectedStrategyNamesArray = selectedStrategies.split(",");
        Set<PatientMatchingConfiguration> usedConfigurations = new TreeSet<PatientMatchingConfiguration>();
        PatientMatchingReportMetadataService reportMetadataService = Context.getService(PatientMatchingReportMetadataService.class);

        for(String strategyName : selectedStrategyNamesArray){
            PatientMatchingConfiguration configuration = reportMetadataService.findPatientMatchingConfigurationByName(strategyName);
            usedConfigurations.add(configuration);
        }

        report.setUsedConfigurationList(usedConfigurations);
        PatientService patientService = Context.getPatientService();
        Set<MatchingRecord> matchingRecordSet = new TreeSet<MatchingRecord>();
        for (int j = 0; j < matchingPairs.size(); j++) {
            Set<Long> matchSet = matchingPairs.get(j);
            for(Long patientId: matchSet){
                MatchingRecord matchingRecord = new MatchingRecord();
                matchingRecord.setGroupId(j);
                matchingRecord.setState("PENDING");   //TODO move to a constant
                matchingRecord.setPatient(patientService.getPatient(patientId.intValue()));
                matchingRecord.setReport(report);

                Set<MatchingRecordAttribute> matchingRecordAttributeSet = new TreeSet<MatchingRecordAttribute>();
                Record record = RecordSerializer.deserialize(String.valueOf(patientId));
                for(String includedColumn:includeColumns){
                    MatchingRecordAttribute matchingRecordAttribute = new MatchingRecordAttribute();
                    matchingRecordAttribute.setFieldName(includedColumn);
                    matchingRecordAttribute.setFieldValue(record.getDemographic(includedColumn));
                    matchingRecordAttribute.setMatchingRecord(matchingRecord);
                    matchingRecordAttributeSet.add(matchingRecordAttribute);
                }
                matchingRecord.setMatchingRecordAttributeSet(matchingRecordAttributeSet);
                matchingRecordSet.add(matchingRecord);
            }
        }
        report.setMatchingRecordSet(matchingRecordSet);

        Set<ReportGenerationStep> reportGenerationSteps = new TreeSet<ReportGenerationStep>();
        List<Long> proTimeList = MatchingRunData.getInstance().getProTimeList();
        int noOfSteps = Math.min(proTimeList.size(),REPORT_GEN_STAGES.length);
        for (int j = 0; j < noOfSteps; j++) {
            ReportGenerationStep step = new ReportGenerationStep();
            step.setProcessName(REPORT_GEN_STAGES[j]);
            step.setTimeTaken(proTimeList.get(j).intValue());
            step.setReport(report);
            step.setSequenceNo(j);
            reportGenerationSteps.add(step);
        }
        report.setReportGenerationSteps(reportGenerationSteps);
        reportMetadataService.savePatientMatchingReport(report);
	 }

    public static Set<String> getAllFieldsUsed(Report report){
        Set<String> fieldsUsed = new TreeSet<String>();
        for(PatientMatchingConfiguration configuration: report.getUsedConfigurationList()){
            for(ConfigurationEntry entry : configuration.getConfigurationEntries()){
                if(!entry.isIgnored()){
                    fieldsUsed.add(entry.getFieldName());
                }
            }
        }
        return fieldsUsed;
    }
}