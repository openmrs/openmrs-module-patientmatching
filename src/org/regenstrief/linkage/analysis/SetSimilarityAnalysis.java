package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

/**
 * Class written to group like Records together.  If Records A, B, C, and D
 * were matched, and the matching determined that A = B and B = D, then
 * A should be grouped with D.
 * 
 * Normal matching returns a list of MatchResults.  This object takes that list
 * and returns a list of list of MatchResults, where every MatchResult from the
 * argument List is present in the return object, even if it's one element lists.
 * 
 * Where a MatchResult is A=B, a List of MatchResults like
 * {A=B,C=D,E=F,C=G}
 * would be returned as
 * {{A=B},{C=D,C=G},{E=F}}
 * 
 * 
 * @author jegg
 *
 */

public class SetSimilarityAnalysis {
	
	/**
	 * Method returns the argument list separated out into sub lists
	 * based on similarity between both Records in the MatchResult objects
	 * 
	 * @param results	a single list of all the MatchResult objects
	 * @return	a sublists of results, where all MatchResult objects in a sublist have Records in common
	 */
	public List<List<MatchResult>> getSimilarSets(List<MatchResult> results){
		/*
		 * Method not currently implemented, just copies results to ret
		 */
		
		List<List<MatchResult>> ret = new ArrayList<List<MatchResult>>();
		ret.add(results);
		return ret;
	}
	
	/**
	 * Method performs the same grouping as getSimilarSets(List<MatchResult> results), but
	 * it takes a threshold that MatchResult objects must meet before being inserted into
	 * the return object
	 * 
	 * @param results	a single list of all the MatchResult objects
	 * @param threshold	a score requirement for a MatchResult to be copied into the return object
	 * @return	a sublists of results, where all MatchResult objects in a sublist have Records in common and meet the threshold
	 */
	public List<List<MatchResult>> getSimilarSets(List<MatchResult> results, double threshold){
		List<MatchResult> true_matches = new ArrayList<MatchResult>();
		Iterator<MatchResult> it = results.iterator();
		while(it.hasNext()){
			MatchResult mr = it.next();
			if(mr.getScore() > threshold){
				true_matches.add(mr);
			}
		}
		
		return getSimilarSets(true_matches);
	}
	
	/**
	 * Method flattens the results of getSimilarSets methods and just returns
	 * lists of Records without matching performance information
	 * 
	 * @param match_result_sets	lists of lists of MatchResult objects
	 * @return	lists of Records, each list having Records determined to be the same entity
	 */
	public List<List<Record>> getSimilarPatients(List<List<MatchResult>> match_result_sets){
		Iterator<List<MatchResult>> it = match_result_sets.iterator();
		List<List<Record>> ret = new ArrayList<List<Record>>();
		
		while(it.hasNext()){
			List<MatchResult> set = it.next();
			List<Record> current_records = new ArrayList<Record>();
			Iterator<MatchResult> it2 = set.iterator();
			while(it2.hasNext()){
				MatchResult mr = it2.next();
				// currently, Records may appear twice within a list
				current_records.add(mr.getRecord1());
				current_records.add(mr.getRecord2());
			}
			ret.add(current_records);
		}
		
		return ret;
	}
}
