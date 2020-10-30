package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.matchresult.RecordPairId;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.StringMatch;

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
		Map<String, List<String>> setIdAndTransposables = MatchingUtils.getSetIdAndFieldsMap(mc);
		Map<String, Boolean> setIdMatchMap = new HashMap();
		for (MatchingConfigRow mcr : mc.getIncludedColumns()) {
			final String demographic = mcr.getName();
			final int algorithm = mcr.getAlgorithm();
			final double threshold = mcr.getThreshold();
			
			String data1 = rec1.getDemographic(demographic);
			String data2 = rec2.getDemographic(demographic);
			
			// multi-field demographics need to be analyzed on each combination of values
			// TODO base this on a flag on MatchingConfigRow vs the beginning of the name
			boolean match = false;
			if (demographic.startsWith("(Identifier)")) {
				for (String[] candidate : MatchingUtils.getCandidatesFromMultiFieldDemographics(data1, data2)) {
					// TODO use something other than String[] or guarantee size == 2
					match = MatchingUtils.match(algorithm, threshold, candidate[0], candidate[1]).isMatch();
					if (match) {
						break;
					}
				}
			} else {
				match = MatchingUtils.match(algorithm, threshold, data1, data2).isMatch();
			}
			
			if (match) {
				if (mc.isTransposableRow(mcr)) {
					//Mark all other transposable fields as matches too
					setIdMatchMap.put(mcr.getSetID(), true);
				}
			} else {
				if (!mc.isTransposableRow(mcr)) {
					return false;
				}
				
				if (setIdMatchMap.getOrDefault(mcr.getSetID(), false)) {
					//We already found a match in the set this field belongs to
					continue;
				}
				
				match = equalToAnyTransposableField(demographic, data1, rec2, setIdAndTransposables.get(mcr.getSetID()));
				setIdMatchMap.put(mcr.getSetID(), match);
			}
		}
		
		//At this point, we didn't find a mismatch for any non-transposable field.
		//If any set didn't match then its a no match otherwise it's a match
		return !setIdMatchMap.values().contains(false);
	}
	
	/**
	 * Checks if the value of the specified field matches any value of another record's transposable
	 * fields.
	 * 
	 * @param fieldName the field to check
	 * @param value the value of the field
	 * @param other the other record
	 * @param transposableFields the list of the field names transposable with the field
	 * @return true if the field matches any of the other record's trasposable fields otherwise faelse
	 */
	private boolean equalToAnyTransposableField(String fieldName, String value, Record other,
	        List<String> transposableFields) {
		
		for (String field : transposableFields) {
			if (!fieldName.equals(field)) {
				String otherValue = other.getDemographic(field);
				if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(otherValue)) {
					//TODO Make the algorithm and threshold configurable
					float similarity = StringMatch.getLCSMatchSimilarity(value, otherValue);
					if (similarity > StringMatch.LCS_THRESH) {
						return true;
					}
				}
			}
		}
		
		return false;
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
