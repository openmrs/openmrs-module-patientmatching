package org.regenstrief.linkage.util;

public class DataColumn {
	private int include_position;
	private String column_id;
	private String name;
	private int type, n_non_null, n_null;
	
	public final static int STRING_TYPE = MatchingConfig.STRING_TYPE;
	public final static int NUMERIC_TYPE = MatchingConfig.NUMERIC_TYPE;
	
	public final static int INCLUDE_NA = -1;
	public final static int UNKNOWN_COUNT = -1;
	
	public DataColumn(String column_id){
		// Constructor requires a data position field, 
		// data position is the one field of the object
		// that should not change
		this.column_id = column_id;
		n_non_null = UNKNOWN_COUNT;
		n_null = UNKNOWN_COUNT;
	}
	
	public int getNullCount(){
		return n_null;
	}
	
	public void setNullCount(int count){
		n_null = count;
	}
	
	public int getNonNullCount(){
		return n_non_null;
	}
	
	public void setNonNullCont(int count){
		n_non_null = count;
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
	
	
}
