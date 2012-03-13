package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/** 
 * Class return Record pairs based on querying the OpenMRS person tables.
 * 
 * Class takes the given MatchingConfig, queries the table to get blocking demographic
 * values, and uses the cohort builder to retrieve sets of patients to form pairs.
 * 
 * As in other parts of the module, a demographic name with a "." inside it
 * is taken to refer to an OpenMRS data model table instead of an explicit
 * person attribute name.
 * 
 * @author jegg
 *
 */

public class OpenMRSFormPairs extends FormPairs{
	
	private SessionFactory sessionFactory;
	private Iterator<String> value_iterator;
	private Iterator<Integer[]> pair_iterator;
	
	public OpenMRSFormPairs(MatchingConfig mc){
		super(mc);
		String[] blocking_cols = mc.getBlockingColumns();
		List<String> values = getDemographicValues(blocking_cols[0]);
		
		// iterator to store which value we're on
		value_iterator = values.iterator();
		
		// initialize iterators and get ready for first getNextRecordPair call
		String first_value = value_iterator.next();
		List<Integer> ids = getPatientIDs(blocking_cols[0], first_value);
		List<Integer[]> pairs;
		while((pairs = createPairs(ids)) == null){
			ids = getPatientIDs(blocking_cols[0], value_iterator.next());
		}
		pair_iterator = pairs.iterator();
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
				Query q = sessionFactory.getCurrentSession().createQuery("select distinct t." + table_field[1] + " from " + table_field[0] + " t");
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
	
	/**
	 * Method takes the set of person IDs that have the same blocking column values and forms a complet set
	 * of pairs between them, returning a list of Integer arrays.  If a list of 1, 2, 3, 4 is given, then
	 * a list of:
	 * 1, 2
	 * 1, 3
	 * 1, 4
	 * 2, 3
	 * 2, 4
	 * 3, 4
	 * 
	 * is returned.
	 * 
	 * @param set_IDs
	 * @return
	 */
	private List<Integer[]> createPairs(List<Integer> set_IDs){
		if(set_IDs.size() > 1){
			List<Integer[]> ret = new ArrayList<Integer[]>();
			for(int i = 0; i < set_IDs.size(); i++){
				Integer left = set_IDs.get(i);
				for(int j = i+1; j < set_IDs.size(); j++){
					Integer right = set_IDs.get(j);
					Integer[] pair = {left, right};
					ret.add(pair);
				}
			}
			return ret;
		} else {
			return null;
		}
		
	}
	
	public Record[] getNextRecordPair(){
		if(pair_iterator.hasNext()){
			Integer[] to_return = pair_iterator.next();
			Record[] ret = new Record[2];
			// convert from openmrs ID integers to Record objects
			Patient p = Context.getPatientService().getPatient(to_return[0]);
			ret[0] = LinkDBConnections.getInstance().patientToRecord(p);
			p = Context.getPatientService().getPatient(to_return[1]);
			ret[1] = LinkDBConnections.getInstance().patientToRecord(p);
			
			return ret;
		} else {
			//increment to next value
			if(value_iterator.hasNext()){
				String val = value_iterator.next();
				List<Integer> ids = getPatientIDs(mc.getBlockingColumns()[0], val);
				List<Integer[]> id_pairs = createPairs(ids);
				pair_iterator = id_pairs.iterator();
				return getNextRecordPair();
			} else {
				// at the end of blocking variable values
				return null;
			}
		}
		
	}
}
