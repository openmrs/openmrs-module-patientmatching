package org.regenstrief.linkage.matchresult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.openmrs.module.patientmatching.MatchingConstants.SERIAL_DIR_DEFAULT;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
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
@PrepareForTest({ RecordSerializer.class, OpenmrsUtil.class, Context.class })
public class DedupMatchResultListTest {
	
	@Mock
	private File mockSerialFolder;
	
	@Mock
	private AdministrationService mockAdminService;
	
	private DedupMatchResultList strategy;
	
	private static final double SCORE_THRESHOLD = 9.6;
	
	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(RecordSerializer.class);
		PowerMockito.mockStatic(OpenmrsUtil.class);
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(mockAdminService);
		final String runName = "test";
		when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(SERIAL_DIR_DEFAULT + File.separator + runName))
		        .thenReturn(mockSerialFolder);
		strategy = new DedupMatchResultList(runName);
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
	
	@Test
	public void match_shouldReturnTrueIfTheTransposableFieldValuesAreSwapped() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String name1 = "Horatio";
		final String name2 = "Hornblower";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, name1);
		rec1.addDemographic(givenNameDem, name2);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, name2);
		rec2.addDemographic(givenNameDem, name1);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID("2");
		//Should not match because the names are not transposable
		assertFalse(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
		gnMcr.setSetID(setId1);
		//Should match because the names are now transposable
		assertTrue(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
	}
	
	@Test
	public void match_shouldReturnTrueForMultipleSwappedTransposableFields() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String middleNameDem = "middleName";
		final String swappedName1 = "abc";
		final String swappedName2 = "def";
		final String swappedName3 = "xyz";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, swappedName3);
		rec1.addDemographic(middleNameDem, swappedName1);
		rec1.addDemographic(givenNameDem, swappedName2);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, swappedName1);
		rec2.addDemographic(middleNameDem, swappedName2);
		rec2.addDemographic(givenNameDem, swappedName3);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		MatchingConfigRow mnMcr = new MatchingConfigRow(middleNameDem);
		mnMcr.setInclude(true);
		mnMcr.setSetID(setId1);
		assertTrue(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
	}
	
	@Test
	public void match_shouldReturnFalseIfTheTransposableFieldsDoNotMatch() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, "abc");
		rec1.addDemographic(givenNameDem, "def");
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, "ghi");
		rec2.addDemographic(givenNameDem, "jkl");
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		assertFalse(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
	}
	
	@Test
	public void match_shouldReturnTrueIfAnyTransposableFieldsMatchAndHigherThanThreshold() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		//We make the name long enough since the threshold requires it to be 85% of the concatenated value of all names
		final String commonName = "HoratioHornablower";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, "abc");
		rec1.addDemographic(givenNameDem, commonName);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, commonName);
		rec2.addDemographic(givenNameDem, "def");
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID("2");
		assertFalse(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
		gnMcr.setSetID(setId1);
		assertTrue(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
	}
	
	@Test
	public void match_shouldReturnTrueIfMultipleTransposableFieldsMatchAndHigherThanThreshold() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String middleNameDem = "middleName";
		final String givenNameDem = "givenName";
		//We make the name long enough since the threshold requires it to be 85% of the concatenated value of all names
		final String commonName = "MisterHoratioHornablower";
		Record r1 = new Record(1, null);
		r1.addDemographic(firstNameDem, commonName);
		r1.addDemographic(middleNameDem, "ab");
		r1.addDemographic(givenNameDem, "cd");
		Record r2 = new Record(2, null);
		r2.addDemographic(firstNameDem, "ef");
		r2.addDemographic(middleNameDem, commonName);
		r2.addDemographic(givenNameDem, "gh");
		Record r3 = new Record(3, null);
		r3.addDemographic(firstNameDem, "ij");
		r3.addDemographic(middleNameDem, "kl");
		r3.addDemographic(givenNameDem, commonName);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID("2");
		MatchingConfigRow mnMcr = new MatchingConfigRow(middleNameDem);
		mnMcr.setInclude(true);
		mnMcr.setSetID("3");
		assertFalse(strategy.match(r1, r2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
		assertFalse(strategy.match(r1, r3, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
		assertFalse(strategy.match(r2, r3, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
		gnMcr.setSetID(setId1);
		mnMcr.setSetID(setId1);
		assertTrue(strategy.match(r1, r2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
		assertTrue(strategy.match(r1, r3, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
		assertTrue(strategy.match(r2, r3, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
	}
	
	@Test
	public void match_shouldReturnFalseIfTheTransposableFieldsAreNullOrBlankForBothRecords() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, " ");
		Record rec2 = new Record(2, null);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		assertFalse(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr })));
	}
	
	@Test
	public void match_shouldReturnFalseForMultipleTransposableSetsWhereAnyOfTheSetsDoesNotMatch() {
		final String setId1 = "1";
		final String setId2 = "2";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String cellPhoneDem = "cell";
		final String homePhoneDem = "home";
		final String name1 = "Horatio";
		final String name2 = "Hornblower";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, name1);
		rec1.addDemographic(givenNameDem, name2);
		rec1.addDemographic(cellPhoneDem, "012");
		rec1.addDemographic(homePhoneDem, "345");
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, name2);
		rec2.addDemographic(givenNameDem, name1);
		rec2.addDemographic(cellPhoneDem, "657");
		rec2.addDemographic(homePhoneDem, "89*");
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		MatchingConfigRow cellMcr = new MatchingConfigRow(cellPhoneDem);
		cellMcr.setInclude(true);
		cellMcr.setSetID(setId2);
		MatchingConfigRow homeMcr = new MatchingConfigRow(homePhoneDem);
		homeMcr.setInclude(true);
		homeMcr.setSetID(setId2);
		//Should match because the names are now transposable
		assertFalse(strategy.match(rec1, rec2,
		    new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, cellMcr, homeMcr })));
	}
	
	@Test
	public void match_shouldReturnTrueForMultipleTransposableSetsAndTheFieldValuesMatch() {
		final String setId1 = "1";
		final String setId2 = "2";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String cellPhoneDem = "cell";
		final String homePhoneDem = "home";
		final String name1 = "Horatio";
		final String name2 = "Hornblower";
		final String cell = "12345";
		final String home = "65789";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, name1);
		rec1.addDemographic(givenNameDem, name2);
		rec1.addDemographic(cellPhoneDem, cell);
		rec1.addDemographic(homePhoneDem, home);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, name2);
		rec2.addDemographic(givenNameDem, name1);
		rec2.addDemographic(cellPhoneDem, home);
		rec2.addDemographic(homePhoneDem, cell);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		MatchingConfigRow cellMcr = new MatchingConfigRow(cellPhoneDem);
		cellMcr.setInclude(true);
		cellMcr.setSetID(setId2);
		MatchingConfigRow homeMcr = new MatchingConfigRow(homePhoneDem);
		homeMcr.setInclude(true);
		homeMcr.setSetID(setId2);
		//Should match because the names are now transposable
		assertTrue(strategy.match(rec1, rec2,
		    new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, cellMcr, homeMcr })));
	}
	
	@Test
	public void match_shouldNotCompareAFieldWithTransposablesIfItAlreadyMatchesTheSameFieldOnTheOtherRecord() {
		final String setId1 = "1";
		final String firstNameDem = "firstName";
		final String givenNameDem = "givenName";
		final String middleNameDem = "middleName";
		final String commonFirstName = "Horatio";
		final String swappedName1 = "Hornblower";
		final String swappedName2 = "xyz";
		Record rec1 = new Record(1, null);
		rec1.addDemographic(firstNameDem, commonFirstName);
		rec1.addDemographic(middleNameDem, swappedName1);
		rec1.addDemographic(givenNameDem, swappedName2);
		Record rec2 = new Record(2, null);
		rec2.addDemographic(firstNameDem, commonFirstName);
		rec2.addDemographic(middleNameDem, swappedName2);
		rec2.addDemographic(givenNameDem, swappedName1);
		MatchingConfigRow fnMcr = new MatchingConfigRow(firstNameDem);
		fnMcr.setInclude(true);
		fnMcr.setSetID(setId1);
		MatchingConfigRow gnMcr = new MatchingConfigRow(givenNameDem);
		gnMcr.setInclude(true);
		gnMcr.setSetID(setId1);
		MatchingConfigRow mnMcr = new MatchingConfigRow(middleNameDem);
		mnMcr.setInclude(true);
		mnMcr.setSetID(setId1);
		assertTrue(strategy.match(rec1, rec2, new MatchingConfig(null, new MatchingConfigRow[] { fnMcr, gnMcr, mnMcr })));
	}
	
}
