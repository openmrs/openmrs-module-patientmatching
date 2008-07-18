package org.regenstrief.linkage.util;

/*
 * Class holds the column information for the linkage data source.
 * The columns are referenced by the index of the columns in the
 * data source, since that index will not change.
 */

import java.util.*;

public class LinkDataSource implements Cloneable {
	private String source_name;
	private String type;
	private String access;
	private String unique_id_field;
	private int ds_id;
	
	public static final int UNKNOWN_REC_COUNT = -1;
	public static final int UNKNOWN_DS_ID = -1;
	
	private List<DataColumn> column_settings;
	
	public LinkDataSource(String name, String type, String access, int id){
		this.source_name = name;
		this.type = type;
		this.access = access;
		column_settings = new ArrayList<DataColumn>();
		ds_id = id;
	}
		
	/*
	 * Method added to conveniently go from a list of columns by name,
	 * such as would be returned from a matching config object, and
	 * returns the display position of them.  Method uses lds1
	 * since lds2 might not be present if linking between same file
	 */
	public int[] getIncludeIndexesOfColumnNames(String[] names){
		int[] ret = new int[names.length];
		for(int i = 0; i < names.length; i++){
			ret[i] = getDisplayPositionByName(names[i]);
		}
		return ret;
	}
	
	public String getUniqueID(){
		return unique_id_field;
	}
	
	public void setUniqueID(String column_name){
		unique_id_field = column_name;
	}
	
	public void setSourceName(String name){
		source_name = name;
	}
	
	public String getName(){
		return source_name;
	}
	
	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getType() {
		return type;
	}
	
	public int getDisplayPositionByName(String name){
		Iterator<DataColumn> it = column_settings.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getName().equals(name)){
				return dc.getIncludePosition();
			}
		}
		return DataColumn.INCLUDE_NA;
	}
	
	public String getColumnIDByName(String name){
		Iterator<DataColumn> it = column_settings.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getName().equals(name)){
				return dc.getColumnID();
			}
		}
		return null;
	}
	
	public int getColumnTypeByName(String name){
		Iterator<DataColumn> it = column_settings.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getName().equals(name)){
				return dc.getType();
			}
		}
		return DataColumn.STRING_TYPE;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public DataColumn getDataColumnByIncludePosition(int column_id) {
		Iterator<DataColumn> it = column_settings.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() == column_id){
				return dc;
			}
		}
		return null;
	}
	
	public DataColumn getDataColumn(int data_position){
		return column_settings.get(data_position);
	}
	
	public void addDataColumn(DataColumn dc){
		column_settings.add(dc);
	}
	
	public void addNewDataColumn(String col_id){
		DataColumn dc = new DataColumn(col_id);
		addDataColumn(dc);
	}
	
	public List<DataColumn> getDataColumns(){
		return column_settings;
	}
	
	/**
	 * Returns the DataColumns that have an include position
	 * @return A Hashtable of DataColumns, indexed by column name
	 */
	public Hashtable<String, DataColumn> getIncludedDataColumns(){
		Hashtable<String, DataColumn> columns = new Hashtable<String, DataColumn>(column_settings.size());
		Iterator<DataColumn> it = column_settings.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA) {
				columns.put(dc.getName(), dc);
			}
		}
		return columns;
	}
	
	/*
	 * Table of only included DataColumns that have weight scaling
	 * @param mc
	 * @return A table indexed by column names
	 
	public Hashtable<String, DataColumn> getScaleWeightDataColumns(MatchingConfig mc) {
		Hashtable<String, DataColumn> columns = new Hashtable<String, DataColumn>();
		Iterator<DataColumn> it = column_settings.iterator();
		Hashtable<String, Boolean> is_scaleweight = mc.getScaleWeightorNotTable(); 
		while(it.hasNext()){
			DataColumn dc = it.next();
			String col_name = dc.getName();
			// if it is a scale weight column
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA && is_scaleweight.get(col_name)) {
				columns.put(col_name, dc);
			}
		}
		return columns;
	}
	*/
	/*
	 * returns the number of columns that are displayed and re-written
	 * to the new file for linkage
	 */
	public int getIncludeCount(){
		Iterator<DataColumn> it = column_settings.iterator();
		int total = 0;
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				total++;
			}
		}
		return total;
	}
	
	/**
	 * Generates a link between name of the DataColumn and its type (String, Numeric etc.)
	 * @return
	 */
	public Hashtable<String, Integer> getTypeTable(){
		Hashtable<String, Integer> type_table = new Hashtable<String, Integer>();
		List<DataColumn> dc = getDataColumns();
		Iterator<DataColumn> it = dc.iterator();
		while(it.hasNext()){
			DataColumn d = it.next();
			if(d.getIncludePosition() != DataColumn.INCLUDE_NA){
				type_table.put(d.getName(), Integer.valueOf(d.getType()));
			}
		}
		return type_table;
	}

	public int getDataSource_ID() {
		return ds_id;
	}

	public void setDataSource_ID(int ds_id) {
		this.ds_id = ds_id;
	}
    
    public Object clone() {
        LinkDataSource linkDataSource = null;
        
        try {
            linkDataSource = (LinkDataSource) super.clone();
            
            linkDataSource.column_settings = new ArrayList<DataColumn>();
            for (DataColumn column: this.column_settings) {
                DataColumn dataColumn = (DataColumn) column.clone();
                linkDataSource.column_settings.add(dataColumn);
            }
            
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        
        return linkDataSource;
    }
}
