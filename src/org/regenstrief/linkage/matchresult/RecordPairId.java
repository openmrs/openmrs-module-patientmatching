/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.regenstrief.linkage.matchresult;


/**
 * Convenience class to store the id only of <code>MatchResult</code> record.
 */
public class RecordPairId {
    /**
     * first record id of the pair
     */
	private Integer firstRecordId;
	
	/**
	 * second record id of the pair
	 */
	private Integer secondRecordId;
	
	/**
	 * flag denoting whether this record pair has been 
	 * processed for flattening process
	 */
	private boolean processed;

    /**
     * @return the firstRecordId
     */
    public Integer getFirstRecordId() {
        return firstRecordId;
    }

    /**
     * @param firstRecordId the firstRecordId to set
     */
    public void setFirstRecordId(Integer firstRecordId) {
        this.firstRecordId = firstRecordId;
    }

    /**
     * @return the secondRecordId
     */
    public Integer getSecondRecordId() {
        return secondRecordId;
    }

    /**
     * @param secondRecordId the secondRecordId to set
     */
    public void setSecondRecordId(Integer secondRecordId) {
        this.secondRecordId = secondRecordId;
    }

    /**
     * @return the processed
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * @param processed the processed to set
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    
    /**
     * Convenience method to check if a RecordPairId contains
     * certain id or not
     * @param i the id to be checked
     * @return true if the id is found
     */
    private boolean contains(Integer i) {
    	if (firstRecordId.equals(i) || secondRecordId.equals(i)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Method to check whether a pair can be flatten or not
     * @param otherPairId pair to be tested for flatten process
     * @return true if both pair can be flatten
     */
    public boolean flattenable(RecordPairId otherPairId) {
        if (contains(otherPairId.getFirstRecordId()) ||
                contains(otherPairId.getSecondRecordId())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Default constructor
     * @param r1
     * @param r2
     */
    public RecordPairId(Integer firstRecordId, Integer secondRecordId) {
        this.firstRecordId = firstRecordId;
        this.secondRecordId = secondRecordId;
        processed = false;
    }
}
