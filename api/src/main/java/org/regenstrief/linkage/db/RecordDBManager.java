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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

import com.mysql.jdbc.Driver;

public class RecordDBManager extends DBManager {
	
	private LinkDataSource lds;
	
	private String table;
	
	/**
	 * Constructor parses the database connection information from the LinkDataSource object and uses
	 * the given MatchingConfig object's analytical options when the findMatch is called when a Record
	 * is added to the database.
	 * 
	 * @param lds contains a description of the connection information for the database
	 */
	public RecordDBManager(LinkDataSource lds) {
		super();
		this.lds = lds;
		table = lds.getName();
		if (StringUtils.isNotBlank(lds.getAccess())) {
			String[] access = lds.getAccess().split(",");
			driver = access[0];
			url = access[1];
			user = access[2];
			passwd = access[3];
		} else {
			Properties c = Context.getRuntimeProperties();
			driver = c.getProperty("connection.driver_class");
			if (StringUtils.isBlank(driver)) {
				driver = Driver.class.getName();
			}
			url = c.getProperty("connection.url");
			user = c.getProperty("connection.username");
			passwd = c.getProperty("connection.password");
		}
	}
	
	/**
	 * Method removes all database row that matches this Record object. Method checks for multiple rows
	 * that contain the Record's information. The unique demographic parameter signifies what
	 * demographic is globally unique among the objects represented in the database.
	 * 
	 * @param r the Record object that needs its database representation removed
	 * @param key_demographic the demographic in the Record that uniquely identifies the object
	 * @return the number of rows deleted from the database
	 */
	public boolean deleteRecord(Record r, String key_demographic) {
		String query = new String();
		query += "DELETE FROM " + table + " where " + key_demographic + " = ?";
		String value = r.getDemographic(key_demographic);
		
		try {
			PreparedStatement ps = db.prepareStatement(query);
			ps.setString(1, value);
			boolean ret = executeUpdate(ps);
			ps.close();
			return ret;
		}
		catch (SQLException sqle) {
			return false;
		}
		
	}
	
	/**
	 * Method updates the row in the database with new values by constructing an SQL statement similar
	 * to "UPDATE
	 * <table>
	 * SET <demographic1> = <value1>,. . . <demographicN> = <valueN> WHERE <key_demographic> =
	 * <r.getDemographic(key_demographic);" This will allow partial updates, as only the demographics
	 * that are present in Record r will be touched; other columns will be uneffected.
	 * 
	 * @param r the Record object containing the new demographics and values
	 * @param key_demographic the demographic that discriminates between records
	 * @return true if the jdbc method indicated row(s) were updated, false if exceptions were thrown or
	 *         no rows were changed
	 */
	public boolean updateRecord(Record r, String key_demographic) {
		String query = new String();
		
		query += "UPDATE " + table + " SET ";
		Iterator<String> it = r.getDemographics().keySet().iterator();
		int count = 0;
		ArrayList<String> vals = new ArrayList<String>();
		while (it.hasNext()) {
			String demographic = it.next();
			String value = r.getDemographic(demographic);
			count++;
			
			if (!demographic.equals(key_demographic)) {
				vals.add(value);
				if (it.hasNext()) {
					query += demographic + " = ?,";
				} else {
					query += demographic + " = ? ";
				}
			}
			
		}
		
		String conditional = key_demographic + " = ?";
		query += " WHERE " + conditional;
		vals.add(r.getDemographic(key_demographic));
		
		try {
			PreparedStatement ps = db.prepareStatement(query);
			for (int i = 1; i <= count; i++) {
				String value = vals.get(i - 1);
				ps.setString(i, value);
			}
			boolean ret = executeUpdate(ps);
			ps.close();
			return ret;
		}
		catch (SQLException sqle) {
			return false;
		}
	}
	
	/**
	 * Method merges the information in new_record to the database given that the information in the
	 * original Record is in the database already and needs to be preserved. The new record is
	 * understood to contain alternate demographic information for the same person/thing represented in
	 * original.
	 * 
	 * @param original
	 * @param new_record
	 * @return true if merge was successful, false if otherwise
	 */
	public boolean mergeRecord(Record original, Record new_record) {
		return false;
	}
	
