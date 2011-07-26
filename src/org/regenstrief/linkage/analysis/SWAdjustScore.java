package org.regenstrief.linkage.analysis;

/**
 * Designed to be used by ScorePair class
 * Stores weight scaling information and calculates scaling factor
 * 
 * @author scentel
 *
 */
public class SWAdjustScore {

	// Parameters needed for weight scaling
	private int total_tokens;
	private int unique_tokens;
	private int token_freq;
	
	public SWAdjustScore(int total, int unique, int token) {
		this.total_tokens = total;
		this.unique_tokens = unique;
		this.token_freq = token;
	}
	
	/**
	 * Creates a new AdjustScore object whose fields are the sum of two AdjustScore objects
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static SWAdjustScore sumTwoScores(SWAdjustScore s1, SWAdjustScore s2, int union_unique) {
		//return new SWAdjustScore(s1.total_tokens + s2.total_tokens, s1.unique_tokens + s2.unique_tokens, s1.token_freq + s2.token_freq);
		return new SWAdjustScore(s1.total_tokens + s2.total_tokens, union_unique, s1.token_freq + s2.token_freq);
	}
	
	/**
	 * Calculates the scaling factor
	 * @return
	 */
	public double getScalingFactor() {
		// Convert to double to avoid integer division errors
		double total = this.total_tokens;
		double unique = this.unique_tokens;
		double freq = this.token_freq;
		return Math.sqrt(total/(unique*freq));
	}
}
