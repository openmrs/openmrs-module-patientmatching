package org.openmrs.module.patientmatching.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.RecordDBManager;

/**
 * Class provides AOP advice for the OpenMRS PatientService methods
 * of createPatient, updatePatient, and findPatient using
 * around advice.
 * 
 * If the class intercepts the findPatient method, then it might return
 * a different patient than core based on how the MatchFinder object
 * scores a match.  For the other two methods, the return value is
 * not changed.  It intercepts these methods to keep the record linkage
 * table consistent with the OpenMRS Patient table.
 *  
 * @author jegg
 *
 */

public class PatientMatchingAdvice implements MethodInterceptor {
	
	private Log log = LogFactory.getLog(this.getClass());
	private MatchFinder matcher;
	private RecordDBManager link_db;
	private Logger file_log = Logger.getLogger(PatientMatchingActivator.FILE_LOG);
	
	public PatientMatchingAdvice(MatchFinder matcher, RecordDBManager link_db){
		this.matcher = matcher;
		this.link_db = link_db;
	}
	
	/**
	 * Method intercepts the OpenMRS method invocation and depending on which
	 * method is intercepts, uses the MatchFinder or LinkDBManager objects.
	 * 
	 * If MethodInvocation name matches PatientMatchingActivator.CREATE_METHOD,
	 * the LinkDBManager object is used to add the patient to the record linkage table.
	 * 
	 * If MethodInvocation name matches PatientMatchingActivator.UPDATE_METHOD,
	 * the LinkDBManager object is used to update patients in the record linkage table
	 * that match on the key demographic.
	 * 
	 * If MethodInvocation name matches PatientMatchingActivator.FIND_METHOD,
	 * the MatchFinder object is used to try to find a better match.  The linkage
	 * table should be storing the unique OpenMRS patient ID and the method uses
	 * that as the hook back to the OpenMRS Patient objects.
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		log.debug("Advice intercepting " + invocation.getMethod().getName());
		Object[] args = invocation.getArguments();
		
		// for two of the methods, need to let invocation proceed, so go ahead and let it
		Object o = invocation.proceed();
		
		if(link_db == null || matcher == null){
			log.warn("Advice has null objects for link database or matchfinder; returning default invocation object");
			return o;
		}
		
		if(args[0] != null && args[0] instanceof Patient){
			Patient to_match = (Patient)args[0];
			String method_name = invocation.getMethod().getName();
			
			if(method_name.equals(PatientMatchingActivator.CREATE_METHOD)){
				log.debug("Trying to add patient to link table");
				file_log.info("Trying to add patient to link table");
				if(o instanceof Patient){
					Patient just_added = (Patient)o;
					if(link_db.addRecordToDB(PatientMatchingActivator.patientToRecord(just_added))){
						// need to reset the reader so data base update is found
						if(!matcher.resetReader()){
							log.warn("LinkDBManager object successfully added patient, but database reader not reset; next read might not find latest update");
							file_log.warn("LinkDBManager object successfully added patient, but database reader not reset; next read might not find latest update");
						}
						if(log.isDebugEnabled()){
							log.debug("LinkDBManager object successfully added patient");
							file_log.info("LinkDBManager object successfully added patient");
						}
					} else {
						log.warn("Error when using LinkDBManager object to add patient " + just_added.getPatientId() + " to linkage table");
						file_log.warn("Error when using LinkDBManager object to add patient " + just_added.getPatientId() + " to linkage table");
					}
					
				}
			} else if(method_name.equals(PatientMatchingActivator.FIND_METHOD)){
				try{
					Record r = PatientMatchingActivator.patientToRecord(to_match);
					MatchResult mr = matcher.findBestMatch(r);
					if(mr != null){
						Record rec_match = mr.getRecord2();
						log.info("Found a best match - score: " + mr.getScore() + "\tTprob: " + mr.getTrueProbability() + "\tFprob: " + mr.getFalseProbability() + "\tSens: " + mr.getSensitivity() + "\tSpec: " + mr.getSpecificity());
						file_log.info("Found a best match - score: " + mr.getScore() + "\tTprob: " + mr.getTrueProbability() + "\tFprob: " + mr.getFalseProbability() + "\tSens: " + mr.getSensitivity() + "\tSpec: " + mr.getSpecificity());
						Patient patient_match = Context.getPatientService().getPatient(new Integer(rec_match.getDemographic(PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC)));
						return patient_match;
					} else {
						return o;
					}
				}
				catch(Exception e){
					log.warn("Exception when trying to match against link table: " + e.getMessage() + ", returning null");
					file_log.warn("Exception when trying to match against link table: " + e.getMessage() + ", returning null");
					return o;
				}
				
				
			} else if(method_name.equals(PatientMatchingActivator.UPDATE_METHOD)){
				log.debug("Updating patient");
				file_log.info("Updating patient");
				if(o instanceof Patient){
					Patient just_updated = (Patient)o;
					
					Record ju = PatientMatchingActivator.patientToRecord(just_updated);
					if(link_db.updateRecord(ju, PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC)){
						log.debug("Record for patient " + just_updated.getPatientId() + " updated in database");
						file_log.info("Record for patient " + just_updated.getPatientId() + " updated in database");
					} else {
						log.warn("Update of Patient " + just_updated.getPatientId() + " to link db failed");
						file_log.warn("Update of Patient " + just_updated.getPatientId() + " to link db failed");
					}
					
					
				}
				
				
			}
		}
		
		return o;
	}
	
}
