package org.regenstrief.linkage.util;

import static org.openmrs.module.patientmatching.MatchingConstants.CONCATENATED_FIELD_PREFIX;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.patientmatching.MatchingUtils;
import org.regenstrief.linkage.MatchItem;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.NullDemographicsMatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.Modifier;
import org.regenstrief.linkage.analysis.VectorTable;

/**
 * Takes a record and calculates a score between them using the options in the given MatchingConfig
 * object TODO: Add size parameter to lds1_frequencies & lds2_frequencies TODO: Implement
 * functionality for these two parameters: - A flag indicating whether to use null tokens when
 * scaling agreement weight based on term frequency (default-no) - A flag indicating how to
 * establish agreement among fields when one or both fields are null (eg, apply disagreement weight,
 * apply agreement weight, or apply ze
 */
public class ScorePair {
	
	private VectorTable vt;
	
	private MatchingConfig mc;
	
	private List<Modifier> modifiers;
	
	private Hashtable<MatchVector, Long> observed_vectors;
	
	public ScorePair(MatchingConfig mc) {
		this.mc = mc;
		vt = new VectorTable(mc);
		modifiers = new ArrayList<Modifier>();
		observed_vectors = new Hashtable<MatchVector, Long>();
	}
	
	public void addScoreModifier(Modifier sm) {
		modifiers.add(sm);
	}
	
	/**
	 * Scores a pair of records
	 * 
	 * @param rec1 first record to score
	 * @param rec2 second record to score
	 * @return MatchResult of rec1 paired with rec2
	 * @should indicate a match on patients with multiple identifiers for an identifier type
	 */
	public MatchResult scorePair(Record rec1, Record rec2) {
		final MatchVector mv;
		if (rec1.hasNullValues() || rec2.hasNullValues()) {
			mv = new NullDemographicsMatchVector();
		} else {
			mv = new MatchVector();
		}
		
		for (final MatchingConfigRow mcr : mc.getIncludedColumns()) {
			final String comparison_demographic = mcr.getName();
			final int algorithm = mcr.getAlgorithm();
			final double threshold = mcr.getThreshold();
			
			String data1 = rec1.getDemographic(comparison_demographic);
			String data2 = rec2.getDemographic(comparison_demographic);
			
			if (StringUtils.isBlank(data1) || StringUtils.isBlank(data2)) {
				NullDemographicsMatchVector nsmv = (NullDemographicsMatchVector) mv;
				nsmv.hadNullValue(comparison_demographic);
			}
			
			// multi-field demographics need to be analyzed on each combination of values
			// TODO base this on a flag on MatchingConfigRow vs the beginning of the name
			if (comparison_demographic.startsWith("(Identifier)")) {
				for (final String[] candidate : MatchingUtils.getCandidatesFromMultiFieldDemographics(data1, data2)) {
					// TODO use something other than String[] or guarantee size == 2
					if (match(mv, comparison_demographic, algorithm, threshold, candidate[0], candidate[1])) {
						break;
					}
				}
			} else {
				match(mv, comparison_demographic, algorithm, threshold, data1, data2);
			}
		}
		
		enableInterchangeableFieldComparison(rec1, rec2, mv);
		MatchResult mr = new MatchResult(vt.getScore(mv), vt.getInclusiveScore(mv), vt.getMatchVectorTrueProbability(mv),
		        vt.getMatchVectorFalseProbability(mv), vt.getSensitivity(mv), vt.getSpecificity(mv), mv,
		        vt.getScoreVector(mv), rec1, rec2, mc);
		for (Modifier m : modifiers) {
			mr = m.getModifiedMatchResult(mr, mc);
		}
		
		mr.setCertainty(1);
		mr.setMatch_status(MatchResult.UNKNOWN);
		
		Long l = observed_vectors.get(mr.getMatchVector());
		if (l == null) {
			l = Long.valueOf(1);
		} else {
			l = Long.valueOf(l.longValue() + 1);
		}
		observed_vectors.put(mr.getMatchVector(), l);
		
		return mr;
	}
	
	private boolean match(MatchVector mv, String comparison_demographic, int algorithm, double threshold, String data1,
	        String data2) {
		MatchItem matchItem = MatchingUtils.match(algorithm, threshold, data1, data2);
		mv.setMatch(comparison_demographic, matchItem.getSimilarity(), matchItem.isMatch());
		
		return matchItem.isMatch();
	}
	
	/**
	 * This method would check if the interchangeable columns have same value , if the columns match
	 * with each other then the individual columns which make the concat1 column would be set as true
	 **/
	private void enableInterchangeableFieldComparison(Record rec1, Record rec2, final MatchVector mv) {
		Map<String, List<String>> setIdAndFieldsMap = MatchingUtils.getSetIdAndFieldsMap(mc);
		Map<String, String> r1Concats = MatchingUtils.getConcatValueMap(rec1, setIdAndFieldsMap);
		Map<String, String> r2Concats = MatchingUtils.getConcatValueMap(rec2, setIdAndFieldsMap);
		
		setIdAndFieldsMap.forEach((setId, fields) -> {
			String comparision_demographic = CONCATENATED_FIELD_PREFIX + setId;
			String rec1ConcatValue = r1Concats.get(comparision_demographic);
			String rec2ConcatValue = r2Concats.get(comparision_demographic);
			if (StringUtils.isNotBlank(rec1ConcatValue) && StringUtils.isNotBlank(rec2ConcatValue)) {
				float similarity = StringMatch.getLCSMatchSimilarity(rec1ConcatValue, rec2ConcatValue);
				if (similarity > StringMatch.LCS_THRESH) {
					for (String mcr : fields) {
						mv.setMatch(mcr, similarity, true);
					}
				}
			}
		});
	}
	
	public Hashtable<MatchVector, Long> getObservedVectors() {
		return observed_vectors;
	}
}
