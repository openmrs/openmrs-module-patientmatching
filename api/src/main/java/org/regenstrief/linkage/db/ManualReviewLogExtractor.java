/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.regenstrief.linkage.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import org.regenstrief.linkage.matchresult.DBMatchResultStore;

/**
 * ManualReviewLogExtractor
 */
public class ManualReviewLogExtractor {
	
	private final static Pattern PAT_PIPE = Pattern.compile("\\|");
	
	private static Connection con = null;
	
	private static DBMatchResultStore mrs;
	
	private static PreparedStatement update = null;
	
	public final static void main(final String[] args) throws Exception {
		final String dbFile = args[0];
		final String logFile = (args.length > 1) ? args[1] : dbFile + ".review.log";
		run(dbFile, logFile);
	}
	
	private final static void run(final String dbFile, final String logFile) throws Exception {
		final Reader in = new FileReader(logFile);
		try {
			run(dbFile, in);
		}
		finally {
			in.close();
		}
	}
	
	private final static void run(final String dbFile, final Reader logReader) throws Exception {
		final BufferedReader in = new BufferedReader(logReader);
		con = SavedResultDBConnection.openDBResults(dbFile);
		try {
			init();
			String line;
			while ((line = in.readLine()) != null) {
				final String[] tokens = PAT_PIPE.split(line);
				final long uid1 = Long.parseLong(tokens[1]);
				final long uid2 = Long.parseLong(tokens[2]);
				final float certaintyRaw = Integer.parseInt(tokens[3]); // 1 - 4
				final float certaintyNormalized = (certaintyRaw - 1.0f) / 3.0f; // 0.0 - 1.0
				final int status = (certaintyNormalized < 0.5f) ? -1 : 1;
				update.setInt(1, status);
				update.setFloat(2, certaintyNormalized);
				update.setLong(3, uid1);
				update.setLong(4, uid2);
				update.addBatch();
			}
			update.executeBatch();
			con.commit();
			close();
		}
		finally {
			con.close();
		}
	}
	
	private final static void init() throws Exception {
		mrs = ManualReviewDBGenerator.initDB(con, false);
		final StringBuilder b = new StringBuilder();
		b.append("update matchresult\n");
		b.append("set status=?,certainty=?\n");
		b.append("where uid1=? and uid2=?");
		update = con.prepareStatement(b.toString());
	}
	
	private final static void close() throws Exception {
		update.close();
		mrs.close();
	}
}
