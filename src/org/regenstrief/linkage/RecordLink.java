package org.regenstrief.linkage;

/**
 * Class represents a link between two Records, a decision that the two Records
 * refer to the same thing.
 *
 */

public abstract class RecordLink {
	Record r1, r2;
	
	public RecordLink(Record r1, Record r2){
		this.r1 = r1;
		this.r2 = r2;
	}
	
	/**
	 * Method returns the first Record that is equal
	 * 
	 * @return	the first equivalent Record object
	 */
	public Record getRecord1(){
		return r1;
	}
	
	/**
	 * Method returns the second Record that is equal
	 * @return	the second equivalent Record object
	 */
	public Record getRecord2(){
		return r2;
	}
}
