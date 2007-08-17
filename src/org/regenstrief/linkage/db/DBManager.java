package org.regenstrief.linkage.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base class to be extended for database operations
 * 
 * @author scentel
 *
 */

public class DBManager {
	
	protected String driver, url, user, passwd;
	protected Connection db;
	
	/**
	 * Empty constructor needed by RecordDBManager
	 */ 
	public DBManager() {
	}
	
	/** 
	 * @param driver Example: com.mysql.jdbc.Driver
	 * @param url Example: jdbc:mysql://localhost/patientmatching_datasource_analysis
	 * @param table Table in the database
	 * @param user Database username
	 * @param passwd Database password
	 */
	public DBManager(String driver, String url, String user, String passwd){
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.passwd = passwd;
	}
	
	/**
	 * Opens a database connection
	 * @return True if a connection is established
	 */
	public boolean connect(){
		try{
			Class.forName(driver);
			db = DriverManager.getConnection(url, user, passwd);
		}
		catch(ClassNotFoundException cnfe){
			db = null;
			return false;
		}
		catch(SQLException se){
			db = null;
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the database connection
	 */
	public void disconnect(){
		try{
			db.close();
		}
		catch(SQLException sqle){
			
		}
	}
	
	/**
	 * Executes a SELECT query
	 * @param query
	 * @return
	 */
	protected ResultSet getResultSet(String query) {
		try {
			Statement stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			return rs;
		}
		catch(SQLException sqle) {
			return null;
		}
	}
	
	/**
	 * To be used for queries that will result in a number
	 * 
	 * @param query
	 * @return Number in the first column of the query result
	 */
	protected int executeQuery(String query) {
		try {
			Statement stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			return rs.getInt(1);
		}
		catch(SQLException sqle) {
			return -1;
		}
	}
	
	/**
	 * Executes an update query (INSERT, UPDATE, DELETE) 
	 * 
	 * @param query
	 * @return True if one or more rows are effected by the query
	 */
	protected boolean executeUpdate(String query) {
		int updated_rows = 0;
		try{
			Statement stmt = db.createStatement();
			updated_rows = stmt.executeUpdate(query);
			stmt.close();
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
			return false;
		}
		
		if(updated_rows > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * Method executes a general, prepared statement as an
	 * update.
	 * 
	 * @param ps	the prepared statement to execute
	 * @return	true if no exceptions occurred and execution indicates
	 * rows changed
	 */
	protected boolean executeUpdate(PreparedStatement ps){
		try{
			if(ps.executeUpdate() == 1){
				return true;
			} else {
				return false;
			}
		}
		catch(SQLException sqle){
			return false;
		}
	}
}
