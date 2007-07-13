package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;

/**
 * Class is used by ScorePair to modify the score used when comparing two Records.
 * 
 * It contains a list of Record objects that store regular expressions
 * as demographic values.  If a Record matches the pattern Record, then the
 * scaling is returned.  For example, if you need to get the modification for
 * the Record - first_name - foo
 * 				last_name - bar
 * 				yb - 1984
 * 
 * and there is a pattern Record of:
 * 				first_name - foo
 * 
 * then it will match and the scaling returned.  It is a Record that has
 * the effect of "all Record objects with a first_name value of 'foo' will
 * be scaled by <scale>"
 * 
 * If the pattern Record were:
 * 				first_name - *
 * 
 * then it will match every first name and all Records will be scaled.
 * 
 * @author jegg
 *
 */

public class ScoreModifier {
	
	HashMap<Record, Double> pattern_records;
	HashMap<Record, Matches> pattern_scope;
	public enum Matches { REC1, REC2, BOTH };
	
	public ScoreModifier(){
		pattern_records = new HashMap<Record, Double>();
		pattern_scope = new HashMap<Record, Matches>();
	}
	
	/**
	 * Method adds the given pattern Record to it's list of what to scale
	 * 
	 * @param pattern	a Record object containing regular expressions for demographics
	 * @param scale		the amount to scale the score when a Record matches pattern
	 */
	public void addPatternRecord(Record pattern, double scale, Matches which){
		pattern_records.put(pattern, new Double(scale));
		pattern_scope.put(pattern, which);
	}
	
	/**
	 * Method adds a scaling for every Record for the given demographic, regardless of
	 * value in that Record.
	 * 
	 * @param demographic	the column/demographic that is to be modified
	 * @param scale		the amount to scale the score Records
	 */
	public void addColumnModifier(String demographic, double scale, Matches which){
		addColumnValueModifier(demographic, "*", scale, which);
	}
	
	/**
	 * Method adds a scaling for every Record for the given demographic only when the
	 * value of the demographic matches the regular expression. 
	 * 
	 * @param demographic	the column/demographic that is to be modified
	 * @param regexp	the regular expression that the value in the Record needs to amtch
	 * @param scale		the amount to scale the score Records
	 */
	public void addColumnValueModifier(String demographic, String regexp, double scale, Matches which){
		Record pattern = new Record();
		pattern.addDemographic(demographic, regexp);
		addPatternRecord(pattern, scale, which);
	}
	
	private boolean matchesPattern(Record pattern, Record rec){
		boolean matches = true;
		for(String demographic : pattern.getDemographics().keySet()){
			String regex = pattern.getDemographic(demographic);
			String test_value = rec.getDemographic(demographic);
			
			matches = matches && test_value.matches(regex);
		}
		return matches;
	}
	
	private boolean matchesPattern(Record pattern, Record rec1, Record rec2, MatchVector mv){
		boolean matches = true;
		for(String demographic : pattern.getDemographics().keySet()){
			String regex = pattern.getDemographic(demographic);
			String test_value1 = rec1.getDemographic(demographic);
			String test_value2 = rec2.getDemographic(demographic);
			
			matches = matches && (test_value1.matches(regex) || test_value2.matches(regex)) && mv.matchedOn(demographic);
		}
		return matches;
	}
	
	private List<Record> getPatternRecord(Record rec1, Record rec2, MatchVector mv){
		List<Record> ret = new ArrayList<Record>();
		for(Record pattern : pattern_records.keySet()){
			boolean matches_rec1 = matchesPattern(pattern, rec1) && pattern_scope.get(pattern) == Matches.REC1;
			boolean matches_rec2 = matchesPattern(pattern, rec2) && pattern_scope.get(pattern) == Matches.REC2;
			boolean matches_both = pattern_scope.get(pattern) == Matches.BOTH && matchesPattern(pattern, rec1, rec2, mv);
			if(matches_rec1 || matches_rec2 || matches_both){
				ret.add(pattern);
			}
		}
		return ret;
	}
	
	/**
	 * Method modifies a MatchResult object's score according to the guidelines of this object.
	 * 
	 * @param mr	the MatchResult object to modify according to this object's rules
	 */
	public void modifyMatchResult(MatchResult mr){
		double score = mr.getScore();
		Record r1 = mr.getRecord1();
		Record r2 = mr.getRecord2();
		MatchVector mv = mr.getMatchVector();
		
		for(Record pattern : getPatternRecord(r1, r2, mv)){
			Double scale = pattern_records.get(pattern);
			if(scale != null){
				score *= pattern_records.get(pattern);
			}
		}
		
		mr.setScore(score);
	}
	
}
