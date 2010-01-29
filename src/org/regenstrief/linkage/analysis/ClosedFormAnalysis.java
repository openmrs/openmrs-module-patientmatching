package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class analyzes a blocking scheme and uses token frequency information to directly calculate u values
 * 
 * @author jegg
 *
 */

public class ClosedFormAnalysis implements PairAnalyzer {
	MatchingConfig mc;
	DataSourceFrequency dsf1, dsf2;
	
	public ClosedFormAnalysis(MatchingConfig mc){
		dsf1 = new MemoryBackedDataSourceFrequency();
		dsf2 = new MemoryBackedDataSourceFrequency();
		this.mc = mc;
	}

	public void analyzeRecordPair(Record[] pair) {
		analyzeRecordValues(dsf1, pair[0]);
		analyzeRecordValues(dsf2, pair[1]);
		
	}
	
	private void analyzeRecordValues(DataSourceFrequency dsf, Record r){
		Iterator<String> it = r.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String value = r.getDemographic(demographic);
			dsf.incrementCount(demographic, value);
		}
	}

	public void finishAnalysis() {
		CloseFormUCalculator cfuc = new CloseFormUCalculator(mc, dsf1, dsf2);
		cfuc.calculateUValues();
	}

}
