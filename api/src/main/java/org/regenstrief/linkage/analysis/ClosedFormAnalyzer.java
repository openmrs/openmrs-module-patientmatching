package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Class analyzes a blocking scheme and uses token frequency information to directly calculate u values
 * 
 * @author jegg
 *
 */

public class ClosedFormAnalyzer extends RecordPairAnalyzer {
	DataSourceFrequency dsf1, dsf2;
	
	public ClosedFormAnalyzer(MatchingConfig mc){
		super(mc);
		dsf1 = new MemoryBackedDataSourceFrequency();
		dsf2 = new MemoryBackedDataSourceFrequency();
	}

	public void analyzeRecordPair(Record[] pair) {
		analyzeRecordValues(dsf1, pair[0]);
		analyzeRecordValues(dsf2, pair[1]);
		
	}
	
	private void analyzeRecordValues(DataSourceFrequency dsf, Record r){
		Iterator<String> it = r.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			if(mc.getMatchingConfigRowByName(demographic).isIncluded() || mc.getMatchingConfigRowByName(demographic).getBlockOrder() > 0){
				String value = r.getDemographic(demographic);
				dsf.incrementCount(demographic, value);
			}
		}
	}

	public void finishAnalysis() {
		CloseFormUCalculator cfuc = new CloseFormUCalculator(mc, dsf1, dsf2);
		cfuc.calculateUValues();
		log.info("calcluted values:");
		Iterator<MatchingConfigRow> it = mc.getMatchingConfigRows().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			log.info(mcr.getName() + ":\t" + mcr.getNonAgreement());
		}
	}

}
