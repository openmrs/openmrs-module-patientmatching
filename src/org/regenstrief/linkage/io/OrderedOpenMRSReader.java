package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

public class OrderedOpenMRSReader implements OrderedDataSourceReader{
	
	String[] blocking_cols;
	List<Iterator<String>> values_iterators;
	List<List<String>> blocking_values;
	List<String> current_blocking_values;
	List<Integer> value_set;
	
	private SessionFactory sessionFactory;
	
	public OrderedOpenMRSReader(MatchingConfig mc){
		// need to initialize sessionFactory to avoid null pointer exception when issuing query
		
		values_iterators = new ArrayList<Iterator<String>>();
		value_set = new ArrayList<Integer>();
		current_blocking_values = new ArrayList<String>();
		
		blocking_cols = mc.getBlockingColumns();
		blocking_values = new ArrayList<List<String>>();
		
		for(int i = 0; i < blocking_cols.length; i++){
			List<String> values = getDemographicValues(blocking_cols[i]);
			blocking_values.add(values);
			values_iterators.add(values.iterator());
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
	
	public Record nextRecord(){
		// possible future optimization - remove values from blocking_values if all references
		// to a value has been read
		
		boolean has_record = value_set.size() > 0;
		while(!has_record){
			// need to increment the least significant iterator, or restart iterators, until we find
			// IDs at the combination, or 
			if(incrementedIterators()){
				// get the set of IDs at the current values
				value_set = getPatientIDs(blocking_cols[0], current_blocking_values.get(0));
				int col = 1;
				while(value_set.size() > 0 && col < blocking_cols.length){
					value_set.retainAll(getPatientIDs(blocking_cols[1], current_blocking_values.get(1)));
				}
				if(value_set.size() > 0){
					has_record = true;
				}
			} else {
				// at the end of values
				return null;
			}
		}
		
		// if there are IDs in the value_set, then return a Record of one of the IDs
		if(value_set.size() > 0){
			Integer id = value_set.remove(0);
			Patient p = Context.getPatientService().getPatient(id);
			return PatientMatchingActivator.patientToRecord(p);
		}
		
		return null;
	}
	
	protected boolean incrementedIterators(){
		for(int i = values_iterators.size() - 1; i >= 0; i--){
			Iterator<String> it = values_iterators.get(i);
			if(it.hasNext()){
				String new_block_val = it.next();
				current_blocking_values.set(i, new_block_val);
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
	private List<String> getDemographicValues(String demographic){
		List<String> ret = null;
		// need to see if demographic refers to a datamodel table value, or an attribute
		if(demographic.contains(".")){
			// need to query the table name before the . for the field
			String[] table_field = demographic.split("\\.");
			if(table_field != null && table_field.length == 2){
				String query_text = "HQLQUERY: " + "select distinct t." + table_field[1] + " from " + table_field[0] + " t";
				System.out.println(query_text);
				Query q = sessionFactory.getCurrentSession().createQuery(query_text);
				ret = (List<String>)q.list();
			}
		} else {
			// get the various attribute values from the person attribute table
			PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(demographic);
			if(pat != null){
				// HQL query to get all values
				// something like 
				// select distinct value from person_attribute where person_attribute_type_id = <id>
				Query q = sessionFactory.getCurrentSession().createQuery("select distinct p.value from person_attribute p where p.person_attribute_type_id = :attr_id ");
				q.setInteger("attr_id", pat.getPersonAttributeTypeId());
				ret = (List<String>)q.list();
			}
		}
		return ret;
	}
	
	/**
	 * Returns a set of patient IDs for a given demographic and value
	 * 
	 * @param demographic
	 * @param value
	 * @return
	 */
	private List<Integer> getPatientIDs(String demographic, String value){
		List<Integer> ret = null;
		// get patient IDs using HQL that have this value for
		// this demographic
		if(demographic.contains(".")){
			// need to query the table name before the . for the field
			String[] table_field = demographic.split("\\.");
			if(table_field != null && table_field.length == 2){
				Query q = sessionFactory.getCurrentSession().createQuery("select distinct person_id from " + table_field[0] + " where " + table_field[1] + " = :value");
				q.setString("value", value);
				ret = (List<Integer>)q.list();
			}
		} else {
			// get the various attribute values from the person attribute table
			PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(demographic);
			Query q = sessionFactory.getCurrentSession().createQuery("select distinct p.person_id from person_attribute p where p.person_attribute_type_id = :attr_id and p.attribute = :value");
			q.setString("value", value);
			q.setInteger("attr_id", pat.getPersonAttributeTypeId());
			ret = (List<Integer>)q.list();
		}
		return ret;
	}
}
