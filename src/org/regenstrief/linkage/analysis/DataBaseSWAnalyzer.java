package org.regenstrief.linkage.analysis;

import java.sql.*;
import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * @author scentel
 * Used to analyze databases
 * 
 * TODO: Test with PostgreSQL
 */

public class DataBaseSWAnalyzer extends SWAnalyzer {
	
	private DataBaseReader db_reader;
	private String data_table;
	
	public DataBaseSWAnalyzer(LinkDataSource lds, String access, String token_table) {
		super(access, token_table);
		reader = new DataBaseReader(lds);
		datasource_id = reader.data_source.getDataSource_ID();
		db_reader = (DataBaseReader) reader;
		data_table = lds.getName();
	}
	
	public int getNullCount(DataColumn target_column) {
		String query ="SELECT COUNT(*) FROM " + data_table + " WHERE " + target_column.getName() + " IS NULL";
		//return db_reader.getQueryResult(query);
		return 0;
	}
	
	public int getNonNullCount(DataColumn target_column) {
		String query ="SELECT COUNT(*) FROM " + data_table + " WHERE " + target_column.getName() + " IS NOT NULL";
		//return db_reader.getQueryResult(query);
		return 0;
	}
	
	public int getRecordCount() {
		String query = "SELECT COUNT(*) FROM " + data_table;
		//return db_reader.getQueryResult(query);
		return 0;
	}
	
	public int getUniqueRecordCount(DataColumn target_column) {
		String query = "SELECT COUNT(DISTINCT (" + target_column.getName() + ")) FROM " + data_table;
		//return db_reader.getQueryResult(query);
		return 0;
	}
	
	public void analyzeTokenFrequencies(DataColumn target_column, int record_limit) {
		int upper_limit = getUniqueRecordCount(target_column);
		String column_name = target_column.getName();
		for(int offset=0; offset < upper_limit ; offset = offset + record_limit ) {
			// Use here a StringBuilder instead?
			String query = "SELECT DISTINCT " + column_name + " AS token, COUNT(" + column_name + ") AS frequency FROM " + data_table + " GROUP BY " + column_name + " LIMIT " + offset + "," + record_limit;
			
			/* try{
				Statement stmt = db_reader.db.createStatement();
				ResultSet rows = stmt.executeQuery(query);
				while(rows.next()){
					String token = rows.getString(1);
					Integer frequency = rows.getInt(2);
					sw_connection.addOrUpdateToken(target_column, datasource_id, token, frequency);
				}
			}
			catch(SQLException sqle){
				System.out.println("Error: " + sqle.getStackTrace());
			}
			*/
		}
	}
}