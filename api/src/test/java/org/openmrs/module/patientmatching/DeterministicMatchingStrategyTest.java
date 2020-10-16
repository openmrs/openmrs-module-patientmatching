package org.openmrs.module.patientmatching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

@RunWith(PowerMockRunner.class)
public class DeterministicMatchingStrategyTest {
	
	private DeterministicMatchingStrategy strategy = new DeterministicMatchingStrategy();
	
	@Test
	public void match_shouldReturnFalseIfAnyDemoGraphicDoesNotMatch() {
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String genderDem = "gender";
		final String fn = "Horatio";
		final String gn = "Hornblower";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, fn);
		rec1.addDemographic(givenNameDem, gn);
		rec1.addDemographic(genderDem, "M");
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, fn);
		rec2.addDemographic(givenNameDem, gn);
		rec2.addDemographic(genderDem, "F");
		MatchingConfigRow fNameMcr = new MatchingConfigRow(firstNameDem);
		fNameMcr.setInclude(true);
		MatchingConfigRow gNameMcr = new MatchingConfigRow(givenNameDem);
		gNameMcr.setInclude(true);
		MatchingConfigRow genderMcr = new MatchingConfigRow(genderDem);
		genderMcr.setInclude(true);
		MatchingConfig mc = new MatchingConfig(null, new MatchingConfigRow[] { fNameMcr, gNameMcr, genderMcr });
		assertFalse(strategy.match(rec1, rec2, mc));
	}
	
	@Test
	public void match_shouldReturnTrueIfAllDemoGraphicsMatch() {
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String genderDem = "gender";
		final String fn = "Horatio";
		final String gn = "Hornblower";
		final String g = "M";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, fn);
		rec1.addDemographic(givenNameDem, gn);
		rec1.addDemographic(genderDem, g);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, fn);
		rec2.addDemographic(givenNameDem, gn);
		rec2.addDemographic(genderDem, g);
		MatchingConfigRow fNameMcr = new MatchingConfigRow(firstNameDem);
		fNameMcr.setInclude(true);
		MatchingConfigRow gNameMcr = new MatchingConfigRow(givenNameDem);
		gNameMcr.setInclude(true);
		MatchingConfigRow genderMcr = new MatchingConfigRow(genderDem);
		genderMcr.setInclude(true);
		MatchingConfig mc = new MatchingConfig(null, new MatchingConfigRow[] { fNameMcr, gNameMcr, genderMcr });
		assertTrue(strategy.match(rec1, rec2, mc));
	}
	
}
