package org.regenstrief.linkage.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * identifier and attribute convention and examples:
 * 
 * (Attribute) Birthplace
 * (Identifer) OpenMRS Identification Number
 * (Attribute) Credit Card Number
 * 
 * Object fields convention and examples:
 * 
 * org.openmrs.Person.gender
 * org.openmrs.PersonAddress.cityVillage
 * 
 * 
 * @author jegg
 *
 */

public class OrderedOpenMRSReader implements OrderedDataSourceReader{
	
	List<String> blocking_cols;
	//List<Iterator<Object>> values_iterators;
	//List<List<Object>> blocking_values;
	List<Object[]> blocking_values;
	Object[] current_blocking_values;
	List<Integer> value_set;
	
	public final static String ATTRIBUTE_PREFIX = "(Attribute) ";
	public final static String IDENT_PREFIX = "(Identifier) ";
	public final static String GET_ID_METHOD = "getPersonId";
	//public final static String GET_PATIENT_ID_METHOD = "getPatientId";
	public final static String GET_PERSON_METHOD = "getPerson";
	public final static String GET_PATIENT_METHOD = "getPatient";
	
	
	private Log log = LogFactory.getLog(this.getClass());
	protected SessionFactory sessionFactory;
	
	public OrderedOpenMRSReader(MatchingConfig mc, SessionFactory session_factory){
		sessionFactory = session_factory;
		
		//values_iterators = new ArrayList<Iterator<Object>>();
		value_set = new ArrayList<Integer>();
		
		String[] b_cols = mc.getBlockingColumns();
		blocking_cols = new ArrayList<String>();
		
		current_blocking_values = new Object[b_cols.length];
		
		for(int i = 0; i < b_cols.length; i++){
			blocking_cols.add(b_cols[i]);
		}
		
		blocking_values = getDemographicValues(blocking_cols);
		
		if(blocking_values.size() > 0){
			current_blocking_values = blocking_values.remove(0);
			fillIDValueSet();
		}
		
		// initial increment and blocking value assignment
		/*
		for(int i = values_iterators.size() - 1; i >= 0; i--){
			Iterator<Object> it = values_iterators.get(i);
			Object new_block_val = it.next();
			current_blocking_values[i] = new_block_val;
		}*/
		
		
		
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	public int getRecordSize(){
		return 0;
	}
	
	public boolean reset(){
		return false;
	}
	
	public boolean hasNextRecord(){
		if(value_set.size() > 0){
			// there are more IDs at the current set of blocking values
			return true;
		}
		
		return false;
	}
	
	public boolean close(){
		return false;
	}
	
	protected int fillIDValueSet(){
		value_set = getPatientIDs(blocking_cols, current_blocking_values);
		//int col = 1;
		//while(value_set.size() > 0 && col < blocking_cols.length){
		//	value_set.retainAll(getPatientIDs(blocking_cols[col], current_blocking_values[col]));
		//	col++;
		//}
		//log.warn("OpenMRS reader filled with " + value_set.size() + " IDs");
		return value_set.size();
	}
	
	public Record nextRecord(){
		// possible future optimization - remove values from blocking_values if all references
		// to a value has been read
		
		if(value_set.size() > 0){
			Integer id = value_set.remove(0);
			Patient p = Context.getPatientService().getPatient(id);
			if(value_set.size() == 0 && blocking_values.size() > 0){
				// removed last ID at this point in blocking values, need to increment iterators and refill
				// value_set
				//while(incrementIterators() && fillIDValueSet() < 1){
				current_blocking_values = blocking_values.remove(0);
				fillIDValueSet();
			}
			
			return LinkDBConnections.getInstance().patientToRecord(p);
		}
		
		return null;
	}
	
	/*protected boolean incrementIterators(){
		for(int i = values_iterators.size() - 1; i >= 0; i--){
			Iterator<Object> it = values_iterators.get(i);
			if(it.hasNext()){
				Object new_block_val = it.next();
				current_blocking_values[i] =  new_block_val;
				return true;
			} else {
				values_iterators.set(i, blocking_values.get(i).iterator());
			}
		}
		return false;
	}*/
	
	/**
	 * 
	 * @param demographic
	 * @return
	 */
	private List<Object[]> getDemographicValues(List<String> demographics){
		List<Object[]> ret = null;
		String query_text = new String();
		
		String select_clause = getSelectDistinctValuesClause(demographics);
		String from_clause = getFromClause(demographics);
		String where_clause = getValuesWhereClause(demographics);
		query_text = select_clause + " " + from_clause + " " + where_clause;
		log.info("getting values for " + demographics + " using query of:  " + query_text);
		
		Query q = sessionFactory.getCurrentSession().createQuery(query_text);
		
		// if list of demographics includes attributes or identifiers, need to set types in query text from method
		for(int i = 0; i < demographics.size(); i++){
			String demographic = demographics.get(i);
			if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
				// set o<i>.type equal to pat
				PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName(stripType(demographic));
				if(pat != null){
					q.setParameter("val" + i, pat);
				}
			} else if(demographic.indexOf(IDENT_PREFIX) != -1){
				// everything we need to query for is in PatientIdentifier
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(stripType(demographic));
				if(pit != null){
					q.setParameter("val" + i, pit);
				}
			}
		}
		
		if(demographics.size() == 1){
			ret = new ArrayList<Object[]>();
			List l = q.list();
			for(int i = 0; i < l.size(); i++){
				Object[] val = new Object[1];
				val[0] = l.get(i);
				ret.add(val);
			}
			
		} else {
			ret = q.list();
		}
		
		
		return ret;
	}
	
