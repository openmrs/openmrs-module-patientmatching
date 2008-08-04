package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
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
	
	String[] blocking_cols;
	List<Iterator<Object>> values_iterators;
	List<List<Object>> blocking_values;
	Object[] current_blocking_values;
	List<Integer> value_set;
	
	public final static String ATTRIBUTE_PREFIX = "(Attribute) ";
	public final static String IDENT_PREFIX = "(Identifier)";
	
	private SessionFactory sessionFactory;
	
	public OrderedOpenMRSReader(MatchingConfig mc, SessionFactory session_factory){
		sessionFactory = session_factory;
		
		values_iterators = new ArrayList<Iterator<Object>>();
		value_set = new ArrayList<Integer>();
		
		blocking_cols = mc.getBlockingColumns();
		blocking_values = new ArrayList<List<Object>>();
		
		current_blocking_values = new Object[blocking_cols.length];
		
		for(int i = 0; i < blocking_cols.length; i++){
			List<Object> query_values = getDemographicValues(blocking_cols[i]);
			Iterator<Object> it = query_values.iterator();
			List<Object> values = new ArrayList<Object>();
			while(it.hasNext()){
				Object obj = it.next();
				values.add(obj);
			}
			blocking_values.add(values);
			values_iterators.add(values.iterator());
		}
		
		// initial increment and blocking value assignment
		for(int i = values_iterators.size() - 1; i >= 0; i--){
			Iterator<Object> it = values_iterators.get(i);
			Object new_block_val = it.next();
			current_blocking_values[i] = new_block_val;
		}
		
		while(fillIDValueSet() < 1 && incrementIterators()){
			// do until IDs are set, or iterators are all incremented
		}
		
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
		value_set = getPatientIDs(blocking_cols[0], current_blocking_values[0]);
		int col = 1;
		while(value_set.size() > 0 && col < blocking_cols.length){
			value_set.retainAll(getPatientIDs(blocking_cols[col], current_blocking_values[col]));
			col++;
		}
		
		return value_set.size();
	}
	
	public Record nextRecord(){
		// possible future optimization - remove values from blocking_values if all references
		// to a value has been read
		
		if(value_set.size() > 0){
			Integer id = value_set.remove(0);
			Patient p = Context.getPatientService().getPatient(id);
			
			if(value_set.size() == 0){
				// removed last ID at this point in blocking values, need to increment iterators and refill
				// value_set
				while(incrementIterators() && fillIDValueSet() < 1){
					// do until IDs are set, or iterators are all incremented
				}
			}
			return PatientMatchingActivator.patientToRecord(p);
		}
		
		return null;
	}
	
	protected boolean incrementIterators(){
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
	}
	
	/**
	 * 
	 * @param demographic
	 * @return
	 */
	private List<Object> getDemographicValues(String demographic){
		List<Object> ret = null;
		String query_text = new String();
		// need to see if demographic refers to a data model table value, or an attribute
		if(demographic.contains(".")){
			// need to query the table name before the . for the field
			String[] object_field = demographic.split("\\.");
			String object_name = object_field[0];
			String field = object_field[object_field.length - 1];
			for(int i = 1; i < object_field.length - 1; i++){
				object_name += "." + object_field[i];
			}
			
			query_text = "SELECT DISTINCT o." + field + " FROM " + object_name + " o";
			Query q = sessionFactory.getCurrentSession().createQuery(query_text);
			ret = q.list();
		} else {
			if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
				// need to look at attribute type first to get the right query
				String attr = stripType(demographic);
				PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(attr);
				if(pat != null){
					// HQL query to get all values
					// something like 
					// select distinct value from person_attribute where person_attribute_type_id = <id>
					int id = pat.getPersonAttributeTypeId();
					query_text = "SELECT DISTINCT p.value FROM PersonAttribute p WHERE p.attributeID = " + id;
					Query q = sessionFactory.getCurrentSession().createQuery(query_text);
					ret = q.list();
				}
			} else if(demographic.indexOf(IDENT_PREFIX) != -1){
				// need to look at identifier types first to get right query
				String ident = stripType(demographic);
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(ident);
				if(pit != null){
					// HQL query to get all values
					int id = pit.getPatientIdentifierTypeId();
					query_text = "SELECT DISTINCT p.value FROM PatientIdentifier p WHERE p.typeID = " + id;
					Query q = sessionFactory.getCurrentSession().createQuery(query_text);
					ret = q.list();
				}
			}
			
		}
		
		return ret;
	}
	
	private String stripType(String type_name){
		// method strips the first 
		if(type_name.indexOf(ATTRIBUTE_PREFIX) != -1){
			return type_name.replaceFirst(ATTRIBUTE_PREFIX,"");
		} else if(type_name.indexOf(IDENT_PREFIX) != -1){
			return type_name.replaceFirst(IDENT_PREFIX, "");
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
	private List<Integer> getPatientIDs(String demographic, Object value){
		List<Integer> ret = null;
		String query_text = new String();
		// get patient IDs using HQL that have this value for
		// this demographic
		if(demographic.contains(".")){
			// need to query the table name before the . for the field
			String[] object_field = demographic.split("\\.");
			String object_name = object_field[0];
			String field = object_field[object_field.length - 1];
			for(int i = 1; i < object_field.length - 1; i++){
				object_name += "." + object_field[i];
			}
			
			query_text = "SELECT o.personID FROM " + object_name + " o WHERE " + field + " =:value";
			Query q = sessionFactory.getCurrentSession().createQuery(query_text);
			q.setEntity("value", value);
			ret = new ArrayList<Integer>();
			ret.addAll(q.list());
		} else {
			if(demographic.indexOf(ATTRIBUTE_PREFIX) != -1){
				// need to look at attribute type first to get the right query
				String attr = stripType(demographic);
				PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(attr);
				if(pat != null){
					// HQL query to get all values
					// something like 
					// select distinct value from person_attribute where person_attribute_type_id = <id>
					int id = pat.getPersonAttributeTypeId();
					query_text = "SELECT DISTINCT p.personID FROM PersonAttribute p WHERE p.value = :value";
					Query q = sessionFactory.getCurrentSession().createQuery(query_text);
					q.setEntity("value", value);
					ret = new ArrayList<Integer>();
					ret.addAll(q.list());
				}
			} else if(demographic.indexOf(IDENT_PREFIX) != -1){
				// need to look at identifier types first to get right query
				String ident = stripType(demographic);
				PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(ident);
				if(pit != null){
					// HQL query to get all values
					int id = pit.getPatientIdentifierTypeId();
					query_text = "SELECT DISTINCT p.personID FROM PatientIdentifier p WHERE p.value = :value";
					Query q = sessionFactory.getCurrentSession().createQuery(query_text);
					q.setEntity("value", value);
					ret = new ArrayList<Integer>();
					ret.addAll(q.list());
				}
			}
			
		}
		
		return ret;
	}
	
}
