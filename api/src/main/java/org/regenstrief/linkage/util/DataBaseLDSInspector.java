package org.regenstrief.linkage.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;

/**
 * Class inspects a database type LinkDataSource and creates DataColumn objects for it. It does this
 * by using the information in the LinkDataSource object to connect to the database, query the
 * table, and discover the information about all the columns.
 * 
 * @author jegg
 */

public class DataBaseLDSInspector implements LinkDataSourceInspector {
	
	public DataBaseLDSInspector() {
		
	}
	
	public void setDefaultDataColumns(LinkDataSource lds) {
		Connection db = getConnection(lds);
		String table = lds.getName();
		String query = "Select * from " + table;
		
		try {
			Statement stmt = db.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsm = rs.getMetaData();
			for (int i = 1; i <= rsm.getColumnCount(); i++) {
				String col_name = rsm.getColumnName(i);
				DataColumn dc = new DataColumn(col_name);
				dc.setIncludePosition(i - 1);
				int col_type = rsm.getColumnType(i);
				if (col_type == Types.NUMERIC || col_type == Types.INTEGER || col_type == Types.FLOAT
				        || col_type == Types.DOUBLE) {
					dc.setType(DataColumn.NUMERIC_TYPE);
				} else {
					dc.setType(DataColumn.STRING_TYPE);
				}
				dc.setName(col_name);
				lds.addDataColumn(dc);
			}
		}
		catch (Exception e) {
			String msg = e.getMessage();
			System.err.println(msg);
		}
		
	}
	
	private Connection getConnection(LinkDataSource lds) {
		try {
			String driver, url, user, passwd;
			String[] access = lds.getAccess().split(",");
			driver = access[0];
			url = access[1];
			user = access[2];
			passwd = access[3];
			
			Class.forName(driver);
			
			Connection db = DriverManager.getConnection(url, user, passwd);
			
			return db;
		}
		catch (Exception e) {
			
		}
		
		return null;
	}
	
}
