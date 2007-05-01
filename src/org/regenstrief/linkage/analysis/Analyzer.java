package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.*;

/**
 * Class calculates information on a set of Records fed to it
 * incrementally through successive calls to its analyzeRecord
 * method.  The finishAnalysis method is used when aggregate
 * calculations need to be performed as a last step of the analysis
 * 
 */

public abstract class Analyzer {
	
	/**
	 * Analyzes the given record
	 * 
	 * @param rec	the latest Record to be included in the analysis
	 */
	public abstract void analyzeRecord(Record rec);
	
	/**
	 * Performs any final calculations that require the complete
	 * set of Records before being finished
	 *
	 */
	public abstract void finishAnalysis();
}
