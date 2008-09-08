package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;


/**
 * Object wraps a FormPairs object and provides direct access to a specific Record pair.
 * The class was written assuming that sub-classes will store a list of UIDs of the pairs from
 * the FormPairs given in the constructor, and the sub class will implement getRecordFromUID()
 * as the way to recreate the Record from the requested unique ID.
 * 
 * @author jegg
 *
 */

public abstract class LookupFormPairs extends FormPairs {
	
	protected FormPairs fp;
	
	public LookupFormPairs(FormPairs fp){
		super(fp.getMatchingConfig());
		this.fp = fp;
	}
	
	public abstract boolean reset();
	
	public abstract int size();
	
	public abstract Record[] getRecordPair(int index);
	
	protected abstract Record getRecordFromUID(int ID, String context);

}
