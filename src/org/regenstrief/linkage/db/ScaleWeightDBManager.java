package org.regenstrief.linkage.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.regenstrief.linkage.analysis.SWAnalyzer.ScaleWeightSetting;
import org.regenstrief.linkage.util.DataColumn;

/**
 * This class performs database operations of a weight scaling analyzer
 * 
 * @author scentel
 */

public class ScaleWeightDBManager extends DBManager {
	
	// For better semantics
	private String token_table;
	
	public ScaleWeightDBManager(String driver, String url, String table, String user, String passwd){
		super(driver, url, table, user, passwd);
		this.token_table = table;
	}
	
	/**
	 * Checks if the token frequency exists in the database
	 * Updates the frequency or inserts a new record depending on the result
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @param token
	 * @param frequency
	 */
	public void addOrUpdateToken(DataColumn target_column, String datasource_id, String token, Integer frequency) {
		int db_frequency = getTokenFrequencyFromDB(target_column,datasource_id, token);
		// Database and memory are at the same state, we don't need to do anything
		if(db_frequency != frequency) {
			// New record, not in the database
			if(frequency == 1 || db_frequency == 0) {
				insertToken(target_column, datasource_id, token, frequency);
			}
			else {
				updateTokenFrequency(target_column, datasource_id, token, frequency);
			}
		}
	}
	
	/**
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @return The number of unique tokens in a DataColumn
	 */
	public int getDistinctRecordCount(DataColumn target_column, String datasource_id) {
		String query = "SELECT COUNT(token) FROM " + token_table + " WHERE datasource_id = " + datasource_id + " AND field_id = " + target_column.getColumnID();
		return executeQuery(query);
	}

	/**
	 * Empties the token table belonging
	 * @param target_column
	 * @param datasource_id
	 * @return
	 */
	public boolean deleteAnalysis(DataColumn target_column, String datasource_id) {
		String query = "DELETE FROM " + token_table + " WHERE datasource_id = " + datasource_id;
		return executeUpdate(query);
	}
	
	/**
	 * Inserts a new token with its frequency into where frequencies are stored
	 * 
	 * @param field The DataColumn that this token belongs
	 * @param datasource_id DataSource ID
	 * @param token
	 * @param frequency
	 * @return Whether the insert was successful or not
	 */
	public boolean insertToken(DataColumn field, String datasource_id, String token, int frequency){
		String query = "INSERT INTO " + token_table +  " VALUES (" + datasource_id + "," + field.getColumnID() + ",'" + token + "'," + frequency + ")"; 
		return executeUpdate(query);
	}
	

	/**
	 * Loads precalculated token frequencies from he database
	 * 
	 * @param target_column
	 * @param datasource_id
	 * @param topbottom Determines which values will be loaded into the hash table
	 * @param limit The parameter N, should be between 0.0 and 1.0 for percentages
	 * @return A hashtable containing frequencies, indexed by token
	 */
	public Hashtable<String,Integer> getTokenFrequenciesFromDB(DataColumn target_column, String datasource_id, ScaleWeightSetting topbottom, Float limit) {
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
				int tokens = getDistinctRecordCount(target_column, datasource_id);
				int token_limit = Math.round(tokens*limit);
				query.append(" ORDER BY frequency DESC LIMIT " + token_limit);
			}
			else {
				System.out.println("Error: N should be between 0 and 1");
			}
			break;
		case BottomNPercent:
			if(N <= 1) {
				int tokens = getDistinctRecordCount(target_column, datasource_id);
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
		
		ResultSet frequency_rs = getResultSet(query.toString());
		Hashtable<String,Integer> frequencies = new Hashtable<String,Integer>(2*N);
		try {
			while(frequency_rs != null && frequency_rs.next()) {
				String token = frequency_rs.getString(1);
				Integer frequency = frequency_rs.getInt(2);
				frequencies.put(token, frequency);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return frequencies;

	}

	/**
	 * Retrieves a token frequency from the database
	 * @param field The DataColumn that this token belongs
	 * @param id DataSource ID
	 * @param token
	 * @return
	 */
	public int getTokenFrequencyFromDB(DataColumn field, String id, String token) {
		String query = "SELECT frequency FROM " + token_table + " WHERE token = '" + token + "' AND datasource_id = " + id + " AND field_id = " + field.getColumnID();
		try{
			Statement stmt = db.createStatement();
			ResultSet rows = stmt.executeQuery(query);
			if(rows.next()) {
				int frequency = rows.getInt(1);
				// Check if more than one row is returned
				if(!rows.next()) {
					// Return frequency if only one row is returned
					return frequency;
				}
				else {
					// If more than one row is returned, it means that there is something wrong
					return -1;
				}
			}
			// ResultSet is empty, it means that token is not in the database
			else {
				return 0;
			}
		}
		catch (Exception e) {
			return -1;
		}
			
	}

	/**
	 * Updates the frequency of a token in the database
	 * 
	 * @param field DataColumn that the token belongs
	 * @param id DataSource ID
	 * @param token
	 * @param frequency
	 * @return If the update was successful or not
	 */
	
	public boolean updateTokenFrequency(DataColumn field, String id, String token, int frequency) {
		String query = "UPDATE " + token_table + " SET frequency = " + frequency + " WHERE datasource_id = " + id + " AND field_id = " + field.getColumnID() + " AND token = '" + token + "'";
		return executeUpdate(query);
	}
}
