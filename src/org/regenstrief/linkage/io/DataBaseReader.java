package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.*;
import java.sql.*;
import java.util.*;

/**
 * Class opens a connection from a database to read Record objects.  Sorting
 * on the blocking variables is done within the SQL query.  For the LinkDataSource
 * object, it is assumed that the access variable is a comma delimited string
 * with the parts:
 * 	Driver - the JDBC driver class name
 * 	URL - the connection URL to the database
 * 	User - the database connection user
 * 	Password - the user's password
 * 
 * The driver class needs to be in the classpath when the program is run.
 *
 */
public class DataBaseReader extends DataSourceReader {
	
	String driver, url, user, passwd, query;
	Connection db;
	ResultSet data;
	
	// columns queried from the data base
	List<DataColumn> incl_cols;
	
	/**
	 * Constructor parses information from the LinkDataSource object and MatchingConfig
	 * object, construct the query, connects to the database, and gets a ResultSet to
	 * iterate over to create the Record objects.
	 * 
	 * @param lds	contains the database connection information
	 * @param mc	contains blocking variable information required in query construction
	 */
	public DataBaseReader(LinkDataSource lds, MatchingConfig mc){
		super(lds, mc);
		query = constructQuery();
		// parse the string in access variable to get driver and URL info
		// then create DB connection
		// decide how to handle different failures later
		try{
			String[] access = lds.getAccess().split(",");
			driver = access[0];
			url = access[1];
			user = access[2];
			passwd = access[3];
			
			Class.forName(driver);
			db = DriverManager.getConnection(url, user, passwd);
			Statement stmt = db.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			data = stmt.executeQuery(query);
		}
		catch(ArrayIndexOutOfBoundsException aioobe){
			db = null;
		}
		catch(ClassNotFoundException cnfe){
			db = null;
		}
		catch(SQLException se){
			db = null;
		}
		
		
		
	}
	
	/*
	 * Construct a query based on the table name and blocking variables.
	 */
	private String constructQuery(){
		String query = new String("SELECT ");
		incl_cols = new ArrayList<DataColumn>();
		Iterator<DataColumn> it = data_source.getDataColumns().iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				incl_cols.add(dc);
			}
		}
		
		for(int i = 0; i < incl_cols.size() - 1; i++){
			query += incl_cols.get(i).getName() + ", ";
		}
		
		query += incl_cols.get(incl_cols.size() - 1).getName();
		query += " FROM " + data_source.getName();
		query += " ORDER BY ";
		String[] b_columns = mc.getBlockingColumns();
		for(int i = 0; i < b_columns.length - 1; i++){
			query += b_columns[i] + ", ";
		}
		query += b_columns[b_columns.length - 1];
		return query;
	}
	
	/*
	 * Uses a query to get the record size of the data source.
	 */
	public int getRecordSize(){
		String query = "SELECT count(*) from " + data_source.getName();
		try{
			Statement stmt = db.createStatement();
			ResultSet count = stmt.executeQuery(query);
			return count.getInt(1);
		}
		catch(SQLException sqle){
			return -1;
		}
		
	}
	
	public Record nextRecord() {
		try{
			if(data.next()){
				Record ret = new Record();
				for(int i = 0; i < incl_cols.size(); i++){
					String demographic = data.getString(i+1);
					ret.addDemographic(incl_cols.get(i).getName(), demographic);
				}
				return ret;
			} else {
				return null;
			}
		}
		catch(SQLException sqle){
			return null;
		}
		
	}
	
	public boolean hasNextRecord() {
		try{
			if(db != null){
				return !data.isLast();
			} else {
				return false;
			}
		}
		catch(SQLException sqle){
			return false;
		}
	}

	public boolean reset(){
		try{
			return data.first();
		}
		catch(SQLException sqle){
			return false;
		}
	}
}
