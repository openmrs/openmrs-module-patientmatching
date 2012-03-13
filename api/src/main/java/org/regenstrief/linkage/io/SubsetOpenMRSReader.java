package org.regenstrief.linkage.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Method returns a subset of Records from OpenMRS based on the values in a specific Record.
 * 
 * @author jegg
 *
 */

public class SubsetOpenMRSReader extends OrderedOpenMRSReader implements SubsetDataSourceReader {
	
	protected Patient pattern_patient;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public SubsetOpenMRSReader(MatchingConfig mc, SessionFactory session_factory, Patient p){
		super(mc, session_factory);
		pattern_patient = p;
		// initReader caled in super class constructor, but call again now that pattern_patient has been set
		initReader(mc);
	}
	
	protected List<Object[]> getDemographicValues(List<String> demographics){
		System.out.println("calling getDemographicValues");
		if(pattern_patient != null){
			// need to get the Object[] that corresponds to the pattern_record
			Object[] patient_values = new Object[blocking_cols.size()];
			for(int i = 0; i < blocking_cols.size(); i++){
				String blocking_col = blocking_cols.get(i);
				Object val = null;
				
				if(blocking_col.indexOf(ATTRIBUTE_PREFIX) != -1){
					PersonAttribute pa = pattern_patient.getAttribute(stripType(blocking_col));
					if(pa != null){
						val = pa.getValue();
					}
				} else if(blocking_col.indexOf(IDENT_PREFIX) != -1){
					PatientIdentifier pi = pattern_patient.getPatientIdentifier(stripType(blocking_col));
					if(pi != null){
						val = pi.getIdentifier();
					}
				} else {
					System.out.println("getting field value");
					val = getFieldValue(blocking_col);
				}
				
				patient_values[i] = val;
			}
			
			List<Object[]> ret = new ArrayList<Object[]>();
			ret.add(patient_values);
			return ret;
		}
		return null;
	}
	
	protected Object getFieldValue(String demographic){
		if(pattern_patient != null){
			// get values of something like Patient.gender or Patient.birthdate or Patient.getPersonAddress.getPostalCode
			Object base = pattern_patient;
			
			// change the object to call method on if it's not patient or person
			String type = getObjectName(demographic);
			String field = getFieldName(demographic);
			
			try{
				if(!type.endsWith("Patient") && !type.endsWith("Person")){
					// need to call pattern_patient method to get base object
					String[] type_parts = type.split("\\.");
					String class_name = type_parts[type_parts.length - 1];
					String method_name = "get" + class_name;
					Class c = Class.forName("org.openmrs.Patient");
					Method[] methods = c.getMethods();
					for(int i = 0; i < methods.length; i++){
						String m_name = methods[i].getName().toLowerCase();
						if(m_name.equals(method_name.toLowerCase())){
							base = methods[i].invoke(base, null);
						}
					}
				}
				
				Class c = base.getClass();
				
				Method[] methods = c.getMethods();
				for(int i = 0; i < methods.length; i++){
					String method_name = "get" + field;
					String m_name = methods[i].getName().toLowerCase();
					if(m_name.equals(method_name.toLowerCase())){
						return methods[i].invoke(base, null);
					}
				}
			}
			catch(ClassNotFoundException cnfe){
				System.out.println(cnfe.getMessage());
				return "";
			}
			catch(IllegalAccessException iae){
				System.out.println(iae.getMessage());
				return "";
			}
			catch(InvocationTargetException ite){
				System.out.println(ite.getMessage());
				return "";
			}
			return null;
		}
		return null;
	}
}
