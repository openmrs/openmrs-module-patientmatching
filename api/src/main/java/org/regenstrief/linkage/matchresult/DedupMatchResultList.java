/**
 */
package org.regenstrief.linkage.matchresult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.RecordSerializer;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

/**
 *
 */
public class DedupMatchResultList extends MatchResultList {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private List<RecordPairId> pairIdList;
	
	private List<Set<Long>> flattenedPairIds;
	
	private Set<Long> serializedRecord;
	
	public DedupMatchResultList() {
		super();
		
		pairIdList = new ArrayList<RecordPairId>();
		flattenedPairIds = new ArrayList<Set<Long>>();
		serializedRecord = new TreeSet<Long>();
		
		String configLocation = MatchingConstants.SERIAL_FOLDER_NAME;
		File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
		
		cleanUpFolder(configFileFolder);
		boolean deleted = configFileFolder.delete();
		if (deleted) {
			log.info("Deleted record serialization folder ...");
		}
	}
	
	/**
	 * Clean up the serialization folder to ensure that we will get the latest serialization data.
	 * 
	 * @param folder the folder of the serialization will be stored
	 */
	public void cleanUpFolder(File folder) {
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				cleanUpFolder(file);
			} else {
				file.delete();
			}
		}
	}
	
	/**
	 * @see org.regenstrief.linkage.matchresult.MatchResultList#acceptMatchResult(org.regenstrief.linkage.MatchResult)
	 */
	@Override
	public synchronized void acceptMatchResult(MatchResult mr) {
		
		if (mr.getScore() > mr.getMatchingConfig().getScoreThreshold()) {
			Record r1 = mr.getRecord1();
			Record r2 = mr.getRecord2();
			
			boolean r1SerializedBefore = true;
			boolean r2SerializedBefore = true;
			// serialize only record that is not serialized before
			if (!serializedRecord.contains(r1.getUID())) {
				try {
					RecordSerializer.serialize(r1);
					serializedRecord.add(r1.getUID());
					r1SerializedBefore = false;
				}
				catch (FileNotFoundException e) {
					log.info(
					    "FileNotFoundException: Unable to serialize record with id " + r1.getUID() + ": " + e.getMessage());
				}
				catch (SecurityException e) {
					log.info("SecurityException: Unable to serialize record with id " + r1.getUID() + ": " + e.getMessage());
				}
				catch (IOException e) {
					log.info(
					    "IOException: Error in closing serialized record with id " + r1.getUID() + ": " + e.getMessage());
				}
			}
			if (!serializedRecord.contains(r2.getUID())) {
				try {
					RecordSerializer.serialize(r2);
					serializedRecord.add(r2.getUID());
					r2SerializedBefore = false;
				}
				catch (FileNotFoundException e) {
					log.info(
					    "FileNotFoundException: Unable to serialize record with id " + r2.getUID() + ": " + e.getMessage());
				}
				catch (SecurityException e) {
					log.info("SecurityException: Unable to serialize record with id " + r2.getUID() + ": " + e.getMessage());
				}
				catch (IOException e) {
					log.info(
					    "IOException: Error in closing serialized record with id " + r2.getUID() + ": " + e.getMessage());
				}
			}
			
			// if both have been encountered, don't need to process this type of record
			if (!r1SerializedBefore || !r2SerializedBefore) {
				RecordPairId r = new RecordPairId(r1.getUID(), r2.getUID());
				pairIdList.add(r);
			}
		}
	}
	
	/**
	 * Flatten the pair id list and make a list id set. The method exploit the ability of a set to
	 * reject duplicate entries.
	 */
	public void flattenPairIdList() {
		for (int i = 0; i < pairIdList.size(); i++) {
			Set<Long> set = new TreeSet<Long>();
			RecordPairId currentId = pairIdList.get(i);
			if (!currentId.isProcessed()) {
				set.add(currentId.getFirstRecordId());
				set.add(currentId.getSecondRecordId());
				for (int j = i + 1; j < pairIdList.size(); j++) {
					RecordPairId processedIdPair = pairIdList.get(j);
					if (processedIdPair.flattenable(currentId)) {
						set.add(processedIdPair.getFirstRecordId());
						set.add(processedIdPair.getSecondRecordId());
						processedIdPair.setProcessed(true);
					}
				}
				flattenedPairIds.add(set);
			}
		}
		
		// if records are expanded so multiple IDs might appear and be paired, then
		// it's possible that a set of IDs might just have one ID; these need to be removed
		Iterator<Set<Long>> it = flattenedPairIds.iterator();
		while (it.hasNext()) {
			Set<Long> entity = it.next();
			if (entity.size() == 1) {
				it.remove();
			}
		}
	}
	
	/**
	 * @return the pairIdList
	 */
	public List<RecordPairId> getPairIdList() {
		return pairIdList;
	}
	
	/**
	 * @param pairIdList the pairIdList to set
	 */
	public void setPairIdList(List<RecordPairId> pairIdList) {
		this.pairIdList = pairIdList;
	}
	
	/**
	 * @return the flattenedPairIds
	 */
	public List<Set<Long>> getFlattenedPairIds() {
		return flattenedPairIds;
	}
	
	/**
	 * @param flattenedPairIds the flattenedPairIds to set
	 */
	public void setFlattenedPairIds(List<Set<Long>> flattenedPairIds) {
		this.flattenedPairIds = flattenedPairIds;
	}
	
	/**
	 * @return the serializedRecord
	 */
	public Set<Long> getSerializedRecord() {
		return serializedRecord;
	}
	
	/**
	 * @param serializedRecord the serializedRecord to set
	 */
	public void setSerializedRecord(Set<Long> serializedRecord) {
		this.serializedRecord = serializedRecord;
	}
}
