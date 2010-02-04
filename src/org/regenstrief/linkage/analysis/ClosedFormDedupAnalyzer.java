package org.regenstrief.linkage.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

public class ClosedFormDedupAnalyzer implements PairAnalyzer {
	protected DataSourceFrequency dsf;
	private MatchingConfig mc;
	private Map<Long, Boolean> encountered;
	
	public ClosedFormDedupAnalyzer(MatchingConfig mc){
		this.mc = mc;
		dsf = new MemoryBackedDataSourceFrequency();
		encountered = new HashMap<Long,Boolean>();
	}

	public void analyzeRecordPair(Record[] pair) {
		for(int i = 0; i < pair.length; i++){
			long id = pair[i].getUID();
			if(!encountered.containsKey(new Long(id))){
				analyzeRecordValues(dsf, pair[0]);
				encountered.put(new Long(id), Boolean.TRUE);
			}
		}
		
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
		CloseFormUCalculatorDedup cfuc = new CloseFormUCalculatorDedup(mc, dsf);
		cfuc.calculateUValues();
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

}
