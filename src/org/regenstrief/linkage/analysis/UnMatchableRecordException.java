package org.regenstrief.linkage.analysis;

/**
 * Class is thrown when a Record object is inspected and found to be
 *
 */

import org.regenstrief.linkage.*;

public class UnMatchableRecordException extends Exception {
	Record invalid;
	
	public UnMatchableRecordException(Record r){
		super();
		invalid = r;
	}
	
	public Record getRecord(){
		return invalid;
	}
}
