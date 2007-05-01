package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.*;
import java.util.*;

/**
 * Class calculates a score, sensitivity, and specificity table
 * for a given set of m and u values from the MatchingConfig
 * object.
 *
 */

public class VectorTable {
	
	MatchingConfig mc;
	Hashtable<MatchVector,Double> match_scores;
	Hashtable<MatchVector,Double> match_specificities;
	Hashtable<MatchVector,Double> match_sensitivities;
	Hashtable<String,Double> u_values, m_values;
	
	public static final double MATCH_ONE = 0.999999;
	public static final double MATCH_ZERO = 0.000001;
	
	public VectorTable(MatchingConfig mc){
		this.mc = mc;
		match_scores = new Hashtable<MatchVector,Double>();
		match_sensitivities = new Hashtable<MatchVector,Double>();
		match_specificities = new Hashtable<MatchVector,Double>();
		
		m_values = new Hashtable<String,Double>();
		u_values = new Hashtable<String,Double>();
		
		List<MatchingConfigRow> incl_cols = mc.getIncludedColumns();
		Iterator<MatchingConfigRow> it = incl_cols.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			double match_rate = mcr.getAgreement();
			double umatch_rate = mcr.getNonAgreement();
			if(match_rate > MATCH_ONE || match_rate < MATCH_ZERO){
				System.out.println("ERROR: m-value is out of range");
			}
			if(umatch_rate > MATCH_ONE || match_rate < MATCH_ZERO){
				System.out.println("ERROR: u-value is out of range");
			}
			// m[k] = log(mtx[used[k]].m  /    mtx[used[k]].u) / log(2);
			// u[k] = log( (1 - mtx[used[k]].m) / (1 - mtx[used[k]].u) ) / log(2);
			// m[i] = Math.log(match_rate / umatch_rate) / Math.log(2);
			// u[i] = Math.log( (1 - match_rate) / (1 - umatch_rate) ) / Math.log(2);
			double m = Math.log(match_rate / umatch_rate) / Math.log(2);
			double u = Math.log( (1 - match_rate) / (1 - umatch_rate) ) / Math.log(2);
			m_values.put(mcr.getName(), new Double(m));
			u_values.put(mcr.getName(), new Double(u));
			
		}
		
		List<MatchVector> vectors = getPossibleMatchingVectors();
		Iterator<MatchVector> it2 = vectors.iterator();
		List<MatchResult> results = new ArrayList<MatchResult>();
		while(it2.hasNext()){
			MatchVector mv = it2.next();
			double score = getMatchVectorScore(mv);
			match_scores.put(mv, new Double(score));
			double true_prob = getMatchVectorTrueProbability(mv);
			double false_prob = getMatchVectorFalseProbability(mv);
			results.add(new MatchResult(score, true_prob, false_prob, mv, null, null));
		}
		
		// sort the match results (since it sorts on score) and get running
		// total of true and false probabilities to create sensitivity and
		// specificity
		Collections.sort(results, Collections.reverseOrder());
		Iterator<MatchResult> results_it = results.iterator();
		double sens = 0;
		while(results_it.hasNext()){
			MatchResult mr = results_it.next();
			sens = sens + mr.getTrueProbability();
			match_sensitivities.put(mr.getMatchVector(), sens);
		}
		
		// sort the match results to start with the lowest scores and
		// calculate specificity
		Collections.sort(results);
		double spec = 0;
		results_it = results.iterator();
		while(results_it.hasNext()){
			MatchResult mr = results_it.next();
			match_specificities.put(mr.getMatchVector(), spec);
			spec = spec + mr.getFalseProbability();
		}
		
	}
	
	public double getScore(MatchVector mv){
		return match_scores.get(getEquivalentMatchVector(mv)).doubleValue();
	}
	
	public double getSensitivity(MatchVector mv){
		return match_sensitivities.get(getEquivalentMatchVector(mv)).doubleValue();
	}
	
	public double getSpecificity(MatchVector mv){
		return match_specificities.get(getEquivalentMatchVector(mv)).doubleValue();
	}
	
	/*
	 * Method created since Hashtable.get(MatchVector) will
	 * not work directly.
	 */
	private MatchVector getEquivalentMatchVector(MatchVector mv){
		Iterator<MatchVector> it = match_scores.keySet().iterator();
		while(it.hasNext()){
			MatchVector test = it.next();
			if(test.equals(mv)){
				return test;
			}
		}
		return null;
	}
	
	/*
	 * Method returns a list of MatchVectors that cover all
	 * possible combinations of matching/non-match between 
	 * demographics.
	 */
	private List<MatchVector> getPossibleMatchingVectors(){
		ArrayList<MatchVector> mvs = new ArrayList<MatchVector>();
		String[] demographics = mc.getIncludedColumnsNames();
		for(int i = 0; i < Math.pow(2,demographics.length); i++){
			MatchVector mv = new MatchVector();
			String binary_string = Integer.toBinaryString(i);
			
			// pad to full length of demographics
			while(binary_string.length() < demographics.length){
				binary_string = "0" + binary_string;
			}
			
			// iterate over characters in string
			for(int j = 0; j < binary_string.length(); j++){
				char bit = binary_string.charAt(j);
				if(bit == '1'){
					mv.setMatch(demographics[j], true);
				} else {
					mv.setMatch(demographics[j], false);
				}
			}
			mvs.add(mv);
		}
		
		return mvs;
	}
	
	/*
	 * True probability calculated from the matched values in the
	 * MatchingConfig object.  If the match in the MatchVector is true,
	 * then the m value is used.  If the match was false, then 
	 * 1 - m is used.  The probabilities are multiplied together and the 
	 * product returned.
	 */
	public double getMatchVectorTrueProbability(MatchVector mv){
		Iterator<String> it = mv.getDemographics().iterator();
		double prob = 1;
		while(it.hasNext()){
			String demographic = it.next();
			double demo_prob = mc.getAgreementValue(mc.getRowIndexforName(demographic));
			if(mv.matchedOn(demographic)){
				prob = prob * demo_prob;
			} else {
				prob = prob * (1 - demo_prob);
			}
		}
		return prob;
	}
	
	public double getMatchVectorFalseProbability(MatchVector mv){
		Iterator<String> it = mv.getDemographics().iterator();
		double prob = 1;
		while(it.hasNext()){
			String demographic = it.next();
			double demo_prob = mc.getNonAgreementValue(mc.getRowIndexforName(demographic));
			if(mv.matchedOn(demographic)){
				prob = prob * demo_prob;
			} else {
				prob = prob * (1 - demo_prob);
			}
		}
		return prob;
	}
	
	private double getMatchVectorScore(MatchVector mv){
		double score = 0;
		List<String> demographics = mv.getDemographics();
		Iterator<String> it = demographics.iterator();
		while(it.hasNext()){
			String d = it.next();
			//MatchingConfigRow mcr = mc.getMatchingConfigRows().get(mc.getRowIndexforName(d));
			if(mv.matchedOn(d)){
				score += m_values.get(d).doubleValue();
			} else {
				score += u_values.get(d).doubleValue();
			}
		}
		
		return score;
	}
	
	public String toString(){
		String ret = new String("vector,score,sens,spec\n");
		
		Iterator<MatchVector> it = match_scores.keySet().iterator();
		while(it.hasNext()){
			MatchVector mv = it.next();
			double score = match_scores.get(mv);
			double sens = match_sensitivities.get(mv);
			double spec = match_specificities.get(mv);
			ret += mv + "," + score + "," + sens + "," + spec + "\n";
		}
		
		return ret;
	}
}
