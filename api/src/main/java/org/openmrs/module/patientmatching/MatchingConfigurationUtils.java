/**
 *
 */
package org.openmrs.module.patientmatching;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;

/**
 * Utility class to manage the duplication removal configuration. This utility class contains methods to
 * read, update and delete a duplication removal configuration from the actual file in the file system.
 * Each de-duplicatizon configuration will be converted to a
 * <code>PatientMatchingConfiguration</code> object and the object will be sent to the web page
 * controller. During enhancements to move data stored in a flat file system to a database, we
 * introduced several database related methods to this class. from these, the suffix '_db' was added
 * to the end of methods which specifically work with the new database tables.
 */
public class MatchingConfigurationUtils {

	protected static final Log log = LogFactory.getLog(MatchingConfigurationUtils.class);

	/**
	 * Create a blank <code>PatientMatchingConfig</code> object and sent it to the web controller.
	 * This object will be used to enable user to tailor the configuration to match their need. This
	 * is the method that needs to be called to create a new duplication removal configuration. During
	 * the saving process, this <code>PatientMatchingConfiguration</code> will be saved as a
	 * blocking run in the configuration file.
	 *
	 * @see PatientMatchingConfiguration
	 * @return blank <code>PatientMatchingConfiguration</code>
	 */
	@SuppressWarnings("unchecked")
	public static final PatientMatchingConfiguration createPatientMatchingConfig(List<String> listExcludedProperties) {

		if (log.isDebugEnabled()) {
			log.debug("Creating blank PatientMatchingConfiguration");
		}

		PatientMatchingConfiguration patientMatchingConfig = new PatientMatchingConfiguration();

		Class[] classes = { Patient.class, PersonAddress.class, PersonName.class };
		SortedSet<ConfigurationEntry> configurationEntries = new TreeSet<ConfigurationEntry>();
		for (Class clazz : classes) {
			configurationEntries.addAll(MatchingUtils.generateProperties(listExcludedProperties, clazz));
		}

		PatientService patientService = Context.getPatientService();
		List<PatientIdentifierType> patientIdentifierTypes = patientService.getAllPatientIdentifierTypes();
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setFieldName("(Identifier) " + patientIdentifierType.getName());
			configurationEntry.setFieldViewName("(Identifier) " + patientIdentifierType.getName());
			configurationEntry.setIgnored();
			configurationEntries.add(configurationEntry);
		}

		PersonService personService = Context.getPersonService();
		List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();
		for (PersonAttributeType personAttributeType : personAttributeTypes) {
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setFieldName("(Attribute) " + personAttributeType.getName());
			configurationEntry.setFieldViewName("(Attribute) " + personAttributeType.getName());
			configurationEntry.setIgnored();
			configurationEntries.add(configurationEntry);
		}

		//Collections.sort(configurationEntries);

		patientMatchingConfig.setConfigurationEntries(configurationEntries);
		patientMatchingConfig.setConfigurationName("new strategy");
		patientMatchingConfig.setUsingRandomSample(true);
		patientMatchingConfig.setRandomSampleSize(100000);

