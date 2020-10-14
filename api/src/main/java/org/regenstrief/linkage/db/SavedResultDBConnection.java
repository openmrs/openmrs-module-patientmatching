package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class either creates a JavaDB database from a given file name and returns a connection to the new
 * database or opens the existing database in the given file and returns the connection
 */
public class SavedResultDBConnection {
	
	public static final String PROP_VALIDATE = "org.regenstrief.linkage.db.SavedResultDBConnection.validate";
	
	public static final String CREATE_RECORD_TABLE = "create table record(" + "uid bigint primary key" + ")";
	
	public static final String CREATE_MATCH_RESULT_TABLE = "create table matchresult(" + "ID bigint primary key,"
	        + "mc varchar(40)," + "report_date timestamp," + "score double," + "true_prob double," + "false_prob double,"
	        + "spec double," + "sens double," + "status integer," + "certainty double," + "uid1 bigint not null,"
	        + "uid2 bigint not null," + "note varchar(255)," + "foreign key(uid1) REFERENCES record(uid),"
	        + "foreign key(uid2) REFERENCES record(uid)" + ")";
	
	public static final String CREATE_DEMOGRAPHIC_TABLE = "create table demographic(" + "uid bigint not null,"
	        + "field varchar(40) not null," + "value varchar(40)," + "foreign key(uid) REFERENCES record(uid)" + ")";
	
	public static final String CREATE_FIELD_AGREEMENT_TABLE = "create table field_agreement(" + "id integer,"
	        + "field varchar(40)," + "algorithm varchar(40)," + "agreement integer,"
	        + "foreign key(id) REFERENCES match_result(id)" + ")";
	
	public static final String CREATE_DATE_TABLE = "create table report_dates(" + "report_date timestamp" + ")";
	
	public static Connection openDBResults(final File f) {
		return openDBResults(f.getPath());
	}
	
	public static Connection openDBResults(final String fileLocation) {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + fileLocation);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void createMatchResultTables(final Connection db) {
		try {
			final Statement st = db.createStatement();
			st.execute(CREATE_RECORD_TABLE);
			st.execute(CREATE_MATCH_RESULT_TABLE);
			st.execute(CREATE_DEMOGRAPHIC_TABLE);
			st.execute(CREATE_FIELD_AGREEMENT_TABLE);
			st.execute(CREATE_DATE_TABLE);
			st.close();
		}
		catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void validateMatchResultTables(final Connection db) {
		if (!"true".equalsIgnoreCase(System.getProperty(PROP_VALIDATE))) {
			return;
		}
		try {
			final Statement st = db.createStatement();
			validate0(st, "select count(1) from matchresult m where not exists(select 1 from record r where r.uid=m.uid1)");
			validate0(st, "select count(1) from matchresult m where not exists(select 1 from record r where r.uid=m.uid2)");
			validate0(st,
			    "select count(1) from matchresult m where not exists(select 1 from demographic d where d.uid=m.uid1)");
			validate0(st,
			    "select count(1) from matchresult m where not exists(select 1 from demographic d where d.uid=m.uid2)");
			st.close();
		}
		catch (final SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void validate0(final Statement st, final String sql) throws SQLException {
		final ResultSet rs = st.executeQuery(sql);
		try {
			rs.next();
			final int actual = rs.getInt(1);
			if (actual != 0) {
				throw new IllegalStateException("Expected 0 but found " + actual + " for\n" + sql);
			}
		}
		finally {
			rs.close();
		}
	}
}
