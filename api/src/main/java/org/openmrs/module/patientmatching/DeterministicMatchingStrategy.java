package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.RecordPairId;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * Deterministic implementation of a {@link MatchingStrategy}
 */
public class DeterministicMatchingStrategy implements MatchingStrategyAndStore {
	
	private List<RecordPairId> pairIdList = new ArrayList();
	
	private List<Set<Long>> flattenedPairIds = new ArrayList();
	
	private Set<Long> serializedRecords = new TreeSet();
	
	/**
	 * @see MatchingStrategy#match(Record, Record, MatchingConfig)
	 */
	@Override
	public boolean match(Record rec1, Record rec2, MatchingConfig mc) {
		for (final MatchingConfigRow mcr : mc.getIncludedColumns()) {
			final String demographic = mcr.getName();
			final int algorithm = mcr.getAlgorithm();
			final double threshold = mcr.getThreshold();
			
			String data1 = rec1.getDemographic(demographic);
			String data2 = rec2.getDemographic(demographic);
			
			//TODO Support for transposable fields to be added a part of https://issues.openmrs.org/browse/PTM-95
			
			// multi-field demographics need to be analyzed on each combination of values
			// TODO base this on a flag on MatchingConfigRow vs the beginning of the name
			boolean match = false;
			if (demographic.startsWith("(Identifier)")) {
				for (final String[] candidate : MatchingUtils.getCandidatesFromMultiFieldDemographics(data1, data2)) {
					// TODO use something other than String[] or guarantee size == 2
					match = MatchingUtils.match(algorithm, threshold, candidate[0], candidate[1]).isMatch();
					if (match) {
						match = true;
						break;
					}
				}
			} else {
				match = MatchingUtils.match(algorithm, threshold, data1, data2).isMatch();
			}
			
			if (!match) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @see MatchedRecordsStore#getPairIdList()
	 */
	@Override
	public List<RecordPairId> getPairIdList() {
		return pairIdList;
	}
	
	/**
	 * @see MatchedRecordsStore#getFlattenedPairIds()
	 */
	@Override
	public List<Set<Long>> getFlattenedPairIds() {
		return flattenedPairIds;
	}
	
	/**
	 * @see MatchedRecordsStore#getSerializedRecords()
	 */
	@Override
	public Set<Long> getSerializedRecords() {
		return serializedRecords;
	}
	
}
