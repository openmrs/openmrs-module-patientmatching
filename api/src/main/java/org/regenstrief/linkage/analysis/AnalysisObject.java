package org.regenstrief.linkage.analysis;

import java.util.Comparator;

public class AnalysisObject implements Comparable {

	public String token;
	public Integer frequency;
	
	public AnalysisObject(String token, Integer frequency) {
		this.token = token;
		this.frequency = frequency;
	}
	
	public boolean equals(Object o) {
		AnalysisObject oo = (AnalysisObject) o;
		if(oo.frequency == frequency && oo.token.equals(token))
			return true;
		else
			return false;
	}
	
	public int compareTo(Object o) throws ClassCastException {
		 if (!(o instanceof AnalysisObject))
		      throw new ClassCastException("An AnalysisObject expected.");
		AnalysisObject other = (AnalysisObject) o;
		return other.frequency - frequency;
	}
	
	public int hashCode(){
		return token.hashCode();
	}

	public static final Comparator<AnalysisObject> frequencyComparator = new Comparator<AnalysisObject>() {
		public int compare(AnalysisObject o1, AnalysisObject o2) {
			return o1.frequency - o2.frequency;
		}
	};
}
