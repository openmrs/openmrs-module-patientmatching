package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class provides a singleton for database access to where the
 * link table.
 * 
 * @author jegg
 *
 */

public class LinkDBConnections {
	private MatchFinder finder;
	private RecordDBManager link_db;
	private ReaderProvider rp;
	
	// caches patientToRecord return objects
	private Map<Integer,Record> cache;
	public final static int RECORD_CACHE_SIZE = 1000;
	
	protected static Log logger = LogFactory.getLog(LinkDBConnections.class);
	
	private final static LinkDBConnections INSTANCE = new LinkDBConnections();
	
	private List<PatientIdentifierType> patientIdentifierTypes;
	private List<PersonAttributeType> personAttributeTypes;
    private List<String> patientPropertyList;
    private List<String> namePropertyList;
    private List<String> addressPropertyList;
	
    private PersonAttributeType matching_attr_type;
    
    private boolean running_dedup;
    
	private LinkDBConnections(){
		
		AdministrationService adminService = Context.getAdministrationService();
		String configFilename = adminService.getGlobalProperty("patientmatching.linkConfigFile",
				PatientMatchingActivator.CONFIG_FILE);
		
		// used to cache patientToRecord objects
		cache = new LinkedHashMap<Integer,Record>(RECORD_CACHE_SIZE + 1){
			 public boolean removeEldestEntry(Map.Entry<Integer,Record> eldest) {
		            return size() > RECORD_CACHE_SIZE;
		     }
		};
		
		running_dedup = false;
		
		if(!parseConfig(new File(configFilename))){
			finder = null;
			link_db = null;
			rp = null;
		}
	}
    
    /**
     * This method must be called before any call to LinkDBConnection's patientToRecord method.
     */
    public void syncRecordDemographics() {
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();
        
        patientIdentifierTypes = patientService.getAllPatientIdentifierTypes();
        personAttributeTypes = personService.getAllPersonAttributeTypes();
        
        // nothing is excluded
        List<String> listExcludedProperties = new ArrayList<String>();
        
        patientPropertyList = MatchingConfigUtilities.introspectBean(listExcludedProperties, Patient.class);
        
        namePropertyList = MatchingConfigUtilities.introspectBean(listExcludedProperties, PersonName.class);
        
        addressPropertyList = MatchingConfigUtilities.introspectBean(listExcludedProperties, PersonAddress.class);
        
        matching_attr_type = personService.getPersonAttributeTypeByName(PatientMatchingActivator.MATCHING_ATTRIBUTE);
    }

    /**
	 * Method returns an object holding a MatchFinder object and
	 * a RecordDBManager object.  The first is used to find matches, 
	 * the second is used to add, update, or delete the records
	 * used for matching.
	 * 
	 * @return	an instance of the class with database connections
	 */
	public static LinkDBConnections getInstance(){
		return INSTANCE;
	}
	
	/**
	 * 
	 * @return	the object used to find patients in the database
	 */
	public MatchFinder getFinder(){
		return finder;
	}
	
	/**
	 * Method sets the flag indication that a deduplication process is running to false
	 */
	public synchronized void releaseLock(){
		running_dedup = false;
	}
	
