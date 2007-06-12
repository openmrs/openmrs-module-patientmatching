package org.openmrs.module.patientmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.patientmatching.advice.PatientMatchingAdvice;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
	public final static double DEFAULT_THRESHOLD = 0;
	public final static String MATCHING_ATTRIBUTE = "Other Matching Information";
	public final static String LINK_TABLE_KEY_DEMOGRAPHIC = "openmrs_id";
	
	private Log log = LogFactory.getLog(this.getClass());
	private BufferedWriter link_log;
	
	private MatchFinder matcher;
	private LinkDBManager link_db;
	
	/**
	 * Method calls the disconnect method in the LinkDBManager object.
	 */
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
		link_db.disconnect();
	}
	
	/**
	 * At startup, module parses the configuration file and makes sure the
	 * record linkage table is populated with the existing patients in OpenMRS.
	 */
	public void startup() {
		log.info("Starting Patient Matching Module");
		boolean ready = parseConfig(new File(CONFIG_FILE));
		log.info("Starting to populate matching table");
		populateMatchingTable();
		log.info("Matching table populated");
		if(!ready){
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
		
		// iterate through them, and if linkage tables does not contain 
		// a record with openmrs_id equal to patient.getID, then add
		PatientSetService pss = Context.getPatientSetService();
		List<Patient> patient_list = pss.getAllPatients().getPatients();
		Iterator<Patient> it = patient_list.iterator();
		while(it.hasNext()){
			Patient p = it.next();
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
			log.debug("parsing config file " + config);
			// Load the XML configuration file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(config);
			RecMatchConfig rmc = XMLTranslator.createRecMatchConfig(doc);
			MatchingConfig test_mc = rmc.getMatchingConfigs().get(0);
			link_db = new LinkDBManager(rmc.getLinkDataSource1(), test_mc);
			matcher = new MatchFinder(rmc.getLinkDataSource1(), test_mc, new RecordFieldAnalyzer());
			
		}
		catch(ParserConfigurationException pce){
			//writeLog(pce.getMessage());
			return false;
		}
		catch(SAXException spe){
			//writeLog(spe.getMessage());
			return false;
		}
		catch(IOException ioe){
			//writeLog(ioe.getMessage());
			return false;
		}
		log.debug("file parsed");
		return link_db.connect();
	}
	
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
		if(link_db == null){
			log.debug("Starting to parse config file for advice object");
			parseConfig(new File(CONFIG_FILE));
			log.debug("Config file parsed");
		}
		return new PatientMatchingAdvice(matcher, link_db);
	}
	
	public boolean isPerInstance(){
		return false;
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
	public static Record patientToRecord(Patient patient){
		Record ret = new Record();
		
		// OpenMRS unique patient ID
		ret.addDemographic("openmrs_id", Integer.toString(patient.getPatientId()));
		
		// first, try to get the "Matching Information" attribute type
		PersonAttributeType matching_attr_type = Context.getPersonService().getPersonAttributeType(MATCHING_ATTRIBUTE);
		if(matching_attr_type != null){
			// expected attribute with information is present, so use all the information from there
			PersonAttribute matching_attr = patient.getAttribute(matching_attr_type.getPersonAttributeTypeId());
			String matching_string = matching_attr.getValue();
			
			String[] demographics = matching_string.split(";");
			for(int i = 0; i < demographics.length; i++){
				String demographic_value = demographics[i];
				String[] pair = demographic_value.split(":", -1);
				ret.addDemographic(pair[0], pair[1]);
			}
			
		} else {
			// parse the Patient fields as best we can to get the information
			// "mrn" - medical record number
			
			
			PatientIdentifier pid = patient.getPatientIdentifier();
			//	"ln"  - last name
			//ret.addDemographic("ln", name.getFamilyName());
			
			
			// "lny" - last name NYSIIS
			// currently not being used
			
			// 	"fn"  - first name
			//ret.addDemographic("fn", name.getGivenName());
			
			// 	"sex"  - sex/gender
			ret.addDemographic("sex", patient.getGender());
			
			Date birthdate = patient.getBirthdate();
			Calendar cal = new GregorianCalendar();
			cal.setTime(birthdate);
			// 	"mb" - month of birth
			ret.addDemographic("mb", Integer.toString(cal.get(Calendar.MONTH)));
			
			// 	"db" - date of birth
			ret.addDemographic("db", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
			
			// 	"yb" - year of birth
			ret.addDemographic("yb", Integer.toString(cal.get(Calendar.YEAR)));
			
			
			// 	"city" - address city
			//ret.addDemographic("city", address.getCityVillage());
			
			// 	"st" - address street
			//String adr1 = address.getAddress1();
			//String adr2 = address.getAddress2();
			//if(adr2 == null){
			//	ret.addDemographic("st", adr1);
			//} else {
			//	ret.addDemographic("st", adr1 + " " + adr2);
			//}
			
			// 	"zip" - address zip code
			//ret.addDemographic("zip", address.getPostalCode());
			
			// 	"tel" - telephone number
			
			
			// 	"nkln" - next of kin last name
			
			
			// 	"nkfn" - next of kin first name
			
			
			// 	"drid" - Dr. ID
			
			
			// 	"drfn" - Dr. first name
			
			
			// 	"drln" - Dr. last name
			
			
		}
		
		
		return ret;
	}
}
