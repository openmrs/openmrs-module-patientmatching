package org.openmrs.module.patientmatching.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.LinkDBManager;

/**
 * Class intercepts the findPatient method call and
 * instead uses the module's findPatient that's backed
 * by the record linkage database.
 *  
 * @author jegg
 *
 */

public class PatientMatchingAdvice implements MethodInterceptor {
	
	
	
	private Log log = LogFactory.getLog(this.getClass());
	private MatchFinder matcher;
	private PatientMatchingActivator activator;
	private LinkDBManager link_db;
	
	public PatientMatchingAdvice(MatchFinder matcher, LinkDBManager link_db){
		this.matcher = matcher;
		this.link_db = link_db;
	}
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		log.warn("advice intercepting " + invocation.getMethod().getName());
		Object[] args = invocation.getArguments();
		Object o = invocation.proceed();
		if(args[0] != null && args[0] instanceof Patient){
			Patient to_match = (Patient)args[0];
			Record r = PatientMatchingActivator.patientToRecord(to_match);
			String method_name = invocation.getMethod().getName();
			if(method_name.equals(PatientMatchingActivator.CREATE_METHOD)){
				if(o instanceof Patient){
					Patient just_added = (Patient)o;
					link_db.addRecordToDB(PatientMatchingActivator.patientToRecord(just_added));
				}
			} else if(method_name.equals(PatientMatchingActivator.FIND_METHOD)){
				MatchResult mr = matcher.findBestMatch(r);
				if(mr.getScore() > PatientMatchingActivator.DEFAULT_THRESHOLD){
					Record rec_match = mr.getRecord1();
					Patient patient_match = Context.getPatientService().getPatient(new Integer(rec_match.getDemographic("openmrs_id")));
					//return patient_match;
				}
			} else if(method_name.equals(PatientMatchingActivator.UPDATE_METHOD)){
				log.warn("updating patient");
				
			}
		}
		
		return o;
	}
	
}
