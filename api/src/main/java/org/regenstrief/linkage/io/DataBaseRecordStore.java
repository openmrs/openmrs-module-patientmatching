package org.regenstrief.linkage.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class implements storing Records in a database.
 * @author jegg
 *
 */

public class DataBaseRecordStore implements RecordStore {
	Connection db_connection;
	LinkDataSource lds;
	String table_name, driver, url, user, password;
	
	PreparedStatement insert_stmt;
	List<String> insert_demographics;
	
	String quote_string;
	private int batch_size;
	
	public static final String UID_COLUMN = "import_uid";
	public static final String INVALID_COLUMN_CHARS = "\\W";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final int MAXIMUM_BATCH_SIZE = 1000;

	/**
	 * 
	 * @param db	the database connection to create the table of Records
	 * @param lds	information on what fields the Records will have
	 * @param driver	
	 * @param url	
	 * @param user	
	 * @param password	
	 */
	public DataBaseRecordStore(Connection db, LinkDataSource lds, String driver, String url, String user, String password){
		db_connection = db;
		this.lds = lds;
		table_name = "import";
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		
		insert_demographics = new ArrayList<String>();
		batch_size = 0;
		try{
			quote_string = db_connection.getMetaData().getIdentifierQuoteString();
			log.info("Identifier quote string is " + quote_string);
		}
		catch(SQLException sqle){
			log.warn("Unable to get underlying database identifiers' quote character, using none!");
			quote_string = "";
		}
		
		dropTableIfExists(table_name);
		if(!createTable()){
			close();
		} else {
			insert_stmt = createInsertQuery();
		}
	}
	
	public boolean clearRecords(){
		String delete_query = "DELETE FROM " + table_name;
		try{
			if(db_connection.isClosed()){
				return false;
			}
			Statement s = db_connection.createStatement();
			s.execute(delete_query);
			s.close();
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected boolean createTable(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("CREATE TABLE ")
		      .append(table_name)
		      .append("(")
		      .append(UID_COLUMN)
		      .append("\tbigint");
		
		// iteratoe over lds to see what fields to expect, and set order in insert_demographics
		Enumeration<String> e = lds.getIncludedDataColumns().keys();
		while(e.hasMoreElements()){
			String column = e.nextElement().trim();
			
			insert_demographics.add(column);
			buffer.append(", ")
			      .append(quote_string)
			      .append(column)
			      .append(quote_string)
			      .append("\ttext");

		}
		buffer.append(")");
		
		try{
			log.trace("Creating table " + table_name);
			db_connection.createStatement().execute(buffer.toString());
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected void dropTableIfExists(String table){
		try{
			log.trace("Dropping table " + table);
			db_connection.createStatement().execute("DROP TABLE IF EXISTS " + table);
		} catch(SQLException e){
			log.warn("error dropping " + table + " table", e);
		}
	}
	
	protected PreparedStatement createInsertQuery(){
		PreparedStatement stmt = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO ").append(table_name);
		
		StringBuffer bufferColumn = new StringBuffer();
		bufferColumn.append("(").append(UID_COLUMN);
		
		StringBuffer bufferValues = new StringBuffer();
		bufferValues.append("VALUES (?");
		for(int i = 0; i < insert_demographics.size(); i++){
			String demographic = insert_demographics.get(i);
			bufferColumn.append(", ").append(quote_string).append(demographic).append(quote_string);
			bufferValues.append(", ?");
		}
		bufferColumn.append(")");
		bufferValues.append(")");
		buffer.append(bufferColumn).append(" ").append(bufferValues);
		
		try{
			stmt = db_connection.prepareStatement(buffer.toString());
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
			return null;
		}
		return stmt;
	}
	
	/**
	 * Returns a LinkDataSource describing the connection parameters and fields that will be in a Reader
	 * created from the database table
	 */
	public LinkDataSource getRecordStoreLinkDataSource() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(driver)
		      .append(",")
		      .append(url)
		      .append(",")
		      .append(user)
		      .append(",")
		      .append(password);
		String access = buffer.toString();
		LinkDataSource ret = new LinkDataSource(table_name, "DataBase", access, 0);
		ret.setUniqueID(UID_COLUMN);
        DataColumn dc = new DataColumn(UID_COLUMN);
        dc.setIncludePosition(0);
        dc.setName(UID_COLUMN);
        dc.setType(lds.getColumnTypeByName(UID_COLUMN));
        ret.addDataColumn(dc);
		for(int i = 0; i < insert_demographics.size(); i++){
			String demographic = insert_demographics.get(i);
			dc = new DataColumn(demographic);
			dc.setIncludePosition(i + 1);
			dc.setName(demographic);
			dc.setType(lds.getColumnTypeByName(demographic));
			ret.addDataColumn(dc);
		}
		return ret;
	}
	
	public Connection getRecordStoreConnection(){
		return db_connection;
	}

	/**
	 * Stores the Records in the database
	 */
	public boolean storeRecord(Record r) {
		try{
			if(db_connection.isClosed()){
				return false;
			}
			
			insert_stmt.setLong(1, r.getUID());
			batch_size++;
			for(int i = 0; i < insert_demographics.size(); i++){
				String demographic = insert_demographics.get(i);
				insert_stmt.setString(i + 2, r.getDemographic(demographic));
			}
			insert_stmt.addBatch();
			executeBatchIfNeeded(MAXIMUM_BATCH_SIZE);
			return true;
		}
		catch(SQLException sqle){
			sqle.printStackTrace();
			return false;
		}
	}
	
	private void executeBatchIfNeeded(final int minimum_batch_size) throws SQLException {
		if (batch_size >= minimum_batch_size) {
			insert_stmt.executeBatch();
			batch_size = 0;
		}
	}

	public boolean close() {
        try{
            if(insert_stmt != null){
            	executeBatchIfNeeded(1);
                insert_stmt.clearParameters();
                insert_stmt.close();
            }
            if(db_connection != null){
                db_connection.close();
            }
            return true;
        } catch(SQLException sqle){
			sqle.printStackTrace();
            return false;
        }
    }
}
