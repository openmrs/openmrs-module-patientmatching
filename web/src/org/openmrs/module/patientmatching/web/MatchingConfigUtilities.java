package org.openmrs.module.patientmatching.web;

import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.ConfigEntry;
import org.openmrs.module.patientmatching.HibernateConnection;
import org.openmrs.module.patientmatching.PatientMatchingConfig;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.EMAnalyzer;
import org.regenstrief.linkage.analysis.PairDataSourceAnalysis;
import org.regenstrief.linkage.analysis.RandomSampleAnalyzer;
import org.regenstrief.linkage.analysis.SetSimilarityAnalysis;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedOpenMRSReader;
import org.regenstrief.linkage.matchresult.DedupMatchResultList;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.regenstrief.linkage.util.XMLTranslator;

//TODO: should we incorporate mode? this will depends on our assumption.
// if editing the blocking runs will create a new blocking runs, then no need
// for mode. but if editing the blocking runs only update the blocking runs,
// then we need mode
public class MatchingConfigUtilities {
    protected static final Log log = LogFactory.getLog(MatchingConfigUtilities.class);
    /**
     * Create a blank <code>PatientMatchingConfig</code> object.
     * 
     * @return blank <code>PatientMatchingConfig</code>
     */
    @SuppressWarnings("unchecked")
    public static final PatientMatchingConfig createPatientMatchingConfig(List<String> listExcludedProperties) {
        log.info("Creating blank PatientMatchingConfig");
        
        PatientMatchingConfig patientMatchingConfig = new PatientMatchingConfig();
        
        Class[] classes = {Patient.class, PersonAddress.class, PersonName.class};
        List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
        for (Class clazz : classes) {
            configEntries.addAll(generateProperties(listExcludedProperties, clazz));
        }
        
        PatientService patientService = Context.getPatientService();
        List<PatientIdentifierType> patientIdentifierTypes = patientService.getAllPatientIdentifierTypes();
        for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
            ConfigEntry configEntry = new ConfigEntry();
            configEntry.setFieldName("(Identifier) " + patientIdentifierType.getName());
            configEntry.setFieldViewName("(Identifier) " + patientIdentifierType.getName());
            configEntry.setBlocking(new Boolean(false));
            configEntry.setIncluded(new Boolean(false));
            configEntries.add(configEntry);
        }
        
        PersonService personService = Context.getPersonService();
        List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();
        for (PersonAttributeType personAttributeType : personAttributeTypes) {
            ConfigEntry configEntry = new ConfigEntry();
            configEntry.setFieldName("(Attribute) " + personAttributeType.getName());
            configEntry.setFieldViewName("(Attribute) " + personAttributeType.getName());
            configEntry.setBlocking(new Boolean(false));
            configEntry.setIncluded(new Boolean(false));
            configEntries.add(configEntry);
        }
        
        Collections.sort(configEntries);
        for (ConfigEntry configEntry : configEntries) {
            log.info("Entry: " + configEntry.getFieldName());
        }
        
        patientMatchingConfig.setConfigEntries(configEntries);
        patientMatchingConfig.setConfigName("new configuration");
        patientMatchingConfig.setUseRandomSampling(true);
        patientMatchingConfig.setRandomSampleSize(100000);
        
