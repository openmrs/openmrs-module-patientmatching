package org.regenstrief.linkage.deprecated;

import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/*
 * The Linker class outlines the basic RecordLinkage process
 * defined by:
 * 		Forming pairs of records
 * 		Possibly inspecting the data to optimize weighting of matches
 * 		Scoring matches to find the most likely true matches
 * 
 * Implementing subclasses define the methods that do the above steps
 */

public abstract class Linker implements Runnable {
	
	// a flag telling whether match and non-match values should be optimized
	protected boolean estimate;
	
	// LinkDataSource contains information on where the data is
	protected LinkDataSource lds1, lds2;
	
	// MatchingConfig object with information on what columns are used in
	// the matching
	protected MatchingConfig mc;
	
	public Linker(LinkDataSource lds1, LinkDataSource lds2, MatchingConfig mc, boolean est) {
		this.mc = mc;
		this.lds1 = lds1;
		this.lds2 = lds2;
		estimate = est;
	}
	
	public Linker(MatchingConfig mc, LinkDataSource lds) {
		this.mc = mc;
		this.lds1 = lds1;
		this.lds2 = lds2;
		estimate = false;
	}
	
	public void run() {
		// calling runLinkage after starting a new thread will keep the main application responsive
		// threading is implemented in this class as 
		runLinkage();
	}
	
	public boolean runLinkage() {
		// run the steps to perform the linking
		// success variable will keep track of each step succeeding
		boolean success;
		
		// first step is to form the pairs
		success = formPairs();
		
		// if estimating, call EM
		if (success && estimate) {
			success = estimateValues();
		}
		
		// last step is to score the pairs to rank the possible matches
		if (success) {
			success = scorePairs();
		}
		
		return success;
	}
	
	/*
	 * The first step in the linkage process is to form pairs between the records
	 * in preparation for the comparisons
	 */
	public abstract boolean formPairs();
	
	/*
	 * The second step is optional and optimizes the weights of the different
	 * columns that are matched or unmatched
	 */
	public abstract boolean estimateValues();
	
	/*
	 * The last step is to step through the record pairs and determine one score
	 * for each possible match
	 */
	public abstract boolean scorePairs();
	
}
