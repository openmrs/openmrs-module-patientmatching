package org.regenstrief.linkage;

import java.util.List;

import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.StringMatch;

/**
 * Class represents the results of matching two Record objects.
 * There is the main score that is a numeric represntation of
 * how well the two records match as well as a Hashtable that
 * stores boolean values for the given demographic indicating
 * whether for the given demographic the two records were a
 * match. 
 * 
 * TODO: Implement hashCode()
 */

public class MatchResult extends RecordLink implements Comparable{
	protected double score, incl_score, true_prob, false_prob, sensitivity, specificity;
	protected MatchVector match_vct;
	protected ScoreVector score_vct;
	protected MatchingConfig mc;
	protected int match_status;
	protected double certainty;
	protected String note;
	
	public static final int MATCH = 1;
	public static final int NON_MATCH = -1;
	public static final int UNKNOWN = 0;
	/**
	 * Constructor initializes the Hashtable match_table
	 *
	 */
	public MatchResult(double score, double incl_score, double true_prob, double false_prob, double sensitivity, double specificity, MatchVector match_vct, ScoreVector score_vct, Record r1, Record r2, MatchingConfig mc){
		super(r1, r2);
		this.score = score;
		this.incl_score = incl_score;
		this.true_prob = true_prob;
		this.false_prob = false_prob;
		this.sensitivity = sensitivity;
		this.specificity = specificity;
		this.match_vct = match_vct;
		this.score_vct = score_vct;
		this.mc = mc;
		match_status = UNKNOWN;
		certainty = 0;
	}
	
	public int getMatch_status() {
		return match_status;
	}

	public void setMatch_status(int match_status) {
		this.match_status = match_status;
	}

	public double getCertainty() {
		return certainty;
	}

	public void setCertainty(double certainty) {
		this.certainty = certainty;
	}

	/**
	 * Returns the match score
	 * 
	 * @return	the score of the match
	 */
	public double getScore(){
		return score;
	}
	
	public double getInclusiveScore(){
		return incl_score;
	}
	
	public double getTrueProbability(){
		return true_prob;
	}
	
	public double getFalseProbability(){
		return false_prob;
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
		this.score = score_vct.getTotalScore();
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
	
	/**
	 * Method returns the MatchingConfig that the comparisons were performed with
	 * 
	 * @return	the MatchingConfig object used to create the match result
	 */
	public MatchingConfig getMatchingConfig(){
		return mc;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	/**
	 * Method returns the value between 0 and 1 that the used  comparator calculated for
	 * the given demographic and the two values for the two records
	 * 
	 * @param demographic	the demographic of interest
	 * @return	the similarity of the two values using the comparator designated in the MatchingConfig object
	 */
	public double getSimilarityScore(String demographic){
		double ret = 0;
		MatchingConfigRow mcr = mc.getMatchingConfigRowByName(demographic);
		int algorithm = mcr.getAlgorithm();
		String val1 = r1.getDemographic(demographic);
		String val2 = r2.getDemographic(demographic);
		switch(algorithm){
		case MatchingConfig.EXACT_MATCH:
			ret = StringMatch.getExactMatchSimilarity(val1, val2);
			break;
		case MatchingConfig.LCS:
			ret = StringMatch.getLCSMatchSimilarity(val1, val2);
			break;
		case MatchingConfig.LEV:
			ret = StringMatch.getLEVMatchSimilarity(val1, val2);
			break;
		case MatchingConfig.JWC:
			ret = StringMatch.getJWCMatchSimilarity(val1, val2);
			break;
		default:
			return 0;
				
		}
		return ret;
	}
	
	public boolean equals(Object o) throws ClassCastException {
		if(o == null) {
			return false;
		}
		
		MatchResult mr = (MatchResult) o;
		return mr.getScore() == score;
	}
	
	public int compareTo(Object o) throws ClassCastException{
		MatchResult mr = (MatchResult) o;
		return new Double(score).compareTo(new Double(mr.getScore()));
	}
	
	public int hashCode() {
		  assert false : "hashCode not designed";
		  return 42; // any arbitrary constant will do 
	}

	public String toString(){
		return Double.toString(score);
	}
}
