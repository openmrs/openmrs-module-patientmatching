package org.regenstrief.linkage.analysis;

/**
 * Interface defines method an object needs to have for ScorePair to
 * have it scale the score of two RecordPairs.
 */

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.util.MatchingConfig;

public interface Modifier {
	/**
	 * Method must be implemented for a class to be used by ScorePair to modify a
	 * score.
	 * 
	 * @param mr	the object containing the two Records and the base Score
	 * @param mc	the particular configuration with flags indicating which columns to scale
	 */
	public void modifyMatchResult(MatchResult mr, MatchingConfig mc);
}
