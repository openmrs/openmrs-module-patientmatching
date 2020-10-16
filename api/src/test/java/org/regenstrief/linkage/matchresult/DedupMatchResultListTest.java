package org.regenstrief.linkage.matchresult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.patientmatching.MatchingConstants;
import org.openmrs.module.patientmatching.RecordSerializer;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RecordSerializer.class, OpenmrsUtil.class })
public class DedupMatchResultListTest {
	
	@Mock
	private File mockSerialFolder;
	
	private DedupMatchResultList strategy;
	
	private static final double SCORE_THRESHOLD = 9.6;
	
	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(RecordSerializer.class);
		PowerMockito.mockStatic(OpenmrsUtil.class);
		when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(MatchingConstants.SERIAL_FOLDER_NAME))
		        .thenReturn(mockSerialFolder);
		strategy = new DedupMatchResultList();
		strategy.clean();
	}
	
	@Test
	public void acceptMatchResult_shouldNotAcceptAMatchResultIfTheScoreIsLessThanTheThreshold() {
		Record r1 = new Record(1, null);
		Record r2 = new Record(2, null);
		MatchingConfig mc = new MatchingConfig("myMatchConfig", new String[] {});
		mc.setScoreThreshold(10);
		
		strategy.acceptMatchResult(new MatchResult(9, 0, 0, 0, 0, 0, null, null, r1, r2, mc));
		
		Assert.assertTrue(strategy.getPairIdList().isEmpty());
	}
	
	@Test
	public void acceptMatchResult_shouldAcceptAMatchResultIfTheScoreIsEqualToTheThreshold() {
		Record r1 = new Record(1, null);
		Record r2 = new Record(2, null);
		final double score = 10;
		MatchingConfig mc = new MatchingConfig("myMatchConfig", new String[] {});
		mc.setScoreThreshold(score);
		
		strategy.acceptMatchResult(new MatchResult(score, 0, 0, 0, 0, 0, null, null, r1, r2, mc));
		
		Assert.assertTrue(strategy.getPairIdList().isEmpty());
	}
	
	@Test
	public void acceptMatchResult_shouldAcceptAMatchResultIfTheScoreIsGreaterThanTheThreshold() {
		Record r1 = new Record(1, null);
		Record r2 = new Record(2, null);
		MatchingConfig mc = new MatchingConfig("myMatchConfig", new String[] {});
		mc.setScoreThreshold(10);
		
		strategy.acceptMatchResult(new MatchResult(11, 0, 0, 0, 0, 0, null, null, r1, r2, mc));
		
		Assert.assertEquals(1, strategy.getPairIdList().size());
	}
	
	@Test
	public void match_shouldReturnFalseIfAnyDemographicsDoesNotMatch() {
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
		mc.setScoreThreshold(SCORE_THRESHOLD);
		assertFalse(strategy.match(rec1, rec2, mc));
	}
	
	@Test
	public void match_shouldReturnFalseIfAllTheDemographicsMatchButTheScoreIsLessThanTheThreshold() {
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
		mc.setScoreThreshold(SCORE_THRESHOLD);//Calculated score is about 9.509775004326938
		assertFalse(strategy.match(rec1, rec2, mc));
	}
	
	@Test
	public void match_shouldReturnTrueIfAllDemographicsMatchAndTheScoreIsAboveTheThreshold() {
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
		mc.setScoreThreshold(SCORE_THRESHOLD - 0.1);//Calculated score is about 9.509775004326938
		assertTrue(strategy.match(rec1, rec2, mc));
	}
	
}
