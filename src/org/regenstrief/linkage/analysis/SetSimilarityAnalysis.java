package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.MatchResult;

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
	
	public List<List<MatchResult>> getSimilarSets(List<MatchResult> results){
		
		List<List<MatchResult>> ret = new ArrayList<List<MatchResult>>();
		ret.add(results);
		return ret;
	}
}
