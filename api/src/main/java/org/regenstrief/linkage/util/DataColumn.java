package org.regenstrief.linkage.util;

public class DataColumn implements Cloneable {
	
	private int include_position;
	
	// Original column name in a database, or its order in a CharDelimFile
	private String column_id;
	
	// Column label
	private String name;
	
	private int type;
	
	public final static int STRING_TYPE = MatchingConfig.STRING_TYPE;
	
	public final static int NUMERIC_TYPE = MatchingConfig.NUMERIC_TYPE;
	
	public final static int INCLUDE_NA = -1;
	
	public final static int UNKNOWN_COUNT = -1;
	
	public DataColumn(String column_id) {
		// Constructor requires a data position field, 
		// data position is the one field of the object
		// that should not change
		this.column_id = column_id;
	}
	
	public int getIncludePosition() {
		return include_position;
	}
	
	public void setIncludePosition(int include_position) {
		this.include_position = include_position;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getColumnID() {
		return column_id;
	}
	
	public Object clone() {
		DataColumn dataColumn = null;
		
		try {
			dataColumn = (DataColumn) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return dataColumn;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
