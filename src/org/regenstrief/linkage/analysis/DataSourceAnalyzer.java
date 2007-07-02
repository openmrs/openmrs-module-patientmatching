package org.regenstrief.linkage.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.DataColumn;

/**
 * @author sarpc
 * Abstract class that analyzers should extend
 * 
 * TODO: Test with PostgreSQL
 */

public abstract class DataSourceAnalyzer {
	
	// Connection to the database where token frequencies will be stored
	public LinkDBManager sw_connection;
	// Determines if bottom/top N frequencies will be loaded
	public enum ScaleWeightSetting { TopN, BottomN, TopNPercent, BottomNPercent, AboveN, BelowN}
	protected DataSourceReader reader;
	protected String token_table;
	protected String datasource_id;
	
	public DataSourceAnalyzer(LinkDBManager ldbm) {
		this.sw_connection = ldbm;
		this.token_table = sw_connection.getTable();
	}
	
	/**
	 * Loads precalculated token frequencies from a relational database
	 * 
	 * @return A hashtable containing frequencies, indexed by token
	 */
	public Hashtable<String,Integer> getTokenFrequencies(DataColumn target_column, ScaleWeightSetting topbottom, Float limit) {
		StringBuilder query = new StringBuilder("SELECT token, frequency FROM " + token_table);
		Integer N = Math.round(limit);
		switch (topbottom) {
		case BottomN:
			query.append(" ORDER BY frequency ASC LIMIT " + N);
			break;
		case TopN:
			query.append(" ORDER BY frequency DESC LIMIT " + N);
			break;
		case TopNPercent:
			// Maybe an exception here?
			if(N <= 1) {
				int tokens = getDistinctRecordCount(target_column);
				int token_limit = Math.round(tokens*limit);
				query.append(" ORDER BY frequency DESC LIMIT " + token_limit);
			}
			else {
				System.out.println("Error: N should be between 0 and 1");
			}
			break;
		case BottomNPercent:
			if(N <= 1) {
				int tokens = getDistinctRecordCount(target_column);
				int token_limit = Math.round(tokens*limit);
				query.append(" ORDER BY frequency ASC LIMIT " + token_limit);
			}
			else {
				System.out.println("Error: N should be between 0 and 1");
			}
			break;
		case AboveN:
			query.append(" WHERE frequency > " + N);
			break;
		case BelowN:
			query.append(" WHERE frequency < " + N);
			break;
		}
		
		ResultSet frequency_rs = sw_connection.getResultSet(query.toString());
		Hashtable<String,Integer> frequencies = new Hashtable<String,Integer>(2*N);
		try {
			while(frequency_rs != null && frequency_rs.next()) {
				String token = frequency_rs.getString(1);
				Integer frequency = frequency_rs.getInt(2);
				System.out.println(token + " :: " + frequency);
				frequencies.put(token, frequency);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return frequencies;
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
	
	public int getDistinctRecordCount(DataColumn target_column) {
		String query = "SELECT COUNT(token) FROM " + token_table + " WHERE datasource_id = " + datasource_id + " AND field_id = " + target_column.getColumnID();
		return sw_connection.executeQuery(query);
	}

	public boolean deleteAnalysis(DataColumn target_column) {
		String query = "DELETE FROM " + token_table + " WHERE datasource_id = " + datasource_id;
		return sw_connection.executeUpdate(query);
	}
	
	public void addOrUpdateToken(DataColumn target_column, String datasource_id, String token, Integer frequency) {
		int db_frequency = sw_connection.getTokenFrequency(target_column,datasource_id, token);
		// Database and memory are at the same state, we don't need to do anything
		if(db_frequency != frequency) {
			// New record, not in the database
			if(frequency == 1 || db_frequency == 0) {
				sw_connection.insertToken(target_column, datasource_id, token, frequency);
			}
			else {
				sw_connection.updateTokenFrequency(target_column, datasource_id, token, frequency);
			}
		}
	}
	
	public void setNonNullCount(DataColumn target_column) {
		target_column.setNonNullCont(getNonNullCount(target_column));
	}
	
	public void setNullCount(DataColumn target_column) {
		target_column.setNullCount(getNullCount(target_column));
	}
	
	public void setRecordCount() {
		reader.data_source.setRecordCount(getRecordCount());
	}
}
