package org.regenstrief.linkage.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;
import org.regenstrief.linkage.util.MatchingConfig;

public class ManualReviewDBGenerator2 {
	
	private static final Pattern PAT_PIPE = Pattern.compile("\\|");
	private static String pairFile = null;
	private static Connection con = null;
	private static BufferedReader in = null;
	private static DBMatchResultStore mrs;
	private static MatchingConfig mc = null;
	private static int pairId = 0;
	private static final Map<String, Integer> fields1 = new LinkedHashMap<String, Integer>();
	private static final Map<String, Integer> fields2 = new LinkedHashMap<String, Integer>();
	
	public final static void main(final String[] args) throws Exception {
		info("Starting");
		for (final String arg : args) {
			run(arg);
		}
		info("Finished");
	}
	
	private final static void run(final String pairFile) throws Exception {
		info("Starting " + pairFile);
		pairId = 0;
		ManualReviewDBGenerator2.pairFile = pairFile;
		in = new BufferedReader(new FileReader(pairFile));
		int count = 0;
		try {
			init();
			mrs = ManualReviewDBGenerator.initDB(con, true);
			String line;
			final String header = in.readLine();
			parseHeader(header);
			while ((line = in.readLine()) != null) {
				runPair(PAT_PIPE.split(line));
				count++;
				if ((count % 500) == 0) {
					info("Processed " + count + " records");
				}
			}
			info("Adding indexes");
			mrs.addIndexes();
			con.commit();
			mrs.close();
		} finally {
			close();
		}
		info("Finished " + pairFile + " after " + count + " records");
	}
	
	private final static void init() throws Exception {
		con = SavedResultDBConnection.openDBResults(pairFile + ".db");
	}
	
	private final static void close() throws Exception {
		con.close();
		in.close();
	}
	
	private final static void parseHeader(final String header) {
		final String[] fields = PAT_PIPE.split(header);
		final int size = fields.length;
		for (int i = 0; i < size; i++) {
			final String field = fields[i];
			if (field.endsWith("1")) {
				fields1.put(removeSide(field), Integer.valueOf(i));
			} else if (field.endsWith("2")) {
				final String f = removeSide(field);
				if (!fields1.containsKey(f)) {
					throw new IllegalStateException("Found " + field + " but not corresponding side-1 field");
				}
				fields2.put(f, Integer.valueOf(i));
			}
		}
		if (fields1.size() != fields2.size()) {
			throw new IllegalStateException("Found " + fields1.size() + " side-1 fields but " + fields2.size() + "side-2 fields");
		}
	}
	
	private final static String removeSide(final String field) {
		return field.substring(0, field.length() - 1);
	}
	
	private final static void runPair(final String[] tokens) throws Exception {
		final Record r1 = getRecord(tokens, fields1, "1"), r2 = getRecord(tokens, fields2, "2");
		final MatchResult mr = ManualReviewDBGenerator.newMatchResult(r1, r2, mc);
		mrs.addMatchResult(mr, pairId);
		pairId++;
	}
	
	private final static Record getRecord(final String[] tokens, final Map<String, Integer> fields, final String context) throws Exception {
		Record r = null;
		final int size = fields.size();
		final String[] dems;
		if (mc == null) {
			dems = new String[size];
		} else {
			dems = null;
		}
		int i = 0;
		final Integer uidIndex = fields.get("UID");
		if (uidIndex != null) {
			r = new Record(Long.parseLong(tokens[uidIndex.intValue()]), context);
		}
		for (final Entry<String, Integer> entry : fields.entrySet()) {
			final String key = entry.getKey(), value = tokens[entry.getValue().intValue()];
			if (r == null) {
				r = new Record(Long.parseLong(value), context);
			}
			r.addDemographic(key, value);
			if (dems != null) {
				dems[i] = key;
				i++;
			}
		}
		if (dems != null) {
			mc = new MatchingConfig("ManualReview", dems);
		}
		return r;
	}
	
	private final static void info(final Object s) {
		System.out.println(new Date() + " - " + s);
	}
}
