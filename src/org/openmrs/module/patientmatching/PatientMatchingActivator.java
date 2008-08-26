package org.openmrs.module.patientmatching;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aopalliance.aop.Advice;
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
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.patientmatching.advice.PatientMatchingAdvice;
import org.openmrs.module.patientmatching.web.MatchingConfigUtilities;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.RecordDBManager;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * Class implements the startup and initial methods for the module.  When
 * loading, module runs the populateMatchingTable() method to synchronize
 * the OpenMRS patient database with the record linkage table.  To do this,
 * the module needs to check that ever patient's openmrs_id exists in the table
 * and add it if it is not present.  This can take a long time.
 * 
 * @author jegg
 *
 */

/*
 * 
 * The demographic information used in the linkage process is the same:
 * 
 * 	"mrn" - medical record number
 * 	"ln"  - last name
 * 	"lny" - last name NYSIIS
 * 	"fn"  - first name
 * 	"sex"  - sex/gender
 * 	"mb" - month of birth
 * 	"db" - date of birth
 * 	"yb" - year of birth
 * 	"city" - address city
 * 	"st" - address street
 * 	"zip" - address zip code
 * 	"tel" - telephone number
 * 	"nkln" - next of kin last name
 * 	"nkfn" - next of kin first name
 * 	"drid" - Dr. ID
 * 	"drfn" - Dr. first name
 * 	"drln" - Dr. last name 
 *
 */
public class PatientMatchingActivator extends StaticMethodMatcherPointcutAdvisor implements Activator, Advisor{
	
	public static final String CREATE_METHOD = "createPatient";
	public static final String UPDATE_METHOD = "updatePatient";
	public static final String FIND_METHOD = "findPatient";
	
	public final static String CONFIG_FILE = "link_config.xml";
	public final static String MATCHING_ATTRIBUTE = "Other Matching Information";
	public final static String LINK_TABLE_KEY_DEMOGRAPHIC = "openmrs_id";
	
	// to fix the automatic startup issue, need to get this privilege
	// it's either "View Patients" or "View Cohorts"
	public final static String PRIVILEGE = "View Patients";
	public final static String PRIVILEGE2 = "View Patient Cohorts";
	
	protected static Log logger = LogFactory.getLog(PatientMatchingActivator.class);
	private Log log = LogFactory.getLog(this.getClass());
	public static String FILE_LOG = "patient_matching_file_log";
	