	/**
	 * Method tries to set the flag indicating a deduplication process is running
	 * 
	 * @return		true if the lock was set, false if a deduplication is already running and lock is unavailable
	 */
	public synchronized boolean getLock(){
		if(!running_dedup){
			running_dedup = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @return	the object used to add, update, or delete records used in matching
	 */
	public RecordDBManager getRecDBManager(){
		return link_db;
	}
	
	/**
	 * 
	 * @return	the object used to get DataSourceReader objects for a LinkDataSource
	 */
	public ReaderProvider getReaderProvider(){
		return rp;
	}
	
	/**
	 * Method parses a configuration file.  Program assumes that the information under
	 * the first datasource tag in the file is the connection information for the 
	 * record linkage table.  The columns to be used for matching and the string comparators
	 * are also listed in this file.
	 * 
	 * @param config	the configuration file with connection and linkage information
	 * @return	true if there were no errors while parsing the file, false if there was an 
	 * exception
	 */
	private boolean parseConfig(File config){
		try{
			//log.debug("parsing config file " + config);
			//file_log.debug("parsing config file " + config);
			if(!config.exists()){
				//log.warn("cannot find config file in " + config.getPath());
				//file_log.warn("cannot find config file in " + config.getPath());
				return false;
			}
			// Load the XML configuration file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			rp = new ReaderProvider();
			
			// as of version 1.2.0, removing RecordFieldAnalyzer object from Matchfiner since new Blocking exclusion
			// feature is being used
			//finder = new MatchFinder(rmc.getLinkDataSource1(), rp, rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			finder = new MatchFinder(rmc.getLinkDataSource1(), rp, rmc.getMatchingConfigs(), MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			link_db = new RecordDBManager(rmc.getLinkDataSource1());
		}
		catch(ParserConfigurationException pce){
			//log.warn("XML parser error with config file: " + pce.getMessage());
			//file_log.warn("XML parser error with config file: " + pce.getMessage());
			return false;
		}
		catch(SAXException spe){
			//log.warn("XML parser error with config file: " + spe.getMessage());
			//file_log.warn("XML parser error with config file: " + spe.getMessage());
			return false;
		}
		catch(IOException ioe){
			//log.warn("IOException with config file: " + ioe.getMessage());
			//file_log.warn("IOException with config file: " + ioe.getMessage());
			return false;
		}
		//log.debug("file parsed");
		return link_db.connect();
		
	}
	
	public Record patientToRecord(Patient patient){
		// OpenMRS unique patient ID should be present if the patient is within
		// the OpenMRS patient store, but if patient is new and being searched on
		// before inserted, it would be null
		Integer id = patient.getPatientId();
		Record cache_record;
		if((cache_record = cache.get(id)) != null){
			return cache_record;
		}
		
		Record ret = new Record(patient.getPatientId(),"OpenMRS");
		
		if(id != null){
			ret.addDemographic(PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC, Integer.toString(id));
		}
		
		// first, try to get the "Matching Information" attribute type
		
		if(matching_attr_type != null){
			try{
			// expected attribute with information is present, so use all the information from there
			PersonAttribute matching_attr = patient.getAttribute(matching_attr_type.getPersonAttributeTypeId());
			String matching_string = matching_attr.getValue();
			
			String[] demographics = matching_string.split(";");
			for(int i = 0; i < demographics.length; i++){
				String demographic_value = demographics[i];
				String[] pair = demographic_value.split(":", -1);
				ret.addDemographic(pair[0], pair[1]);
			}
			}
			catch(NullPointerException npe){
				return ret;
			}
		} else {
            
            for (String property : patientPropertyList) {
                String value = "";
                try {
                    String classProperty = property.substring(property.lastIndexOf(".") + 1);
                    value = BeanUtils.getProperty(patient, classProperty);
                } catch (Exception e) {
                    logger.debug("Error getting the value for property: " + property, e);
                } finally {
                    ret.addDemographic(property, value);
                }
            }
            
            PersonName personName = patient.getPersonName();
            
            for (String property : namePropertyList) {
                String value = "";
                try {
                    String classProperty = property.substring(property.lastIndexOf(".") + 1);
                    value = BeanUtils.getProperty(personName, classProperty);
                } catch (Exception e) {
                    logger.debug("Error getting the value for property: " + property, e);
                } finally {
                    ret.addDemographic(property, value);
                }
            }

            PersonAddress personAddress = patient.getPersonAddress();
            
            for (String property : addressPropertyList) {
                String value = "";
                try {
                    String classProperty = property.substring(property.lastIndexOf(".") + 1);
                    value = BeanUtils.getProperty(personAddress, classProperty);
                } catch (Exception e) {
                    logger.debug("Error getting the value for property: " + property, e);
                } finally {
                    ret.addDemographic(property, value);
                }
            }
            
            for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
                PatientIdentifier identifier = patient.getPatientIdentifier(patientIdentifierType.getName());
                if (identifier != null) {
                    ret.addDemographic(("(Identifier) " + patientIdentifierType.getName()), identifier.getIdentifier());
                } else {
                    ret.addDemographic(("(Identifier) " + patientIdentifierType.getName()), "");
                }
            }

            for (PersonAttributeType personAttributeType : personAttributeTypes) {
                PersonAttribute attribute = patient.getAttribute(personAttributeType.getName());
                if (attribute != null) {
                    ret.addDemographic(("(Attribute) " + personAttributeType.getName()), attribute.getValue());
                } else {
                    ret.addDemographic(("(Attribute) " + personAttributeType.getName()), "");
                }
            }
		}
		
		cache.put(id, ret);
		return ret;
	}
}
