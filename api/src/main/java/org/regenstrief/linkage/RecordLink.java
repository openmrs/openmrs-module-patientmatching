package org.regenstrief.linkage;

/**
 * Class represents a link between two Records, a possibliity that the two Records refer to the same
 * thing.
 */

public abstract class RecordLink {
	
	protected Record r1, r2;
	
	protected boolean match;
	
	public RecordLink(Record r1, Record r2) {
		this.r1 = r1;
		this.r2 = r2;
		match = false;
	}
	
	/**
	 * Method returns whether or not the two Records are considered the same entity
	 * 
	 * @return if two Records are the same
	 */
	public boolean isMatch() {
		return match;
	}
	
	/**
	 * Method sets the flag to determine if the two Records describe the same thing
	 * 
	 * @param match boolaen indicating whether Record 1 and 2 are the same
	 */
	public void setMatch(boolean match) {
		this.match = match;
	}
	
	/**
	 * Method returns the first Record that is equal
	 * 
	 * @return the first equivalent Record object
	 */
	public Record getRecord1() {
		return r1;
	}
	
	/**
	 * Method returns the second Record that is equal
	 * 
	 * @return the second equivalent Record object
	 */
	public Record getRecord2() {
		return r2;
	}
}
