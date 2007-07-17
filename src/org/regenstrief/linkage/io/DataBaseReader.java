package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.*;
import java.sql.*;
import java.util.*;

/**
 * Class opens a connection from a database to read Record objects.  For the LinkDataSource
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
	
	protected String driver, url, user, passwd, query;
	protected Connection db;
	protected ResultSet data;
	protected boolean queried;
	
	// columns queried from the data base
	List<DataColumn> incl_cols;
	
	/**
	 * Constructor parses information from the LinkDataSource
	 * object and connects to the database.
	 * 
	 * @param lds	contains the database connection information
	 */
	public DataBaseReader(LinkDataSource lds){
		super(lds);
		queried = false;
		
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
	 * ResultSet is created when object is first used, not when constructor is called.
	 * 
	 * Method separated from cosntructor since the constructQuery() method might need elements only
	 * in subclasses.  For example, OrderedDataBaseReader uses a MatchingConfig object to make the query,
	 * and this class's constructur must be called before the object is assigned in the subclass.  
	 */
	protected void getResultSet(){
		queried = true;
		try{
			query = constructQuery();
			Statement stmt = db.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			data = stmt.executeQuery(query);
		}
		catch(SQLException se){
			db = null;
		}
	}
	
	/*
	 * Construct a query based on the table name and blocking variables.
	 */
	protected String constructQuery(){
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
			query += incl_cols.get(i).getColumnID() + ", ";
		}
		
		query += incl_cols.get(incl_cols.size() - 1).getColumnID();
		query += " FROM " + data_source.getName();
		
		return query;
	}
	
	public Record nextRecord() {
		if(!queried){
			getResultSet();
		}
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
		if(!queried){
			getResultSet();
		}
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
		getResultSet();
		try{
			return data.first();
		}
		catch(SQLException sqle){
			return false;
		}
	}
}
