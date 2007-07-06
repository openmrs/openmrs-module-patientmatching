package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.db.ScaleWeightDBManager;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.DataColumn;

/**
 * @author scentel
 * Abstract class that scale weight analyzers should extend
 * 
 * TODO: Test with PostgreSQL
 */

public abstract class SWAnalyzer {
	
	// Connection to the database where token frequencies will be stored
	protected ScaleWeightDBManager  sw_connection;
	// Determines if bottom/top N frequencies will be loaded
	public enum ScaleWeightSetting { TopN, BottomN, TopNPercent, BottomNPercent, AboveN, BelowN}
	protected DataSourceReader reader;
	protected String token_table;
	protected String datasource_id;
	
	public SWAnalyzer(String access_parameter, String token_table) {
		this.token_table = token_table;
		String [] access = access_parameter.split(",");
		sw_connection = new ScaleWeightDBManager(access[0], access[1], token_table, access[2], access[3]);
		sw_connection.connect();
	}
	
	/**
	 * 
	 * @param target_column
	 * @param record_limit Determines the number of record that will be stored in memory
	 */
	public abstract void analyzeTokenFrequencies(DataColumn target_column, int record_limit);
	public abstract int getRecordCount();
	public abstract int getNonNullCount(DataColumn target_column);
	public abstract int getNullCount(DataColumn target_column);
		
	public void setNonNullCount(DataColumn target_column) {
		target_column.setNonNullCont(getNonNullCount(target_column));
	}
	
	public void setNullCount(DataColumn target_column) {
		target_column.setNullCount(getNullCount(target_column));
	}
	
	public void setRecordCount() {
		reader.data_source.setRecordCount(getRecordCount());
	}
	
	public void setUniqueNonNullCount(DataColumn target_column) {
		target_column.setUnique_non_null(sw_connection.getDistinctRecordCount(target_column, datasource_id));
	}
}
