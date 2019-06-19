package org.regenstrief.linkage.db;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;
import org.regenstrief.linkage.util.MatchingConfig;

public class ManualReviewDBGenerator {
	
	private static final String PROP_EXCLUDE_BASED_ON_EMPIRICAL_RULES = "org.regenstrief.linkage.db.ManualReviewDBGenerator.excludeBasedOnEmpiricalRules";
	private static final String PROP_SKIP_HEADER = "org.regenstrief.linkage.db.ManualReviewDBGenerator.skipHeader";
	private static final String PROP_FIELD_INDEX_UID1 = "org.regenstrief.linkage.db.ManualReviewDBGenerator.fieldIndexUid1";
	private static final String PROP_FIELD_INDEX_UID2 = "org.regenstrief.linkage.db.ManualReviewDBGenerator.fieldIndexUid2";
	private static final String PROP_REVIEWER_INDEX_MAX = "org.regenstrief.linkage.db.ManualReviewDBGenerator.reviewerIndexMax";
	private static final String PROP_PAIR_FILE_EXTENSION = "org.regenstrief.linkage.db.ManualReviewDBGenerator.pairFileExtension";
	
	private static final Pattern PAT_PIPE = Pattern.compile("\\|");
	
	private static String pairFile = null;
	private static String databaseLocation1 = null;
	private static String table1 = null;
	private static Connection con1 = null;
	private static PreparedStatement read1 = null;
	private static String databaseLocation2 = null;
	private static String table2 = null;
	private static Connection con2 = null;
	private static PreparedStatement read2 = null;
	private static Connection conPair = null;
	private static BufferedReader in = null;
	private static DBMatchResultStore mrs;
	private static MatchingConfig mc = null;
	private static int pairId = 0;
	private static boolean excludeBasedOnEmpiricalRules = true;
	
	public final static void main(final String[] args) throws Exception {
		start(args[0], args[1], args[2], args[3], args[4]);
	}
	
	private final static void start(final String pairFile, final String databaseLocation1, final String table1, final String databaseLocation2, final String table2) throws Exception {
		info("Starting");
		final String reviewerIndexMax = System.getProperty(PROP_REVIEWER_INDEX_MAX);
		if (reviewerIndexMax == null) {
			run(pairFile, databaseLocation1, table1, databaseLocation2, table2);
		} else {
			run(pairFile, Integer.parseInt(reviewerIndexMax), databaseLocation1, table1, databaseLocation2, table2);
		}
		info("Finished");
	}
	
	private final static void run(final String pairFile, final String databaseLocation1, final String table1, final String databaseLocation2, final String table2) throws Exception {
		info("Starting " + pairFile);
		ManualReviewDBGenerator.pairFile = pairFile;
		ManualReviewDBGenerator.databaseLocation1 = databaseLocation1;
		ManualReviewDBGenerator.table1 = table1;
		ManualReviewDBGenerator.databaseLocation2 = databaseLocation2;
		ManualReviewDBGenerator.table2 = table2;
		pairId = 0;
		int pairsAdded = 0, pairsSkipped = 0;
		in = new BufferedReader(new FileReader(pairFile));
		try {
			init();
			final int fieldIndexUid1 = Integer.parseInt(System.getProperty(PROP_FIELD_INDEX_UID1, "0"));
			final int fieldIndexUid2 = Integer.parseInt(System.getProperty(PROP_FIELD_INDEX_UID2, "1"));
			mrs = initDB(conPair, true);
			String line;
			if (!"false".equalsIgnoreCase(System.getProperty(PROP_SKIP_HEADER))) {
				in.readLine(); // Skip header
			}
			while ((line = in.readLine()) != null) {
				debug("Processing " + line);
				final String[] tokens = PAT_PIPE.split(line);
				if (excludeBasedOnEmpiricalRules) {
					final String empMatch = tokens[2];
					if ("1".equals(empMatch)) {
						pairsSkipped++;
						continue;
					}
					final String empNonmatch = tokens[3];
					if ("1".equals(empNonmatch)) {
						pairsSkipped++;
						continue;
					}
				}
				final String uid1 = tokens[fieldIndexUid1], uid2 = tokens[fieldIndexUid2];
				runPair(uid1, uid2);
				pairsAdded++;
			}
			info("Adding indexes");
			mrs.addIndexes();
			conPair.commit();
			mrs.close();
		} finally {
			close();
		}
		info("Finished " + pairFile + " after adding " + pairsAdded + " pairs and skipping " + pairsSkipped);
	}
	
