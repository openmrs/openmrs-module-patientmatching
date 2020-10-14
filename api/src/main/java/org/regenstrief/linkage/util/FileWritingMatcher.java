package org.regenstrief.linkage.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchItem;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.RecordLink;
import org.regenstrief.linkage.SameEntityRecordGroup;
import org.regenstrief.linkage.analysis.NullDemographicScoreModifier;
import org.regenstrief.linkage.analysis.SetSimilarityAnalysis;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.db.SavedResultDBConnection;
import org.regenstrief.linkage.io.DedupOrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.NoMatchFilteringFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.matchresult.DBMatchResultStore;

/**
 * Purpose of class is to find matches between two data sources using all the given MatchingConfigs
 * and write a simple output file This class is a temp measure until better output can be created
 * 
 * @author jegg
 */
public class FileWritingMatcher {
	
	public static final String OUT_FILE = "linkage.out";
	
	private static boolean scoreNeededInOutput = Boolean
	        .getBoolean("org.regenstrief.linkage.util.FileWritingMatcher.scoreNeededInOutput");
	
	public static File writeMatchResults(RecMatchConfig rmc) {
		return writeMatchResults(rmc, new File(OUT_FILE), false, 0, false, false, false);
	}
	
	public static File writeMatchResults(RecMatchConfig rmc, File f, boolean writeXml, int reviewers, boolean groupAnalysis,
	        boolean vectorObs, boolean filterPairs) {
		if (writeXml) {
			throw new UnsupportedOperationException(
			        "writeXml not supported; depended on a result list that is no longer maintained");
		}
		final int numDbs = (reviewers == 2) ? 1 : reviewers;
		final List<int[]> reviewerPairs = (numDbs > 1) ? generateReviewerPairs(reviewers) : null;
		// set output order based on include position in lds
		final LinkDataSource lds = rmc.getLinkDataSource1();
		final List<String> names = new ArrayList<String>();
		for (final DataColumn dc : lds.getDataColumns()) {
			if (!dc.getName().equals(lds.getUniqueID())) {
				names.add(dc.getName());
			}
		}
		final String[] order = names.toArray(new String[lds.getIncludeCount() - 1]);
		final Date report_time = new Date();
		BufferedWriter fout = null;
		try {
			final ReaderProvider rp = ReaderProvider.getInstance();
			
			// if diong a group analysis, then create list for all RecordLink objects from blocking runs
			List<RecordLink> all_links = null;
			if (groupAnalysis) {
				all_links = new ArrayList<RecordLink>();
			}
			
			// iterate over each MatchingConfig
			final StringBuilder buf = new StringBuilder();
			for (final MatchingConfig mc : rmc.getMatchingConfigs()) {
				final File f2 = new File(f.getPath() + "_" + mc.getName() + ".txt");
				fout = new BufferedWriter(new FileWriter(f2));
				
				// write db if needed
				final DBMatchResultStore[] mrss = new DBMatchResultStore[numDbs];
				for (int i = 0; i < numDbs; i++) {
					final Connection db = SavedResultDBConnection.openDBResults(new File(f2.getPath() + "." + i + ".db"));
					SavedResultDBConnection.createMatchResultTables(db);
					try {
						db.setAutoCommit(false);
					}
					catch (SQLException e) {
						throw new RuntimeException(e);
					}
					final DBMatchResultStore mrs = new DBMatchResultStore(db);
					mrs.setDate(report_time);
					mrss[i] = mrs;
				}
				
				final OrderedDataSourceReader odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				final OrderedDataSourceReader odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);
				/*if (odsr1 != null && odsr2 != null) {
					// analyze with EM
					org.regenstrief.linkage.io.FormPairs fp2 = new org.regenstrief.linkage.io.FormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
					EMAnalyzer ema = new EMAnalyzer(rmc.getLinkDataSource1(), rmc.getLinkDataSource2(), mc);
					ema.analyzeRecordPairs(fp2, mc);
				}
				odsr1.close();
				odsr2.close();
				
				// create form pair object
				odsr1 = rp.getReader(rmc.getLinkDataSource1(), mc);
				odsr2 = rp.getReader(rmc.getLinkDataSource2(), mc);*/
				if (odsr1 != null && odsr2 != null) {
					FormPairs fp;
					if (rmc.isDeduplication()) {
						fp = new DedupOrderedDataSourceFormPairs(odsr1, mc, rmc.getLinkDataSource1().getTypeTable());
					} else {
						fp = new OrderedDataSourceFormPairs(odsr1, odsr2, mc, rmc.getLinkDataSource1().getTypeTable());
					}
					
					if (filterPairs) {
						fp = new NoMatchFilteringFormPairs(fp);
					}
					
					final ScorePair sp = new ScorePair(mc);
					
					// check if scoring needs to be modified
					if (mc.isNullScoring()) {
						sp.addScoreModifier(new NullDemographicScoreModifier());
					}
					
					Record[] pair;
					int count = 0;
					while ((pair = fp.getNextRecordPair()) != null) {
						final MatchResult mr = sp.scorePair(pair[0], pair[1]);
						
						// changed to write output line without sorting results first
						fout.write(getOutputLine(buf, mr, order, true));
						
						// add match result to db, if needed
						if (numDbs == 1) {
							mrss[0].addMatchResult(mr, count);
						} else if (numDbs > 1) {
							final int reviewerPairIndex = count % reviewerPairs.size();
							final int[] reviewerPair = reviewerPairs.get(reviewerPairIndex);
							final DBMatchResultStore mrs1 = mrss[reviewerPair[0]], mrs2 = mrss[reviewerPair[1]];
							mrs1.addMatchResultToEachStore(mrs2, mr);
						}
						
						// add to grouping list, if needed
						if (all_links != null) {
							if (mr.getScore() >= mc.getScoreThreshold()) {
								all_links.add(mr);
							}
						}
						
						count++;
					}
					
					if (filterPairs && fp instanceof NoMatchFilteringFormPairs) {
						final NoMatchFilteringFormPairs nmffp = (NoMatchFilteringFormPairs) fp;
						System.out.println("filtered " + nmffp.getFilteredCount());
						System.out.println("allowed " + nmffp.getAllowedCount() + " pairs to output file");
					}
					
					for (final DBMatchResultStore mrs : mrss) {
						mrs.addIndexes();
						mrs.close();
						final Connection db = mrs.getDb();
						try {
							db.commit();
							db.close();
						}
						catch (final SQLException e) {
							throw new RuntimeException(e);
						}
					}
					
					// write to an xml file also, to test this new format
					//if (writeXml) {
					//	final File xml_out = new File(f2.getPath() + ".xml");
					//	MatchResultsXML.resultsToXML(results, xml_out); // results no longer populated, maybe to conserve memory
					//}
					
					if (vectorObs) {
						BufferedWriter v_out = new BufferedWriter(new FileWriter(new File(f2.getPath() + "_vectors.txt")));
						
						// print header with vector details
						final Hashtable<MatchVector, Long> vectors = sp.getObservedVectors();
						final Iterator<MatchVector> mv_it = vectors.keySet().iterator();
						if (mv_it.hasNext()) {
							boolean first = true;
							for (final String dem : mv_it.next().getDemographics()) {
								if (first) {
									v_out.write(dem);
									first = false;
								} else {
									v_out.write("," + dem);
								}
							}
						}
						
						v_out.write("|score|true_prob|false_prob|expected|observed\n");
						final double p = mc.getP();
						final VectorTable vt = new VectorTable(mc);
						for (final MatchVector mv_obs : vectors.keySet()) {
							final Long l = vectors.get(mv_obs);
							final double score = vt.getScore(mv_obs);
							// expected = (true_probability * p * count) + (false_probability * (1 - p) * count
							final double expected_true = vt.getMatchVectorTrueProbability(mv_obs) * p * count;
							final double expected_false = vt.getMatchVectorFalseProbability(mv_obs) * (1 - p) * count;
							final double expected = expected_true + expected_false;
							v_out.write("\"" + mv_obs + "\"|" + score + "|" + vt.getMatchVectorTrueProbability(mv_obs) + "|"
							        + vt.getMatchVectorFalseProbability(mv_obs) + "|" + expected + "|" + l + "\n");
						}
						v_out.flush();
						v_out.close();
					}
				}
				
				fout.flush();
				fout.close();
			}
			
			if (groupAnalysis) {
				// run group analysis and write results to files
				final List<SameEntityRecordGroup> groups = new SetSimilarityAnalysis().getRecordGroups(all_links);
				fout = new BufferedWriter(new FileWriter(new File(f.getPath() + "_groups.txt")));
				
				// iterate over members of groups List
				for (final SameEntityRecordGroup entity : groups) {
					final String group_id = Integer.toString(entity.getGroupID());
					for (final RecordLink link : entity.getGroupLinks()) {
						if (link instanceof MatchResult) {
							fout.write(group_id + "|" + getOutputLine(buf, (MatchResult) link, order, true));
						}
					}
				}
				fout.flush();
				fout.close();
				
				// write xml file if xml output is checked
				if (writeXml) {
					MatchResultsXML.groupsToXML(groups, new File(f.getPath() + "_groups.xml"));
				}
			}
		}
		catch (final IOException e) {
			throw new RuntimeException("error writing linkage results", e);
		}
		finally {
			close(fout);
		}
		
