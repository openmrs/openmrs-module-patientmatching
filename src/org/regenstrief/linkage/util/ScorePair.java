package org.regenstrief.linkage.util;

/*
 * Takes a record and calculates a score between them using the options in the
 * given MatchingConfig object
 */

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.analysis.*;
import java.util.*;

public class ScorePair {
	VectorTable vt;
	MatchingConfig mc;
	
	public ScorePair(MatchingConfig mc){
		this.mc = mc;
		vt = new VectorTable(mc);
	}
	
	public MatchResult scorePair(Record rec1, Record rec2){
		
		MatchVector mv = new MatchVector();
		Iterator<MatchingConfigRow> it = mc.getIncludedColumns().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			String comparison_demographic = mcr.getName();
			String data1 = rec1.getDemographic(comparison_demographic);
			String data2 = rec2.getDemographic(comparison_demographic);
			
			boolean match = false;
			if(data1 != null && data2 != null){
				switch(mcr.getAlgorithm()){
				case(MatchingConfig.EXACT_MATCH):
					match = StringMatch.exactMatch(data1, data2);
					break;
				case(MatchingConfig.JWC):
					match = StringMatch.JWCMatch(data1, data2);
					break;
				case(MatchingConfig.LCS):
					match = StringMatch.LCSMatch(data1, data2);
					break;
				case(MatchingConfig.LEV):
					match = StringMatch.LEVMatch(data1, data2);
					break;
				}
			}
			
			mv.setMatch(comparison_demographic, match);
			
		}
		
		MatchResult mr = new MatchResult(vt.getScore(mv),vt.getMatchVectorTrueProbability(mv),vt.getMatchVectorFalseProbability(mv),mv,rec1,rec2);
		return mr;
	}
}
