package org.regenstrief.linkage;

import java.util.*;
/**
 * Record stores demographics associated with one entity.  The values are
 * stored in a Hashtable indexed on the type of information.  For example,
 * an examlpe of a demographic for first name would be stored in the 
 * Hashtable with a key of "FN" and a value of "John." 
 * 
 * Unique ID should be a unique integer, such that all Records from a given
 * datasource receives a different integer ID.  Also, different datasources
 * should have a unique uid_context so that same ID records from different
 * datasources will have a different context and uid pair. 
 *
 */
public class Record {
	
	Hashtable<String,String> demographics;
	int uid;
	String uidContext;
	
	/**
	 * Initializes the demographics Hashtable and stores the unique ID and ID context
	 *
	 */
	public Record(int id, String context){
		demographics = new Hashtable<String,String>();
		uid = id;
		uidContext = context;
	}
	
	/**
	 * Value should be unique among all Records created from the same DataSource
	 * 
	 * @return	the Record's unique ID
	 */
	public int getUID(){
		return uid;
	}
	
	/**
	 * Method returns the context for the unique ID
	 * 
	 * @return	Context of unique ID
	 */
	public String getContext(){
		return uidContext;
	}
	
	/**
	 * Adds an entry to the demographics table with value of the value
	 * parameter.  Previous values, if present, are over written.
	 * 
	 * @param demographic	the demographic of the Record to add or set
	 * @param value	the value of the demographic
	 */
	public void addDemographic(String demographic, String value){
		if(value == null){
			value = "";
		}
		demographics.put(demographic, value);
	}
	
	/**
	 * Returns the Hashtable of the Record
	 * 
	 * @return	the Hashtable of demographics for this Record
	 */
	public Hashtable<String,String> getDemographics(){
		return demographics;
	}
	
	/**
	 * Returns the value of the given demographic for this Record object
	 * 
	 * @param demographic	the name of the demographic requested
	 * @return	the value of the given demographic for this object
	 */
	public String getDemographic(String demographic){
		return demographics.get(demographic);
	}
	
	/**
	 * Returns a string with each demographic name and its value
	 */
	public String toString(){
		String ret = new String("Record:\n");
		Iterator<String> it = demographics.keySet().iterator();
		while(it.hasNext()){
			String k = it.next();
			ret += "\t" + k + ": " + demographics.get(k) + "\n";
		}
		return ret;
	}
	
}
