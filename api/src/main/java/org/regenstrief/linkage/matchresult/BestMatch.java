package org.regenstrief.linkage.matchresult;

import org.regenstrief.linkage.MatchResult;

/**
 * Class filters a stream of MatchResult objects, only storing the one with the highest score. Only
 * higher scoring matches replace the current best, so the first match of the highest score
 * encountered will be returned.
 * 
 * @author jegg
 */

public class BestMatch implements MatchResultHandler {
	
	private MatchResult best;
	
	private double best_score;
	
	public BestMatch() {
		best_score = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Method returns the highest scoring MatchResult the object has encountered so far
	 * 
	 * @return the highest scoring MatchResult
	 */
	public MatchResult getBestMatch() {
		return best;
	}
	
	/**
	 * Method returns the highest score the object has encountered so far
	 * 
	 * @return the current highest score
	 */
	public double getBestScore() {
		return best_score;
	}
	
	public void acceptMatchResult(MatchResult mr) {
		if (mr.getScore() > best_score) {
			best = mr;
			best_score = mr.getScore();
		}
	}
	
	public void close() {
		// do nothing
	}
	
	public boolean isOpen() {
		// always stays open to compare MatchResult scores
		return true;
	}
	
}
