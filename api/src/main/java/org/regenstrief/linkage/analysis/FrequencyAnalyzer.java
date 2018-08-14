package org.regenstrief.linkage.analysis;

import java.util.Map;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

public class FrequencyAnalyzer extends DataSourceAnalyzer {
	
	final DataSourceFrequency counter;
	
	public FrequencyAnalyzer(final LinkDataSource lds, final MatchingConfig mc, final DataSourceFrequency dsf) {
		super(lds, mc);
		counter = dsf;
	}
	
	@Override
	public final void analyzeRecord(final Record rec) {
		incrementCount(rec);
		counter.incrementTotal();
	}
	
	protected void incrementCount(final Record rec) {
		for (final Map.Entry<String, String> entry : rec.getDemographics().entrySet()) {
			counter.incrementCount(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean isAnalyzedDemographic(final MatchingConfigRow mcr) {
		return true;
	}

	@Override
	public void finishAnalysis() {
		counter.setFinished(true);
	}
}