	private String stripType(String type_name){
		// method strips the first 
		if(type_name.indexOf(ATTRIBUTE_PREFIX) != -1){
			return type_name.substring(ATTRIBUTE_PREFIX.length());
		} else if(type_name.indexOf(IDENT_PREFIX) != -1){
			return type_name.substring(IDENT_PREFIX.length());
		}
		return type_name;
	}
	
	/**
	 * Returns a set of patient IDs for a given demographic and value
	 * 
	 * @param demographic
	 * @param value
	 * @return
	 */
	private List<Integer> getPatientIDs(List<String> demographics, Object[] vals){
		List<Integer> ret = null;
		String query_text = new String();
		
		List<Object> values = new ArrayList<Object>();
		for(int i = 0; i < vals.length; i++){
			values.add(vals[i]);
		}
		
		String select_clause = getSelectIDsClause(demographics);
		String from_clause = getFromClause(demographics);
		String where_clause = getIDsWhereClause(demographics);
		query_text = select_clause + " " + from_clause + " " + where_clause;
		//log.warn("getting ID for values " + values + " using query of:  " + query_text);
		
		Query q = sessionFactory.getCurrentSession().createQuery(query_text);
		
		// if list of demographics includes attributes or identifiers, need to set types in query text from method
		for(int i = 0; i < values.size(); i++){
			q.setParameter("val" + i, values.get(i));
		}
		
		ret = q.list();
		
		return ret;
	}
	
	/**
	 * Method returns a string with the select clause of the HQL query used to get
	 * the distinct values for the demographics
	 * 
	 * @param demographics
	 * @return
	 */
	private String getSelectDistinctValuesClause(List<String> demographics){
		String clause = "SELECT DISTINCT ";
		for(int i = 0; i < demographics.size(); i++){
			String demographic = demographics.get(i);
			if(i > 0){
				clause += ", ";
			}
			
			if(demographic.contains(".")){
				// only need to know the field name of the object
				clause += "o" + i + "." + getFieldName(demographic);
			} else {
				if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
					// attribute values are stored in PersonAttribute.value
					clause += "o" + i + ".value";
				} else if(demographic.indexOf(IDENT_PREFIX) != -1){
					// identifier values are stored in PatientIdentifier.identifier
					clause += "o" + i + ".identifier";
				}
				
			}
		}
		