		return f;
	}
	
	private final static List<int[]> generateReviewerPairs(final int reviewers) {
		final List<int[]> reviewerPairs = new ArrayList<int[]>();
		final int reviewers1 = reviewers - 1;
		for (int i = 0; i < reviewers1; i++) {
			for (int j = i + 1; j < reviewers; j++) {
				reviewerPairs.add(new int[] { i, j });
			}
		}
		return reviewerPairs;
	}
	
	private final static void close(final Closeable c) {
		if (c != null) {
			try {
				c.close();
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String getOutputLine(MatchResult mr, String[] order) {
		final StringBuilder s = new StringBuilder();
		return getOutputLine(s, mr, order, false);
	}
	
	public static String getOutputLine(final StringBuilder s, MatchResult mr, String[] order, boolean lineBreak) {
		s.setLength(0);
		s.append(mr.getScore());
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		s.append('|').append(r1.getUID()).append('|').append(r2.getUID());
		final MatchVector matchVector = mr.getMatchVector();
		for (int i = 0; i < order.length; i++) {
			String demographic = order[i];
			s.append('|').append(r1.getDemographic(demographic)).append('|').append(r2.getDemographic(demographic));
			if (isScoreNeededInOutput()) {
				s.append('|');
				MatchItem matchItem = matchVector.getMatchItem(demographic);
				if (matchItem != null) {
					s.append(matchItem.getSimilarity());
				}
			}
		}
		if (lineBreak) {
			s.append('\n');
		}
		return s.toString();
	}
	
	public static boolean isScoreNeededInOutput() {
		return scoreNeededInOutput;
	}
	
	public static void setScoreNeededInOutput(boolean scoreNeededInOutput) {
		FileWritingMatcher.scoreNeededInOutput = scoreNeededInOutput;
	}
}
