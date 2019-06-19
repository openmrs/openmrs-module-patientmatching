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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;

/**
 * ManualReviewDBCombiner
 */
public class ManualReviewDBCombiner {
	
	private final static String fileSep = System.getProperty("file.separator");
	private final static Set<String> fields = new LinkedHashSet<String>();
	protected final static Map<PairKey, PairResult> pairs = new HashMap<PairKey, PairResult>();
	private static Connection con = null;
	private static DBMatchResultStore mrs = null;
	
	public final static void main(final String[] args) throws Exception {
		run(args);
	}
	
	private final static void run(final String[] args) throws Exception {
		info("Analyzing input");
		final String mode = args[0];
		final String outPrefix;
		if ("sub".equalsIgnoreCase(mode)) {
			outPrefix = runSubDirectories(args[1], args[2]);
		} else if ("list".equalsIgnoreCase(mode)) {
			outPrefix = runList(args[1], args, 1);
		} else {
			throw new IllegalArgumentException("Unexpected mode: " + mode);
		}
		info("Writing output");
		writeOutput(outPrefix);
		info("Finished");
	}
	
	private final static String runSubDirectories(final String directory, final String databaseFileName) throws Exception {
		for (final File child : new File(directory).listFiles()) {
			if (!child.isDirectory()) {
				continue;
			}
			final String childPath = child.getAbsolutePath(), childName = child.getName();
			final File databaseFile = new File(childPath + fileSep + databaseFileName);
			final String databaseFilePath = databaseFile.getAbsolutePath();
			if (databaseFile.exists()) {
				runReviewer(childName, databaseFilePath);
			} else {
				warn("Could not find: " + databaseFilePath);
			}
		}
		return directory + fileSep + databaseFileName + ".results.";
	}
	
	private final static String runList(final String outPrefix, final String[] list, final int startIndex) throws Exception {
		final int size = list.length;
		for (int i = startIndex; i < size; i++) {
			runReviewer(list[i]);
		}
		return outPrefix;
	}
	
	private final static void runReviewer(final String databaseLocation) throws Exception {
		runReviewer(databaseLocation, databaseLocation);
	}
	
	protected final static void runReviewer(final String reviewer, final String databaseLocation) throws Exception {
		info("Analyzing " + databaseLocation);
		final int size;
		con = SavedResultDBConnection.openDBResults(databaseLocation);
		try {
			mrs = ManualReviewDBGenerator.initDB(con, false);
			size = mrs.getSize();
			for (int i = 0; i < size; i++) {
				final MatchResult mr = mrs.getMatchResult(i);
				final Record record1 = mr.getRecord1(), record2 = mr.getRecord2();
				final long uid1 = record1.getUID(), uid2 = record2.getUID();
				if (mr.getMatch_status() == MatchResult.UNKNOWN) {
					throw new IllegalStateException(databaseLocation + " contained unknown match status for " + uid1 + "-" + uid2);
				}
				final boolean matchStatus = ManualReviewEvaluator.getMatchStatus(ManualReviewEvaluator.getMatchScore(mr.getCertainty()));;
				final PairKey pk = new PairKey(uid1, uid2);
				PairResult pr = pairs.get(pk);
				if (pr == null) {
					pr = new PairResult(record1, record2, reviewer, matchStatus);
					pairs.put(pk, pr);
				} else {
					pr.addSecondStatus(reviewer, matchStatus);
				}
			}
			mrs.close();
		} finally {
			con.close();
		}
		info("Finished analyzing " + databaseLocation + " which had " + size + " record pairs; found " + pairs.size() + " total record pairs so far across all reviewers");
	}
	
	private final static void writeOutput(final String outPrefix) throws Exception {
		final String discordantLoc = outPrefix + "discordant.txt", agreedLoc = outPrefix + "agreed.txt";
		PrintStream discordantOut = null, agreedOut = null;
		int discordantCount = 0, agreedCount = 0;
		final Map<String, Integer> disagreementsPerReviewerCombo = new HashMap<String, Integer>();
		final Map<String, Integer> disagreementsPerReviewer = new HashMap<String, Integer>();
		try {
			discordantOut = new PrintStream(new FileOutputStream(discordantLoc));
			agreedOut = new PrintStream(new FileOutputStream(agreedLoc));
			boolean first = true;
			for (final PairResult pr : pairs.values()) {
				if (first) {
					initFields(pr.record1, discordantOut, agreedOut);
					first = false;
				}
				final PrintStream out;
				if (pr.reviewer2 == null) {
					throw new IllegalStateException("Only 1 reviewer for " + pr + ": " + pr.reviewer1);
				}
				final String matchStatus;
				if (pr.matchStatus == null) {
					out = discordantOut;
					discordantCount++;
					matchStatus = "discordant";
					putDisagreement(disagreementsPerReviewerCombo, disagreementsPerReviewer, pr);
				} else {
					out = agreedOut;
					agreedCount++;
					matchStatus = pr.matchStatus.toString();
				}
				print(out, pr.record1);
				print(out, pr.record2);
				out.println(matchStatus + "|" + pr.reviewer1 + "|" + pr.reviewer2);
			}
		} finally {
			close(discordantOut);
			close(agreedOut);
		}
		dumpDisagreements(disagreementsPerReviewerCombo, "Disagreements per reviewer combo");
		dumpDisagreements(disagreementsPerReviewer, "Disagreements per reviewer");
		info("Found " + agreedCount + " harmonious pairs and " + discordantCount + " discordant pairs");
	}
	
