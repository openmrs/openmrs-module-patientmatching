/**
 * 
 */
package org.openmrs.module.patientmatching;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
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
 * Utility class to manage the de-duplication configuration. This utility class
 * contains methods to read, update and delete a de-duplication configuration
 * from the actual file in the file system.
 * 
 * Each de-duplication configuration will be converted to a <code>PatientMatchingConfiguration</code>
 * object and the object will be sent to the web page controller.
 * 
 */
public class MatchingConfigurationUtils {
    protected static final Log log = LogFactory.getLog(MatchingConfigurationUtils.class);
    
    /**
     * Create a blank <code>PatientMatchingConfig</code> object and sent it to
     * the web controller. This object will be used to enable user to tailor the
     * configuration to match their need.
     * 
     * This is the method that needs to be called to create a new de-duplication
     * configuration. During the saving process, this <code>PatientMatchingConfiguration</code>
     * will be saved as a blocking run in the configuration file.
     * 
     * @see PatientMatchingConfiguration
     * @return blank <code>PatientMatchingConfiguration</code>
     */
    @SuppressWarnings("unchecked")
    public static final PatientMatchingConfiguration createPatientMatchingConfig(List<String> listExcludedProperties) {
        
        if(log.isDebugEnabled()) {
            log.debug("Creating blank PatientMatchingConfiguration");
        }
        
        PatientMatchingConfiguration patientMatchingConfig = new PatientMatchingConfiguration();
        
        Class[] classes = {Patient.class, PersonAddress.class, PersonName.class};
        List<ConfigurationEntry> configurationEntries = new ArrayList<ConfigurationEntry>();
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
        
        Collections.sort(configurationEntries);
        
        patientMatchingConfig.setConfigurationEntries(configurationEntries);
        patientMatchingConfig.setConfigurationName("new configuration");
        patientMatchingConfig.setUsingRandomSample(true);
        patientMatchingConfig.setRandomSampleSize(100000);
        
        return patientMatchingConfig;
    }
    
    /**
     * Retrieve a particular matching configuration from a configuration file
     * based on the blocking run name and then return it as <code>PatientMatchingConfiguration</code>
     * object to the web controller
     * 
     * @param name the blocking run name
     * @return <code>PatientMatchingObject</code> representing the selected blocking run
     */
    public static final PatientMatchingConfiguration loadPatientMatchingConfig(String name, List<String> listExcludedProperties) {
        String configurationFolder = MatchingConstants.CONFIG_FOLDER_NAME;
        File configurationFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configurationFolder);
        File configurationFile = new File(configurationFileFolder, MatchingConstants.CONFIG_FILE_NAME);
        
        PatientMatchingConfiguration patientMatchingConfig = createPatientMatchingConfig(listExcludedProperties);
        log.info("Loading PatientMatchingConfig with name: " + name);
        
