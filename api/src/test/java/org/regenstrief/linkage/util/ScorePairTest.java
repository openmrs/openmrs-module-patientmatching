package org.regenstrief.linkage.util;

import org.junit.Assert;
import org.junit.Test;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

public class ScorePairTest {
	
	/**
	 * @see ScorePair#scorePair(Record, Record)
	 * @verifies indicate a match on patients with multiple identifiers for an identifier type
	 */
	@Test
	public void scorePair_shouldIndicateAMatchOnPatientsWithMultipleIdentifiersForAnIdentifierType() throws Exception {
		
		// set up the records
		Record rec1 = new Record(1, "foo");
		Record rec2 = new Record(2, "foo");
		rec1.addDemographic("(Identifier) Old OpenMRS Identifier", "111,222,333");
		rec2.addDemographic("(Identifier) Old OpenMRS Identifier", "222,444,555");
		
		// add the identifier demographic for scoring
		MatchingConfigRow mcr = new MatchingConfigRow("(Identifier) Old OpenMRS Identifier");
		mcr.setInclude(true);
		
		MatchingConfig mc = new MatchingConfig("bar", new MatchingConfigRow[] { mcr });
		ScorePair sp = new ScorePair(mc);
		
		// see what happens
		MatchResult mr = sp.scorePair(rec1, rec2);
		Assert.assertTrue(mr.matchedOn("(Identifier) Old OpenMRS Identifier"));
	}
	
}
