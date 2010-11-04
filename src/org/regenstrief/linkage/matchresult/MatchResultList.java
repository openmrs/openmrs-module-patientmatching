package org.regenstrief.linkage.matchresult;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.MatchResult;

/**
 * Class stores MatchResults and makes them available for later using
 * the MatchResultStore interface
 * 
 * @author jegg
 *
 */

public class MatchResultList implements MatchResultHandler, MatchResultStore {
	protected List<MatchResult> results;
	
	public MatchResultList(){
		results = new ArrayList<MatchResult>();
	}
	
	public void acceptMatchResult(MatchResult mr) {
		results.add(mr);

	}

	public void close() {
		// do nothing here
	}

	public boolean isOpen() {
		return true;
	}

	/**
	 * Method returns a MatchResult at the given index in it's
	 * internal list of MatchResults
	 */
	public MatchResult getMatchResult(int index) {
		return results.get(index);
	}

	public void addMatchResult(MatchResult mr) {
		// TODO Auto-generated method stub
		
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addMatchResult(MatchResult mr, int id) {
		// TODO Auto-generated method stub
		
	}

	public void removeMatchResult(int id) {
		// TODO Auto-generated method stub
		
	}

}
