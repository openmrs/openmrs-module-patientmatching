package org.regenstrief.linkage.db;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ManualReviewEvaluator {
	
	private static final int CERTAINTY_LEVELS = 4;
	
	private static final int MATCH_THRESHOLD = CERTAINTY_LEVELS / 2;
	
	private static final Map<Long, ManualReviewResult> map = new LinkedHashMap<Long, ManualReviewResult>();
	
	private static int numReviewers = 0;
	
	private static PrintStream out = null;
	
	public final static void main(final String[] args) throws Exception {
		run(args[0], (args.length < 2) ? "3" : args[1], (args.length < 3) ? null : args[2]);
	}
	
	private final static void run(final String dbFiles, final String outputLevel, final String outFile) throws Exception {
		final List<String> dbFileList = Arrays.asList(dbFiles.split(","));
		numReviewers = dbFileList.size();
		out = (outFile == null) ? System.out : new PrintStream(new FileOutputStream(outFile));
		if ("all".equalsIgnoreCase(outputLevel)) {
			info("Starting all output levels");
			for (int i = 1; i <= 3; i++) {
				run(dbFileList, i);
			}
			info("Finished all output levels");
		} else {
			run(dbFileList, Integer.parseInt(outputLevel));
		}
	}
	
	private final static void run(final List<String> dbFileList, final int outputLevel) throws Exception {
		info("Starting");
		info("Database Files - " + dbFileList);
		info("Output Level - " + outputLevel);
		map.clear();
		for (final String dbFile : dbFileList) {
			runFile(dbFile);
		}
		final StringBuilder b = new StringBuilder();
		final Map<String, Integer> dissenters = new TreeMap<String, Integer>();
		for (final Entry<Long, ManualReviewResult> entry : map.entrySet()) {
			b.setLength(0);
			b.append(entry.getKey()).append(" - ");
			final ManualReviewResult result = entry.getValue();
			for (final Entry<Integer, List<String>> scoreEntry : result.scoreToReviewer.entrySet()) {
				final List<String> reviewers = scoreEntry.getValue();
				if (reviewers.size() == 1) {
					final boolean matchStatus = getMatchStatus(scoreEntry.getKey().intValue());
					boolean dissent = true;
					for (final Entry<Integer, List<String>> otherScoreEntry : result.scoreToReviewer.entrySet()) {
						final List<String> otherReviewers = otherScoreEntry.getValue();
						if (otherReviewers == reviewers) {
							continue;
						} else if (getMatchStatus(otherScoreEntry.getKey().intValue()) == matchStatus) {
							dissent = false;
							break;
						}
					}
					if (dissent) {
						final String dissenter = reviewers.get(0);
						final Integer count = dissenters.get(dissenter);
						dissenters.put(dissenter, Integer.valueOf((count == null) ? 1 : (count.intValue() + 1)));
					}
				}
			}
			if (!result.append(b, outputLevel)) {
				continue;
			}
			out.println(b);
		}
		for (final Entry<String, Integer> entry : dissenters.entrySet()) {
			final int count = entry.getValue().intValue();
			info(entry.getKey() + " disagreed with all other reviewers " + count + " time" + ((count == 1) ? "" : "s"));
		}
		info("Finished");
	}
	
	private final static void runFile(final String dbDef) throws Exception {
		final int i = dbDef.indexOf('*');
		final String dbFile, reviewer;
		if (i > 0) {
			reviewer = dbDef.substring(0, i);
			dbFile = dbDef.substring(i + 1);
		} else {
			reviewer = dbFile = dbDef;
		}
		final Connection con = SavedResultDBConnection.openDBResults(dbFile);
		try {
			final PreparedStatement stmt = con.prepareStatement("select id,certainty from matchresult");
			final ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				final long id = rs.getLong(1);
				final double certainty = rs.getDouble(2);
				if (rs.wasNull()) {
					throw new IllegalStateException("Found null certainty for " + reviewer);
				} else if (certainty < 0) {
					throw new IllegalStateException("Found certainty " + certainty + " < 0 for " + reviewer);
				} else if (certainty > 1) {
					throw new IllegalStateException("Found certainty " + certainty + " > 1 for " + reviewer);
				}
				final Long key = Long.valueOf(id);
				ManualReviewResult result = map.get(key);
				if (result == null) {
					result = new ManualReviewResult();
					map.put(key, result);
				}
				result.add(certainty, reviewer);
			}
			rs.close();
			stmt.close();
		}
		finally {
			con.close();
		}
	}
	
	protected static int getMatchScore(final double certainty) {
		return ((int) Math.round(certainty * (CERTAINTY_LEVELS - 1))) + 1;
	}
	
	protected static boolean getMatchStatus(final int score) {
		return score > MATCH_THRESHOLD;
	}
	
	private final static void info(final Object s) {
		System.out.println(new Date() + " - " + s);
	}
	
	private final static class ManualReviewResult {
		
		private final Map<Integer, List<String>> scoreToReviewer = new TreeMap<Integer, List<String>>();
		
		private final void add(final double certainty, final String reviewer) {
			final int score = getMatchScore(certainty);
			final Integer key = Integer.valueOf(score);
			List<String> reviewers = this.scoreToReviewer.get(key);
			if (reviewers == null) {
				reviewers = new ArrayList<String>(numReviewers);
				this.scoreToReviewer.put(key, reviewers);
			}
			reviewers.add(reviewer);
		}
		
		private Boolean getMatchStatusIfAgreed() {
			Boolean matchStatus = null;
			for (final Integer score : this.scoreToReviewer.keySet()) {
				final boolean currentMatchStatus = getMatchStatus(score.intValue());
				if (matchStatus == null) {
					matchStatus = Boolean.valueOf(currentMatchStatus);
				} else if (matchStatus.booleanValue() != currentMatchStatus) {
					return null;
				}
			}
			return matchStatus;
		}
		
		private boolean append(final StringBuilder b, final int level) {
			final Boolean matchStatus = getMatchStatusIfAgreed();
			if (this.scoreToReviewer.size() == 1) {
				if (level < 3) {
					return false;
				}
				b.append("Status: ").append(matchStatus).append("; Score: ")
				        .append(this.scoreToReviewer.keySet().iterator().next());
				return true;
			}
			b.append("Status: ");
			if (matchStatus == null) {
				b.append("discordant");
			} else {
				if (level < 2) {
					return false;
				}
				b.append(matchStatus);
			}
			b.append("; Score: ");
			boolean firstScore = true;
			for (final Entry<Integer, List<String>> entry : this.scoreToReviewer.entrySet()) {
				if (firstScore) {
					firstScore = false;
				} else {
					b.append(" & ");
				}
				b.append(entry.getKey()).append(" (");
				boolean firstReviewer = true;
				for (final String reviewer : entry.getValue()) {
					if (firstReviewer) {
						firstReviewer = false;
					} else {
						b.append(", ");
					}
					b.append(reviewer);
				}
				b.append(')');
			}
			return true;
		}
	}
}
