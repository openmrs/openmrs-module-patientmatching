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
	private TreeMap<String, Integer> freq_table;
	private HashSet<String> known_values;
	private int total_records;

	public AverageFrequencyAnalyzer(LinkDataSource lds, MatchingConfig mc) {
		super(lds, mc);
		freq_table = new TreeMap<String, Integer>();
		known_values = new HashSet<String>();
	}

	/**
	 * @see org.regenstrief.linkage.analysis.DataSourceAnalyzer#analyzeRecord(org.regenstrief.linkage.Record)
	 */
	@Override
	public void analyzeRecord(Record rec) {
		// log.info("analyzing record...");
		Iterator<String> it = rec.getDemographics().keySet().iterator();
		while (it.hasNext()) {
			String demographic = it.next();
			// log.info("  demographic: " + demographic);
			String value = rec.getDemographic(demographic);
			if (!known_values.contains(value)) {
				// log.info("found new item in Record " + new Long(rec.getUID()) + ", demographic " + demographic);
				Integer count = freq_table.get(demographic);
				if (count == null) {
					// haven't seen this demographic before, set to 1
					freq_table.put(demographic, new Integer(1));
				} else {
					// have seen it -- increment by 1
					++count;
					freq_table.put(demographic, count);
				}
				
				known_values.add(value);
			}
		}
		
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
		Iterator<String> demographic_it = freq_table.keySet().iterator();
		while (demographic_it.hasNext()) {
			String current_demographic = demographic_it.next();

			Double favg = new Double(total_records) / freq_table.get(current_demographic);
			log.info("column/demographic " + current_demographic
					+ " has average frequency: "
					+ favg);
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
