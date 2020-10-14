package org.regenstrief.linkage.matchresult;

import org.regenstrief.linkage.MatchResult;

/**
 * Interface defines objects that store a set of MatchResults.
 * 
 * @author jegg
 */

public interface MatchResultStore {
	
	public MatchResult getMatchResult(int index);
	
	public void addMatchResult(MatchResult mr, int id);
	
	public void removeMatchResult(int id);
	
	public int getSize();
}
