package org.openmrs.module.patientmatching.advice;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.UnMatchableRecordException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * Class intercepts the findPatient method call and
 * instead uses the module's findPatient that's backed
 * by the record linkage database.
 *  
 * @author jegg
 *
 */

public class PatientMatchingAdvice extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {
	
	public static final String CREATE_METHOD = "createPatient";
	public static final String UPDATE_METHOD = "updatePatient";
	public static final String FIND_METHOD = "findPatient";
	
	private Log log = LogFactory.getLog(this.getClass());
	private MatchFinder matcher;
	
	
	public PatientMatchingAdvice(MatchFinder matcher){
		this.matcher = matcher;
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
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		if(args[0] != null && args[0] instanceof Patient){
			//Patient to_match = (Patient)args[0];
			//Record r = PatientMatchingActivator.patientToRecord(to_match);
			
		}
		
		return null;
	}
	
	/**
	 * Method intercepts the createPatient method to 
	 * 
	 * @param patient
	 * @return
	 */
	public boolean createPatient(Patient patient){
		
		return false;
	}
	
	/**
	 * Method intercepts the OpenMRS core updatePatient method to
	 * modify the matching database in order to keep the two
	 * synchonized.
	 * 
	 * @param patient	the patient to modify
	 * @return	the modified patient
	 */
	public boolean updatePatient(Patient patient){
		
		return false;
	}
	
	/**
	 * Method calls the findBestMatch method in MatchFinder to get a
	 * Record of the best match in the linkage database.  The openmrs_id
	 * field in the Record is the key to getting the existing Patient
	 * that the parameter matches.
	 * 
	 * @param patient	the Patient object to match
	 * @return	the already existing OpenMRS Patient object that best matches patient
	 */
	public Patient findPatient(Patient patient) throws UnMatchableRecordException{
		
		MatchResult mr = matcher.findBestMatch(PatientMatchingActivator.patientToRecord(patient));
		if(mr.getScore() > PatientMatchingActivator.DEFAULT_THRESHOLD){
			mr.getRecord1();
		}
		
		
		return null;
	}
}
