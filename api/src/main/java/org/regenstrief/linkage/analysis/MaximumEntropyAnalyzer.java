package org.regenstrief.linkage.analysis;

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
public class MaximumEntropyAnalyzer extends DataSourceAnalyzer {
	private TreeMap<String, Double> average_freq_table;
	private TreeMap<String, Integer> unique_freq_table;
	private int total_records;
	private SummaryStatisticsStore sss;

	public MaximumEntropyAnalyzer(LinkDataSource lds, MatchingConfig mc, TreeMap<String, Double> computed_average_freq_table, TreeMap<String, Integer> computed_unique_freq_table, SummaryStatisticsStore s) {
		super(lds, mc);
		sss = s;
		average_freq_table = computed_average_freq_table;
		unique_freq_table = computed_unique_freq_table;
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#analyzeRecord(org.regenstrief.linkage.Record)
	 */
	@Override
	public void analyzeRecord(Record rec) {
		// already have everything we need from AverageFrequencyAnalyzer and UniqueAnalyzer
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
		log.info("maximumentropyanalyzer finishing analysis");
		Iterator<String> demographic_it = unique_freq_table.keySet().iterator();
		while (demographic_it.hasNext()) {
			String current_demographic = demographic_it.next();

			Double favg = average_freq_table.get(current_demographic);
			Integer uqval = unique_freq_table.get(current_demographic);
			Double mxent = -1 * (( favg / total_records ) * Math.log(favg / total_records) / Math.log(2)) * uqval;
			log.info("column/demographic " + current_demographic
					+ " has maximum entropy: "
					+ mxent);
			sss.setMaxEntropy(current_demographic, mxent);
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
