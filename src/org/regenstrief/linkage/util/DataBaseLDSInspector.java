package org.regenstrief.linkage.util;

import java.sql.Connection;

/**
 * Class inspects a database type LinkDataSource and creates DataColumn
 * objects for it.  It does this by using the information in the
 * LinkDataSource object to connect to the database, query the table, and
 * discover the information about all the columns.
 * 
 * @author jegg
 *
 */

public class DataBaseLDSInspector implements LinkDataSourceInspector{
	
	public DataBaseLDSInspector(){
		
	}
	
	public void setDefaultDataColumns(LinkDataSource lds){
		
	}
	
	private Connection getConnection(LinkDataSource lds){
		//String driver = ;
		//String url = ;
		//String 
		return null;
	}
	
}
