package org.regenstrief.linkage.io;

import java.util.Iterator;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.ScorePair;

/**
 * Class takes a FormPairs object, and only passes through pairs which match
 * on one of the include fields other than sex/gender
 * 
 * @author jegg
 *
 */

public class NoMatchFilteringFormPairs extends FormPairs {

	private FormPairs fp;
	private ScorePair sp;
	private String allowed_demographic;
	private int filtered, printed;
	
	public NoMatchFilteringFormPairs(FormPairs fp){
		super(fp.getMatchingConfig());
		this.fp = fp;
		sp = new ScorePair(fp.getMatchingConfig());
		allowed_demographic = "gender";
		printed = filtered = 0;
	}
	
	public String getAllowedDemographic(){
		return allowed_demographic;
	}
	
	public void setAllowedDemographic(String demographic){
		allowed_demographic = demographic;
	}
	
	@Override
	public Record[] getNextRecordPair() {
		boolean finished = false;
		Record[] candidate = null;
		while(!finished){
			candidate = fp.getNextRecordPair();
			if(candidate == null){
				finished = true;
			} else {
				if(passesFilter(candidate)){
					finished = true;
					printed++;
				} else {
					filtered++;
				}
			}
		}
		
		return candidate;
	}
	
	private boolean passesFilter(Record[] pair){
		MatchResult mr = sp.scorePair(pair[0], pair[1]);
		boolean passes = false;
		Iterator<String> it = mr.getMatchVector().getDemographics().iterator();
		while(it.hasNext() && !passes){
			String demographic = it.next();
			if(!demographic.equals(allowed_demographic)){
				if(mr.getMatchVector().matchedOn(demographic)){
					passes = true;
				}
			}
		}
		
		return passes;
	}
	
	public int getFilteredCount(){
		return filtered;
	}
	
	public int getAllowedCount(){
		return printed;
	}

}
