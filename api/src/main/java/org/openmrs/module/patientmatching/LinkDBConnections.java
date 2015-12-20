package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class provides a singleton for database access to the
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
	//private Map<Integer,Record> cache;
	//public final static int RECORD_CACHE_SIZE = 1000;

	protected static Log log = LogFactory.getLog(LinkDBConnections.class);

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
		/*cache = new LinkedHashMap<Integer,Record>(RECORD_CACHE_SIZE + 1){
			 public boolean removeEldestEntry(Map.Entry<Integer,Record> eldest) {
		            return size() > RECORD_CACHE_SIZE;
		     }
		};*/

		running_dedup = false;

		if(!parseConfig(new File(configFilename))){
			finder = null;
			link_db = null;
			rp = null;
		}

		syncRecordDemographics();
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
        AdministrationService adminService = Context.getAdministrationService();
        String excluded = adminService.getGlobalProperty("patientmatching.excludedProperties", "");
        String[] excludedArray = excluded.split(",");
        List<String> listExcludedProperties = Arrays.asList(excludedArray);

        patientPropertyList = MatchingUtils.introspectBean(listExcludedProperties, Patient.class);

        namePropertyList = MatchingUtils.introspectBean(listExcludedProperties, PersonName.class);

        addressPropertyList = MatchingUtils.introspectBean(listExcludedProperties, PersonAddress.class);

        matching_attr_type = personService.getPersonAttributeTypeByName(PatientMatchingActivator.MATCHING_ATTRIBUTE);
    }

    /**
     * @return the patientIdentifierTypes
     */
    public List<PatientIdentifierType> getPatientIdentifierTypes() {
        return patientIdentifierTypes;
    }

    /**
     * @param patientIdentifierTypes the patientIdentifierTypes to set
     */
    public void setPatientIdentifierTypes(
            List<PatientIdentifierType> patientIdentifierTypes) {
        this.patientIdentifierTypes = patientIdentifierTypes;
    }

    /**
     * @return the personAttributeTypes
     */
    public List<PersonAttributeType> getPersonAttributeTypes() {
        return personAttributeTypes;
    }

    /**
     * @param personAttributeTypes the personAttributeTypes to set
     */
    public void setPersonAttributeTypes(
            List<PersonAttributeType> personAttributeTypes) {
        this.personAttributeTypes = personAttributeTypes;
    }

    /**
     * @return the patientPropertyList
     */
    public List<String> getPatientPropertyList() {
        return patientPropertyList;
    }

    /**
     * @param patientPropertyList the patientPropertyList to set
     */
    public void setPatientPropertyList(List<String> patientPropertyList) {
        this.patientPropertyList = patientPropertyList;
    }

    /**
     * @return the namePropertyList
     */
    public List<String> getNamePropertyList() {
        return namePropertyList;
    }

    /**
     * @param namePropertyList the namePropertyList to set
     */
    public void setNamePropertyList(List<String> namePropertyList) {
        this.namePropertyList = namePropertyList;
    }

    /**
     * @return the addressPropertyList
     */
    public List<String> getAddressPropertyList() {
        return addressPropertyList;
    }

    /**
     * @param addressPropertyList the addressPropertyList to set
     */
    public void setAddressPropertyList(List<String> addressPropertyList) {
        this.addressPropertyList = addressPropertyList;
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
	 * Method that sets the flag indicating whether a duplicate removal process is running to false
	 */
	public synchronized void releaseLock(){
		running_dedup = false;
	}

	/**
	 * Method tries to set the flag indicating whether a duplicate removal process is running
	 *
	 * @return		true if the lock was set, false if the process is already running and flag is unavailable
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
	 * Method parses a configuration file. Method assumes that the information under
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
				log.warn("cannot find config file in " + config.getPath());
				//file_log.warn("cannot find config file in " + config.getPath());
				return false;
			}
			// Load the XML configuration file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			rp = ReaderProvider.getInstance();

			// as of version 1.2.0, removing RecordFieldAnalyzer object from MatchFiner since new Blocking exclusion
			// feature is being used
			//finder = new MatchFinder(rmc.getLinkDataSource1(), rp, rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			finder = new MatchFinder(rmc.getLinkDataSource1(), rp, rmc.getMatchingConfigs(), MatchFinder.Scoring.BLOCKING_EXCLUSIVE);
			link_db = new RecordDBManager(rmc.getLinkDataSource1());
		}
		catch(ParserConfigurationException pce){
			log.warn("XML parser error with config file: " + pce.getMessage());
			//file_log.warn("XML parser error with config file: " + pce.getMessage());
			return false;
		}
		catch(SAXException spe){
			log.warn("XML parser error with config file: " + spe.getMessage());
			//file_log.warn("XML parser error with config file: " + spe.getMessage());
			return false;
		}
		catch(IOException ioe){
			log.warn("IOException with config file: " + ioe.getMessage());
			//file_log.warn("IOException with config file: " + ioe.getMessage());
			return false;
		}
		//log.debug("file parsed");

		return link_db.connect();

	}

	/**
	 * converts a patient to a record
	 *
	 * @param patient the patient to be converted
	 * @return the resulting record
	 * @should encode more than one identifier of the same type properly
	 */
	public Record patientToRecord(Patient patient){
		// OpenMRS unique patient ID should be present if the patient is within
		// the OpenMRS patient store, but if patient is new and is being searched
		// before inserted, this would return null
		Integer id = patient.getPatientId();
		if(id == null){
			id = new Integer(PatientMatchingActivator.DEFAULT_RECORD_MATCHING_ID);
		}
		/*Record cache_record;
		if(id != PatientMatchingActivator.DEFAULT_RECORD_MATCHING_ID && (cache_record = cache.get(id)) != null){
			return cache_record;
		}*/

		Record ret = new Record(id,"OpenMRS");

		ret.addDemographic(PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC, Integer.toString(id));

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
                    log.debug("Error getting the value for property: " + property + " for patient " + id);
                } finally {
                    ret.addDemographic(property, value);
                }
            }

            PersonName personName = patient.getPersonName();
            String concat="";
            int i=0;
            for (String property : namePropertyList) {
                String value = "";
                try {
                    String classProperty = property.substring(property.lastIndexOf(".") + 1);
                    value = BeanUtils.getProperty(personName, classProperty);
                    if(classProperty.equals("familyName")){
                    	i++;
                    	concat+=value;
                    	}
                    else if(classProperty.equals("middleName")){
                    	i++;
                    	concat+=value;
                    	}
                    else if(classProperty.equals("givenName")){
                    	i++;
                    	concat+=value;}
                    if(i==3){
                    	ret.addDemographic("concat1", concat);
                    	i=0;
                    	concat="";
                    }
                } catch (Exception e) {
                    log.debug("Error getting the value for property: " + property + " for patient " + id);
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
                    log.debug("Error getting the value for property: " + property + " for patient " + id);
                } finally {
                    ret.addDemographic(property, value);
                }
            }
			for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
				ret.addDemographic(
						("(Identifier) " + patientIdentifierType.getName()),
						serializePatientIdentifiers(patient.getPatientIdentifiers(patientIdentifierType)));
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
		return ret;
	}

	public Record patientToRecord(Object[] objs) {

        Integer patientId = (Integer) objs[0];
        Record record = new Record(patientId, "OpenMRS");

        int fieldCounter = 1;

        for (String patientProperty: getPatientPropertyList()) {
            String classProperty = patientProperty.substring(patientProperty.lastIndexOf(".") + 1);
            if (!classProperty.equals("patientId")) {
                record.addDemographic(patientProperty, String.valueOf(objs[fieldCounter]));
                fieldCounter ++;
            }
        }

        for (String nameProperty: getNamePropertyList()) {
            record.addDemographic(nameProperty, String.valueOf(objs[fieldCounter]));
            fieldCounter ++;
        }

        for (String addressProperty: getAddressPropertyList()) {
            record.addDemographic(addressProperty, String.valueOf(objs[fieldCounter]));
            fieldCounter ++;
        }

        for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
            record.addDemographic(("(Identifier) " + patientIdentifierType.getName()), String.valueOf(objs[fieldCounter]));
            fieldCounter ++;
        }

        for (PersonAttributeType personAttributeType : personAttributeTypes) {
            record.addDemographic(("(Attribute) " + personAttributeType.getName()), String.valueOf(objs[fieldCounter]));
            fieldCounter ++;
        }

        Patient patient = Context.getPatientService().getPatient(patientId);
        String concatName="";
				if(patient.getPersonName().getFamilyName()!=null)
					concatName=patient.getPersonName().getFamilyName();
				if(patient.getPersonName().getMiddleName()!=null)
					concatName=concatName+patient.getPersonName().getMiddleName();
				if(patient.getPersonName().getGivenName()!=null)
					concatName=concatName+patient.getPersonName().getGivenName();
        record.addDemographic("concat1",concatName);
        fieldCounter++;
      return patientToRecord(patient);
    }

	/**
	 * serializes a list of patient identifiers to a delimited string after sorting and removing blanks
	 *
	 * @param identifiers the patient identifiers to be serialized
	 * @return a string containing all non-blank identifier values delimited by {@link DELIMITER}
	 */
    private String serializePatientIdentifiers(List<PatientIdentifier> identifiers) {
		List<String> idlist = new ArrayList<String>();
		for (PatientIdentifier identifier: identifiers) {
			String id = identifier.getIdentifier();
			if (StringUtils.hasText(id))
				idlist.add(id);
		}

		if (idlist == null || idlist.isEmpty())
			return "";

		Collections.sort(idlist);

		// TODO escape strings in list with DELIMITER before joining
		return OpenmrsUtil.join(idlist, MatchingConstants.MULTI_FIELD_DELIMITER);
	}

}
