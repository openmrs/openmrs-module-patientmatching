package org.regenstrief.linkage.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.regex.Pattern;

public class FileToDB {
	
	private final static String PROP_DELIM = "org.regenstrief.linkage.db.FileToDB.delim";
	
	private static Connection con = null;
	
	private static int numColumns = -1;
	
	private static String tableName = "record";
	
	private static PreparedStatement insert = null;
	
	private static Pattern patDelim = null;
	
	public final static void main(final String[] args) throws Exception {
		run(args[0], (args.length < 2) ? null : args[1], (args.length < 3) ? null : args[2]);
	}
	
	private final static void run(final String inFile, String outFile, String tableName) throws Exception {
		final String delim = System.getProperty(PROP_DELIM);
		if ((delim == null) || "pipe".equalsIgnoreCase(delim) || "|".equalsIgnoreCase(delim)
		        || "\\|".equalsIgnoreCase(delim)) {
			patDelim = Pattern.compile("\\|");
		} else if ("tab".equalsIgnoreCase(delim) || "\t".equalsIgnoreCase(delim) || "\\t".equalsIgnoreCase(delim)) {
			patDelim = Pattern.compile("\\t");
		}
		if (outFile == null) {
			outFile = inFile + ".db";
		}
		if (tableName == null) {
			tableName = FileToDB.tableName;
		} else {
			FileToDB.tableName = tableName;
		}
		info("Converting " + inFile + " into " + outFile + " (" + tableName + ")");
		con = SavedResultDBConnection.openDBResults(new File(outFile));
		con.setAutoCommit(false);
		final BufferedReader in = new BufferedReader(new FileReader(inFile));
		try {
			final String header = in.readLine();
			createTable(header);
			String line;
			long n = 0;
			while ((line = in.readLine()) != null) {
				addBatch(line);
				n++;
				if ((n % 1000) == 0) {
					executeBatch();
					info("Processed " + n + " records");
				}
			}
			executeBatch();
			info("Finished converting " + inFile + " into " + outFile + " after " + n + " records");
			insert.close();
		}
		finally {
			in.close();
			con.close();
		}
	}
	
	private final static void createTable(final String header) throws Exception {
		final String[] tokens = split(header);
		final StringBuilder b = new StringBuilder();
		b.append("create table ").append(tableName).append(" (\n");
		numColumns = tokens.length;
		for (int i = 0; i < numColumns; i++) {
			final String token = formatColumnName(tokens[i]);
			tokens[i] = token;
			b.append(token).append(" varchar(4000)");
			if (i < (numColumns - 1)) {
				b.append(',');
			}
			b.append('\n');
		}
		b.append(')');
		info(b);
		final Statement stmt = con.createStatement();
		stmt.execute(b.toString());
		con.commit();
		stmt.close();
		prepareInsert(tokens);
	}
	
	private final static String formatColumnName(final String name) {
		return name.replace(' ', '_');
	}
	
	private final static void prepareInsert(final String[] tokens) throws Exception {
		final StringBuilder b = new StringBuilder();
		b.append("insert into ").append(tableName).append('\n');
		b.append("(");
		for (int i = 0; i < numColumns; i++) {
			if (i > 0) {
				b.append(',');
			}
			b.append(tokens[i]);
		}
		b.append(")\n");
		b.append("values(");
		for (int i = 0; i < numColumns; i++) {
			if (i > 0) {
				b.append(',');
			}
			b.append('?');
		}
		b.append(')');
		info(b);
		insert = con.prepareStatement(b.toString());
	}
	
	private final static void addBatch(final String line) throws Exception {
		final String[] tokens = split(line);
		final int size = tokens.length;
		for (int i = 0; i < numColumns; i++) {
			final String token = (i < size) ? tokens[i] : null;
			insert.setString(i + 1, ((token == null) || token.isEmpty()) ? null : token);
		}
		insert.addBatch();
	}
	
	private final static void executeBatch() throws Exception {
		insert.executeBatch();
		con.commit();
	}
	
	private final static String[] split(final String line) {
		return patDelim.split(line);
	}
	
	private final static void info(final Object s) {
		System.out.println(new Date() + " - " + s);
	}
}
