/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientmatching;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.RecordPairId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclasses of this interface provide a mechanism to save matched records, it has a default
 * implementation that writes the contents of the records to the configured serialization folder.
 * <p>
 * <b>MUST</b> call clean() method before using the store
 * </p>
 */
public interface MatchedRecordsStore {
	
	Logger logger = LoggerFactory.getLogger(MatchedRecordsStore.class);
	
	/**
	 * Ensures the store is in a pristine state by discarding in memory matched records and also deletes
	 * them from the serialization folder.
	 * 
	 * @throws IOException
	 */
	default void clean() throws IOException {
		logger.info("Cleaning matched records store ...");
		getPairIdList().clear();
		getFlattenedPairIds().clear();
		getSerializedRecords().clear();
		
		FileUtils.deleteDirectory(MatchingUtils.getSerializationFolder());
		logger.info("Deleted record serialization folder ...");
	}
	
	/**
	 * Stores the specified pair of matched records, this default implementation serializes the records
	 * and writes their contents to the configured serialization folder.
	 * 
	 * @param r1 the record to save
	 * @param r2 the other record to save
	 * @throws IOException
	 */
	default void storePair(Record r1, Record r2) throws IOException {
		boolean r1SerializedBefore = true;
		boolean r2SerializedBefore = true;
		if (!getSerializedRecords().contains(r1.getUID())) {
			RecordSerializer.serialize(r1);
			getSerializedRecords().add(r1.getUID());
			r1SerializedBefore = false;
		}
		
		if (!getSerializedRecords().contains(r2.getUID())) {
			RecordSerializer.serialize(r2);
			getSerializedRecords().add(r2.getUID());
			r2SerializedBefore = false;
		}
		
		// if both have been encountered, don't need to process this type of record
		if (!r1SerializedBefore || !r2SerializedBefore) {
			RecordPairId r = new RecordPairId(r1.getUID(), r2.getUID());
			getPairIdList().add(r);
		}
	}
	
	/**
	 * Flatten the pair id list and make a list id set. The method exploit the ability of a set to
	 * reject duplicate entries.
	 */
	default void flattenPairIdList() {
		for (int i = 0; i < getPairIdList().size(); i++) {
			Set<Long> set = new TreeSet();
			RecordPairId currentId = getPairIdList().get(i);
			if (!currentId.isProcessed()) {
				set.add(currentId.getFirstRecordId());
				set.add(currentId.getSecondRecordId());
				for (int j = i + 1; j < getPairIdList().size(); j++) {
					RecordPairId processedIdPair = getPairIdList().get(j);
					if (processedIdPair.flattenable(currentId)) {
						set.add(processedIdPair.getFirstRecordId());
						set.add(processedIdPair.getSecondRecordId());
						processedIdPair.setProcessed(true);
					}
				}
				getFlattenedPairIds().add(set);
			}
		}
		
		// if records are expanded so multiple IDs might appear and be paired, then
		// it's possible that a set of IDs might just have one ID; these need to be removed
		Iterator<Set<Long>> it = getFlattenedPairIds().iterator();
		while (it.hasNext()) {
			Set<Long> entity = it.next();
			if (entity.size() == 1) {
				it.remove();
			}
		}
	}
	
	/**
	 * Gets the list of matched RecordPairIds
	 * 
	 * @return a list of RecordPairIds
	 */
	List<RecordPairId> getPairIdList();
	
	/**
	 * Gets the flattened list of matched id pairs
	 * 
	 * @return a list of if sets
	 */
	List<Set<Long>> getFlattenedPairIds();
	
	/**
	 * Gets the set of serialized matched record ids
	 * 
	 * @return a set of record ids
	 */
	Set<Long> getSerializedRecords();
	
}
