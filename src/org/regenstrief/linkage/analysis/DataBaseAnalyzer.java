package org.regenstrief.linkage.analysis;
import java.sql.*;
import java.util.Hashtable;

import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.io.DataSourceReader.Job;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/*
 * @author sarpc
 * Used to analyze databases
 */

public class DataBaseAnalyzer extends DataSourceAnalyzer {
	
	private String table_name;
	private DataBaseReader reader;
	
	public DataBaseAnalyzer(LinkDataSource lds, MatchingConfig mc, LinkDBManager ldbm) {
		super(ldbm);
		reader = new DataBaseReader(lds,mc,Job.Analysis);
		table_name = reader.data_source.getName();
	}
	
	public int getNullCount(DataColumn target_column) {
		String query ="SELECT COUNT(*) FROM " + table_name + " WHERE " + target_column.getName() + " IS NULL";
		return reader.getQueryResult(query);
	}
	
	public int getNonNullCount(DataColumn target_column) {
		String query ="SELECT COUNT(*) FROM " + table_name + " WHERE " + target_column.getName() + " IS NOT NULL";
		return reader.getQueryResult(query);
	}
	
	public int getRecordCount() {
		String query = "SELECT COUNT(*) FROM " + table_name;
		return reader.getQueryResult(query);
	}
	
	public void setNullCount(DataColumn target_column) {
		target_column.setNullCount(getNullCount(target_column));
	}
	
	public void setNonNullCount(DataColumn target_column) {
		target_column.setNonNullCont(getNonNullCount(target_column));
	}
	
	public void setRecordCount() {
		reader.data_source.setRecordCount(getRecordCount());
	}	
	
	public Hashtable<String,Integer> getTokenFrequencies(DataColumn target_column) {
		String column_name = target_column.getName();
		String query = "SELECT DISTINCT " + column_name + " AS token, COUNT(" + column_name + ") AS frequency FROM " + table_name + " GROUP BY " + column_name + " ORDER BY frequency DESC";
		Hashtable<String,Integer> frequencies = new Hashtable<String,Integer>();
		try{
			Statement stmt = reader.db.createStatement();
			ResultSet rows = stmt.executeQuery(query);
			while(rows.next()){
				String token = rows.getString(1);
				Integer frequency = rows.getInt(2);
				frequencies.put(token,frequency);
				sw_connection.insertToken(target_column,reader.data_source.getDataSource_ID(),token,frequency);
			}
		}
		catch(SQLException sqle){
			return null;
		}
		return frequencies;
	}
}