	private final static void putDisagreement(final Map<String, Integer> disagreementsPerReviewerCombo, final Map<String, Integer> disagreementsPerReviewer, final PairResult pr) {
		final String reviewer1 = pr.reviewer1, reviewer2 = pr.reviewer2, key;
		if (reviewer1.compareTo(reviewer2) < 0) {
			key = reviewer1 + ", " + reviewer2;
		} else {
			key = reviewer2 + ", " + reviewer1;
		}
		increment(disagreementsPerReviewerCombo, key);
		increment(disagreementsPerReviewer, reviewer1);
		increment(disagreementsPerReviewer, reviewer2);
	}
	
	private final static void increment(final Map<String, Integer> map, final String key) {
		Integer old = map.get(key);
		map.put(key, Integer.valueOf((old == null) ? 1 : (old.intValue() + 1)));
	}
	
	private final static void dumpDisagreements(final Map<String, Integer> map, final String label) {
		info("********** " + label + " **********");
		final List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new DisagreementComparator());
		for (final Entry<String, Integer> entry : list) {
			final String key = entry.getKey();
			final String name;
			if (key.contains(",")) {
				name = "(" + key + ")";
			} else {
				name = key;
			}
			info(name + " disagreement count: " + entry.getValue());
		}
	}
	
	private final static void print(final PrintStream out, final Record record) {
		for (final String key : fields) {
			out.print(record.getDemographic(key) + "|");
		}
	}
	
	private final static void initFields(final Record record, final PrintStream discordantOut, final PrintStream agreedOut) {
		if (fields.isEmpty()) {
			for (final String key : record.getDemographics().keySet()) {
				fields.add(key);
			}
		}
		printHeader(discordantOut);
		printHeader(agreedOut);
	}
	
	private final static void printHeader(final PrintStream out) {
		for (final String key : fields) {
			out.print(key + "1|");
		}
		for (final String key : fields) {
			out.print(key + "2|");
		}
		out.println("match_status|reviewer1|reviewer2");
	}
	
	private final static void close(final Closeable c) throws Exception {
		if (c != null) {
			c.close();
		}
	}
	
	private final static void info(final Object s) {
		System.err.println(new Date() + " - " + s);
	}
	
	private final static void warn(final Object s) {
		System.err.println(new Date() + " - " + s);
	}
	
	protected final static class PairKey {
		
		private final long uid1;
		private final long uid2;
		
		protected PairKey(final long uid1, final long uid2) {
			this.uid1 = uid1;
			this.uid2 = uid2;
		}
		
		@Override
		public final int hashCode() {
			return Long.hashCode(this.uid1) ^ Long.hashCode(this.uid2);
		}
		
		@Override
		public final boolean equals(final Object o) {
			if (o == this) {
				return true;
			} else if (!(o instanceof PairKey)) {
				return false;
			}
			final PairKey pk = (PairKey) o;
			return (this.uid1 == pk.uid1) && (this.uid2 == pk.uid2);
		}
	}
	
	protected final static class PairResult {
		
		private final Record record1;
		private final Record record2;
		private final String reviewer1;
		private String reviewer2 = null;
		protected Boolean matchStatus;
		// Need to know both of the reviewers (at least if they're discordant) so that a different reviewer can be chosen as the tie-breaker
		
		private PairResult(final Record record1, final Record record2, final String reviewer, final boolean matchStatus) {
			this.record1 = record1;
			this.record2 = record2;
			this.reviewer1 = reviewer;
			this.matchStatus = Boolean.valueOf(matchStatus);
		}
		
		private void addSecondStatus(final String reviewer, final boolean matchStatus) {
			if (this.reviewer2 != null) {
				throw new IllegalStateException("Found 3 reviewers for " + this + ": " + this.reviewer1 + ", " + this.reviewer2 + ", " + reviewer);
			} else if (this.reviewer1.equals(reviewer)) {
				throw new IllegalStateException(reviewer + " reviewed same pair twice: " + this);
			}
			this.reviewer2 = reviewer;
			if (this.matchStatus.booleanValue() != matchStatus) {
				this.matchStatus = null;
			}
		}
		
		@Override
		public final String toString() {
			return "(" + this.record1.getUID() + ", " + this.record2.getUID() + ")";
		}
	}
	
	private final static class DisagreementComparator implements Comparator<Entry<String, Integer>> {
		
		@Override
		public final int compare(final Entry<String, Integer> o1, final Entry<String, Integer> o2) {
			final int c = o1.getValue().compareTo(o2.getValue());
			if (c != 0) {
				return c;
			}
			return o1.getKey().compareTo(o2.getKey());
		}
	}
}