		//clause += ")";
		return clause;
	}
	
	private String getSelectIDsClause(List<String> demographics){
		return "SELECT p.patientId";
	}
	
	/**
	 * Method returns a From clause that contains the objects the HQL query will need to use
	 * 
	 * @param demographics
	 * @return
	 */
	private String getFromClause(List<String> demographics){
		String clause = "FROM Patient p, ";
		for(int i = 0; i < demographics.size(); i++){
			String demographic = demographics.get(i);
			if(i > 0){
				clause += ", ";
			}
			
			if(demographic.contains(".")){
				// only need to know object name to query from
				clause += getObjectName(demographic)  + " o" + i;
			} else {
				if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
					// everything we need to query for is in PersonAttribute objects
					clause += "PersonAttribute o" + i;
				} else if(demographic.indexOf(IDENT_PREFIX) != -1){
					// everything we need to query for is in PatientIdentifier
					clause += "PatientIdentifier o" + i;
				}
				
			}
		}
		
		return clause;
	}
	
	private String getIDsWhereClause(List<String> demographics){
		String clause = "WHERE ";
		
		for(int i = 0; i < demographics.size(); i++){
			String demographic = demographics.get(i);
			if(i > 0){
				clause += " AND ";
			}
			
			if(demographic.contains(".")){
				// only need to know object name to query from
				clause += "o" + i + "." + getFieldName(demographic)  + " = :val" + i;
				
			} else {
				if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
					// everything we need to query for is in PersonAttribute objects
					PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName(stripType(demographic));
					if(pat != null){
						clause += "o" + i + ".value = :val" + i;
					}
				} else if(demographic.indexOf(IDENT_PREFIX) != -1){
					// everything we need to query for is in PatientIdentifier
					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(stripType(demographic));
					if(pit != null){
						clause += "o" + i + ".identifier = :val" + i;
					}
				}
			}
			clause += " AND " + getPatientRelation(i, demographic);
		}
		
		return clause;
	}
	
	private String getValuesWhereClause(List<String> demographics){
		String clause = "WHERE ";
		for(int i = 0; i < demographics.size(); i++){
			String demographic = demographics.get(i);
			if(i > 0){
				clause += " AND ";
			}
			
			if(demographic.contains(".")){
				// only need to know object name to query from
				clause += "o" + i + "." + getFieldName(demographic)  + " IS NOT NULL";
			} else {
				if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
					// everything we need to query for is in PersonAttribute objects
					PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName(stripType(demographic));
					if(pat != null){
						clause += "o" + i + ".value IS NOT NULL AND o" + i + ".attributeType = :val" + i;
					}
				} else if(demographic.indexOf(IDENT_PREFIX) != -1){
					// everything we need to query for is in PatientIdentifier
					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(stripType(demographic));
					if(pit != null){
						clause += "o" + i + ".identifier IS NOT NULL AND o" + i + ".identifierType = :val" + i;
					}
				}
			}
			
			clause += " AND " + getPatientRelation(i, demographic);
		}
		
		return clause;
	}
	
	/**
	 * Method returns the portion in the WHERE clause that relates the given demographic to the
	 * Patient objects.  We need this since we only want Patient IDs (not Users or Persons) and
	 * when querying for IDs using multiple values, we want the results to be for the same person
	 * 
	 * @param suffix	position of the given demographic within the list of demographics and values
	 * @param demographic	the demographic that needs linked to Patient
	 * @return	a String that can be place in an HQL WHERE clause that will specify equivalence of Patient and demographic
	 */
	private String getPatientRelation(int suffix, String demographic){
		if(demographic.contains(".")){
			// load class given by demographic and examine what fields it has
			String type = getObjectName(demographic);
			if(type.equals("Patient") || type.equals("org.openmrs.Patient")){
				return "p = o" + suffix;
			} else {
				try{
					Class c = Class.forName(type);
					Method[] methods = c.getMethods();
					for(int i = 0; i < methods.length; i++){
						if(methods[i].getName().equals("getPerson")){
							return "p = o" + suffix + ".person";
						}
					}
				}
				catch(ClassNotFoundException cnfe){
					return "";
				}
			}
		} else {
			// if it's attribute or identifier, then it'll have a Person or Patient field
			if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
				return "p = o" + suffix + ".person";
			} else if(demographic.indexOf(IDENT_PREFIX) != -1){
				return "p = o" + suffix + ".patient";
			}
		}
	
	return "";
	}
	
	/**
	 * Method returns the field name of a demographic using the convention for specifying an object's
	 * field.  For example, if the demographic is "org.openmrs.Patient.gender", this this method would return
	 * "gender"
	 * 
	 * @param demographic
	 * @return	the last field of the demographic when split on "."
	 */
	private String getFieldName(String demographic){
		String[] object_field = demographic.split("\\.");
		String field = object_field[object_field.length - 1];
		return field;
	}
	
	/**
	 * Method returns the object name of a demographic using the convention for specifying an object's
	 * field as a demographic.  For example, if the demographic is "org.openmrs.Patient.gender", this this method would return
	 * "org.openmrs.Patient"
	 * 
	 * @param demographic
	 * @return	all but the last element when the demographic is split on "."
	 */
	private String getObjectName(String demographic){
		String[] object_field = demographic.split("\\.");
		String object_name = object_field[0];
		for(int i = 1; i < object_field.length - 1; i++){
			object_name += "." + object_field[i];
		}
		
		return object_name;
	}
	
	/*
	 * Method checks for a method that returns the person ID, patient, or person
	 * of the object returned from a query and inserts the IDs into a list
	 */
	private List<Integer> getIDs(List<Object> openmrs_objects){
		List<Integer> ret = new ArrayList<Integer>();
		Iterator<Object> it = openmrs_objects.iterator();
		try{
			while(it.hasNext()){
				Object o = it.next();
				
				Class cls = o.getClass();
				Method[] methods = cls.getMethods();
				for(int i = 0; i < methods.length; i++){
					Method m = methods[i];
					if(m.getName().equals(GET_ID_METHOD)){
						Integer id = (Integer)m.invoke(o, (Object[])null);
						ret.add(id);
						i = methods.length;
					} else if(m.getName().equals(GET_PERSON_METHOD)){
						Person p = (Person)m.invoke(o, (Object[])null);
						ret.add(p.getPersonId());
						i = methods.length;
					} else if(m.getName().equals(GET_PATIENT_METHOD)){
						Patient p = (Patient)m.invoke(o, (Object[])null);
						ret.add(p.getPatientId());
						i = methods.length;
					}
				}
			}
		}
		catch(IllegalAccessException iae){
			log.warn("exception reading OpenMRS DB:  " + iae.getMessage());
		}
		catch(InvocationTargetException ite){
			log.warn("exception reading OpenMRS DB:  " + ite.getMessage());
		}
		
		return ret;
	}
}
