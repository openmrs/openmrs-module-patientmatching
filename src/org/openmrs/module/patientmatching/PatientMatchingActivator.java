package org.openmrs.module.patientmatching;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.*;
import org.openmrs.*;
import org.regenstrief.linkage.Record;

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

	private Log log = LogFactory.getLog(this.getClass());
	
	public void shutdown() {
		// TODO Auto-generated method stub
		log.info("Shutting down Patient Matching Module");
	}

	public void startup() {
		// TODO Auto-generated method stub
		log.info("Starting Patient Matching Module");
	}
	
}
