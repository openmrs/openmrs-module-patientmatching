package org.regenstrief.linkage;

import java.sql.Connection;

/**
 * Class stores the database connection to use when importing data and linking records, as well as
 * creating any standard tables that might be needed or seeing what is already stored in the
 * database from a previous session.
 */

public class Workspace {
	
	private Connection db;
	
	public Workspace(String driver, String url, String user, String passwd) {
		// connect to database
		
		// create necessary tables, if needed
		createWorkspaceTables();
	}
	
	private boolean createWorkspaceTables() {
		return false;
	}
	
}
