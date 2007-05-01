package org.openmrs.module.patientmatching;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.*;
import org.openmrs.*;
import org.regenstrief.linkage.Record;

public class PatientMatchingActivator implements Activator{

	private Log log = LogFactory.getLog(this.getClass());
	
	public void shutdown() {
		// TODO Auto-generated method stub
		log.info("Shutting down Record Linkage Module");
	}

	public void startup() {
		// TODO Auto-generated method stub
		log.info("Starting Record Linkage Module");
	}
	
	/**
	 * Converts the person entity used within OpenMRS, Patient objects,
	 * to the object type used in the record linkage software, Records.
	 * 
	 * The patient information used in the linkage is:
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
	 * @param p the patient to convert
	 */
	public Record patientToRecord(Patient p){
		Record rec = new Record();
		
		// medical record number
		p.getPatientId();
		
		PatientName pn = p.getPatientName();
		
		// last name
		pn.getFamilyName();
		
		// last name NYSIIS
		
		// first name
		pn.getGivenName();
		
		// first name NYSIIS
		
		// gender
		p.getGender();
		
		Date bd = p.getBirthdate();
		
		
		// month of birth
		SimpleDateFormat month = new SimpleDateFormat("MM");
		month.format(bd);
		
		// day of birth
		SimpleDateFormat date = new SimpleDateFormat("d");
		date.format(bd);
		
		// year of birth
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		year.format(bd);
		
		
		PatientAddress pa = p.getPatientAddress();
		// city
		pa.getCityVillage();
		
		// street
		pa.getAddress1();
		
		// zip
		pa.getPostalCode();
		
		// telephone
		
		
		// next of kin last name
		
		
		// next of kin first name
		
		
		// Dr. ID
		
		
		// Dr. first name
		
		
		// Dr. last name
		
		
		
		return rec;
	}
}
