package org.regenstrief.linkage.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Calculates the average frequency of values in each demographic.
 * 
 */
public class AverageFrequencyAnalyzer extends DataSourceAnalyzer {
	private TreeMap<String, Integer> unique_freq_table;
	private TreeMap<String, Double> result_table;
	private int total_records;

	public AverageFrequencyAnalyzer(LinkDataSource lds, MatchingConfig mc, TreeMap<String, Integer> computed_unique_freq_table) {
		super(lds, mc);
		unique_freq_table = computed_unique_freq_table;
		result_table = new TreeMap<String, Double>();
	}
	
	public TreeMap<String, Double> getResults() {
		return result_table;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#analyzeRecord(org.regenstrief.linkage.Record)
	 */
	@Override
	public void analyzeRecord(Record rec) {
		// already have everything we need from UniqueAnalyzer
		
		++total_records;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#isAnalyzedDemographic(org.regenstrief.linkage.util.MatchingConfigRow)
	 */
	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return false;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.Analyzer#finishAnalysis()
	 */
	public void finishAnalysis() {
		log.info("averagefrequencyanalyzer finishing analysis");
		Iterator<String> demographic_it = unique_freq_table.keySet().iterator();
		while (demographic_it.hasNext()) {
			String current_demographic = demographic_it.next();

			Double favg = new Double(total_records) / unique_freq_table.get(current_demographic);
			log.info("column/demographic " + current_demographic
					+ " has average frequency: "
					+ favg);
			result_table.put(current_demographic, favg);
		}
	}

	/**
	 * Inherited from DataSourceAnalyzer.
	 * 
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#getLogger()
	 */
	public Logger getLogger() {
		return log;
	}
}
