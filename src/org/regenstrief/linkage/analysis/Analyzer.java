package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Class calculates information on a set of Records fed to it
 * incrementally through successive calls to its analyzeRecord
 * method.  The finishAnalysis method is used when aggregate
 * calculations need to be performed as a last step of the analysis
 * 
 */

public abstract class Analyzer {
	protected MatchingConfig config;
	protected LinkDataSource lds;
	protected Hashtable<String,Boolean> analyzed_demographics;
	
	/**
	 * Constructor takes the LinkDataSource to be analyzed
	 * and the MatchingConfig is inspected to determine what
	 * demographics need to be analyzed by the Analyzer
	 * 
	 * @param lds	the source of Records and demographics to analyze
	 * @param mc	object contains matching information, including
	 * 	which scores to modify using an analysis
	 */
	public Analyzer(LinkDataSource lds, MatchingConfig mc){
		this.config = mc;
		this.lds = lds;
		analyzed_demographics = new Hashtable<String,Boolean>();
		if(mc != null){
			List<MatchingConfigRow> matching_options = mc.getMatchingConfigRows();
			for(MatchingConfigRow mcr : matching_options){
				if(isAnalyzedDemographic(mcr)){
					// store this demographic as one to analyze when analyzeRecord(Record) is called
					analyzed_demographics.put(mcr.getName(), Boolean.TRUE);
				}
			}
		} else {
			// analyze all columns in the datasource
			for(DataColumn dc : lds.getDataColumns()){
				analyzed_demographics.put(dc.getName(), Boolean.TRUE);
			}
		}
		
	}
	
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
	
	/**
	 * Method is implemented by subclasses to determine if the
	 * MatchingConfigRow (information on demographics used in matching)
	 * is valid for score modification by the specific Analyzer
	 * 
	 * @param mcr	object containing information on how a demographic will be used
	 * @return	true if this demographic needs to be analyzed for use in matching
	 */
	public abstract boolean isAnalyzedDemographic(MatchingConfigRow mcr);
	
	/**
	 * Methods returns true if the given demographic needs to be
	 * analyzed in the analyzeRecord() method
	 * 
	 * @param demographic	the name of the demographic to look-up
	 * @return	true if the demographic needs to be analyzed
	 */
	protected boolean analyzeDemographic(String demographic){
		if(demographic == null){
			return false;
		}
		Boolean analyze = analyzed_demographics.get(demographic);
		if(analyze == null || !analyze){
			return false;
		} else {
			return true;
		}
	}
}
