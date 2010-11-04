package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class either creates a JavaDB database from a given file name and returns a connection to
 * the new database or opens the existing database in the given file and returns the connection
 * 
 * 
 */

public class SavedResultDBConnection {
	
	public static String CREATE_MATCH_RESULT_TABLE = "create table matchresult(" +
			"ID bigint," +
			"mc varchar(40)," +
			"report_date timestamp," +
			"score double," +
			"true_prob double," +
			"false_prob double," +
			"spec double," +
			"sens double," +
			"status integer," +
			"certainty double," +
			"uid1 bigint," +
			"uid2 bigint," +
			"note varchar(255)" +
			")";
	public static String CREATE_DEMOGRAPHIC_TABLE = "create table demographic(" +
			"uid bigint," +
			"field varchar(40)," +
			"value varchar(40)" +
			")";
	public static String CREATE_FIELD_AGREEMENT_TABLE = "create table field_agreement(" +
			"id integer," +
			"field varchar(40)," + 
			"algorithm varchar(40)," +
			"agreement integer" +
			")";
	
	public static Connection openDBResults(File f){
		Connection ret = null;
		String url = null;
		try{
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException cnfe){
			return null;
		}
		
		url = "jdbc:sqlite:" + f.getPath();
		//if(!f.exists()){
			//url += ";create=true";
		//}
		//url = "jdbc:derby:Results;create=true";
		
		try {
			ret = DriverManager.getConnection(url);
		} catch (SQLException sqle) {
			System.err.println(sqle.getMessage());
			return null;
		}
		
		return ret;
	}
	
	public static void createMatchResultTables(Connection db){
		try{
			Statement st = db.createStatement();
			st.execute(CREATE_MATCH_RESULT_TABLE);
			st.execute(CREATE_DEMOGRAPHIC_TABLE);
			st.execute(CREATE_FIELD_AGREEMENT_TABLE);
		}
		catch(SQLException sqle){
			System.err.println(sqle.getMessage());
		}
	}
}
