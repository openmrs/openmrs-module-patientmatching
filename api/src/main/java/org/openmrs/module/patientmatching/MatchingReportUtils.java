package org.openmrs.module.patientmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
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
	 * Fields that need to be checked to see the current status of the creating
	 * report process
	 */
	public static String status = NO_PROCESS;
	public static int i;
	/**
	 * List of steps in the reporting process.
	 */
	public static final String[] steps = { MatchingReportUtils.NO_PROCESS,
			"Read configuration file", "Create scratch table",
			"Create random sample analyzer",
			"Create analyzer form pairs",
			"Create pair data source analyzer",
			"Create expectation maximization (EM) analyzer",
			"Analyze pairs", 
			"Score pairs",
			"Write report", MatchingReportUtils.END_PROCESS };
	
	/**
	 * 
	 * Get the list of steps (statuses) that the analysis has to go through
	 * 
	 * @return list of steps
	 */
	public static List<String> listSteps() {
		List<String> stepList = new ArrayList<String>();
		for (String step : MatchingReportUtils.steps) {
			stepList.add(step);
		}
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
		List<MatchingConfig> matchConf = recMatchConfig.getMatchingConfigs();
		List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();

		for (String selectedStrat : selectedStrategies) {
			for (MatchingConfig conf : matchConf) {
				if (OpenmrsUtil.nullSafeEquals(conf.getName(), selectedStrat)) {
					matchingConfigLists.add(conf);
				}
			}
		}
		
		Set<String> globalIncludeColumns = new TreeSet<String>();
		DedupMatchResultList handler = new DedupMatchResultList();

		HibernateSessionFactoryBean bean = new HibernateSessionFactoryBean();
		Configuration cfg = bean.newConfiguration();
		Properties c = cfg.getProperties();

		String url = c.getProperty("hibernate.connection.url");
		String user = c.getProperty("hibernate.connection.username");
		String passwd = c.getProperty("hibernate.connection.password");
		String driver = c.getProperty("hibernate.connection.driver_class");
		log.info("URL: " + url);

		Connection databaseConnection = null;
		try {
			Class.forName(driver);
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
					url, user, passwd);
			databaseConnection = connectionFactory.createConnection();
		} catch (ClassNotFoundException e) {
			log.warn("patientmatching: error loading database driver to use when matching");
		} catch (SQLException e) {
			log.warn("patientmatching: error connectiong to database to do matching - \n" + e.getMessage());
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
		objects.put("globalIncludeColumns", globalIncludeColumns);
		return objects;
	}
	//New Method1 End 2
	
	//New Method2 3
	public static Map<String, Object> InitScratchTable(Map<String,Object> objects){
		Connection databaseConnection=(Connection) objects.get("databaseConnection");
		RecMatchConfig recMatchConfig=(RecMatchConfig) objects.get("recMatchConfig");
		String driver=(String) objects.get("driver");
		String url = (String) objects.get("url");
		String user = (String) objects.get("user");
		String passwd = (String) objects.get("passwd");
		
		log.info("Initiating scratch table");
		DataBaseRecordStore recordStore = new DataBaseRecordStore(
				databaseConnection, recMatchConfig.getLinkDataSource1(),
				driver, url, user, passwd);
		recordStore.clearRecords();
		OpenMRSReader reader = new OpenMRSReader();
		while (reader.hasNextRecord()) {
			Record patientRecord = reader.nextRecord();
			recordStore.storeRecord(patientRecord);
		}
		recordStore.close();
		reader.close();
		
		ReaderProvider rp = ReaderProvider.getInstance();

		objects.put("recordStore", recordStore);
		objects.put("rp", rp);
		objects.put("recMatchConfig", recMatchConfig);
		MatchingConfig matchingConfig = ((List<MatchingConfig>)objects.get("matchingConfigLists")).get(0);
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
		databaseReaderRandom.close();      //TODO
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
		Set<String> globalIncludeColumns = (Set<String>) objects.get("globalIncludeColumns");
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
		int counter = 0;

		while ((pair = formPairsScoring.getNextRecordPair()) != null) {
			mr = sp.scorePair(pair[0], pair[1]);
			counter++;
			handler.acceptMatchResult(mr);
		}

		databaseReaderScore.close();

		List<String> blockingColumns = Arrays.asList(matchingConfig.getBlockingColumns());
		globalIncludeColumns.addAll(blockingColumns);
		List<String> includeColumns = Arrays.asList(matchingConfig.getIncludedColumnsNames());
		globalIncludeColumns.addAll(includeColumns);

		objects.put("handler", handler);
		objects.put("globalIncludeColumns", globalIncludeColumns);
		return objects;
	}
	//New Method8 End 9

	//New Method9 10
	public static Map<String, Object> CreatingReport(Map<String,Object> objects) throws IOException{
		log.info("Creating report");
		
		int groupId = 0;
		String separator = "|";

		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		String dateString = format.format(new Date());
		
		String configString = new String();
		try{
			List<MatchingConfig> mcs = (List<MatchingConfig>)objects.get("matchingConfigLists");
			Iterator<MatchingConfig> it = mcs.iterator();
			while(it.hasNext()){
				MatchingConfig mc = it.next();
				configString += mc.getName() + "-";
			}
		}
		catch(Exception e){
			log.warn("error while rendering config string", e);
		}
		
		File configFileFolder = (File)objects.get("configFileFolder");
		File reportFile = new File(configFileFolder, "dedup-report-" + configString + dateString);
		BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));
		DedupMatchResultList handler = (DedupMatchResultList)objects.get("handler");
		Set<String> globalIncludeColumns = (Set<String>)objects.get("globalIncludeColumns");

		handler.flattenPairIdList();
		List<Set<Long>> groupedId = handler.getFlattenedPairIds();

		StringBuffer sb = new StringBuffer();
		sb.append("Group Id|");
		sb.append("Unique Id|");
		for (String include : globalIncludeColumns) {
			if (include.indexOf("(") < 0) {
				sb.append("patientmatching.");
			}
			sb.append(include).append(separator);
		}
		String headerLine = sb.toString();
		headerLine = headerLine.substring(0, headerLine.length() - 1);
				
		writer.write(headerLine);
		writer.write(System.getProperty("line.separator"));

		for (Set<Long> set : groupedId) {
			for (Long integer : set) {
				try {
					Record internalRecord = RecordSerializer.deserialize(String
							.valueOf(integer));
					MatchingReportUtils.generateReport(internalRecord,
							globalIncludeColumns, groupId, separator, writer);
				} catch (IOException e) {
					log.info("Exception caught during the deserializing and writing report for id " + integer);
					log.info("Skipping to the next record...");
				}
			}
			groupId++;
		}
		writer.close();
		persistReportInfo("dedup-report-" + configString + dateString, dateString, groupedId);
		return objects;
	}	
	//New Method9 End 10

	/**
	 * Method to write a single line report entry in the report file. A single
	 * line in the report entry correspond to a preselected properties of a
	 * <code>Record</code> object
	 * 
	 * @param r
	 *            <code>Record</code> object that will be written to the report
	 *            file
	 * @param globalIncludeColumns
	 *            selected properties of the <code>Record</code> object
	 * @param groupId
	 *            grouping id for the current <code>Record</code>
	 * @param separator
	 *            separator character to separate each property
	 * @param writer
	 *            pointer to the report file
	 * @throws IOException
	 */
	private static void generateReport(Record r,
			Set<String> globalIncludeColumns, int groupId, String separator,
			BufferedWriter writer) throws IOException {

		StringBuffer buffer = new StringBuffer();
		buffer.append(groupId).append(separator);

		buffer.append(r.getUID()).append(separator);

		Hashtable<String, String> h = r.getDemographics();
		for (String demographic : globalIncludeColumns) {
			buffer.append(h.get(demographic)).append(separator);
		}
		String report = buffer.toString();
		report = report.substring(0, report.length() - 1);
		writer.write(report);
		writer.write(System.getProperty("line.separator"));
	}

	/**
	 * Method to get the list of the available report for display. The method
	 * will return all report found in the designated folder in the server.
	 * 
	 * @return all available report in the server
	 */
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
	/**
	 * Method to persist report information
	 */
	public static void persistReportInfo(String rptName, String dateCreated, List<Set<Long>> matchingPairs) {
//		List<Long> proTimeList = MatchingRunData.getInstance().getProTimeList();

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
        Set<MatchingSet> matchingSets = new TreeSet<MatchingSet>();
        for (int j = 0; j < matchingPairs.size(); j++) {
            Set<Long> matchSet = matchingPairs.get(j);
            for(Long patientId: matchSet){
                MatchingSet matchingSetEntry = new MatchingSet();
                matchingSetEntry.setGroupId(j);
                matchingSetEntry.setState("PENDING");   //TODO move to a constant
                matchingSetEntry.setPatient(patientService.getPatient(patientId.intValue()));
                matchingSetEntry.setReport(report);
                matchingSets.add(matchingSetEntry);
            }
        }
        report.setMatchingSet(matchingSets);
        reportMetadataService.savePatientMatchingReport(report);

//		String proInfo = "0ms";
//		String uid = Context.getAuthenticatedUser().getUsername();
//		for (int j = 0; j < 8; j++) {
//			proInfo = proInfo + "," + proTimeList.get(j) + "ms";
//		}
//		proInfo = proInfo + ",96ms,0ms";
	 }

}