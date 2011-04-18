package org.regenstrief.linkage.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.MatchVectorRecordFrequencies;
import org.regenstrief.linkage.analysis.RecordFrequencies;
import org.regenstrief.linkage.analysis.ValueFrequencyTable;
import org.regenstrief.linkage.analysis.VectorTable;
import org.regenstrief.linkage.analysis.WeightedSampler;

public class SyntheticRecordGenerator {

	public static final int FREQUENCY_THRESHOLD = 10;
	
	private MatchingConfig mc;
	private RecordFrequencies rf;
	private MatchVectorRecordFrequencies mvrf;
	private VectorTable vt;
	private Random rand;
	private long count;
	private ValueFrequencyTable expected_vectors;
	private Hashtable<String,MatchVector> expected_strings;
	
	public SyntheticRecordGenerator(MatchingConfig mc, long pair_count, RecordFrequencies rf, MatchVectorRecordFrequencies mvrf){
		this.mc = mc;
		this.rf = rf;
		this.mvrf = mvrf;
		count = 0;
		vt = new VectorTable(mc);
		rand = new Random();
		expected_strings = new Hashtable<String,MatchVector>();
		expected_vectors = new ValueFrequencyTable();
		
		generateVectorProbabilities(pair_count);
	}
	
	private void generateVectorProbabilities(long pair_count){
		String true_vector, false_vector;
		Iterator<MatchResult> mr_it = vt.getPossibleMatchResults().iterator();
		while(mr_it.hasNext()){
			MatchVector mv_obs = mr_it.next().getMatchVector();
			double expected_true = vt.getMatchVectorTrueProbability(mv_obs) * mc.getP() * pair_count;
			double expected_false = vt.getMatchVectorFalseProbability(mv_obs) * (1 - mc.getP()) * pair_count;
			true_vector = mv_obs.toString() + "_true";
			false_vector = mv_obs.toString() + "_false";
			expected_vectors.setFrequency(true_vector, (long)expected_true);
			expected_vectors.setFrequency(false_vector, (long)expected_false);
			expected_strings.put(true_vector, mv_obs);
			expected_strings.put(false_vector, mv_obs);
		}
	}
	
	public Record[] getRecordPair(){
		boolean true_positive = false;
		Record[] ret = new Record[2];
		ret[0] = new Record(count++, "synthetic");
		ret[1] = new Record(count++, "synthetic");
		
		// step 1, randomly choose vector to create based on mc
		MatchVector mv = getRandomMatchVector();
		
		// step 2, randomly choose whether vector will be true or false, weighted by true positive/true negative likelihood values
		double true_prob = vt.getMatchVectorTrueProbability(mv);
		double r = rand.nextDouble();
		if(r <= true_prob){
			true_positive = true;
		}
		
		// step 3, determine dominant, correlated fields first, such as sex, that determine the search space for other fields, such as first name
		
		
		// step 4, randomly select field values for left pair, weighted by frequency of values observed within the vector class
		Iterator<String> it = mv.getDemographics().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			RecordFrequencies vector_rf = mvrf.getFrequencies(mv);
			ValueFrequencyTable vft = vector_rf.getDemographicFrequencies(demographic);
			String val = WeightedSampler.weightedRandomSample(vft);
			ret[0].addDemographic(demographic, val);
			
			// step 5, if field in vector agrees, copy value from step 4 to right side record
			// if field in vector disagrees, randomly select value for right side record from observed values in vector class, weighted by frequency
			if(!mv.matchedOn(demographic)){
				String new_val = "";
				do{
					new_val = WeightedSampler.weightedRandomSample(vft);
				}while(val.equals(new_val));
			}
			
			ret[1].addDemographic(demographic, val);
			
		}
		
		
		return ret;
	}
	
	private MatchVector getRandomMatchVector(){
		
		//return expected_strings.get(WeightedSampler.weightedRandomSample(expected_vectors));
		String s = WeightedSampler.weightedRandomSample(expected_vectors);
		MatchVector mv = expected_strings.get(s);
		return mv;
	}
}
