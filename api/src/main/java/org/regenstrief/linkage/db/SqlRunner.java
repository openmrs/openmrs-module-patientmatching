package org.regenstrief.linkage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * SqlRunner
 */
public class SqlRunner {
	
	protected static Connection con = null;
	
	public static void main(final String[] args) throws Exception {
		run(args[0], args[1]);
	}
	
	private final static void run(final String dbFile, final String sql) throws Exception {
		con = SavedResultDBConnection.openDBResults(new File(dbFile));
		try {
			final Statement stmt = con.createStatement();
			if (stmt.execute(sql)) {
				final ResultSet rs = stmt.getResultSet();
				final ResultSetMetaData md = rs.getMetaData();
				final int size = md.getColumnCount();
				for (int i = 1; i <= size; i++) {
					System.out.print(md.getColumnName(i));
					if (i < size) {
						System.out.print('|');
					}
				}
				System.out.println();
				while (rs.next()) {
					for (int i = 1; i <= size; i++) {
						System.out.print(rs.getObject(i));
						if (i < size) {
							System.out.print('|');
						}
					}
					System.out.println();
				}
				rs.close();
			} else {
				stmt.getUpdateCount();
			}
			stmt.close();
		} finally {
			con.close();
		}
	}
}
