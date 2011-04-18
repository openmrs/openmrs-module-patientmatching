package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.ScorePair;

public class VectorValuesFrequencyAnalyzer extends RecordPairAnalyzer
		implements LoggingObject {

	private ScorePair sp;
	private MatchVectorRecordFrequencies mvrf;
	
	public VectorValuesFrequencyAnalyzer(MatchingConfig mc){
		super(mc);
		sp = new ScorePair(mc);
		mvrf = new MatchVectorRecordFrequencies();
	}
	
	public void finishAnalysis() {
		// nothing to do after counting values

	}

	@Override
	public void analyzeRecordPair(Record[] pair) {
		MatchResult mr = sp.scorePair(pair[0], pair[1]);
		MatchVector mv = mr.getMatchVector();
		Iterator<String> it = mv.getDemographics().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String val1 = pair[0].getDemographic(demographic);
			String val2 = pair[1].getDemographic(demographic);
			RecordFrequencies rf = mvrf.getFrequencies(mv);
			if(rf == null){
				rf = new RecordFrequencies();
				mvrf.setFrequencies(mv, rf);
			}
			ValueFrequencyTable vft = rf.getDemographicFrequencies(demographic);
			if(vft == null){
				vft = new ValueFrequencyTable();
				rf.setDemographicFrequencies(demographic, vft);
			}
			// add 1 to frequency count for pair[0] demographic value
			long l = vft.getFrequency(val1);
			vft.setFrequency(val1, l + 1);
			
			// add 1 to frequency count for pair[1] demographic value
			l = vft.getFrequency(val2);
			vft.setFrequency(val2, l + 1);
		}

	}
	
	public MatchVectorRecordFrequencies getVectorFrequencies(){
		return mvrf;
	}

}