        return patientMatchingConfig;
    }
    
    /**
     * Retrieve <code>PatientMatchingConfig</code> object from a configuration file
     * based on the blocking run name
     * 
     * @param name the blocking run name
     * @return <code>PatientMatchingObject</code> representing the selected blocking run
     */
    public static final PatientMatchingConfig createPatientMatchingConfig(String name, List<String> listExcludedProperties) {
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);
        
        PatientMatchingConfig patientMatchingConfig = createPatientMatchingConfig(listExcludedProperties);
        log.info("Creating blank PatientMatchingConfig with name: " + name);
        
        if (configFile.exists()) {
            RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
            List<MatchingConfig> matchingConfigLists = recMatchConfig.getMatchingConfigs();
            MatchingConfig matchingConfig = findMatchingConfigByName(name, matchingConfigLists);
            
            for (ConfigEntry configEntry : patientMatchingConfig.getConfigEntries()) {
                MatchingConfigRow configRow = matchingConfig.getMatchingConfigRowByName(configEntry.getFieldName());
                configEntry.setBlocking(configRow.getBlockOrder() > 0);
                configEntry.setIncluded(configRow.isIncluded());
            }
            Collections.sort(patientMatchingConfig.getConfigEntries());
            
            patientMatchingConfig.setConfigName(matchingConfig.getName());
            patientMatchingConfig.setUseRandomSampling(matchingConfig.isUsingRandomSampling());
            patientMatchingConfig.setRandomSampleSize(matchingConfig.getRandomSampleSize());
        }
        return patientMatchingConfig;
    }
    
    /**
     * Find a certain blocking run from a set of blocking runs of a configuration file
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

    @SuppressWarnings("unchecked")
    private static List<ConfigEntry> generateProperties(List<String> listExcludedProperties, Class clazz) {
        List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
        List<String> listNameProperty = introspectBean(listExcludedProperties, clazz);
        for (String fieldName : listNameProperty) {
            ConfigEntry configEntry = new ConfigEntry();
            configEntry.setFieldName(fieldName);
            configEntry.setFieldViewName("patientmatching." + fieldName);
            configEntry.setBlocking(new Boolean(false));
            configEntry.setIncluded(new Boolean(false));
            configEntries.add(configEntry);
        }
        return configEntries;
    }

    /**
     * Introspect a class and retrieve all readable and writable properties of that class
     * without including properties defined in the <code>listExcludedProperties</code> parameter.
     * 
     * @param listExcludedProperties list of properties that shouldn't be returned
     * @param clazz class that will be reflected for the properties
     * @return properties list of the class excluding all properties defined in the <code>listExcludedProperties</code>
     */
    @SuppressWarnings("unchecked")
    public static List<String> introspectBean(List<String> listExcludedProperties, Class clazz) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);

        List<String> list = new ArrayList<String>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getReadMethod() != null &&
                    propertyDescriptor.getWriteMethod() != null) {
                boolean exclude = false;
                String propertyName = propertyDescriptor.getName();
                for (String excludedProperty : listExcludedProperties) {
                    if (propertyName == null) {
                        break;
                    }
                    if (excludedProperty.trim().length() == 0) {
                        break;
                    }
                    if (propertyName.toUpperCase().contains(excludedProperty.toUpperCase())) {
                        exclude = true;
                        break;
                    }
                }
                if(!exclude) {
                    if (MatchingIntrospector.isSimpleProperty(propertyDescriptor.getPropertyType())) {
                        list.add(clazz.getName() + "." + propertyDescriptor.getName());
                    }
                }
            }
        }
        
        return list;
    }
    
    public static final void savePatientMatchingConfig(PatientMatchingConfig patientMatchingConfig) {
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
            for(ConfigEntry configEntry: patientMatchingConfig.getConfigEntries()) {
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
        MatchingConfig matchingConfig = findMatchingConfigByName(patientMatchingConfig.getConfigName(), matchingConfigLists);
        if (matchingConfig != null) {
            log.info("Updating new blocking run with name: " + patientMatchingConfig.getConfigName());
            // update blocking runs
            matchingConfig.setUsingRandomSampling(patientMatchingConfig.isUseRandomSampling());
            matchingConfig.setRandomSampleSize(patientMatchingConfig.getRandomSampleSize());
            int counterBlockOrder = 1;
            for(ConfigEntry configEntry: patientMatchingConfig.getConfigEntries()) {
                MatchingConfigRow configRow = matchingConfig.getMatchingConfigRowByName(configEntry.getFieldName());
                configRow.setInclude(configEntry.isIncluded());
                if (configEntry.isBlocking()) {
                    configRow.setBlockOrder(counterBlockOrder);
                    counterBlockOrder ++;
                } else {
                    configRow.setBlockOrder(MatchingConfigRow.DEFAULT_BLOCK_ORDER);
                }
            }
        } else {
            log.info("Creating new blocking run with name: " + patientMatchingConfig.getConfigName());
            // create blocking runs
            List<MatchingConfigRow> matchingConfigRows = new ArrayList<MatchingConfigRow>();
            int counterBlockOrder = 1;
            for (ConfigEntry configEntry : patientMatchingConfig.getConfigEntries()) {
                MatchingConfigRow configRow = new MatchingConfigRow(configEntry.getFieldName());
                configRow.setInclude(configEntry.isIncluded());
                if (configEntry.isBlocking()) {
                    configRow.setBlockOrder(counterBlockOrder);
                    counterBlockOrder ++;
                } else {
                    configRow.setBlockOrder(MatchingConfigRow.DEFAULT_BLOCK_ORDER);
                }
                matchingConfigRows.add(configRow);
            }
            MatchingConfigRow[] matchingConfigRowArray = matchingConfigRows.toArray(new MatchingConfigRow[matchingConfigRows.size()]);
            matchingConfig = new MatchingConfig(patientMatchingConfig.getConfigName(), matchingConfigRowArray);
            matchingConfig.setName(patientMatchingConfig.getConfigName());
            matchingConfig.setUsingRandomSampling(patientMatchingConfig.isUseRandomSampling());
            matchingConfig.setRandomSampleSize(patientMatchingConfig.getRandomSampleSize());
            matchingConfigLists.add(matchingConfig);
        }
        XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(recMatchConfig), configFile);
    }
    
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
                configFile.delete();
            }
        }
    }

    public static List<Map<String, String>> doAnalysis() throws IOException {
        
        HibernateConnection connection = new HibernateConnection();
        log.info("Hibernate Connection null? " + (connection == null));
        
        SessionFactory sessionFactory = connection.getSessionFactory();
        log.info("Session factory null? " + (sessionFactory == null));
        
        String configLocation = MatchingConstants.CONFIG_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File configFile = new File(configFileFolder, MatchingConstants.CONFIG_FILE_NAME);

        RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(configFile));
        List<MatchingConfig> matchingConfigLists = recMatchConfig.getMatchingConfigs();
        for (MatchingConfig matchingConfig : matchingConfigLists) {
            log.info("Creating OpenMRS Data Reader");
            OrderedOpenMRSReader openMRSReader = new OrderedOpenMRSReader(matchingConfig, sessionFactory);
            OrderedOpenMRSReader openMRSReader2 = new OrderedOpenMRSReader(matchingConfig, sessionFactory);
            
            if(openMRSReader != null) {
                FormPairs formPairs = new DedupOrderedDataSourceFormPairs(openMRSReader,
                        matchingConfig,
                        recMatchConfig.getLinkDataSource1().getTypeTable());
                FormPairs formPairs2 = new DedupOrderedDataSourceFormPairs(openMRSReader2,
                        matchingConfig,
                        recMatchConfig.getLinkDataSource1().getTypeTable());
                PairDataSourceAnalysis pdsa = new PairDataSourceAnalysis(formPairs);
                
                log.info("Creating Analyzer");
                RandomSampleAnalyzer rsa = new RandomSampleAnalyzer(matchingConfig, formPairs2);
                pdsa.addAnalyzer(rsa);
                EMAnalyzer ema = new EMAnalyzer(matchingConfig);
                pdsa.addAnalyzer(ema);
                pdsa.analyzeData();
            }
            
            openMRSReader.close();
        }
        
        List<String> globalIncludeColumns = new ArrayList<String>();
        DedupMatchResultList list = new DedupMatchResultList();
        for (MatchingConfig matchingConfig : matchingConfigLists) {
            log.info("Creating OpenMRS Data Reader");
            OrderedOpenMRSReader openMRSReader = new OrderedOpenMRSReader(matchingConfig, sessionFactory);
            
            if(openMRSReader != null) {
                FormPairs formPairs = new DedupOrderedDataSourceFormPairs(openMRSReader,
                        matchingConfig,
                        recMatchConfig.getLinkDataSource1().getTypeTable());
                ScorePair sp = new ScorePair(matchingConfig);
                Record[] pair;
                while((pair = formPairs.getNextRecordPair()) != null){
                    MatchResult mr = sp.scorePair(pair[0], pair[1]);
                    list.acceptMatchResult(mr);
                }
                list.sort();
            }
            List<String> blockingColumns = Arrays.asList(matchingConfig.getBlockingColumns());
            globalIncludeColumns.addAll(blockingColumns);
            List<String> includeColumns = Arrays.asList(matchingConfig.getIncludedColumnsNames());
            globalIncludeColumns.addAll(includeColumns);
        }

        SetSimilarityAnalysis ssa = new SetSimilarityAnalysis();
        List<List<MatchResult>> groups = ssa.getSimilarSets(list.getResults());
        List<List<Record>> records = ssa.getSimilarRecords(groups);

        int groupId = 0;
        String separator = "|";

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = format.format(new Date());

        File reportFile = new File(configFileFolder, "dedup-report-" + dateString);
        BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));

        List<Map<String, String>> buffer = new ArrayList<Map<String, String>>();
        
        Collections.sort(globalIncludeColumns);

        for (List<Record> recordList: records) {
            for (Record r : recordList) {
                
                Map<String, String> displayMap = new TreeMap<String, String>();
                
                String report = groupId + separator;
                displayMap.put("Group Id", String.valueOf(groupId));
                
                report = report + r.getUID() + separator;
                displayMap.put("UID", String.valueOf(r.getUID()));
                
                Hashtable<String, String> h = r.getDemographics();
                for(String demographic: globalIncludeColumns) {
                    report = report + h.get(demographic) + separator;
                    if (demographic.indexOf(".") == -1) {
                        displayMap.put(demographic, h.get(demographic));
                    } else {
                        displayMap.put("patientmatching." + demographic, h.get(demographic));
                    }
                }
                buffer.add(displayMap);
                report = report.substring(0, report.length() - 1);
                writer.write(report);
                writer.write(System.getProperty("line.separator"));
            }
            groupId ++;
        }
        writer.close();
        
        return buffer;
    }
}
