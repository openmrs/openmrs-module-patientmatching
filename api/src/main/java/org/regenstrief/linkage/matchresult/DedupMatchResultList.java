/**
 */
package org.regenstrief.linkage.matchresult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.api.APIException;
import org.openmrs.module.patientmatching.MatchingStrategy;
import org.openmrs.module.patientmatching.MatchingStrategyAndStore;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.ScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Probabilistic implementation of a {@link MatchingStrategy}
 * <p>
 * TODO Rename to ProbabilisticMatchingStrategy for clarity
 * </p>
 */
public class DedupMatchResultList extends MatchResultList implements MatchingStrategyAndStore {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private List<RecordPairId> pairIdList = new ArrayList();
	
	private List<Set<Long>> flattenedPairIds = new ArrayList();
	
	private Set<Long> serializedRecords = new TreeSet();
	
	public DedupMatchResultList() {
		super();
	}
	
	/**
	 * @see org.regenstrief.linkage.matchresult.MatchResultList#acceptMatchResult(org.regenstrief.linkage.MatchResult)
	 */
	@Override
	public synchronized void acceptMatchResult(MatchResult mr) {
		if (isMatch(mr)) {
			try {
				storePair(mr.getRecord1(), mr.getRecord2());
			}
			catch (IOException e) {
				throw new APIException("Unable to serialize records with ids: " + mr.getRecord1().getUID() + " and "
				        + mr.getRecord2().getUID(), e);
			}
		}
	}
	
	/**
	 * @see MatchingStrategy#match(Record, Record, MatchingConfig)
	 */
	@Override
	public boolean match(Record rec1, Record rec2, MatchingConfig mc) {
		return isMatch(new ScorePair(mc).scorePair(rec1, rec2));
	}
	
	/**
	 * @see org.openmrs.module.patientmatching.MatchedRecordsStore#getPairIdList()
	 */
	@Override
	public List<RecordPairId> getPairIdList() {
		return pairIdList;
	}
	
	/**
	 * @see org.openmrs.module.patientmatching.MatchedRecordsStore#getFlattenedPairIds()
	 */
	@Override
	public List<Set<Long>> getFlattenedPairIds() {
		return flattenedPairIds;
	}
	
	/**
	 * @see org.openmrs.module.patientmatching.MatchedRecordsStore#getSerializedRecords()
	 */
	@Override
	public Set<Long> getSerializedRecords() {
		return serializedRecords;
	}
	
	private boolean isMatch(MatchResult mr) {
		return mr.getScore() > mr.getMatchingConfig().getScoreThreshold();
	}
	
}
