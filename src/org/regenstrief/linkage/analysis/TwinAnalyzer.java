package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.Record;

/**
 * Class checks two records to see if they are likely to be twins.  Current
 * default rules that will be implemented are:
 *     FN’s disagree and MRN’s disagree
 *     Genders both not null and Genders disagree
 *     FN’s disagree and Genders disagree
 *     FN’s disagree and at least one MRN null
 * 
 * An initial check for different MRNs are done and if they are
 * the same, then the method immediately returns false.
 * 
 * @author jegg
 *
 */

public class TwinAnalyzer {
	
	public TwinAnalyzer(){
		
	}
	
	public boolean areTwins(Record rec1, Record rec2){
		String fn1 = rec1.getDemographic("fn");
		String mrn1 = rec1.getDemographic("mrn");
		String gender1 = rec1.getDemographic("gender");
		
		String fn2 = rec2.getDemographic("fn");
		String mrn2 = rec2.getDemographic("mrn");
		String gender2 = rec2.getDemographic("gender");
		
		// do this check first, MRN is important demographic and equality on
		// this field should always mean not a twin
		if(mrn1 != null && mrn2 != null && mrn1.equals(mrn2)){
			return false;
		}
		
		if(fn1 != null && fn2 != null && !fn1.equals(fn2) && mrn1 != null && mrn2 != null && !mrn1.equals(mrn2)){
			// first names and mrns disagree
			return true;
		}
		if(gender1 != null && gender2 != null && !gender1.equals(gender2)){
			// genders disagree
			return true;
		}
		if(fn1 != null && fn2 != null && !fn1.equals(fn2) && gender1 != null && gender2 != null && !gender1.equals(gender2)){
			// first names disagree and genders disagree
			return true;
		}
		if(fn1 != null && fn2 != null && !fn1.equals(fn2) && (mrn1 == null || mrn2 == null)){
			// first names do not agree and one mrn is null
			return true;
		}
		
		
		return false;
	}
}
