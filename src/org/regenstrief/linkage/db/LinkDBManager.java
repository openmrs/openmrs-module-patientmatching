package org.regenstrief.linkage.db;

/**
 * Class connects to a record linkage database.  The database is
 * assumed to have one table with no normalization.  When
 * finding matches, a query result set just iterates over the table.
 * 
 * The major operations that the class needs to perform on
 * the database are:
 * 	1.  Insert a new record
 * 	2.  Delete a record
 * 	3.  Merge updated demographic information with an already
 * 		existing record
 * 
 * The constructor requires a link data source describing the
 * database access information and a matching config object
 * to describe the analytic choices when finding matches within
 * the database.
 *
 */

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.util.*;

import java.sql.*;
import java.util.*;

public class LinkDBManager {
	String driver, url, table, user, passwd;
	LinkDataSource lds;
	Connection db;
	
	/**
	 * Constructor parses the database connection information from the LinkDataSource
	 * object and uses the given MatchingConfig object's analytical options
	 * when the findMatch is called when a Record is added to the database.
	 * 
	 * @param lds	contains a description of the connection information for the database
	 * @param mc	rules to use when determining if a record already exists
	 */
	public LinkDBManager(LinkDataSource lds, MatchingConfig mc){
		this.table = lds.getName();
		this.lds = lds;
		String[] access = lds.getAccess().split(",");
		driver = access[0];
		url = access[1];
		user = access[2];
		passwd = access[3];
	}
	
	public LinkDBManager(String driver, String url, String table, String user, String passwd){
		this.driver = driver;
		this.url = url;
		this.table = table;
		this.user = user;
		this.passwd = passwd;
	}
	
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
	 * Method adds the given Record object to the linkage database.  
	 * It first checks if the database's linkage analytics 
	 * finds a match within the database.  If there isn't, 
	 * then it adds the record using addRecordToDB
	 * 
	 * @param r	the new Record object needed in the table
	 * @return	true if the Record was successfully added, false if not
	 */
	public boolean addRecord(Record r){
		//Record match = findMatchInDB(r);
		Record match = null;
		if(match == null){
			return addRecordToDB(r);
		}
		
		return false;
	}
	
	/**
	 * Method removes all database row that matches this Record object.  Method
	 * checks for multiple rows that contain the Record's information.  The
	 * unique demographic parameter signifies what demographic is globally unique
	 * among the objects represented in the database.
	 * 
	 * @param r	the Record object that needs its database representation removed
	 * @param key_demographic the demographic in the Record that uniquely identifies the object
	 * @return	the number of rows deleted from the database
	 */
	public int deleteRecord(Record r, String key_demographic){
		String query = new String();
		query += "DELETE FROM " + table + " where " + key_demographic + " = ";
		int deleted;
		String value = r.getDemographic(key_demographic);
		query += "'" + value + "'";
		
		try{
			Statement stmt = db.createStatement();
			deleted = stmt.executeUpdate(query);
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
			return 0;
		}
		
		return deleted;
	}
	
	/**
	 * Method merges the information in new_record to the database
	 * given that the information in the original Record is in the
	 * database already and needs to be preserved.  The new record
	 * is understood to contain alternate demographic information
	 * for the same person/thing represented in original.
	 * 
	 * @param original
	 * @param new_record
	 * @return true if merge was successful, false if otherwise
	 */
	public boolean mergeRecord(Record original, Record new_record){
		return false;
	}
	
	/**
	 * Method immediately adds the given record to the linkage database.
	 * It's called by addRecord if that method decides to add the object.
	 * 
	 * @param r	the Record object to add
	 * @return	boolean indicating success of insertion
	 */
	private boolean addRecordToDB(Record r){
		String query = new String();
		String columns = new String("(");
		String values = new String("('");
		int update = 0;
		
		query += "INSERT INTO " + table + " ";
		Iterator<String> it = r.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String value = r.getDemographic(demographic);
			if(it.hasNext()){
				columns += demographic + ",";
				values += value + "','";
			} else {
				columns += demographic + ")";
				values += value + "')";
			}
		}
		
		query += columns + " VALUES " + values;
		try{
			Statement stmt = db.createStatement();
			update = stmt.executeUpdate(query);
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
			return false;
		}
		
		if(update > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return	the name of the table in the database with the Record objects
	 */
	public String getTable(){
		return table;
	}
	
	/**
	 * Method creates the table as described in the LinkDataSource.  Since this class
	 * provides a means of adding Records, it's possible the table starts out empty or
	 * does not exist.
	 * 
	 * @return	true if the table was created, false if there was an error
	 */
	public boolean createTable(){
		
		return false;
	}
}
