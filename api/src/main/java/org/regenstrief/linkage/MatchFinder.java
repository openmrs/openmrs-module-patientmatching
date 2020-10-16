package org.regenstrief.linkage;

/**
 * Class is main way of using the record linkage code for module
 * 
 * It takes one LinkDataSource as a main source of Records to search,
 * and a list of MatchingConfigs to use.  It then takes one Record and
 * finds matches against the LinkDataSource.  It is slower than matching
 * 
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.analysis.UnMatchableRecordException;
import org.regenstrief.linkage.io.OrderedDataSourceFormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.io.VectorReader;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.ScorePair;

public class MatchFinder {
	
	LinkDataSource matching_database;
	
	ReaderProvider rp;
	
	//List<DataSourceReader> database_readers;
	//Hashtable<DataSourceReader,MatchingConfig> analytics_associations;
	Hashtable<MatchingConfig, ScorePair> analytics_scoring;
	
	List<MatchingConfig> analytics;
	
	Hashtable<String, Integer> type_table;
	
	RecordFieldAnalyzer record_analyzer;
	
	Scoring scoring;
	
	public enum Scoring {
		BLOCKING_EXCLUSIVE,
		BLOCKING_INCLUSIVE
	};
	
	public MatchFinder(LinkDataSource matching_database, ReaderProvider rp, List<MatchingConfig> analytics,
	    RecordFieldAnalyzer record_analyzer, Scoring scoring) {
		this.matching_database = matching_database;
		this.analytics = analytics;
		this.rp = rp;
		
		initAnalytics(analytics);
		
		type_table = matching_database.getTypeTable();
		this.record_analyzer = record_analyzer;
		this.scoring = scoring;
		
	}
	
	public MatchFinder(LinkDataSource matching_database, ReaderProvider rp, List<MatchingConfig> analytics,
	    Scoring scoring) {
		this.matching_database = matching_database;
		this.analytics = analytics;
		this.rp = rp;
		
		initAnalytics(analytics);
		
		type_table = matching_database.getTypeTable();
		record_analyzer = null;
		this.scoring = scoring;
		
	}
	
	/*
	 * Method initializes data structures holding information derived
	 * from the MatchingConfig objects, such as the ordered readers 
	 * and ScorePair Hash table
	 */
	private void initAnalytics(List<MatchingConfig> analytics) {
		analytics_scoring = new Hashtable<MatchingConfig, ScorePair>();
		//database_readers = new ArrayList<DataSourceReader>();
		Iterator<MatchingConfig> it = analytics.iterator();
		while (it.hasNext()) {
			MatchingConfig mc = it.next();
			analytics_scoring.put(mc, new ScorePair(mc));
		}
	}
	
	/**
	 * Method takes a Record object and tries to find a closest match in the Matching database.
	 * 
	 * @param test the Record object to evaluate matches against
	 * @return a list of records from the matching database that are similar to test
	 */
	public synchronized List<MatchResult> findMatch(Record test) throws UnMatchableRecordException {
		// check that Record test is valid
		if (record_analyzer != null) {
			int analysis_result = record_analyzer.analyzeRecordFields(test);
			if (analysis_result == RecordFieldAnalyzer.DISCARD) {
				throw new UnMatchableRecordException(test);
			}
		}
		
		VectorReader test_reader = new VectorReader(test);
		ArrayList<MatchResult> ret = new ArrayList<MatchResult>();
		
		// iterate through the database_readers and get possible matches
		Iterator<MatchingConfig> it = analytics.iterator();
		
		while (it.hasNext()) {
			MatchingConfig mc = it.next();
			
			// get the reader to use with this matching config
			OrderedDataSourceReader reader = rp.getReader(matching_database, mc, test);
			//DataSourceReader reader = (DataSourceReader)rp.getReader(matching_database, mc);
			
			List<MatchResult> mrs = getMatches(test_reader, reader, mc, type_table);
			
			if (mrs != null) {
				ret.addAll(mrs);
				
			}
			
			// reset or close database reader
			reader.close();
			test_reader.reset();
		}
		test_reader = null;
		return ret;
	}
	
	/*
	 * Method returns a list of Matches that meet the given MatchingConfig score_threshold
	 */
	protected synchronized List<MatchResult> getMatches(VectorReader test, OrderedDataSourceReader database_reader,
	        MatchingConfig analytics, Hashtable<String, Integer> type_table) {
		OrderedDataSourceFormPairs fp = new OrderedDataSourceFormPairs(test, database_reader, analytics, type_table);
		List<MatchResult> candidates = new ArrayList<MatchResult>();
		
		Record[] pair;
		ScorePair sp = analytics_scoring.get(analytics);
		if (sp == null) {
			//sp = new ScorePair(analytics);
			//analytics_scoring.put(analytics, sp);
			return null;
		}
		int pair_count = 0;
		while ((pair = fp.getNextRecordPair()) != null) {
			pair_count++;
			
			Record r1 = pair[0];
			Record r2 = pair[1];
			MatchResult mr = sp.scorePair(r1, r2);
			if (mr.getScore() >= analytics.getScoreThreshold()) {
				candidates.add(sp.scorePair(r1, r2));
			}
		}
		
		if (candidates.size() > 0) {
			sortMatchResultList(candidates);
			return candidates;
		} else {
			return null;
		}
	}
	
	/**
	 * Method returns a match with the highest score. If multiple records were found with the same top
	 * score, just one is chosen without preference.
	 * 
	 * @param test the Record object to evaluate matches against
	 * @return one Record object that has the highest score of similarity
	 */
	public MatchResult findBestMatch(Record test) throws UnMatchableRecordException {
		List<MatchResult> matches = findMatch(test);
		if (matches != null && matches.size() > 0) {
			sortMatchResultList(matches);
			MatchResult ret = matches.get(0);
			return ret;
		}
		
		return null;
	}
	
	/**
	 * Method returns a list of matches to Record test no larger than limit. The matches returned are
	 * the highest scoring matches if there were more possible matches than the limit.
	 * 
	 * @param test the Record object to evaluate matches against
	 * @param limit the maximum number of MatchResult objects desired
	 * @return the list of the highest scored MatchResult objects
	 */
	public List<MatchResult> findMatch(Record test, int limit) throws UnMatchableRecordException {
		List<MatchResult> matches = findMatch(test);
		if (matches.size() > limit) {
			sortMatchResultList(matches);
			matches.subList(limit, matches.size()).clear();
		}
		
		return matches;
	}
	
	/*
	 * Method sorts the List<MatchResult> so that the highest scores
	 * are listed first.  For scoring not including blocking fields,
	 * this requires Reverse order.  Scoring including blocking fields
	 * lists mr2 as the first argument to the Double.compare method to achieve this 
	 */
	protected void sortMatchResultList(List<MatchResult> matches) {
		if (scoring == Scoring.BLOCKING_EXCLUSIVE) {
			Collections.sort(matches, Collections.reverseOrder());
		}
		if (scoring == Scoring.BLOCKING_INCLUSIVE) {
			Collections.sort(matches, new Comparator<MatchResult>() {
				
				public int compare(MatchResult mr1, MatchResult mr2) {
					return Double.compare(mr2.getInclusiveScore(), mr1.getInclusiveScore());
				}
			});
		}
	}
	
	/**
	 * Method returns a list of matches to Record test if the score is above the threshold_limit value.
	 * 
	 * @param test the Record object to evaluate matches against
	 * @param threshold_limit the minimum score value the MatchResult objects must havee
	 * @return a list of MatchResult objects that are above limit
	 */
	public List<MatchResult> findMatch(Record test, double threshold_limit) throws UnMatchableRecordException {
		List<MatchResult> matches = findMatch(test);
		Iterator<MatchResult> it = matches.iterator();
		while (it.hasNext()) {
			MatchResult mr = it.next();
			if (scoring == Scoring.BLOCKING_EXCLUSIVE && mr.getScore() < threshold_limit) {
				it.remove();
			} else if (scoring == Scoring.BLOCKING_INCLUSIVE && mr.getInclusiveScore() < threshold_limit) {
				it.remove();
			}
		}
		return matches;
	}
	
	/**
	 * Method returns a list of matches containing no more elements than the limit argument and with no
	 * score lower than the score threshold limit.
	 * 
	 * @param test
	 * @param threshold_limit
	 * @param limit
	 * @return a list of matches that meet the requirements
	 */
	public List<MatchResult> findMatch(Record test, double threshold_limit, int limit) throws UnMatchableRecordException {
		List<MatchResult> matches = findMatch(test, threshold_limit);
		if (matches.size() > limit) {
			Collections.sort(matches, Collections.reverseOrder());
			matches.subList(limit, matches.size()).clear();
		}
		return matches;
	}
	
}