	/**
	 * Method calls the disconnect method in the LinkDBManager object.
	 */
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
		LinkDBConnections ldb_con = LinkDBConnections.getInstance();
		RecordDBManager link_db = ldb_con.getRecDBManager();
		link_db.disconnect();
		Context.removeProxyPrivilege(PRIVILEGE);
		Context.removeProxyPrivilege(PRIVILEGE2);
	}
	
	/**
	 * At startup, module parses the configuration file and makes sure the
	 * record linkage table is populated with the existing patients in OpenMRS.
	 */
	public void startup() {
		log.info("Starting Patient Matching Module");
		
		// to fix automatic startup, get privilege
		Context.addProxyPrivilege(PRIVILEGE);
		Context.addProxyPrivilege(PRIVILEGE2);
		
		log.info("Starting to populate matching table");
		if(LinkDBConnections.getInstance().getRecDBManager() != null){
			populateMatchingTable();
			log.info("Matching table populated");
		} else {
			log.warn("Error parsing config file and creating linkage objects");
		}
		
	}
	
	/**
	 * Method iterates through existing Patient objects and adds them to the linkage
	 * database for use when matching.  Method also creates the table, if needed,
	 * with the correct column taken from the LinkDataSource object.
	 *
	 */
	private void populateMatchingTable(){
		
		// get the list of patient objects
		//link_db.createTable();
		LinkDBConnections ldb_con = LinkDBConnections.getInstance();
		RecordDBManager link_db = ldb_con.getRecDBManager();
		if(!link_db.connect()){
			log.warn("Error connecting to link db when populating database");
			return;
		}
		
		// iterate through them, and if linkage tables does not contain 
		// a record with openmrs_id equal to patient.getID, then add
		PatientSetService pss = Context.getPatientSetService();
		PatientService patientService = Context.getPatientService();
		Set<Integer> patient_list = pss.getAllPatients().getMemberIds();
		Iterator<Integer> it = patient_list.iterator();
		while(it.hasNext()){
			Patient p = patientService.getPatient(it.next());
			Integer id = p.getPatientId();
			int existing_patients = link_db.getRecordCountFromDB(LINK_TABLE_KEY_DEMOGRAPHIC, id.toString());
			if(existing_patients == 0){
				if(link_db.addRecordToDB(patientToRecord(p))){
					if(log.isDebugEnabled()){
						log.debug("Adding patient " + p.getPatientId() + " to link DB succeeded");
					}
				} else {
					log.warn("Adding patient " + p.getPatientId() + " to link DB failed");
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
    public boolean matches(Method method, Class targetClass) {
		String method_name = method.getName();
		if(method_name.equals(CREATE_METHOD)){
			return true;
		} else if(method_name.equals(UPDATE_METHOD)){
			return true;
		} else if(method_name.equals(FIND_METHOD)){
			return true;
		}
		return false;
	}
	
	@Override
	public Advice getAdvice(){
		log.debug("Returning new advice object from " + this);
		return new PatientMatchingAdvice();
	}
	
	/**
	 * Method gets the demographic information used in the record linkage from
	 * the Patient object and creates a Record object with all the fields.
	 * 
	 * The module prefers and tries to find a patient attribute type of
	 * MATCHING_ATTRIBUTE to parse the patient information.  This string is
	 * in the form of "<demographic1>:<value1>;<demographic2>:<value2>" and
	 * assumes that the labels for demographics match the labels in the config
	 * file.
	 * 
	 * If there is no patient attribute of type MATCHING_ATTRIBUTE then the method
	 * tries to get standard information such sa date of birth and name to provide
	 * some information when making a Record object.
	 * 
	 * @param patient	the Patient object to transform
	 * @return	a new Record object representing the Patient
	 */
	@SuppressWarnings("unchecked")
    public static Record patientToRecord(Patient patient){
		Record ret = new Record(patient.getPatientId(),"OpenMRS");
		
		// OpenMRS unique patient ID should be present if the patient is within
		// the OpenMRS patient store, but if patient is new and being searched on
		// before inserted, it would be null
		Integer id = patient.getPatientId();
		if(id != null){
			ret.addDemographic(LINK_TABLE_KEY_DEMOGRAPHIC, Integer.toString(id));
		}
		
		// first, try to get the "Matching Information" attribute type
		PersonAttributeType matching_attr_type = Context.getPersonService().getPersonAttributeTypeByName(MATCHING_ATTRIBUTE);
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
            // nothing is excluded
            List<String> listExcludedProperties = new ArrayList<String>();
            
            Class[] classes = {Patient.class, PersonAddress.class, PersonName.class};
            List<String> propertyList = new ArrayList<String>();
            for (Class clazz : classes) {
                propertyList.addAll(MatchingConfigUtilities.introspectBean(listExcludedProperties, clazz));
            }
            
            for (String property : propertyList) {
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

            PatientService patientService = Context.getPatientService();
            List<PatientIdentifierType> patientIdentifierTypes = patientService.getAllPatientIdentifierTypes();
            for (PatientIdentifierType patientIdentifierType : patientIdentifierTypes) {
                PatientIdentifier identifier = patient.getPatientIdentifier(patientIdentifierType.getName());
                if (identifier != null) {
                    ret.addDemographic("(Identifier) " + patientIdentifierType.getName(), identifier.getIdentifier());
                } else {
                    ret.addDemographic("(Identifier) " + patientIdentifierType.getName(), "");
                }
            }

            PersonService personService = Context.getPersonService();
            List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();
            for (PersonAttributeType personAttributeType : personAttributeTypes) {
                PersonAttribute attribute = patient.getAttribute(personAttributeType.getName());
                if (attribute != null) {
                    ret.addDemographic("(Attribute) " + personAttributeType.getName(), attribute.getValue());
                } else {
                    ret.addDemographic("(Attribute) " + personAttributeType.getName(), "");
                }
            }
		}
		return ret;
	}
}