	private final static void run(final String pairFilePrefix, final int reviewerIndexMax,
	                              final String databaseLocation1, final String table1, final String databaseLocation2, final String table2) throws Exception {
		final String ext = System.getProperty(PROP_PAIR_FILE_EXTENSION, "txt");
		for (int i = 0; i <= reviewerIndexMax; i++) {
			final String pairFile = pairFilePrefix + i + "." + ext;
			if ((i == 0) && !new File(pairFile).exists()) {
				continue;
			}
			run(pairFile, databaseLocation1, table1, databaseLocation2, table2);
		}
	}
	
	protected final static DBMatchResultStore initDB(final Connection con, final boolean create) throws Exception {
		con.setAutoCommit(false);
		if (create) {
			SavedResultDBConnection.createMatchResultTables(con);
		}
		final DBMatchResultStore mrs = new DBMatchResultStore(con);
		if (create) {
			mrs.setDate(new Date());
		} else {
			final List<Date> dates = mrs.getDates();
			if (dates.size() == 1) {
				mrs.setDate(dates.get(0));
			} else {
				throw new IllegalStateException("Expected one date but found " + dates.size());
			}
		}
		return mrs;
	}
	
	private final static void init() throws Exception {
		final String prop = System.getProperty(PROP_EXCLUDE_BASED_ON_EMPIRICAL_RULES);
		if (prop != null) {
			excludeBasedOnEmpiricalRules = Boolean.parseBoolean(prop);
		}
		con1 = SavedResultDBConnection.openDBResults(databaseLocation1);
		createIndex(con1, table1, databaseLocation1);
		if (databaseLocation1.equals(databaseLocation2)) {
			con2 = con1;
		} else {
			con2 = SavedResultDBConnection.openDBResults(databaseLocation2);
			createIndex(con2, table2, databaseLocation2);
		}
		conPair = SavedResultDBConnection.openDBResults(pairFile + ".db");
		read1 = createRead(con1, table1);
		read2 = createRead(con2, table2);
	}
	
	private final static void close() {
		close(conPair);
		close(read1);
		close(con1);
		close(read2);
		close(con2);
		close(in);
	}
	
	private final static void close(final Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private final static void close(final PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private final static void close(final Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private final static void createIndex(final Connection con, final String table, final String databaseLocation) throws Exception {
		info("Creating " + databaseLocation + " " + table + " index if needed");
		final PreparedStatement stmt = con.prepareStatement("create unique index if not exists " + table + "_uid_idx on " + table + " (uid)");
		try {
			stmt.execute();
		} finally {
			stmt.close();
		}
		info("Finished creating " + databaseLocation + " " + table + " index if needed");
	}
	
	private final static PreparedStatement createRead(final Connection con, final String table) throws Exception {
		return con.prepareStatement("select * from " + table + " where uid=?");
	}
	
	private final static void runPair(final String uid1, final String uid2) throws Exception {
		final Record r1 = getRecord(read1, uid1, databaseLocation1), r2 = getRecord(read2, uid2, databaseLocation2);
		final MatchResult mr = newMatchResult(r1, r2, mc);
		mrs.addMatchResult(mr, pairId);
		pairId++;
	}
	
	protected final static MatchResult newMatchResult(final Record r1, final Record r2, final MatchingConfig mc) {
		final MatchVector mv = new MatchVector();
		for (final String key : r1.getDemographics().keySet()) {
			final String value = r1.getDemographic(key);
			mv.setMatch(key, (value != null) && value.equals(r2.getDemographic(key)));
		}
		return new MatchResult(0, 0, 0, 0, 0, 0, mv, null, r1, r2, mc);
	}
	
	private final static Record getRecord(final PreparedStatement read, final String uid, final String context) throws Exception {
		read.setString(1, uid);
		final ResultSet rs = read.executeQuery();
		try {
			if (!rs.next()) {
				throw new IllegalStateException("Could not find " + uid + " in " + context);
			}
			final Record r = new Record(Long.parseLong(uid), context);
			final ResultSetMetaData md = rs.getMetaData();
			final int size = md.getColumnCount();
			final String[] dems;
			if (mc == null) {
				dems = new String[size];
			} else {
				dems = null;
			}
			for (int i = 1; i <= size; i++) {
				final String key = md.getColumnName(i);
				r.addDemographic(key, rs.getString(i));
				if (dems != null) {
					dems[i - 1] = key;
				}
			}
			if (dems != null) {
				mc = new MatchingConfig("ManualReview", dems);
			}
			return r;
		} finally {
			rs.close();
		}
	}
	
	private final static void info(final Object s) {
		System.out.println(new Date() + " - " + s);
	}
	
	private final static void debug(final Object s) {
	}
}
