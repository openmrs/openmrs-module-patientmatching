package org.regenstrief.linkage;

/**
 * Class is main way of using the record likage code.
 *
 */

import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.analysis.*;

import java.util.*;

public class MatchFinder {
	LinkDataSource matching_database;
	DataSourceReader database_reader;
	MatchingConfig analytics;
	Hashtable<String, Integer> type_table;
	RecordFieldAnalyzer record_analyzer;
	
	public MatchFinder(LinkDataSource matching_database, MatchingConfig analytics, RecordFieldAnalyzer record_analyzer){
		this.matching_database = matching_database;
		this.analytics = analytics;
		if(matching_database.getType().equals("CharDelimFile")){
			database_reader = new OrderedCharDelimFileReader(matching_database, analytics);
		} else if(matching_database.getType().equals("DataBase")){
			database_reader = new OrderedDataBaseReader(matching_database, analytics);
		} else if(matching_database.getType().equals("Vector")){
			database_reader = new VectorReader(matching_database, analytics);
		}
		type_table = matching_database.getTypeTable();
		this.record_analyzer = record_analyzer;
		
	}
	
	public MatchFinder(LinkDataSource matching_database, MatchingConfig analytics){
		this.matching_database = matching_database;
		this.analytics = analytics;
		if(matching_database.getType().equals("CharDelimFile")){
			database_reader = new OrderedCharDelimFileReader(matching_database, analytics);
		} else if(matching_database.getType().equals("DataBase")){
			database_reader = new OrderedDataBaseReader(matching_database, analytics);
		} else if(matching_database.getType().equals("Vector")){
			database_reader = new VectorReader(matching_database, analytics);
		}
		type_table = matching_database.getTypeTable();
		record_analyzer = null;
	}
	
	/**
	 * Method takes a Record object and tries to find a closest match
	 * in the Matching database.
	 * 
	 * @param test	the Record object to evaulate matches against
	 * @return	a list of records from the matching database that are similar to test
	 */
	public List<MatchResult> findMatch(Record test) throws UnMatchableRecordException {
		// check that Record test is valid
		if(record_analyzer != null){
			int analysis_result = record_analyzer.analyzeRecordFields(test);
			if(analysis_result == RecordFieldAnalyzer.DISCARD){
				throw new UnMatchableRecordException(test);
			}
		}
		
		VectorReader test_reader = new VectorReader(test);
		ArrayList<MatchResult> ret = new ArrayList<MatchResult>();
		org.regenstrief.linkage.io.FormPairs fp = new org.regenstrief.linkage.io.FormPairs(test_reader, database_reader, analytics, type_table);
		
		Record[] pair;
		ScorePair sp = new ScorePair(analytics);
		while((pair = fp.getNextRecordPair()) != null){
			Record r1 = pair[0];
			Record r2 = pair[1];
			ret.add(sp.scorePair(r1, r2));
		}
		
		// reset database reader
		// need to reinitialize if it's a database reader, since writes might have been performed
		if(matching_database.getType().equals("DataBase")){
			database_reader = new OrderedDataBaseReader(matching_database, analytics);
		} else if(!database_reader.reset()){
			System.err.println("unable to reset database reader after matching");
		}
		
		return ret;
	}
	
	/**
	 * Method returns a match with the highest score.  If multiple records
	 * were found with the same top score, just one is chosen without preference.
	 * 
	 * @param test	the Record object to evaulate matches against
	 * @return	one Record object that has the highest score of similarity
	 */
	public MatchResult findBestMatch(Record test) throws UnMatchableRecordException{
		List<MatchResult> matches = findMatch(test);
		if(matches != null && matches.size() > 0){
			Collections.sort(matches, Collections.reverseOrder());
			return matches.get(0);
		}
		
		return null;
	}
	
	/**
	 * Method returns a list of matches to Record test no larger than limit.  The
	 * matches returned are the highest scoring matches if there were more 
	 * possible matches than the limit.
	 * 
	 * @param test	the Record object to evaluate matches against
	 * @param limit	the maximum number of MatchResult objects desired
	 * @return	the list of the highest scored MatchResult objects
	 */
	public List<MatchResult> findMatch(Record test, int limit) throws UnMatchableRecordException{
		List<MatchResult> matches = findMatch(test);
		if(matches.size() > limit){
			Collections.sort(matches, Collections.reverseOrder());
			matches.subList(limit, matches.size()).clear();
		}
		
		return matches;
	}
	
	/**
	 * Method returns a list of matches to Record test if the score is above
	 * the threshold_limit value.
	 * 
	 * @param test	the Record object to evaluate matches against
	 * @param threshold_limit	the minimum score value the MatchResult objects must havee
	 * @return	a list of MatchResult objects that are above limit
	 */
	public List<MatchResult> findMatch(Record test, double threshold_limit) throws UnMatchableRecordException{
		List<MatchResult> matches = findMatch(test);
		Iterator<MatchResult> it = matches.iterator();
		while(it.hasNext()){
			MatchResult mr = it.next();
			if(mr.getScore() < threshold_limit){
				it.remove();
			}
		}
		return matches;
	}
	
	/**
	 * Method returns a list of matches containing no more elements than the limit
	 * argument and with no score lower than the score threshold limit.
	 * 
	 * @param test
	 * @param threshold_limit
	 * @param limit
	 * @return a list of matches that meet the requirements
	 */
	public List<MatchResult> findMatch(Record test, double threshold_limit, int limit) throws UnMatchableRecordException{
		List<MatchResult> matches = findMatch(test, threshold_limit);
		if(matches.size() > limit){
			Collections.sort(matches, Collections.reverseOrder());
			matches.subList(limit, matches.size()).clear();
		}
		return matches;
	}
}
