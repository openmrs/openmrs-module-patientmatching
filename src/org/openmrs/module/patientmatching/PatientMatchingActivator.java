package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
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
	
	private Log log = LogFactory.getLog(this.getClass());
	
	// linkage objects to create at startup and keep for linkage use
	private MatchFinder matcher;
	private LinkDBManager link_db;
	
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
	}
	
	public void startup() {
		log.info("Starting Patient Matching Module");
		boolean ready = parseConfig(new File(CONFIG_FILE));
		if(!ready){
			log.info("error parsing config file and creating linkage objects");
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
		
		// iterate through them, and if linkage tables does not contain 
		// a record with openmrs_id equal to patient.getID, then add
		
	}
	
	/*
	 * Class parses a config file to create the objects needed for linkage
	 */
	private boolean parseConfig(File config){
		try{
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
			
			return false;
		}
		catch(SAXException spe){
			
			return false;
		}
		catch(IOException ioe){
			
			return false;
		}
		return true;
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
		
		// "mrn" - medical record number
		
		
		
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
		
		
		return ret;
	}
}
