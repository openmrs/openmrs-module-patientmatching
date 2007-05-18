package org.openmrs.module.patientmatching.advice;

import org.aopalliance.intercept.*;
import org.apache.commons.logging.*;
import org.regenstrief.linkage.*;
import org.openmrs.module.patientmatching.*;
import org.openmrs.*;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.*;

/**
 * Class intercepts the findPatient method call and
 * instead uses the module's findPatient that's backed
 * by the record linkage database.
 *  
 * @author jegg
 *
 */

public class PatientMatchingAdvice extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {
	
	private Log log = LogFactory.getLog(this.getClass());
	private MatchFinder matcher;
	
	public PatientMatchingAdvice(MatchFinder matcher){
		this.matcher = matcher;
	}
	
	public boolean matches(Method method, Class targetClass) {
		return false;
	}
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		if(args[0] != null && args[0] instanceof Patient){
			Patient to_match = (Patient)args[0];
			Record r = PatientMatchingActivator.patientToRecord(to_match);
			MatchResult mr = matcher.findBestMatch(r);
			if(mr.getScore() > PatientMatchingActivator.DEFAULT_THRESHOLD){
				return mr.getRecord1();
			}
		}
		
		return null;
	}

}
