package org.regenstrief.linkage.util;

/**
 * Takes a record and calculates a score between them using the options in the
 * given MatchingConfig object
 * 
 * TODO: Add size parameter to lds1_frequencies & lds2_frequencies
 */

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.analysis.*;
import org.regenstrief.linkage.db.ScaleWeightDBManager;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import java.util.*;

public class ScorePair {
	private VectorTable vt;
	private MatchingConfig mc;
	private List<Modifier> modifiers;

	public ScorePair(MatchingConfig mc){
		this.mc = mc;
		vt = new VectorTable(mc);
		modifiers = new ArrayList<Modifier>();
	}

	public void addScoreModifier(Modifier sm){
		modifiers.add(sm);
	}

	public MatchResult scorePair(Record rec1, Record rec2){
		MatchVector mv = new MatchVector();
		List<MatchingConfigRow> config_rows = mc.getIncludedColumns();
		Iterator<MatchingConfigRow> it = config_rows.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			String comparison_demographic = mcr.getName();
			String data1 = rec1.getDemographic(comparison_demographic);
			String data2 = rec2.getDemographic(comparison_demographic);

			boolean match = false;
			if(data1 != null && data2 != null) {
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

		MatchResult mr = new MatchResult(vt.getScore(mv),vt.getMatchVectorTrueProbability(mv),vt.getMatchVectorFalseProbability(mv),vt.getSensitivity(mv),vt.getSpecificity(mv),mv,vt.getScoreVector(mv),rec1,rec2);
		for(Modifier m : modifiers){
			m.modifyMatchResult(mr, mc);
		}

		return mr;
	}
}