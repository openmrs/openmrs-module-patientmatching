package org.regenstrief.linkage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class creates a table to store Records being imported to the record linking workspace. The object
 * imports from one source to one table, and then it is finished.
 */

public class RecordImporter {
	
	Connection workspace;
	
	String table_name;
	
	LinkDataSource source;
	
	public RecordImporter(Connection c, String table_name, LinkDataSource source) {
		workspace = c;
		this.table_name = table_name;
		this.source = source;
		
		// create table to store Record objects
		createTable();
	}
	
	private boolean createTable() {
		
		return false;
	}
	
	public boolean addRecord(Record r) {
		boolean ret = false;
		String query = new String();
		String columns = new String("(");
		String values = new String("(");
		
		query += "INSERT INTO " + table_name + " ";
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
		PreparedStatement ps = null;
		try {
			ps = workspace.prepareStatement(query);
			for (int i = 1; i <= count; i++) {
				String value = vals.get(i - 1);
				ps.setString(i, value);
			}
			int updated = ps.executeUpdate();
			if (updated > 0) {
				ret = true;
			}
			ps.close();
		}
		catch (SQLException sqle) {
			ret = false;
		}
		
		return ret;
	}
}