		return patientMatchingConfig;
	}

	/**
	 * Retrieve a particular matching configuration from a configuration file based on the blocking
	 * run name and then return it as <code>PatientMatchingConfiguration</code> object to the web
	 * controller
	 *
	 * @param name the blocking run name
	 * @return <code>PatientMatchingObject</code> representing the selected blocking run
	 */
	public static final PatientMatchingConfiguration loadPatientMatchingConfig(String name,
	                                                                           List<String> listExcludedProperties) {
		String configurationFolder = MatchingConstants.CONFIG_FOLDER_NAME;
		File configurationFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configurationFolder);
		File configurationFile = new File(configurationFileFolder, MatchingConstants.CONFIG_FILE_NAME);

		PatientMatchingConfiguration patientMatchingConfig = createPatientMatchingConfig(listExcludedProperties);
		log.info("Loading PatientMatchingConfig with name: " + name);

		if (configurationFile.exists()) {
			RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator
			        .getXMLDocFromFile(configurationFile));
			List<MatchingConfig> matchingConfigList = recMatchConfig.getMatchingConfigs();
			MatchingConfig matchingConfig = findMatchingConfigByName(name, matchingConfigList);

			for (ConfigurationEntry configEntry : patientMatchingConfig.getConfigurationEntries()) {
				MatchingConfigRow configRow = matchingConfig.getMatchingConfigRowByName(configEntry.getFieldName());
				configEntry.setBlockOrder(configRow.getBlockOrder());

				if (configRow.isIncluded() || configRow.getBlockOrder() > 0) {
					configRow.setSetID(configRow.getSetID());
					configEntry.setSET(configRow.getSetID());
				}
				if (configRow.isIncluded()) {
					configEntry.setIncluded();
				}
				if (configRow.getBlockOrder() > 0) {
					configEntry.setBlocking();

				}
			}

			PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
			PatientMatchingConfiguration newMatchingConfiguration = service.findPatientMatchingConfigurationByName(name);

			//Collections.sort(patientMatchingConfig.getConfigurationEntries());
			//Collections.sort(newMatchingConfiguration.getConfigurationEntries());
			patientMatchingConfig.setConfigurationEntries(newMatchingConfiguration.getConfigurationEntries());

			patientMatchingConfig.setConfigurationName(newMatchingConfiguration.getConfigurationName());
			patientMatchingConfig.setUsingRandomSample(newMatchingConfiguration.isUsingRandomSample());
			patientMatchingConfig.setRandomSampleSize(newMatchingConfiguration.getRandomSampleSize());
		}
		return patientMatchingConfig;
	}

	public static PatientMatchingConfiguration loadPatientMatchingConfig_db(String name, List<String> listExcludedProperties) {

		PatientMatchingConfiguration patientMatchingConfig = createPatientMatchingConfig(listExcludedProperties);
		log.info("Loading PatientMatchingConfig with name: " + name);

		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		PatientMatchingConfiguration matchingConfiguration = service.findPatientMatchingConfigurationByName(name);

		for (ConfigurationEntry configEntry : matchingConfiguration.getConfigurationEntries()) {
			for (ConfigurationEntry configEntry2 : patientMatchingConfig.getConfigurationEntries()) {
				if (configEntry2.getFieldName().equals(configEntry.getFieldName())) {

					if (configEntry.isIncluded() || configEntry.getBlockOrder() > 0) {
						configEntry2.setSET(configEntry.getFlag());
					}

					configEntry2.setBlockOrder(configEntry.getBlockOrder());

					if (configEntry.isIncluded()) {
						configEntry2.setIncluded();
					}
					if (configEntry.getBlockOrder() > 0) {
						configEntry2.setBlocking();
					}
				}
			}
		}

		//Collections.sort(patientMatchingConfig.getConfigurationEntries());

		patientMatchingConfig.setConfigurationName(matchingConfiguration.getConfigurationName());
		patientMatchingConfig.setUsingRandomSample(matchingConfiguration.isUsingRandomSample());
		patientMatchingConfig.setRandomSampleSize(matchingConfiguration.getRandomSampleSize());

		return matchingConfiguration;
	}

	/**
	 * Method to search certain blocking run from a set of blocking runs in a configuration file
	 *
	 * @param name blocking run name
	 * @param matchingConfigs set of blocking run
	 * @return <code>MatchingConfig</code> representing the selected blocking run
	 */
	private static final MatchingConfig findMatchingConfigByName(String name, List<MatchingConfig> matchingConfigs) {
		MatchingConfig config = null;
		for (MatchingConfig matchingConfig : matchingConfigs) {
			if (matchingConfig.getName().equals(name)) {
				config = matchingConfig;
				break;
			}
		}
		return config;
	}

	/**
	 * Method to save blocking run to the database.
	 *
	 * @param patientMatchingConfig
	 */
	public static final void savePatientMatchingConfig(PatientMatchingConfiguration patientMatchingConfig) {

		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;

		File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);

		File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

		RecMatchConfig recMatchConfig = null;
		LinkDataSource linkDataSource = new LinkDataSource("dummy", "dummy", "dummy", 1);

		if (!configFile.exists()) {
			int counter = 0;
			Set<String> s = new HashSet<String>();

			for (ConfigurationEntry configEntry : patientMatchingConfig.getConfigurationEntries()) {

				if (!(configEntry.getSET().equals("0"))) {
					if (!(configEntry.getSET().equals("")))
						s.add(configEntry.getSET());

				}

				DataColumn column = new DataColumn(String.valueOf(counter));

				column.setIncludePosition(counter);

				column.setName(configEntry.getFieldName());

				column.setType(DataColumn.STRING_TYPE);

				linkDataSource.addDataColumn(column);

				counter++;

			}

			Iterator<String> it = s.iterator();

			while (it.hasNext()) {

				if (!s.contains("")) {

					DataColumn column = new DataColumn(String.valueOf(counter));

					column.setIncludePosition(counter);

					column.setName("concat" + it.next());

					column.setType(DataColumn.STRING_TYPE);

					linkDataSource.addDataColumn(column);

					counter++;

				}
			}
			List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();

			recMatchConfig = new RecMatchConfig(linkDataSource, linkDataSource, matchingConfigLists);

		} else {
			int counter = 0;

			SortedSet<String> s = new TreeSet<String>();

			for (ConfigurationEntry configEntry : patientMatchingConfig.getConfigurationEntries()) {

				if (!(configEntry.getSET().equals("0"))) {

					if (!(configEntry.getSET().equals("")))
						s.add(configEntry.getSET());
				}
			}
			Iterator<String> it = s.iterator();

			while (it.hasNext()) {

				if (!s.contains("")) {

					DataColumn column = new DataColumn(String.valueOf(counter));

					column.setIncludePosition(counter);

					column.setName("concat" + it.next());

					column.setType(DataColumn.STRING_TYPE);

					linkDataSource.addDataColumn(column);

					counter++;

				}
			}

			recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
		}

		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		service.savePatientMatchingConfiguration(patientMatchingConfig);

		XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(recMatchConfig), configFile);
	}

	/**
	 * Method to get the list of all available blocking runs in the configuration file. This will be
	 * used to display all blocking run for further modification or deletion from the web front end.
	 *
	 * @return list of all blocking runs found in the configuration file
     * @deprecated Use listAvailableBlockingRuns_db
	 */
    @Deprecated
	public static final List<String> listAvailableBlockingRuns() {
		log.info("Listing all available blocking run");
		List<String> blockingRuns = new ArrayList<String>();

		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
		File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

		if (configFile.exists()) {
			RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
			List<MatchingConfig> matchingConfigLists = recMatchConfig.getMatchingConfigs();
			for (MatchingConfig matchingConfig : matchingConfigLists) {
				blockingRuns.add(matchingConfig.getName());

			}
		}
		return blockingRuns;
	}

	/**
	 * List all available blocking runs
	 */
	public static final List<String> listAvailableBlockingRuns_db() {
		log.info("Listing all available blocking run");
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		List<String> blockingRunNames = new ArrayList<String>();
		List<PatientMatchingConfiguration> list = service.getMatchingConfigs();
		for (PatientMatchingConfiguration pmc : list) {
			blockingRunNames.add(pmc.getConfigurationName());
		}
		return blockingRunNames;
	}

	/**
	 * List all available blocking configurations
	 * @return list of blocking configurations
	 */
	public static List<PatientMatchingConfiguration> listAvailableBlockingRunConfigs() {
		log.info("Listing all available blocking confugurations");
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		List<PatientMatchingConfiguration> configs = service.getMatchingConfigs();
		log.info("Updating estimations as necessary...");
		new Estimator().updateStrategies(configs);
		return configs;
	}

	/**
	 * Method to delete a particular blocking run from the configuration file. If the blocking run
	 * is the last entry in the configuration file, the configuration file will also be deleted.
	 *
	 * @param name blocking run name that will be deleted
	 */
	public static final void deleteBlockingRun(String name) {
		log.info("Deleting blocking run with name: " + name);
		String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
		File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

		if (configFile.exists()) {
			RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
			List<MatchingConfig> matchingConfigLists = recMatchConfig.getMatchingConfigs();
			MatchingConfig matchingConfig = findMatchingConfigByName(name, matchingConfigLists);
			if (matchingConfig != null) {
				matchingConfigLists.remove(matchingConfig);
			}
			log.info("List Size: " + matchingConfigLists.size());
			if (matchingConfigLists.size() > 0) {
				XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(recMatchConfig), configFile);
			} else {
				log.info("Deleting file: " + configFile.getAbsolutePath());
				boolean deleted = configFile.delete();
				if (deleted) {
					log.info("Config file deleted.");
				}
			}
		}
	}

	public static final void deleteBlockingRun_db(String name) {
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		service.deletePatientMatchingConfigurationByName(name);
	}

	/**
	 * generates an updated set of configuration entries from the current data model.
	 *
	 * @return
	 */
	public static SortedSet<ConfigurationEntry> generateUpdatedConfigurationEntries(List<String> listExcludedProperties){
		Class[] classes = { Patient.class, PersonAddress.class, PersonName.class };

		SortedSet<ConfigurationEntry> configurationEntries = new TreeSet<ConfigurationEntry>();
		for (Class clazz : classes) {
			configurationEntries.addAll(MatchingUtils.generateProperties(listExcludedProperties, clazz));
		}

		PatientService patientService = Context.getPatientService();
		List<PatientIdentifierType> patientIdentifierTypes = patientService.getAllPatientIdentifierTypes();
		for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setFieldName("(Identifier) " + patientIdentifierType.getName());
			configurationEntry.setFieldViewName("(Identifier) " + patientIdentifierType.getName());
			configurationEntry.setIgnored();
			configurationEntries.add(configurationEntry);
		}

		PersonService personService = Context.getPersonService();
		List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();
		for (PersonAttributeType personAttributeType : personAttributeTypes) {
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setFieldName("(Attribute) " + personAttributeType.getName());
			configurationEntry.setFieldViewName("(Attribute) " + personAttributeType.getName());
			configurationEntry.setIgnored();
			configurationEntries.add(configurationEntry);
		}

		//Collections.sort(configurationEntries);
		return configurationEntries;
	}

	/**
	 * creates a hashmap of names to ConfigurationEntries for the purposes of quick lookup.
	 *
	 * @param listExcludedProperties
	 * @return
	 */
	private static Map<String, ConfigurationEntry> generateUpdatedConfigurationEntriesMap(List<String> listExcludedProperties) {
		Map<String, ConfigurationEntry> newConfigsMap = new HashMap<String, ConfigurationEntry>();
		for (ConfigurationEntry ce : MatchingConfigurationUtils.generateUpdatedConfigurationEntries(listExcludedProperties))
			newConfigsMap.put(ce.getFieldName(), ce);
		return newConfigsMap;
	}

	/**
	 * refresh a PatientMatchingConfiguration with properties from current data model.
	 *
	 * @param configuration
	 * @param listExcludedProperties
	 * @should leave a configuration alone if no properties have changed
	 * @should add properties if they are not in target configuration
	 * @should leave properties alone if they no longer exist in the data model
	 */
	public static void refreshPatientMatchingConfig(PatientMatchingConfiguration configuration, List<String> listExcludedProperties) {
		if (log.isDebugEnabled()) {
			log.debug("refreshing PatientMatchingConfiguration \"" + configuration.getConfigurationName() + "\"");
		}

		if (configuration == null)
			throw new APIException("cannot refresh a null configuration");

		Map<String, ConfigurationEntry> newConfigsMap = MatchingConfigurationUtils
				.generateUpdatedConfigurationEntriesMap(listExcludedProperties);

		// iterate through existing configs
		for (ConfigurationEntry ce : configuration.getConfigurationEntries()) {
			// remove from new ones if it already exists
			if (newConfigsMap.containsKey(ce.getFieldName())) {
				newConfigsMap.remove(ce.getFieldName());
			} else {
				// for now the CE's that don't exist are ignored
			}
		}

		// add any new configs to the configuration
		for (ConfigurationEntry ce : newConfigsMap.values())
			configuration.getConfigurationEntries().add(ce);
	}

}
