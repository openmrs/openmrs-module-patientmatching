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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.hibernate.cfg.Configuration;
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

	/**
	 * Initial status of creating report process. The following are three main
	 * status of the process.
	 */
	public static final String NO_PROCESS = "No running process";
	public static final String END_PROCESS = "Report finished";
	public static final String PREM_PROCESS = "Prematurely terminated process";

	/**
	 * Field that need to be checked to see the current status of the creating
	 * report process
	 */
	public static String status = NO_PROCESS;

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

	private static int currentStep = 0;
	
	/**
	 * 
	 * Resets to first step.
	 *
	 */
	public static void resetStep() {
		MatchingReportUtils.currentStep = 0;
		MatchingReportUtils.setStatus(MatchingReportUtils.NO_PROCESS);
	}
	
	public static int getStep() {
		return MatchingReportUtils.currentStep;
	}
	
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

	/**
	 * Main method to generate new report for the entire blocking run available
	 * in the configuration file.
	 * 
	 * @throws IOException
	 */
	public static void doAnalysis() throws IOException {
		MatchingConfigurationUtils.log
				.info("Starting generate report process sequence");

		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil
				.getDirectoryInApplicationDataDirectory(configLocation);
		File configFile = new File(configFileFolder,
				MatchingConstants.CONFIG_FILE_NAME);

		MatchingReportUtils.nextStep();
		MatchingConfigurationUtils.log
				.info("Reading matching config file from "
						+ configFile.getAbsolutePath());
		RecMatchConfig recMatchConfig = XMLTranslator
				.createRecMatchConfig(XMLTranslator
						.getXMLDocFromFile(configFile));
		List<MatchingConfig> matchingConfigLists = recMatchConfig
				.getMatchingConfigs();

		Set<String> globalIncludeColumns = new TreeSet<String>();
		DedupMatchResultList handler = new DedupMatchResultList();

		HibernateSessionFactoryBean bean = new HibernateSessionFactoryBean();
		Configuration cfg = bean.newConfiguration();
		Properties c = cfg.getProperties();

		String url = c.getProperty("hibernate.connection.url");
		String user = c.getProperty("hibernate.connection.username");
		String passwd = c.getProperty("hibernate.connection.password");
		String driver = c.getProperty("hibernate.connection.driver_class");
		MatchingConfigurationUtils.log.info("URL: " + url);

		Connection databaseConnection = null;
		try {
			Class.forName(driver);
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
					url, user, passwd);
			databaseConnection = connectionFactory.createConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		MatchingReportUtils.nextStep();
		MatchingConfigurationUtils.log.info("Initiating scratch table");
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

		ReaderProvider rp = new ReaderProvider();
		for (MatchingConfig matchingConfig : matchingConfigLists) {
			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log
					.info("Creating random sample analyzer");
			OrderedDataSourceReader databaseReaderRandom = rp.getReader(
					recordStore.getRecordStoreLinkDataSource(), matchingConfig);
			DedupOrderedDataSourceFormPairs formPairsRandom = new DedupOrderedDataSourceFormPairs(
					databaseReaderRandom, matchingConfig, recMatchConfig
							.getLinkDataSource1().getTypeTable());
			RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(matchingConfig,
					formPairsRandom);
			databaseReaderRandom.close();

			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log
					.info("Creating analyzer form pairs");
			OrderedDataSourceReader databaseReader = rp.getReader(recordStore
					.getRecordStoreLinkDataSource(), matchingConfig);
			DedupOrderedDataSourceFormPairs formPairs = new DedupOrderedDataSourceFormPairs(
					databaseReader, matchingConfig, recMatchConfig
							.getLinkDataSource1().getTypeTable());

			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log
					.info("Creating pair data source analyzer");
			PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(formPairs);

			MatchingConfigurationUtils.log
					.info("Adding random sample analyzer");
			pdsa.addAnalyzer(rsa);

			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log.info("Creating EM analyzer");
			EMAnalyzer ema = new EMAnalyzer(matchingConfig);

			MatchingConfigurationUtils.log.info("Adding EM analyzer");
			pdsa.addAnalyzer(ema);

			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log.info("Analyzing data");
			pdsa.analyzeData();

			databaseReader.close();

			MatchingReportUtils.nextStep();
			MatchingConfigurationUtils.log.info("Scoring data");
			OrderedDataSourceReader databaseReaderScore = rp.getReader(
					recordStore.getRecordStoreLinkDataSource(), matchingConfig);
			DedupOrderedDataSourceFormPairs formPairsScoring = new DedupOrderedDataSourceFormPairs(
					databaseReaderScore, matchingConfig, recMatchConfig
							.getLinkDataSource1().getTypeTable());
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

			List<String> blockingColumns = Arrays.asList(matchingConfig
					.getBlockingColumns());
			globalIncludeColumns.addAll(blockingColumns);
			List<String> includeColumns = Arrays.asList(matchingConfig
					.getIncludedColumnsNames());
			globalIncludeColumns.addAll(includeColumns);
		}

		int groupId = 0;
		String separator = "|";

		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
		String dateString = format.format(new Date());

		File reportFile = new File(configFileFolder, "dedup-report-"
				+ dateString);
		BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));

		MatchingReportUtils.nextStep();
		MatchingConfigurationUtils.log.info("Creating report");

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
					MatchingConfigurationUtils.log
							.info("Exception caught during the deserializing and writing report for id "
									+ integer);
					MatchingConfigurationUtils.log
							.info("Skipping to the next record...");
				}
			}
			groupId++;
		}
		writer.close();
		MatchingReportUtils.nextStep();
	}

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
	public static final List<String> listAvailableReport() {
		MatchingConfigurationUtils.log.info("Listing all available report");
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
	 * 
	 * Automatically set the status to the next step. If at last step, stay
	 * there.
	 * 
	 * @return the new status
	 */
	public static String nextStep() {
		if (currentStep < steps.length - 1) {
			currentStep++;
		}
		MatchingReportUtils.status = steps[currentStep];
		return MatchingReportUtils.status;
	}

	/**
	 * 
	 * Automatically set the status to the previous step. If at first step, stay
	 * there.
	 * 
	 * @return the new status
	 */
	public static String prevStep() {
		if (currentStep > 0) {
			currentStep--;
		}
		MatchingReportUtils.status = steps[currentStep];
		return MatchingReportUtils.status;
	}

	/**
	 * Get the current status of the creating report process
	 * 
	 * @return current status of the process
	 */
	public static String getStatus() {
		return status;
	}

	/**
	 * Change the current status of the creating report process. If everything
	 * works, we will go through the list of steps, but if something fails, then
	 * this method is used to set the status to indicate an error.
	 * 
	 * @param s
	 *            new status of the creating report process
	 */
	public static void setStatus(String s) {
		status = s;
	}

}
