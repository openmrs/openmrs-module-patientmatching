package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

public class ClosedFormDedupAnalyzer extends DataSourceAnalyzer {
	protected DataSourceFrequency dsf;
	
	public ClosedFormDedupAnalyzer(LinkDataSource lds, MatchingConfig mc){
		super(lds, mc);
		dsf = new MemoryBackedDataSourceFrequency();
	}

	@Override
	public void analyzeRecord(Record rec) {
		Iterator<String> it = rec.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			if(analyzeDemographic(demographic)){
				String value = rec.getDemographic(demographic);
				dsf.incrementCount(demographic, value);
			}
		}
	}

	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		if(config.getMatchingConfigRowByName(mcr.getName()).isIncluded()){
			return true;
		}
		return false;
	}

	public void finishAnalysis() {
		CloseFormUCalculatorDedup cfuc = new CloseFormUCalculatorDedup(config, dsf);
		cfuc.calculateUValues();
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

}