	/**
	 * Method immediately adds the given record to the linkage database.
	 * 
	 * @param r the Record object to add
	 * @return boolean indicating success of insertion
	 */
	public boolean addRecordToDB(Record r) {
		String query = new String();
		String columns = new String("(");
		String values = new String("(");
		
		query += "INSERT INTO " + table + " ";
		Iterator<String> it = r.getDemographics().keySet().iterator();
		int count = 0;
		ArrayList<String> vals = new ArrayList<String>();
		while (it.hasNext()) {
			String demographic = it.next();
			String value = r.getDemographic(demographic);
			vals.add(value);
			count++;
			if (it.hasNext()) {
				columns += demographic + ",";
				values += "?" + ",";
			} else {
				columns += demographic + ")";
				values += "?" + ")";
			}
		}
		query += columns + " VALUES" + values;
		
		try {
			PreparedStatement ps = db.prepareStatement(query);
			for (int i = 1; i <= count; i++) {
				String value = vals.get(i - 1);
				ps.setString(i, value);
			}
			boolean ret = executeUpdate(ps);
			ps.close();
			return ret;
		}
		catch (SQLException sqle) {
			return false;
		}
		
	}
	
	/**
	 * Method returns a list of Record objects created from database rows where the demographic column
	 * matched the value.
	 * 
	 * @param demographic the demographic of interest
	 * @param value the value the demographic must have to be returned
	 * @return a list of Record objects that correspond to the row in the database; null if thing
	 *         matches
	 */
	public List<Record> getRecordFromDB(String demographic, String value) {
		String query = "SELECT * FROM " + table + " WHERE " + demographic + "= '" + value + "'";
		ArrayList<Record> ret = new ArrayList<Record>();
		try {
			Statement stmt = db.createStatement();
			ResultSet rows = stmt.executeQuery(query);
			
			while (rows.next()) {
				Record row_rec = new Record(0, lds.getName());
				ResultSetMetaData meta = rows.getMetaData();
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					String d = meta.getColumnName(i);
					String v = rows.getString(i);
					row_rec.addDemographic(d, v);
					ret.add(row_rec);
				}
				
			}
		}
		catch (SQLException sqle) {
			return null;
		}
		if (ret.size() > 0) {
			return ret;
		}
		return ret;
	}
	
	/**
	 * Method is similar to getRecordFromDB, except that it returns how many items would be in the list
	 * if getRecordFromDB were called. It's a separate method since this information is needed at module
	 * start-up and creating thousands of Record objects is not needed.
	 * 
	 * @param demographic the demographic of interest
	 * @param value the value the demographic must have to be counted
	 * @return the count of records in the database that match the demographic value, -1 if there was an
	 *         error determing the count
	 */
	public int getRecordCountFromDB(String demographic, String value) {
		String query = "SELECT COUNT(*) FROM " + table + " WHERE " + demographic + " = '" + value + "'";
		return executeQuery(query);
	}
	
	/**
	 * @return the name of the table in the database with the Record objects
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * Method creates the table as described in the LinkDataSource. Since this class provides a means of
	 * adding Records, it's possible the table starts out empty or does not exist.
	 * 
	 * @return true if the table was created, false if there was an error
	 */
	public boolean createTable() {
		// sql statement to be created:
		// CREATE TABLE <table name> (
		// 	<column name>	<type>
		// 	. . .
		// );
		String query = new String();
		String columns = new String("(");
		query += "CREATE TABLE " + table + " ";
		Iterator<DataColumn> it = lds.getDataColumns().iterator();
		while (it.hasNext()) {
			DataColumn dc = it.next();
			String name = dc.getName();
			int type = dc.getType();
			String sql_type;
			if (type == DataColumn.STRING_TYPE) {
				sql_type = "varchar";
			} else {
				sql_type = "float";
			}
			
			if (it.hasNext()) {
				columns += name + " " + type + ",";
			} else {
				columns += name + " " + type + ")";
			}
		}
		
		try {
			Statement stmt = db.createStatement();
			stmt.executeUpdate(query);
		}
		catch (SQLException sqle) {
			System.err.println(sqle.getMessage());
			return false;
		}
		
		return true;
	}
}
