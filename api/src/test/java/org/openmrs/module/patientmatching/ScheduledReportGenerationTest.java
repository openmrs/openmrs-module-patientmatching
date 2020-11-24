package org.openmrs.module.patientmatching;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MatchingReportUtils.class, Context.class })
public class ScheduledReportGenerationTest {
	
	@Mock
	private StrategyHolder mockStrategyHolder;
	
	private ScheduledReportGeneration generator;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		PowerMockito.mockStatic(MatchingReportUtils.class);
		when(Context.getRegisteredComponent("patientMatchingStrategyHolder", StrategyHolder.class))
		        .thenReturn(mockStrategyHolder);
		generator = new ScheduledReportGeneration();
	}
	
	@Test
	public void getCurrentProcessStatus_shouldSkipStepsThatDoNotApplyToDeterministicStrategy() throws Exception {
		when(mockStrategyHolder.isProbabilistic()).thenReturn(false);
		generator.getCurrentProcessStatus(2);
		((Map) Whitebox.getInternalState(generator, "objects")).put("matchingConfigLists", Collections.emptyList());
		generator.getCurrentProcessStatus(3);
		generator.getCurrentProcessStatus(4);
		generator.getCurrentProcessStatus(5);
		generator.getCurrentProcessStatus(6);
		generator.getCurrentProcessStatus(7);
		generator.getCurrentProcessStatus(8);
		generator.getCurrentProcessStatus(9);
		generator.getCurrentProcessStatus(10);
		
		verifyStatic();
		MatchingReportUtils.ReadConfigFile(anyMap(), any());
		verifyStatic();
		MatchingReportUtils.InitScratchTable(anyMap());
		verifyStatic();
		MatchingReportUtils.ScoringData(anyMap(), any(MatchingRunData.class));
		verifyStatic();
		MatchingReportUtils.CreatingReport(anyMap(), any(MatchingRunData.class));
		verifyNoMoreInteractions(MatchingReportUtils.class);
		MatchingReportUtils.CreRanSamAnalyzer(anyMap());
		MatchingReportUtils.CreAnalFormPairs(anyMap());
		MatchingReportUtils.CrePairdataSourAnalyzer(anyMap());
		MatchingReportUtils.CreEMAnalyzer(anyMap());
		MatchingReportUtils.AnalyzingData(anyMap());
	}
	
	@Test
	public void getCurrentProcessStatus_shouldRunStepsThatApplyToProbabilisticStrategy() throws Exception {
		when(mockStrategyHolder.isProbabilistic()).thenReturn(true);
		generator.getCurrentProcessStatus(2);
		((Map) Whitebox.getInternalState(generator, "objects")).put("matchingConfigLists", Collections.emptyList());
		generator.getCurrentProcessStatus(3);
		generator.getCurrentProcessStatus(4);
		generator.getCurrentProcessStatus(5);
		generator.getCurrentProcessStatus(6);
		generator.getCurrentProcessStatus(7);
		generator.getCurrentProcessStatus(8);
		generator.getCurrentProcessStatus(9);
		generator.getCurrentProcessStatus(10);
		
		verifyStatic();
		MatchingReportUtils.ReadConfigFile(anyMap(), any());
		verifyStatic();
		MatchingReportUtils.InitScratchTable(anyMap());
		verifyStatic();
		MatchingReportUtils.CreRanSamAnalyzer(anyMap());
		verifyStatic();
		MatchingReportUtils.CreAnalFormPairs(anyMap());
		verifyStatic();
		MatchingReportUtils.CrePairdataSourAnalyzer(anyMap());
		verifyStatic();
		MatchingReportUtils.CreEMAnalyzer(anyMap());
		verifyStatic();
		MatchingReportUtils.AnalyzingData(anyMap());
		verifyStatic();
		MatchingReportUtils.ScoringData(anyMap(), any(MatchingRunData.class));
		verifyStatic();
		MatchingReportUtils.CreatingReport(anyMap(), any(MatchingRunData.class));
	}
	
}
