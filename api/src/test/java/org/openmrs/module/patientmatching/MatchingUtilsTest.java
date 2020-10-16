package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MatchingUtilsTest {
	
	/**
	 * @see MatchingUtils#getCandidatesFromMultiFieldDemographics(String, String)
	 * @verifies return a list of all possible permutations
	 */
	@Test
	public void getCandidatesFromMultiFieldDemographics_shouldReturnAListOfAllPossiblePermutations() throws Exception {
		
		// minimum requirement for setting up a MatchingConfig
		String data1 = "101" + MatchingConstants.MULTI_FIELD_DELIMITER + "202";
		String data2 = "303" + MatchingConstants.MULTI_FIELD_DELIMITER + "404" + MatchingConstants.MULTI_FIELD_DELIMITER
		        + "505";
		
		List<String[]> expected = new ArrayList<String[]>();
		expected.add(new String[] { "101", "303" });
		expected.add(new String[] { "101", "404" });
		expected.add(new String[] { "101", "505" });
		expected.add(new String[] { "202", "303" });
		expected.add(new String[] { "202", "404" });
		expected.add(new String[] { "202", "505" });
		
		List<String[]> actual = MatchingUtils.getCandidatesFromMultiFieldDemographics(data1, data2);
		
		for (int i = 0; i < 6; i++) {
			Assert.assertEquals("permutation", expected.get(i)[0], actual.get(i)[0]);
			Assert.assertEquals("permutation", expected.get(i)[1], actual.get(i)[1]);
		}
	}
}
