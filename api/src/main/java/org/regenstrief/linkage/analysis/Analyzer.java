package org.regenstrief.linkage.analysis;

public interface Analyzer {
	
	/**
	 * Performs any final calculations that require the complete set of Records before being finished
	 */
	public void finishAnalysis();
}
