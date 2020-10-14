package org.regenstrief.linkage;

import java.util.HashMap;

/**
 * Class extends MatchVector to include information about whether one of the demographic values had
 * a null value. For example, if FN (first name) for one of the two records had a blank value, a
 * NullSetMatchVector should be created instead of a normal MatchVector Class does not override
 * MatchVector.equals() and .hashCode(). The desired behaviour currently is that methods like
 * VectorTable.getScore(MatchVector) return the same values if MatchVector and NullSetMatchVector
 * have the same set of agreement on demographics, and that the extra metadata is transparents for
 * these calls. This also means that in code like EMAnalyzer that cares about the metadata
 * MatchResults will need to be stored separetely from NullSetResults so information is not lost.
 */

public class NullDemographicsMatchVector extends MatchVector {
	
	HashMap<String, Boolean> null_comparisons;
	
	public NullDemographicsMatchVector() {
		super();
		null_comparisons = new HashMap<String, Boolean>();
	}
	
	/**
	 * Method sets a boolean flag that one or both of the two records had a null value or empty string
	 * for the given demographic
	 * 
	 * @param demographic record's demographic that had missing information
	 */
	public void hadNullValue(String demographic) {
		null_comparisons.put(demographic, Boolean.TRUE);
	}
	
	/**
	 * Method indicates if the MatchVector was made from two records where one or both had a null or
	 * empty value for the given demographic
	 * 
	 * @param demographic the demographic of interest
	 * @return whether either of the two records had a null value for the demographic
	 */
	public boolean isNullComparison(String demographic) {
		Boolean ret = null_comparisons.get(demographic);
		if (ret != null) {
			return ret.booleanValue();
		} else {
			return false;
		}
	}
}
