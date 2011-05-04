package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
	public static final String DEMOGRAPHIC_RANK_SUFFIX = "_rank";
	
	private Hashtable<String,Integer> thresholds;
	private MatchingConfig mc;
	private RecordFrequencies rf;
	private MatchVectorRecordFrequencies mvrf;
	private VectorTable vt;
	private Random rand;
	private long count;
	private List<String> primary_demographics;
	private List<String> dependent_demographics;
	
	private List<Double> mv_values;
	private List<MatchVector> mv_vectors;
	private Hashtable<MatchVector,Double> mv_true_prob;
	private double rand_limit = 1;
	
	public SyntheticRecordGenerator(MatchingConfig mc, long pair_count, RecordFrequencies rf, MatchVectorRecordFrequencies mvrf){
		this.mc = mc;
		this.rf = rf;
		this.mvrf = mvrf;
		count = 0;
		vt = new VectorTable(mc);
		rand = new Random();
		primary_demographics = new ArrayList<String>();
		dependent_demographics = new ArrayList<String>();
		mv_values = new ArrayList<Double>();
		mv_vectors = new ArrayList<MatchVector>();
		thresholds = new Hashtable<String,Integer>();
		mv_true_prob = new Hashtable<MatchVector,Double>();
		
		generateVectorProbabilities(pair_count);
		generateDemographicsLists();
	}
	
	public int getDemographicThreshold(String demographic){
		Integer i = thresholds.get(demographic);
		if(i == null){
			return FREQUENCY_THRESHOLD;
		} else {
			return i;
		}
	}
	
	public void setDemographicThreshold(String demographic, int i){
		thresholds.put(demographic, i);
	}
	
	private void generateDemographicsLists(){
		Iterator<MatchingConfigRow> it = mc.getIncludedColumns().iterator();
		while(it.hasNext()){
			String demographic = it.next().getName();
			String context = rf.getContext(demographic);
			if(context != null && !primary_demographics.contains(context)){
				primary_demographics.add(context);
				dependent_demographics.add(demographic);
				if(dependent_demographics.contains(context)){
					dependent_demographics.remove(context);
				}
			}
			if(!dependent_demographics.contains(demographic) && !primary_demographics.contains(demographic)){
				dependent_demographics.add(demographic);
			}
			
		}
	}
	
	private void generateVectorProbabilities(long pair_count){
		Iterator<MatchResult> mr_it = vt.getPossibleMatchResults().iterator();
		double prev = 0;
		int i = 0;
		double p = mc.getP();
		while(mr_it.hasNext()){
			MatchVector mv_obs = mr_it.next().getMatchVector();
			double expected_true = vt.getMatchVectorTrueProbability(mv_obs) * p;// * pair_count;
			double expected_false = vt.getMatchVectorFalseProbability(mv_obs) * (1 - p);// * pair_count;
			double true_prob = expected_true / (expected_true + expected_false);
			prev = expected_true + expected_false + prev;
			mv_values.add(i, prev);
			mv_vectors.add(i, mv_obs);
			mv_true_prob.put(mv_obs, true_prob);
			i++;
		}
		
		rand_limit = prev;
	}
	
	public MatchResult getRecordPair(){
		boolean true_positive = false;
		Record[] ret = new Record[2];
		ret[0] = new Record(count++, "synthetic");
		ret[1] = new Record(count++, "synthetic");
		
		// step 1, randomly choose vector to create based on mc
		MatchVector mv = getRandomMatchVector();
		//System.out.println("generating pair with vector " + mv);
		
		// step 2, randomly choose whether vector will be true or false, weighted by true positive/true negative likelihood values
		double r = rand.nextDouble();
		if(r <= mv_true_prob.get(mv)){
			true_positive = true;
		}
		
		// step 3, determine dominant, correlated fields first, such as sex, that determine the search space for other fields, such as first name
		Iterator<String> p_it = primary_demographics.iterator();
		while(p_it.hasNext()){
			String p_demographic = p_it.next();
			setRandomDemographic(mv, ret[0], p_demographic);
			
			if(!mv.matchedOn(p_demographic)){
				setRandomDisagreementDemographic(mv, ret[1], p_demographic, ret[0].getDemographic(p_demographic));
			} else {
				ret[1].addDemographic(p_demographic, ret[0].getDemographic(p_demographic));
				String rank_demographic = p_demographic + DEMOGRAPHIC_RANK_SUFFIX;
				ret[1].addDemographic(rank_demographic, ret[0].getDemographic(rank_demographic));
			}
		}
		
		// step 4, randomly select field values for left pair, weighted by frequency of values observed within the vector class
		// if vector was not observed, choose values from the whole datasource
		Iterator<String> d_it = dependent_demographics.iterator();
		while(d_it.hasNext()){
			String demographic = d_it.next();
			//System.out.println("setting values for " + demographic + ", agreement -> " + mv.matchedOn(demographic));
			setRandomDemographic(mv, ret[0], demographic);
			
			// step 5, if field in vector agrees, copy value from step 4 to right side record
			// if field in vector disagrees, randomly select value for right side record from observed values in vector class, weighted by frequency
			if(!mv.matchedOn(demographic)){
				setRandomDisagreementDemographic(mv, ret[1], demographic, ret[0].getDemographic(demographic));
			} else {
				ret[1].addDemographic(demographic, ret[0].getDemographic(demographic));
				String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
				ret[1].addDemographic(rank_demographic, ret[0].getDemographic(rank_demographic));
			}
			
		}
		// double score, double incl_score, double true_prob, double false_prob, double sensitivity, double specificity, MatchVector match_vct, ScoreVector score_vct, Record r1, Record r2, MatchingConfig mc){
		MatchResult mr = new MatchResult(vt.getScore(mv), vt.getInclusiveScore(mv), vt.getMatchVectorTrueProbability(mv), vt.getMatchVectorFalseProbability(mv), vt.getSensitivity(mv), vt.getSpecificity(mv), mv, vt.getScoreVector(mv), ret[0], ret[1], mc);
		mr.setMatch(true_positive);
		return mr;
	}
	
	private ValueFrequencyTable getSampleValueFrequencyTable(MatchVector mv, Record rec, String demographic){
		RecordFrequencies vector_rf = mvrf.getFrequencies(mv);
		boolean vector_sampling = true;
		if(vector_rf == null){
			// given Vector wasn't observed, so use datasource frequencies instead
			vector_rf = rf;
			vector_sampling = false;
		}
		
		ValueFrequencyTable vft = null;
		if(vector_rf.getContext(demographic) != null){
			String context = rec.getDemographic(vector_rf.getContext(demographic));
			vft = vector_rf.getDependentValueFrequencyTable(demographic).getValueFrequencyTable(context);
			if(vft == null || (vft.getUniqueValueCount() < getDemographicThreshold(demographic) && vector_sampling)){
				// too few unique values in this given vector's observed values
				vft = rf.getDependentValueFrequencyTable(demographic).getValueFrequencyTable(context);
			}
		} else {
			vft = vector_rf.getDemographicFrequencies(demographic);
			if(vft.getUniqueValueCount() < getDemographicThreshold(demographic) && vector_sampling){
				vft = rf.getDemographicFrequencies(demographic);
			}
		}
		return vft;
	}
	
	private void setRandomDemographic(MatchVector mv, Record dest, String demographic){
		ValueFrequencyTable vft = getSampleValueFrequencyTable(mv, dest, demographic);
		String val = WeightedSampler.weightedRandomSample(vft);
		dest.addDemographic(demographic, val);
		String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
		int rank = vft.getRank(val);
		dest.addDemographic(rank_demographic, Integer.toString(rank));
	}
	
	private void setRandomDisagreementDemographic(MatchVector mv, Record dest, String demographic, String not_match){
		ValueFrequencyTable vft = getSampleValueFrequencyTable(mv, dest, demographic);
		if(vft.getUniqueValueCount() == 1){
			dest.addDemographic(demographic, "");
			String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
			int rank = vft.getRank("");
			dest.addDemographic(rank_demographic, Integer.toString(rank));
			return;
		}
		String new_val;
		do{
			new_val = WeightedSampler.weightedRandomSample(vft);
		}while(not_match.equals(new_val) && !not_match.equals(""));
		dest.addDemographic(demographic, new_val);
		String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
		int rank = vft.getRank(new_val);
		dest.addDemographic(rank_demographic, Integer.toString(rank));
	}
	
	private MatchVector getRandomMatchVector(){
		double r = rand.nextDouble() * rand_limit;
		
		double selected = 0;
		int i = 0;
		for(; r > selected && i < mv_values.size(); i++){
			selected = mv_values.get(i);
		}
		
		return mv_vectors.get(i - 1);
	}
}
