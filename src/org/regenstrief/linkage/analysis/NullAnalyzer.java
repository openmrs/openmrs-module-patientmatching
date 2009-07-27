package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Calculates the number of null (i.e. empty string) values in each field of the
 * given stream of Records in one pass.
 * 
 */
public class NullAnalyzer extends DataSourceAnalyzer implements Modifier {
	private Hashtable<String,Integer> freq_table;
	
	public NullAnalyzer(LinkDataSource lds, MatchingConfig mc) {
		super(lds, mc);
		freq_table = new Hashtable<String,Integer>();
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#analyzeRecord(org.regenstrief.linkage.Record)
	 */
	@Override
	public void analyzeRecord(Record rec) {
		Iterator<String> it = rec.getDemographics().keySet().iterator();
		while(it.hasNext()) {
			String demographic = it.next();
			boolean is_null = rec.isDemographicNull(demographic);
			if (is_null) {
				Integer count = new Integer(freq_table.get(demographic));
				if (count == null) {
					// haven't seen this demographic before, set to 1
					freq_table.put(demographic, new Integer(1));
				} else {
					// have seen it -- increment by 1
					++count;
					freq_table.put(demographic, count);
				}
			}
		}
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#isAnalyzedDemographic(org.regenstrief.linkage.util.MatchingConfigRow)
	 */
	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return false;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.Modifier#getModifiedMatchResult(org.regenstrief.linkage.MatchResult,
	 *      org.regenstrief.linkage.util.MatchingConfig)
	 */
	public ModifiedMatchResult getModifiedMatchResult(MatchResult mr,
			MatchingConfig mc) {
		return null;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.Modifier#getModifierName()
	 */
	public String getModifierName() {
		return "Null";
	}

	/**
	 * @see org.regenstrief.linkage.analysis.Modifier#initializeModifier()
	 */
	public void initializeModifier() {
		

	}

	/**
	 * @see org.regenstrief.linkage.analysis.Analyzer#finishAnalysis()
	 */
	public void finishAnalysis() {
		Iterator<String> demographic_it = freq_table.keySet().iterator();
		while(demographic_it.hasNext()){
			String current_demographic = demographic_it.next();
			
			System.out.println("column/demographic " + current_demographic + " has null count of: " + freq_table.get(current_demographic));
		}
	}

	/**
	 * @see org.regenstrief.linkage.util.LoggingObject#getLogger()
	 */
	public Logger getLogger() {
		return null;
	}

}
