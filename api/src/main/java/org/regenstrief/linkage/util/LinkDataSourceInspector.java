package org.regenstrief.linkage.util;

/**
 * Interface defines the methods to inspect a LinkDataSource and create default DataColumns. For
 * example, it would connect to the database when the LinkDataSource is a DataBase type and read the
 * column names.
 * 
 * @author jegg
 */

public interface LinkDataSourceInspector {
	
	/**
	 * Method takes the given LinkDataSource, removes the existing DataColumns (if present) and adds
	 * default ones based on the LinkDataSource object
	 * 
	 * @param lds the object to inspect and modify
	 */
	public void setDefaultDataColumns(LinkDataSource lds);
}
