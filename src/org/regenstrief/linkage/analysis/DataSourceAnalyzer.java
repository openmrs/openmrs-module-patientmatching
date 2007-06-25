package org.regenstrief.linkage.analysis;

import java.util.Hashtable;

import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.util.DataColumn;
/*
 * @author sarpc
 * Abstract class for analyzers
 * 
 * TODO: Add more methods
 */

public abstract class DataSourceAnalyzer {
	
	// Connection to the database where token frequencies will be stored
	public LinkDBManager sw_connection;
	
	public DataSourceAnalyzer(LinkDBManager ldbm) {
		this.sw_connection = ldbm;
	}
	
	public abstract Hashtable<String,Integer> getTokenFrequencies(DataColumn target_column);
	
}
