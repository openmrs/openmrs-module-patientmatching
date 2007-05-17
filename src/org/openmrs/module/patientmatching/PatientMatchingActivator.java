package org.openmrs.module.patientmatching;

import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.*;
import org.openmrs.*;
import org.regenstrief.linkage.*;
import org.regenstrief.linkage.db.*;
import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.analysis.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/*
 * As of a meeting on May 1st, the module will no longer be responsible
 * for converting between an OpenMRS patient or person object and will
 * be given Record objects and return ResultSet objects.
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
public class PatientMatchingActivator implements Activator{
	
	public final static String CONFIG_FILE = "link_config.xml";
	
	private Log log = LogFactory.getLog(this.getClass());
	
	// linkage objects to create at startup and keep for linkage use
	private MatchFinder matcher;
	private LinkDBManager link_db;
	
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
	}
	
	public void startup() {
		log.info("Starting Patient Matching Module");
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
	
	public Patient createPatient(Patient patient){
		
		return null;
	}
	
	public Patient updatePatient(Patient patient){
		
		return null;
	}
	
	public Patient findPatient(Patient patient){
		
		try{
			matcher.findBestMatch(patientToRecord(patient));
		}
		catch(UnMatchableRecordException umre){
			log.info("not matching record: record contains unmatchable data");
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param patient
	 * @return
	 */
	private Record patientToRecord(Patient patient){
		Record ret = new Record();
		
		// "mrn" - medical record number
		
		
		PatientName name = patient.getPatientName();
		//	"ln"  - last name
		ret.addDemographic("ln", name.getFamilyName());
		
		// "lny" - last name NYSIIS
		
		
		// 	"fn"  - first name
		ret.addDemographic("fn", name.getGivenName());
		
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
		
		PatientAddress address = patient.getPatientAddress();
		// 	"city" - address city
		ret.addDemographic("city", address.getCityVillage());
		
		// 	"st" - address street
		String adr1 = address.getAddress1();
		String adr2 = address.getAddress2();
		if(adr2 == null){
			ret.addDemographic("st", adr1);
		} else {
			ret.addDemographic("st", adr1 + " " + adr2);
		}
		
		// 	"zip" - address zip code
		ret.addDemographic("zip", address.getPostalCode());
		
		// 	"tel" - telephone number
		
		
		// 	"nkln" - next of kin last name
		
		
		// 	"nkfn" - next of kin first name
		
		
		// 	"drid" - Dr. ID
		
		
		// 	"drfn" - Dr. first name
		
		
		// 	"drln" - Dr. last name
		
		
		return ret;
	}
}