        if (configurationFile.exists()) {
            RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configurationFile));
            List<MatchingConfig> matchingConfigList = recMatchConfig.getMatchingConfigs();
            MatchingConfig matchingConfig = findMatchingConfigByName(name, matchingConfigList);
            
            for (ConfigurationEntry configEntry : patientMatchingConfig.getConfigurationEntries()) {
                MatchingConfigRow configRow = matchingConfig.getMatchingConfigRowByName(configEntry.getFieldName());
                if (configRow.isIncluded()) {
                	configEntry.setIncluded();
                }
                if (configRow.getBlockOrder() > 0) {
                	configEntry.setBlocking();
                }
            }
            Collections.sort(patientMatchingConfig.getConfigurationEntries());
            
            patientMatchingConfig.setConfigurationName(matchingConfig.getName());
            patientMatchingConfig.setUsingRandomSample(matchingConfig.isUsingRandomSampling());
            patientMatchingConfig.setRandomSampleSize(matchingConfig.getRandomSampleSize());
        }
        return patientMatchingConfig;
    }
    
    /**
     * Method to search certain blocking run from a set of blocking runs in a
     * configuration file
     * 
     * @param name blocking run name
     * @param matchingConfigs set of blocking run
     * @return <code>MatchingConfig</code> represeniting the selected blocking run
     */
    private static final MatchingConfig findMatchingConfigByName(String name, List<MatchingConfig> matchingConfigs) {
        MatchingConfig config = null;
        for (MatchingConfig matchingConfig : matchingConfigs) {
            if(matchingConfig.getName().equals(name)) {
                config = matchingConfig;
                break;
            }
        }
        return config;
    }

    /**
     * Method to save blocking run to the configuration file. This method will
     * convert the <code>PatientMatchingConfiguration</code> object to blocking
     * run.
     * 
     * If no configuration file in the file system, this method will create a
     * new configuration file, create the data source element and the append
     * the blocking run to it. If the file exist, the method will update the
     * blocking run element in accordance to the <code>PatientMatchingConfiguration</code>
     * object.
     * 
     * @param patientMatchingConfig
     */
    public static final void savePatientMatchingConfig(PatientMatchingConfiguration patientMatchingConfig) {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);
        
        //TODO: need to do checking on the column count to know whether the structure is changed and we need
        //      to update the data source element as well
        RecMatchConfig recMatchConfig = null;
        if (!configFile.exists()){
            log.info("Creating new config file");
            LinkDataSource linkDataSource = new LinkDataSource("dummy", "dummy", "dummy", 1);
            int counter = 0;
            for(ConfigurationEntry configEntry: patientMatchingConfig.getConfigurationEntries()) {
                DataColumn column = new DataColumn(String.valueOf(counter));
                column.setIncludePosition(counter);
                column.setName(configEntry.getFieldName());
                column.setType(DataColumn.STRING_TYPE);
                linkDataSource.addDataColumn(column);
                counter ++;
            }
            List<MatchingConfig> matchingConfigLists = new ArrayList<MatchingConfig>();
            recMatchConfig = new RecMatchConfig(linkDataSource, linkDataSource, matchingConfigLists);
        } else {
            log.info("Using old config file");
            recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
        }
        
        List<MatchingConfig> matchingConfigLists = recMatchConfig.getMatchingConfigs();
        MatchingConfig matchingConfig = findMatchingConfigByName(patientMatchingConfig.getConfigurationName(), matchingConfigLists);
        if (matchingConfig != null) {
            log.info("Updating new blocking run with name: " + patientMatchingConfig.getConfigurationName());
            // update blocking runs
            matchingConfig.setUsingRandomSampling(patientMatchingConfig.isUsingRandomSample());
            matchingConfig.setRandomSampleSize(patientMatchingConfig.getRandomSampleSize());
            int counterBlockOrder = 1;
            for(ConfigurationEntry configEntry: patientMatchingConfig.getConfigurationEntries()) {
                MatchingConfigRow configRow = matchingConfig.getMatchingConfigRowByName(configEntry.getFieldName());
                configRow.setInclude(configEntry.isIncluded() || configEntry.isBlocking());
                if (configEntry.isBlocking()) {
                    configRow.setBlockOrder(counterBlockOrder);
                    counterBlockOrder ++;
                    //log.info("saving -- " + patientMatchingConfig.getConfigurationName() + ": blocking field " + configEntry.getFieldName());
                } else {
                    configRow.setBlockOrder(MatchingConfigRow.DEFAULT_BLOCK_ORDER);
                    //log.info(patientMatchingConfig.getConfigurationName() + ": ignore field " + configEntry.getFieldName());
                }
            }
        } else {
            log.info("Creating new blocking run with name: " + patientMatchingConfig.getConfigurationName());
            // create blocking runs
            List<MatchingConfigRow> matchingConfigRows = new ArrayList<MatchingConfigRow>();
            int counterBlockOrder = 1;
            for (ConfigurationEntry configEntry : patientMatchingConfig.getConfigurationEntries()) {
                MatchingConfigRow configRow = new MatchingConfigRow(configEntry.getFieldName());
                configRow.setInclude(configEntry.isIncluded() || configEntry.isBlocking());
                if (configEntry.isBlocking()) {
                    configRow.setBlockOrder(counterBlockOrder);
                    counterBlockOrder ++;
                } else {
                    configRow.setBlockOrder(MatchingConfigRow.DEFAULT_BLOCK_ORDER);
                }
                matchingConfigRows.add(configRow);
            }
            MatchingConfigRow[] matchingConfigRowArray = matchingConfigRows.toArray(new MatchingConfigRow[matchingConfigRows.size()]);
            matchingConfig = new MatchingConfig(patientMatchingConfig.getConfigurationName(), matchingConfigRowArray);
            matchingConfig.setName(patientMatchingConfig.getConfigurationName());
            matchingConfig.setUsingRandomSampling(patientMatchingConfig.isUsingRandomSample());
            matchingConfig.setRandomSampleSize(patientMatchingConfig.getRandomSampleSize());
            matchingConfigLists.add(matchingConfig);
        }
        XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(recMatchConfig), configFile);
    }
    
    /**
     * Method to get the list of all available blocking runs in the
     * configuration file. This will be used to display all blocking run
     * for further modification or deletion from the web front end.
     * 
     * @return list of all blocking runs found in the configuration file
     */
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
     * Method to delete a particular blocking run from the configuration file.
     * If the blocking run is the last entry in the configuration file, the
     * configuration file will also be deleted.
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
            if(matchingConfig != null) {
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
}
