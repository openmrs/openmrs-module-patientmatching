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
	protected PreparedStatement pstmt;
	protected boolean ready;
	protected boolean read_record;
	protected Record next_record;
	
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
		ready = false;
		
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
			db.setReadOnly(true);
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
		
		try{
			if(!ready){
				ready = true;
				query = constructQuery();
				pstmt = db.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			}
			
			data = pstmt.executeQuery();
			if(data.next()){
				parseDataBaseRow();
			} else {
				next_record = null;
			}
			
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
		if(!ready){
			getResultSet();
		}
		if(!read_record){
			read_record = true;
			return next_record;
		} else {
			try{
				if(data.next()){
					parseDataBaseRow();
					read_record = true;
					return next_record;
				} else {
					return null;
				}
			}
			catch(SQLException sqle){
				return null;
			}
			
		}
		
	}
	
	protected void parseDataBaseRow(){
		try{
			next_record = new Record();
			for(int i = 0; i < incl_cols.size(); i++){
				String demographic = data.getString(i+1);
				next_record.addDemographic(incl_cols.get(i).getName(), demographic);
			}
		}
		catch(SQLException sqle){
			next_record = null;
		}
		
	}
	
	public boolean hasNextRecord() {
		if(!ready){
			getResultSet();
		}
		
		try{
			if(db != null){
				if(read_record){
					boolean has_next = data.next();
					if(has_next){
						parseDataBaseRow();
						read_record = false;
						return true;
					}
				} else {
					return next_record != null;
				}
				
			}
			return false;
		}
		catch(SQLException sqle){
			return false;
		}
		
		
	}

	public boolean reset(){
		next_record = null;
		read_record = false;
		try{
			if(data != null){
				data.close();
			}
			getResultSet();
			return db != null;
		}
		catch(SQLException sqle){
			return false;
		}
	}
	
	public boolean connect(){
		try{
			db = DriverManager.getConnection(url, user, passwd);
			db.setReadOnly(true);
			ready = false;
			return true;
		}
		catch(SQLException sqle){
			return false;
		}
	}
	
	public boolean disconnect(){
		try{
			db.close();
			return true;
		}
		catch(SQLException sqle){
			return false;
		}
		
	}
}
