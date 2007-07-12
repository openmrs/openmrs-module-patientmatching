package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	public ScoreModifier(){
		pattern_records = new HashMap<Record, Double>();
	}
	
	/**
	 * Method adds the given pattern Record to it's list of what to scale
	 * 
	 * @param pattern	a Record object containing regular expressions for demographics
	 * @param scale		the amount to scale the score when a Record matches pattern
	 */
	public void addPatternRecord(Record pattern, double scale){
		pattern_records.put(pattern, new Double(scale));
	}
	
	/**
	 * Method adds a scaling for every Record for the given demographic, regardless of
	 * value in that Record.
	 * 
	 * @param demographic	the column/demographic that is to be modified
	 * @param scale		the amount to scale the score Records
	 */
	public void addColumnModifier(String demographic, double scale){
		addColumnValueModifier(demographic, "*", scale);
	}
	
	/**
	 * Method adds a scaling for every Record for the given demographic only when the
	 * value of the demographic matches the regular expression. 
	 * 
	 * @param demographic	the column/demographic that is to be modified
	 * @param regexp	the regular expression that the value in the Record needs to amtch
	 * @param scale		the amount to scale the score Records
	 */
	public void addColumnValueModifier(String demographic, String regexp, double scale){
		Record pattern = new Record();
		pattern.addDemographic(demographic, regexp);
		addPatternRecord(pattern, scale);
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
	
	private List<Record> getPatternRecord(Record rec){
		List<Record> ret = new ArrayList<Record>();
		for(Record pattern : pattern_records.keySet()){
			if(matchesPattern(pattern, rec)){
				ret.add(pattern);
			}
		}
		return ret;
	}
	
	/**
	 * Method returns the scaling to be applied to
	 * 
	 * @param rec
	 * @return
	 */
	public double getScale(Record rec){
		List<Record> patterns = getPatternRecord(rec);
		double scaling = 1;
		if(patterns != null && patterns.size() > 0){
			for(Record pattern : patterns){
				Double pattern_scaling = pattern_records.get(pattern);
				if(pattern_scaling != null){
					scaling *= pattern_scaling;
				}
			}
		}
		
		return scaling;
	}
	
	
}
