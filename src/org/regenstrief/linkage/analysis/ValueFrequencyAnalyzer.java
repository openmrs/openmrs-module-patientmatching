package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

public class ValueFrequencyAnalyzer extends DataSourceAnalyzer {

	private RecordFrequencies frequencies;
	
	public ValueFrequencyAnalyzer(LinkDataSource lds, MatchingConfig mc){
		super(lds,mc);
		frequencies = new RecordFrequencies();
		
		// add dependent demographics
		frequencies.setDependency("sex", "fn");
	}
	
	public void finishAnalysis() {
		// nothing to finalize after reading the records

	}
	
	public RecordFrequencies getRecordFrequencies(){
		return frequencies;
	}

	@Override
	public void analyzeRecord(Record rec) {
		Iterator<String> it = rec.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String value = rec.getDemographic(demographic);
			ValueFrequencyTable vft = null;
			
			if(frequencies.isDependent(demographic)){
				// get the context the demographic depends on, increment frequency
				DependentValueFrequencyTable dvft = frequencies.getDependentValueFrequencyTable(demographic);
				if(dvft == null){
					dvft = new DependentValueFrequencyTable();
					frequencies.setDependentDemographicFrequencies(demographic, dvft);
				}
				String context = rec.getDemographic(frequencies.getContext(demographic));
				if(context != null){
					vft = dvft.getValueFrequencyTable(context);
					if(vft == null){
						vft = new ValueFrequencyTable();
						dvft.setValueFrequencyTable(context, vft);
					}
				}
			}
			
			// is an independent demographic or there is no value for the dependent demographic context
			if(vft == null){
				vft = frequencies.getDemographicFrequencies(demographic);
			}
			if(vft == null){
				vft = new ValueFrequencyTable();
				frequencies.setDemographicFrequencies(demographic, vft);
			}
			long current = vft.getFrequency(value);
			vft.setFrequency(value, current+1);
			
		}
	}

	@Override
	public boolean isAnalyzedDemographic(MatchingConfigRow mcr) {
		return true;
	}

}
