package org.openmrs.module.patientmatching.web.dwr;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;

import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.MatchingReportUtils;
import org.openmrs.module.patientmatching.MatchingRunData;
import org.openmrs.module.patientmatching.StrategyHolder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MatchingReportUtils.class, Context.class, WebContextFactory.class, ServerContextFactory.class })
public class DWRMatchingConfigUtilitiesTest {
	
	@Mock
	private StrategyHolder mockStrategyHolder;
	
	@Mock
	private WebContext mockWebContext;
	
	@Mock
	private ServletContext mockServletContext;
	
	private DWRMatchingConfigUtilities dwr;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		PowerMockito.mockStatic(MatchingReportUtils.class);
		PowerMockito.mockStatic(WebContextFactory.class);
		PowerMockito.mockStatic(ServerContextFactory.class);
		when(WebContextFactory.get()).thenReturn(mockWebContext);
		when(Context.getRegisteredComponent("patientMatchingStrategyHolder", StrategyHolder.class))
		        .thenReturn(mockStrategyHolder);
		dwr = new DWRMatchingConfigUtilities();
	}
	
	@Test
	public void getCurrentProcessStatus_shouldSkipStepsThatDoNotApplyToDeterministicStrategy() throws Exception {
		when(mockStrategyHolder.isProbabilistic()).thenReturn(false);
		dwr.getCurrentProcessStatus(2);
		((Map) Whitebox.getInternalState(DWRMatchingConfigUtilities.class, "objects")).put("matchingConfigLists",
		    Collections.emptyList());
		dwr.getCurrentProcessStatus(3);
		dwr.getCurrentProcessStatus(4);
		dwr.getCurrentProcessStatus(5);
		dwr.getCurrentProcessStatus(6);
		dwr.getCurrentProcessStatus(7);
		dwr.getCurrentProcessStatus(8);
		dwr.getCurrentProcessStatus(9);
		dwr.getCurrentProcessStatus(10);
		
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
		dwr.getCurrentProcessStatus(2);
		((Map) Whitebox.getInternalState(DWRMatchingConfigUtilities.class, "objects")).put("matchingConfigLists",
		    Collections.emptyList());
		dwr.getCurrentProcessStatus(3);
		dwr.getCurrentProcessStatus(4);
		dwr.getCurrentProcessStatus(5);
		dwr.getCurrentProcessStatus(6);
		dwr.getCurrentProcessStatus(7);
		dwr.getCurrentProcessStatus(8);
		dwr.getCurrentProcessStatus(9);
		dwr.getCurrentProcessStatus(10);
		
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
