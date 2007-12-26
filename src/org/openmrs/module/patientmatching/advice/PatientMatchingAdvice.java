package org.openmrs.module.patientmatching.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.TwinAnalyzer;
import org.regenstrief.linkage.analysis.UnMatchableRecordException;
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
	private Logger file_log = Logger.getLogger(PatientMatchingActivator.FILE_LOG);
	
	public PatientMatchingAdvice(){
		
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
		
		LinkDBConnections ldb_con = LinkDBConnections.getInstance();
		MatchFinder matcher = ldb_con.getFinder();
		RecordDBManager link_db = ldb_con.getRecDBManager();
		
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
				if(o instanceof Patient){
					Patient just_added = (Patient)o;
					if(link_db.addRecordToDB(PatientMatchingActivator.patientToRecord(just_added))){
						if(log.isDebugEnabled()){
							log.debug("LinkDBManager object successfully added patient");
						}
					} else {
						log.warn("Error when using LinkDBManager object to add patient " + just_added.getPatientId() + " to linkage table");
					}
					
				}
			} else if(method_name.equals(PatientMatchingActivator.FIND_METHOD)){
				try{
					Record r = PatientMatchingActivator.patientToRecord(to_match);
					MatchResult mr = matcher.findBestMatch(r);
					if(mr != null){
						Record rec_match = mr.getRecord2();
						String which_mc = mr.getMatchingConfig().getName();
						String key_demographic = rec_match.getDemographic(PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC);
						log.warn("Match with patient " + key_demographic + " - score: " + mr.getScore() + "\tTprob: " + mr.getTrueProbability() + "\tFprob: " + mr.getFalseProbability() + "\tSens: " + mr.getSensitivity() + "\tSpec: " + mr.getSpecificity() + "\tBlock: " + which_mc);
						log.warn("Score vector for " + mr.getScore() + ":\t" + mr.getScoreVector());
						file_log.info("Match with patient " + key_demographic + " - score: " + mr.getScore() + "\tTprob: " + mr.getTrueProbability() + "\tFprob: " + mr.getFalseProbability() + "\tSens: " + mr.getSensitivity() + "\tSpec: " + mr.getSpecificity() + "\tBlock: " + which_mc);
						file_log.info("Score vector for " + mr.getScore() + ":\t" + mr.getScoreVector());
						
						TwinAnalyzer ta = new TwinAnalyzer();
						boolean are_twins = ta.areTwins(rec_match, r);
						if(are_twins){
							// returned pair seem to be twins; return o and log reason
							log.warn("Match analyzed as being twins, returning default invocation match");
							file_log.info("Match analyzed as being twins, returning default OpenMRS invocation match");
							return o;
						}
						
						Patient patient_match = Context.getPatientService().getPatient(new Integer(rec_match.getDemographic(PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC)));
						return patient_match;
					} else {
						//file_log.info("No match found within module, returning OpenMRS's findPatient result of " + o);
						return o;
					}
				}
				catch(UnMatchableRecordException umre){
					Record failed = umre.getRecord();
					file_log.info("Patient " + failed + "\nnot matchable due to value(s) in demographics");
					return null;
				}
				catch(Exception e){
					log.warn(e.getClass().toString() + " exception when trying to match against link table: " + e.getMessage() + ", returning default invocation result");
					return o;
				}
				
				
			} else if(method_name.equals(PatientMatchingActivator.UPDATE_METHOD)){
				log.debug("Updating patient");
				if(o instanceof Patient){
					Patient just_updated = (Patient)o;
					
					Record ju = PatientMatchingActivator.patientToRecord(just_updated);
					if(link_db.updateRecord(ju, PatientMatchingActivator.LINK_TABLE_KEY_DEMOGRAPHIC)){
						if(log.isDebugEnabled()){
							log.debug("Record for patient " + just_updated.getPatientId() + " updated in database");
						}
					} else {
						log.warn("Update of Patient " + just_updated.getPatientId() + " to link db failed");
					}
					
					
				}
				
				
			}
			
		}
		
		return o;
	}
	
}
