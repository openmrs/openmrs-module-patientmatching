package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Class analyzes a blocking scheme and uses token frequency information to directly calculate u values
 * 
 * @author jegg
 *
 */

public class ClosedFormDedupAnalyzer extends DataSourceAnalyzer {
	DataSourceFrequency dsf1;
	
	public ClosedFormDedupAnalyzer(LinkDataSource lds, MatchingConfig mc){
		super(lds, mc);
		dsf1 = new MemoryBackedDataSourceFrequency();
	}

	public void analyzeRecord(Record rec) {
		analyzeRecordValues(dsf1, rec);
		
	}
	
	private void analyzeRecordValues(DataSourceFrequency dsf, Record r){
		Iterator<String> it = r.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			if(config.getMatchingConfigRowByName(demographic).isIncluded() || config.getMatchingConfigRowByName(demographic).getBlockOrder() > 0){
				String value = r.getDemographic(demographic);
				dsf.incrementCount(demographic, value);
			}
		}
	}
	
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr){
		return true;
	}

	public void finishAnalysis() {
		CloseFormUCalculatorDedup cfuc = new CloseFormUCalculatorDedup(config, dsf1);
		cfuc.calculateUValues();
		log.info("calcluted values:");
		Iterator<MatchingConfigRow> it = config.getMatchingConfigRows().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			log.info(mcr.getName() + ":\t" + mcr.getNonAgreement());
		}
	}

}
