package org.regenstrief.linkage.analysis;

/**
 * Interface defines method an object needs to have for ScorePair to
 * have it scale the score of two RecordPairs.
 */

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.util.MatchingConfig;

public interface Modifier {
	
	public void initializeModifier();
	
	public String getModifierName();
	
	/**
	 * Method must be implemented for a class to be used by ScorePair to modify a score.
	 * 
	 * @param mr the object containing the two Records and the base Score
	 * @param mc the particular configuration with flags indicating which columns to scale
	 */
	public ModifiedMatchResult getModifiedMatchResult(MatchResult mr, MatchingConfig mc);
}
