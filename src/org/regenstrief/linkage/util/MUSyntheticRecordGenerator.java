package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.RecordFrequencies;
import org.regenstrief.linkage.analysis.VectorTable;

public class MUSyntheticRecordGenerator extends SyntheticRecordGenerator {

	double p;
	Random rand;
	long count;
	VectorTable vt;
	private List<String> primary_demographics;
	private List<String> dependent_demographics;
	private ScorePair sp;
	
	public MUSyntheticRecordGenerator(MatchingConfig mc, RecordFrequencies rf, double p){
		super(mc, rf);
		this.p = p;
		count = 0;
		rand = new Random();
		vt = new VectorTable(mc);
		sp = new ScorePair(mc);
		primary_demographics = new ArrayList<String>();
		dependent_demographics = new ArrayList<String>();
		generateDemographicsLists();
	}
	
	@Override
	public MatchResult getRecordPair() {
		Record[] ret = new Record[2];
		ret[0] = new Record(count++, "synthetic");
		ret[1] = new Record(count++, "synthetic");
		
		boolean true_match = false;
		double d = rand.nextDouble();
		if(d <= p){
			true_match = true;
		}
		
		MatchVector mv = generateMatchVector(true_match);
		
		Iterator<String> p_it = primary_demographics.iterator();
		while(p_it.hasNext()){
			String p_demographic = p_it.next();
			MatchingConfigRow mcr = mc.getMatchingConfigRowByName(p_demographic);
			if(true_match){
				setRandomDemographic(mv, ret[0], p_demographic);
				double agreement = rand.nextDouble();
				if(agreement < mcr.getAgreement()){
					ret[1].addDemographic(p_demographic, ret[0].getDemographic(p_demographic));
					String rank_demographic = p_demographic + DEMOGRAPHIC_RANK_SUFFIX;
					ret[1].addDemographic(rank_demographic, ret[0].getDemographic(rank_demographic));
				} else {
					setRandomDisagreementDemographic(mv, ret[1], p_demographic, ret[0].getDemographic(p_demographic));
				}
			} else {
				// set random demographics for both records
				setRandomDemographic(mv, ret[0], p_demographic);
				setRandomDemographic(mv, ret[1], p_demographic);
			}
		}
		
		Iterator<String> d_it = dependent_demographics.iterator();
		while(d_it.hasNext()){
			String demographic = d_it.next();
			MatchingConfigRow mcr = mc.getMatchingConfigRowByName(demographic);
			if(true_match){
				setRandomDemographic(mv, ret[0], demographic);
				double agreement = rand.nextDouble();
				if(agreement < mcr.getAgreement()){
					ret[1].addDemographic(demographic, ret[0].getDemographic(demographic));
					String rank_demographic = demographic + DEMOGRAPHIC_RANK_SUFFIX;
					ret[1].addDemographic(rank_demographic, ret[0].getDemographic(rank_demographic));
				} else {
					setRandomDisagreementDemographic(mv, ret[1], demographic, ret[0].getDemographic(demographic));
				}
			} else {
				// set random demographics for both records
				setRandomDemographic(mv, ret[0], demographic);
				setRandomDemographic(mv, ret[1], demographic);
			}
		}
		
		// create match vector that describes the generated records
		MatchResult mr = sp.scorePair(ret[0], ret[1]);
		
		// double score, double incl_score, double true_prob, double false_prob, double sensitivity, double specificity, MatchVector match_vct, ScoreVector score_vct, Record r1, Record r2, MatchingConfig mc){
		//MatchResult mr = new MatchResult(vt.getScore(mv), vt.getInclusiveScore(mv), vt.getMatchVectorTrueProbability(mv), vt.getMatchVectorFalseProbability(mv), vt.getSensitivity(mv), vt.getSpecificity(mv), mv, vt.getScoreVector(mv), ret[0], ret[1], mc);
		mr.setMatch(true_match);
		return mr;
		
	}
	
	private MatchVector generateMatchVector(boolean true_match){
		MatchVector ret = new MatchVector();
		Iterator<MatchingConfigRow> it = mc.getMatchingConfigRows().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			String demographic = mcr.getName();
			
		}
		return ret;
	}
	
	private void generateDemographicsLists(){
		//Iterator<MatchingConfigRow> it = mc.getIncludedColumns().iterator();
		Iterator<MatchingConfigRow> it = mc.getMatchingConfigRows().iterator();
		
		while(it.hasNext()){
			String demographic = it.next().getName();
			String context = rf1.getContext(demographic);
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

}
