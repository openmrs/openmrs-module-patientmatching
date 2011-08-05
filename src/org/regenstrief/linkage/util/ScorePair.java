package org.regenstrief.linkage.util;

/**
 * Takes a record and calculates a score between them using the options in the
 * given MatchingConfig object
 * 
 * TODO: Add size parameter to lds1_frequencies & lds2_frequencies
 * TODO: Implement functionality for these two parameters: 
 * - A flag indicating whether to use null tokens when scaling agreement weight based on term frequency (default-no)
 * - A flag indicating how to establish agreement among fields when one or both fields are null (eg, apply disagreement weight, apply agreement weight, or apply ze
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.NullDemographicsMatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.Modifier;
import org.regenstrief.linkage.analysis.VectorTable;

public class ScorePair {
	private VectorTable vt;
	private MatchingConfig mc;
	private List<Modifier> modifiers;
	private Hashtable<MatchVector,Long> observed_vectors;

	public ScorePair(MatchingConfig mc){
		this.mc = mc;
		vt = new VectorTable(mc);
		modifiers = new ArrayList<Modifier>();
		observed_vectors = new Hashtable<MatchVector,Long>();
	}

	public void addScoreModifier(Modifier sm){
		modifiers.add(sm);
	}

	public MatchResult scorePair(Record rec1, Record rec2){
		MatchVector mv;
		if(rec1.hasNullValues() || rec2.hasNullValues()){
			mv = new NullDemographicsMatchVector();
		} else {
			mv = new MatchVector();
		}

		List<MatchingConfigRow> config_rows = mc.getIncludedColumns();
		Iterator<MatchingConfigRow> it = config_rows.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			String comparison_demographic;
			
			comparison_demographic = mcr.getName();
			
			String data1 = rec1.getDemographic(comparison_demographic);
			String data2 = rec2.getDemographic(comparison_demographic);

			if(data1.equals("") || data2.equals("")){
				NullDemographicsMatchVector nsmv = (NullDemographicsMatchVector)mv;
				nsmv.hadNullValue(comparison_demographic);
			}

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
        
		mv = enableInterchageableFieldComparsion(rec1, rec2, mv);
		MatchResult mr = new MatchResult(vt.getScore(mv),vt.getInclusiveScore(mv),vt.getMatchVectorTrueProbability(mv),vt.getMatchVectorFalseProbability(mv),vt.getSensitivity(mv),vt.getSpecificity(mv),mv,vt.getScoreVector(mv),rec1,rec2,mc);
		for(Modifier m : modifiers){
			mr = m.getModifiedMatchResult(mr, mc);
		}
		
		mr.setCertainty(1);
		mr.setMatch_status(MatchResult.UNKNOWN);
		
		Long l = observed_vectors.get(mr.getMatchVector());
		if(l == null){
			l = new Long(1);
		} else {
			l = l + 1;
		}
		observed_vectors.put(mr.getMatchVector(), l);

		return mr;
	}
	
	/**
	 * This method would check if the interchangeable columns have same value , 
	 * if the columns match with each other then the individual columns 
	 * which make the concat1 column would be set as true
	 * */
	private MatchVector enableInterchageableFieldComparsion(Record rec1 , Record rec2, MatchVector mv)
	{   
		Set<String> s=new HashSet<String>();
	    s=mc.getInterchangeableColumns();
	    String name;
		String name1;
	    Iterator<String> i=s.iterator();
	    while(i.hasNext())
	    {  String comparision_demographic=i.next();
	    	name = rec1.getDemographic(comparision_demographic);
			name1=rec2.getDemographic(comparision_demographic);

		boolean b = name.isEmpty() & name1.isEmpty();
		
		if(!b)
		{
			 boolean match = StringMatch.LCSMatch(name,name1,0.85);
		if(match)
		{
			List<String> concatenatedDemographics = mc.getConcatenatedDemographics(comparision_demographic);
			for(String cd : concatenatedDemographics)
			{	
				mv.setMatch(cd,match);
			}
		}
		
		
		return mv;
		}
		else
	    return mv;
	}
		return mv;
	    }
	
	public Hashtable<MatchVector,Long> getObservedVectors(){
		return observed_vectors;
	}
}