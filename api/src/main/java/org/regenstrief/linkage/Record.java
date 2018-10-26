package org.regenstrief.linkage;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Record stores demographics associated with one entity. The values are stored
 * in a Hashtable indexed on the type of information. For example, an examlpe of
 * a demographic for first name would be stored in the Hashtable with a key of
 * "FN" and a value of "John."
 * 
 * Unique ID should be a unique integer, such that all Records from a given
 * datasource receives a different integer ID. Also, different datasources
 * should have a unique uid_context so that same ID records from different
 * datasources will have a different context and uid pair.
 * 
 */
public class Record {

	private Hashtable<String, String> demographics;
	long uid;
	String uidContext;

	/**
	 * Initializes the demographics Hashtable and stores the unique ID and ID
	 * context
	 * 
	 */
	public Record(long id, String context) {
		demographics = new Hashtable<String, String>();
		uid = id;
		uidContext = context;
	}
	
	/**
	 * Method returns true if any of the demographics in this Record has
	 * a null or empty string as its value
	 * 
	 * @return	true if at least one demographic has no value, false if
	 * all demographics have non-empty string, non-null values
	 */
	public boolean hasNullValues(){
		Enumeration<String> e = demographics.keys();
		while(e.hasMoreElements()){
			String d = e.nextElement();
			String value = demographics.get(d);
			if(value == null || value.equals("")){
				return true;
			}
		}
		return false;
	}

	/**
	 * Value should be unique among all Records created from the same DataSource
	 * 
	 * @return the Record's unique ID
	 */
	public long getUID() {
		return uid;
	}

	/**
	 * Method returns the context for the unique ID
	 * 
	 * @return Context of unique ID
	 */
	public String getContext() {
		return uidContext;
	}

	/**
	 * Adds an entry to the demographics table with value of the value
	 * parameter. Previous values, if present, are over written.
	 * 
	 * @param demographic
	 *            the demographic of the Record to add or set
	 * @param value
	 *            the value of the demographic
	 */
	public void addDemographic(String demographic, String value) {
		if (value == null) {
			value = "";
		}
		demographics.put(demographic, value.toLowerCase());
	}

	/**
	 * Returns the Hashtable of the Record
	 * 
	 * @return the Hashtable of demographics for this Record
	 */
	public Hashtable<String, String> getDemographics() {
		return demographics;
	}

	/**
	 * Returns the value of the given demographic for this Record object
	 * 
	 * @param demographic
	 *            the name of the demographic requested
	 * @return the value of the given demographic for this object
	 */
	public String getDemographic(String demographic) {
		if (demographic == null || !demographics.containsKey(demographic)){
			return null;
		}
		return demographics.get(demographic);
	}

	/**
	 * 
	 * Determines whether or not a given demographic field is considered null.
	 * We assume that a field is null if and only if it is an empty string.
	 * 
	 * @param demographic
	 *            the name of the demographic required
	 * @return whether or not this demographic field is considered null
	 */
	public boolean isDemographicNull(String demographic) {
		return getDemographic(demographic).length() == 0;
	}
	
	/**
	 * Returns a string with each demographic name and its value
	 */
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder("Record:\n");
		for (final String k : demographics.keySet()) {
			ret.append('\t').append(k).append(": ").append(demographics.get(k)).append('\n');
		}
		return ret.toString();
	}
}
