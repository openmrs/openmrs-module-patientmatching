package org.regenstrief.linkage;

import java.util.*;

public class MatchVector implements Comparable{
	private Hashtable<String,MatchItem> match_table;
	
	public MatchVector(){
		match_table = new Hashtable<String,MatchItem>();
	}
	
	public int getSize() {
		return match_table.size();
	}
	
	/**
	 * Method sets an entry to store whether the two records matched
	 * on the given demographic.
	 * 
	 * @param demographic	the demographic used in the comparison
	 * @param matched	whether this demographic was a match
	 */
	public void setMatch(String demographic, boolean matched){
		setMatch(demographic, matched ? 1 : 0, matched);
	}
	
	/**
	 * Method sets an entry to store whether the two records matched
	 * on the given demographic.
	 * 
	 * @param demographic	the demographic used in the comparison
	 * @param similarity	the similarity score
	 * @param matched	whether this demographic was a match
	 */
	public void setMatch(String demographic, float similarity, boolean matched){
		match_table.put(demographic, new MatchItem(similarity, matched));
	}
	
	/**
	 * Returns a list of the keys in the match table that contain the demographics
	 * used in this record matching
	 * 
	 * @return	a List<String> of the demographics in the matching
	 */
	public List<String> getDemographics(){
		Iterator<String> it = match_table.keySet().iterator();
		List<String> ret = new ArrayList<String>();
		while(it.hasNext()){
			ret.add(it.next());
		}
		return ret;
	}
	
	/**
	 * Returns whether the two records matched on the given demographic
	 * 
	 * @param demographic	the demographic of interest
	 * @return	whether the demographic matched for the two Records
	 */
	public boolean matchedOn(String demographic){
		MatchItem matched = match_table.get(demographic);
		if(matched == null || !matched.isMatch()){
			return false;
		} else {
			return true;
		}
	}
	
	public MatchItem getMatchItem(String demographic) {
		return match_table.get(demographic);
	}
	
	/**
	 * 
	 * @return returns the matches for the comparison
	 */
	public Hashtable getMatchTable(){
		return match_table;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof MatchVector){
			MatchVector mv = (MatchVector)obj;
			return this.match_table.equals(mv.getMatchTable());
		} else {
			return false;
		}
		
		//return true;
	}
	
	public int compareTo(Object o) throws ClassCastException{
		MatchVector mv = (MatchVector) o;
		return 0;
	}
	
	public int hashCode(){
		return Integer.parseInt(this.toString(), 2);
	}
	
	public String toString(){
		Iterator<String> it = match_table.keySet().iterator();
		String ret = new String();
		while(it.hasNext()){
			String s = it.next();
			if(matchedOn(s)){
				ret += "1";
			} else {
				ret += "0";
			}
		}
		return ret;
	}
}
