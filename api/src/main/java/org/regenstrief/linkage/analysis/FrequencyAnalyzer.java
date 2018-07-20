package org.regenstrief.linkage.analysis;

import java.util.Map;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

public class FrequencyAnalyzer extends DataSourceAnalyzer {
	
	DataSourceFrequency counter;
	
	public FrequencyAnalyzer(LinkDataSource lds, MatchingConfig mc, DataSourceFrequency dsf){
		super(lds, mc);
		counter = dsf;
	}
	
	@Override
	public void analyzeRecord(Record rec) {
		for (final Map.Entry<String, String> entry : rec.getDemographics().entrySet()) {
			counter.incrementCount(entry.getKey(), entry.getValue());
		}
		counter.incrementTotal();
	}

	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return true;
	}

	@Override
	public void finishAnalysis() {
		counter.setFinished(true);
	}
}
