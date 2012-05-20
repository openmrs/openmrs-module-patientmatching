package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

public class ScorePairTest {

	/**
	 * @see ScorePair.scorePair(Record, Record)
	 * @verifies indicate a match on patients with multiple identifiers for an identifier type
	 */
	@Test
	public void scorePair_shouldIndicateAMatchOnPatientsWithMultipleIdentifiersForAnIdentifierType()
			throws Exception {
		
		// set up the records
		Record rec1 = new Record(1, "foo");
		Record rec2 = new Record(2, "foo");
		rec1.addDemographic("(Identifier) Old OpenMRS Identifier", "111,222,333");
		rec2.addDemographic("(Identifier) Old OpenMRS Identifier", "222,444,555");
		
		// add the identifier demographic for scoring
		MatchingConfigRow mcr = new MatchingConfigRow("(Identifier) Old OpenMRS Identifier");
		mcr.setInclude(true);
		
		MatchingConfig mc = new MatchingConfig("bar", new MatchingConfigRow[]{ mcr });
		ScorePair sp = new ScorePair(mc);
		
		// see what happens
		MatchResult mr = sp.scorePair(rec1, rec2);
		Assert.assertTrue(mr.matchedOn("(Identifier) Old OpenMRS Identifier"));
	}
	
	/**
	 * @see ScorePair.getCandidatesFromMultiFieldDemographics(String, String)
	 * @verifies return a list of all possible permutations
	 */
	@Test
	public void getCandidatesFromMultiFieldDemographics_shouldReturnAListOfAllPossiblePermutations()
			throws Exception {

		// minimum requirement for setting up a MatchingConfig
		MatchingConfigRow mcr = new MatchingConfigRow("ack");
		mcr.setInclude(true);
		MatchingConfig mc = new MatchingConfig("foo", new MatchingConfigRow[]{ mcr });
		
		ScorePair sp = new ScorePair(mc);
		
		String data1 = "101" + MatchingConstants.MULTI_FIELD_DELIMITER + "202";
		String data2 = "303" + MatchingConstants.MULTI_FIELD_DELIMITER + "404" + MatchingConstants.MULTI_FIELD_DELIMITER + "505";

		List<String[]> expected = new ArrayList<String[]>();
		expected.add(new String[]{ "101", "303"});
		expected.add(new String[]{ "101", "404"});
		expected.add(new String[]{ "101", "505"});
		expected.add(new String[]{ "202", "303"});
		expected.add(new String[]{ "202", "404"});
		expected.add(new String[]{ "202", "505"});
		
		List<String[]> actual = sp.getCandidatesFromMultiFieldDemographics(data1, data2);
		
		for (int i=0; i<6; i++) {
			Assert.assertEquals("permutation", expected.get(i)[0], actual.get(i)[0]);
			Assert.assertEquals("permutation", expected.get(i)[1], actual.get(i)[1]);
		}
	}
}