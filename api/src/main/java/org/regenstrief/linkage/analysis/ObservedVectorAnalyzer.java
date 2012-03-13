package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.ScorePair;

public class ObservedVectorAnalyzer extends RecordPairAnalyzer implements
		LoggingObject {

	private ScorePair sp;
	private int count;
	
	public ObservedVectorAnalyzer(MatchingConfig mc){
		super(mc);
		sp = new ScorePair(mc);
		count = 0;
	}
	
	public void finishAnalysis() {
		Hashtable<MatchVector,Long> vectors = sp.getObservedVectors();
		VectorTable vt = new VectorTable(mc);
		List<MatchResult> mrs = vt.getPossibleMatchResults();
		Iterator<MatchResult> mrs_it = mrs.iterator();
		log.info("vector|score|true_prob|false_prob|expected|observed");
		while(mrs_it.hasNext()){
			MatchVector mv_obs = mrs_it.next().getMatchVector();
			Long l = vectors.get(mv_obs);
			if(l == null){
				l = (long) 0;
			}
			double score = vt.getScore(mv_obs);
			double expected_true = vt.getMatchVectorTrueProbability(mv_obs) * mc.getP() * count;
			double expected_false = vt.getMatchVectorFalseProbability(mv_obs) * (1 - mc.getP()) * count;
			double expected = expected_true + expected_false;
			log.info("\"" + mv_obs + "\"|" + score + "|" +vt.getMatchVectorTrueProbability(mv_obs) + "|" + vt.getMatchVectorFalseProbability(mv_obs) + "|" + expected + "|" + l);
		}

	}

	@Override
	public void analyzeRecordPair(Record[] pair) {
		sp.scorePair(pair[0], pair[1]);
		count++;
	}

}
