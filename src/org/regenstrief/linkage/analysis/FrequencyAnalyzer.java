package org.regenstrief.linkage.analysis;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
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
		Map<String,String> demographics = rec.getDemographics();
		Iterator<String> it = demographics.keySet().iterator();
		while(it.hasNext()){
			String dem = it.next();
			String value = rec.getDemographic(dem);
			counter.incrementCount(value);
		}
	}

	
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return true;
	}

	public void finishAnalysis() {
		counter.setFinished(true);
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

}
