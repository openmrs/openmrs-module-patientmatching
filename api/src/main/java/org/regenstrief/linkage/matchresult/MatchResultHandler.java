package org.regenstrief.linkage.matchresult;

import org.regenstrief.linkage.MatchResult;

/**
 * Interface defines objects that handle a stream of MatchResult objects. Implementations of
 * MatchResultHandler might write them all to a file or a database, or filter the stream to just
 * matching results that fit a certain criteria. It should not be assumed that MatchResult objects
 * arrive in any meaningful order. Currently, the assumption is that calling acceptMatchResult()
 * after close() has been called will not throw an exception, but the expectation is that the
 * MatchResultHandler will do nothing with the MatchResult given to it then. The MatchResult objects
 * should not be changed by the MatchResultHandlers.
 * 
 * @author jegg
 */

public interface MatchResultHandler {
	
	/**
	 * Method takes the next MatchResult object.
	 * 
	 * @param mr the MatchResult to inspect and/or do work with
	 */
	public void acceptMatchResult(MatchResult mr);
	
	/**
	 * Method is called to send notification that the stream of MatchResult objects are finished
	 */
	public void close();
	
	/**
	 * Method returns if objects can accept more MatchResult objects. Normally, this would just indicate
	 * if it has had it's close() method called.
	 * 
	 * @return true if MatchResult handler can accept more MatchResults
	 */
	public boolean isOpen();
}
