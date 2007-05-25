package org.openmrs.module.patientmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
public class PatientMatchingActivator implements Activator, Advisor{
	
	public final static String CONFIG_FILE = "link_config.xml";
	public final static double DEFAULT_THRESHOLD = 0;
	public final static String MATCHING_ATTRIBUTE = "Other Matching Information";
	
	private Log log = LogFactory.getLog(this.getClass());
	private BufferedWriter link_log;
	
	// linkage objects to create at startup and keep for linkage use
	private MatchFinder matcher;
	private LinkDBManager link_db;
	
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
		
	}
	
	public void startup() {
		try{
			link_log = new BufferedWriter(new FileWriter("linkage_test_log.log"));
		}
		catch(IOException ioe){
			link_log = null;
		}
		log.info("Starting Patient Matching Module");
		boolean ready = parseConfig(new File(CONFIG_FILE));
		writeLog("starting to populate table");
		populateMatchingTable();
		if(!ready){
			log.info("error parsing config file and creating linkage objects");
			writeLog("error creating objects");
		}
	}
	
	public void writeLog(String str){
		if(link_log == null){
			return;
		}
		try{
			String line = new Date().toString() + ": " + str + "\n";
			link_log.write(line);
			link_log.flush();
		}
		catch(IOException ioe){
			
		}
	}
	
	/**
	 * Method iterates through existing Patient objects and adds them to the linkage
	 * database for use when matching.  Method also creates the table, if needed,
	 * with the correct column taken from the LinkDataSource object
	 *
	 */
	private void populateMatchingTable(){
		
		// get the list of patient objects
		//link_db.createTable();
		
		// iterate through them, and if linkage tables does not contain 
		// a record with openmrs_id equal to patient.getID, then add
		PatientSetService pss = Context.getPatientSetService();
		List<Patient> patient_list = pss.getAllPatients().getPatients();
		writeLog("populating table with " + patient_list.size() + " patients");
		
		Iterator<Patient> it = patient_list.iterator();
		while(it.hasNext()){
			Patient p = it.next();
			Integer id = p.getPatientId();
			writeLog("adding patient " + id);
			String patient_attr = p.printAttributes();
			writeLog("patient attributes: "  + patient_attr);
			int existing_patients = link_db.getRecordCountFromDB("openmrs_id", id.toString());
			writeLog("found " + existing_patients + " patients that match on id");
			if(existing_patients == 0){
				if(link_db.addRecordToDB(patientToRecord(p))){
					writeLog("add patient to link DB succeeded");
				} else {
					writeLog("add patient to link DB failed");
				}
			}
		}
	}
	
	/*
	 * Class parses a config file to create the objects needed for linkage
	 */
	private boolean parseConfig(File config){
		try{
			writeLog("parsing config file " + config);
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
		writeLog("file parsed");
		return link_db.connect();
	}
	
	public Advice getAdvice(){
		return new PatientMatchingAdvice(matcher);
	}
	
	public boolean isPerInstance(){
		return false;
	}
	
	/**
	 * Method gets the demographic information used in the record linkage from
	 * the Patient object and creates a Record object with all the fields
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
