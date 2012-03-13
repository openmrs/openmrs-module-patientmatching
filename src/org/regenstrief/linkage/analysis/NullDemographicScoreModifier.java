package org.regenstrief.linkage.analysis;

import java.util.Iterator;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.ModifiedMatchResult;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Class changes the score of a MatchResult to reflect an average of the m and u values
 * of demographics that have at least one null value.
 *
 */

public class NullDemographicScoreModifier implements Modifier{
	
	public String getModifierName(){
		return "NullDemographicScore Modifer";
	}
	
	public void initializeModifier(){
		
	}
	
	public ModifiedMatchResult getModifiedMatchResult(MatchResult mr, MatchingConfig mc){
		ModifiedMatchResult ret = new ModifiedMatchResult(mr);
		Iterator<String> it = mr.getDemographics().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String val1 = mr.getRecord1().getDemographic(demographic);
			String val2 = mr.getRecord2().getDemographic(demographic);
			
			if(val1 == null || val1.equals("") || val2 == null || val2.equals("")){
				// change the score of this demographic to be an average between the score value of
				// a match and non-match
				MatchingConfigRow mcr = mc.getMatchingConfigRowByName(demographic);
				double match_rate = mcr.getAgreement();
				double umatch_rate = mcr.getNonAgreement();
				
				double m = Math.log(match_rate / umatch_rate) / Math.log(2);
				double u = Math.log( (1 - match_rate) / (1 - umatch_rate) ) / Math.log(2);
				
				double delta = (m - u) / 2;
				
				ret.addDemographicScalarModifier(this, demographic, delta, ModifiedMatchResult.Operator.PLUS);
			}
		}
		
		return ret;
	}
}
