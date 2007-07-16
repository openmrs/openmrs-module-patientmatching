package org.regenstrief.linkage;

import java.util.*;

/**
 * Class represents the results of matching two Record objects.
 * There is the main score that is a numeric represntation of
 * how well the two records match as well as a Hashtable that
 * stores boolean values for the given demographic indicating
 * whether for the given demographic the two records were a
 * match. 
 */

public class MatchResult implements Comparable{
	private double score, true_prob, false_prob, sensitivity, specificity;
	private MatchVector match_vct;
	private ScoreVector score_vct;
	private Record r1, r2;
	
	/**
	 * Constructor initializes the Hashtable match_table
	 *
	 */
	public MatchResult(double score, double true_prob, double false_prob, double sensitivity, double specificity, MatchVector match_vct, ScoreVector score_vct, Record r1, Record r2){
		this.score = score;
		this.true_prob = true_prob;
		this.false_prob = false_prob;
		this.sensitivity = sensitivity;
		this.specificity = specificity;
		this.match_vct = match_vct;
		this.score_vct = score_vct;
		this.r1 = r1;
		this.r2 = r2;
	}
	
	/**
	 * Returns the match score
	 * 
	 * @return	the score of the match
	 */
	public double getScore(){
		return score;
	}
	
	public double getTrueProbability(){
		return true_prob;
	}
	
	public double getFalseProbability(){
		return false_prob;
	}
	
	public Record getRecord1(){
		return r1;
	}
	
	public Record getRecord2(){
		return r2;
	}
	
	/**
	 * 
	 * @return	the MatchResult's MatchVector
	 */
	public MatchVector getMatchVector(){
		return match_vct;
	}
	
	/**
	 * 
	 * @return	the MatchResult's ScoreVector
	 */
	public ScoreVector getScoreVector(){
		return score_vct;
	}
	
	public void setScoreVector(ScoreVector score_vct){
		this.score_vct = score_vct;
	}
	
	/**
	 * Returns a list of the keys in the match table that contain the demographics
	 * used in this record matching
	 * 
	 * @return	a List<String> of the demographics in the matching
	 */
	public List<String> getDemographics(){
		return match_vct.getDemographics();
	}
	
	/**
	 * Returns whether the two records matched on the given demographic
	 * 
	 * @param demographic	the demographic of interest
	 * @return	whether the demographic matched for the two Records
	 */
	public boolean matchedOn(String demographic){
		return match_vct.matchedOn(demographic);
	}
	
	/**
	 * Returns the sensitivity of the record pair
	 * 
	 * @return	the sensitivity
	 */
	public double getSensitivity(){
		return sensitivity;
	}
	
	/**
	 * Returns the specificity of the record pair
	 * 
	 * @return	the specificity
	 */
	public double getSpecificity(){
		return specificity;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public int compareTo(Object o) throws ClassCastException{
		MatchResult mr = (MatchResult) o;
		return new Double(score).compareTo(new Double(mr.getScore()));
	}
	
	public String toString(){
		return Double.toString(score);
	}
}
