package org.regenstrief.linkage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class created to provide information on specific column score values
 * to store the individual scores that made up the final score.  It will
 * be stored in a MatchResult object by ScorePair and used by Modifier
 * objects.
 * 
 * @author jegg
 *
 */

public class ScoreVector {
	private HashMap<String,Double> score_table;
	
	public ScoreVector(){
		score_table = new HashMap<String,Double>();
	}
	
	public int getSize() {
		return score_table.size();
	}
	
	/**
	 * Method sets an entry to store whether the two records matched
	 * on the given demographic.
	 * 
	 * @param demographic	the demographic used in the comparison
	 * @param score	the score value the demographic contributes to the final score
	 */
	public void setScore(String demographic, double score){
		score_table.put(demographic, Double.valueOf(score));
	}
	
	/**
	 * Returns a list of the keys in the match table that contain the demographics
	 * used in this record matching
	 * 
	 * @return	a List<String> of the demographics in the matching
	 */
	public List<String> getDemographics(){
		Iterator<String> it = score_table.keySet().iterator();
		List<String> ret = new ArrayList<String>();
		while(it.hasNext()){
			ret.add(it.next());
		}
		return ret;
	}
	
	/**
	 * Returns whether the two records matched on the given demographic
	 * 
	 * @param demographic	the demographic of interest
	 * @return	the score value of the demographic used to calculate score
	 */
	public double getScore(String demographic){
		Double score = score_table.get(demographic);
		if(score == null){
			return 0;
		} else {
			return score.doubleValue();
		}
	}
	
	public double getTotalScore() {
		double score = 0;
		for(double indv_score : score_table.values()) {
			score += indv_score;
		}
		return score;
	}
	
	/**
	 * 
	 * @return returns the matches for the comparison
	 */
	public HashMap<String,Double> getScoreTable(){
		return score_table;
	}
	
	public int hashCode(){
		return Integer.parseInt(this.toString(), 2);
	}
	
	public String toString(){
		String ret = "";
		Iterator<String> it = score_table.keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			double score = score_table.get(demographic);
			ret += demographic + ": " + score;
			if(it.hasNext()){
				ret += "\t";
			}
		}
		
		return ret;
	}
}
