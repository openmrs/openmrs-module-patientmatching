package org.regenstrief.linkage.util;

import java.util.Hashtable;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.RecordFrequencies;
import org.regenstrief.linkage.analysis.ValueFrequencyTable;
import org.regenstrief.linkage.analysis.WeightedSampler;

public abstract class SyntheticRecordGenerator {
	
	public static final int FREQUENCY_THRESHOLD = 10;
	
	public static final String DEMOGRAPHIC_RANK_SUFFIX = "_rank";
	
	protected Hashtable<String, Integer> thresholds;
	
	protected MatchingConfig mc;
	
	protected RecordFrequencies rf1, rf2;
	
	protected long count;
	
	public SyntheticRecordGenerator(MatchingConfig mc, RecordFrequencies rf) {
		this.mc = mc;
		this.rf1 = rf;
		this.rf2 = rf;
		count = 0;
		thresholds = new Hashtable<String, Integer>();
	}
	
	public void setRecordFrequencies2(RecordFrequencies rf) {
		rf2 = rf;
	}
	
	public abstract MatchResult getRecordPair();
	
	public int getDemographicThreshold(String demographic) {
		Integer i = thresholds.get(demographic);
		if (i == null) {
			return FREQUENCY_THRESHOLD;
		} else {
			return i;
		}
	}
	
	protected ValueFrequencyTable getSampleValueFrequencyTable(MatchVector mv, Record rec, String demographic) {
		RecordFrequencies rf;
		if (rec.getUID() % 2 == 0) {
			// left side record, use rf1
			rf = rf1;
		} else {
			rf = rf2;
		}
		ValueFrequencyTable vft = null;
		if (rf.getContext(demographic) != null) {
			String context = rec.getDemographic(rf.getContext(demographic));
			vft = rf.getDependentValueFrequencyTable(demographic).getValueFrequencyTable(context);
			if (vft == null || (vft.getUniqueValueCount() < getDemographicThreshold(demographic))) {
				// too few unique values in this given vector's observed values
				vft = rf.getDependentValueFrequencyTable(demographic).getValueFrequencyTable(context);
			}
		} else {
			vft = rf.getDemographicFrequencies(demographic);
			if (vft.getUniqueValueCount() < getDemographicThreshold(demographic)) {
				vft = rf.getDemographicFrequencies(demographic);
			}
		}
		return vft;
	}
	
	protected void setRandomDemographic(MatchVector mv, Record dest, String demographic) {
		ValueFrequencyTable vft = getSampleValueFrequencyTable(mv, dest, demographic);
		String val = WeightedSampler.weightedRandomSample(vft);
		dest.addDemographic(demographic, val);
		String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
		int rank = vft.getRank(val);
		dest.addDemographic(rank_demographic, Integer.toString(rank));
	}
	
	protected void setRandomDisagreementDemographic(MatchVector mv, Record dest, String demographic, String not_match) {
		ValueFrequencyTable vft = getSampleValueFrequencyTable(mv, dest, demographic);
		if (vft.getUniqueValueCount() == 1) {
			dest.addDemographic(demographic, "");
			String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
			int rank = vft.getRank("");
			dest.addDemographic(rank_demographic, Integer.toString(rank));
			return;
		}
		String new_val;
		do {
			new_val = WeightedSampler.weightedRandomSample(vft);
		} while (not_match.equals(new_val) && !not_match.equals(""));
		dest.addDemographic(demographic, new_val);
		String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
		int rank = vft.getRank(new_val);
		dest.addDemographic(rank_demographic, Integer.toString(rank));
	}
	
}
