package org.regenstrief.linkage.analysis;

/**
 * Class stores the demographic name the object is concerned with,
 * the value the demographic must have, and the action to take if
 * the demographic has the specified value.
 * 
 * One example of use would be:
 * 	demographic - FirstName
 * 	value - NONAME
 * 	action - discard
 * 
 * Comparisons are case insensitive.
 */

import org.regenstrief.linkage.*;

public class DemographicValueAction {
	
	public static final int CONTAINS = 1;
	
	public static final int EQUALS = 2;
	
	public static final int MATCHES = 3;
	
	private String demographic, value;
	
	private int action, type;
	
	public DemographicValueAction(String demographic, String value, int action, int type) {
		this.demographic = demographic;
		this.value = value;
		this.action = action;
		this.type = type;
	}
	
	public boolean recordMatches(Record r) {
		String val = r.getDemographic(demographic);
		if (val == null) {
			return false;
		}
		switch (type) {
			case (CONTAINS):
				if (val.toLowerCase().contains(value.toLowerCase())) {
					return true;
				}
				break;
			case (EQUALS):
				if (val.toLowerCase().equals(value.toLowerCase())) {
					return true;
				}
				break;
			case (MATCHES):
				if (val.toLowerCase().matches(value.toLowerCase())) {
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}
	
	public String getDemographic() {
		return demographic;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getAction() {
		return action;
	}
}
