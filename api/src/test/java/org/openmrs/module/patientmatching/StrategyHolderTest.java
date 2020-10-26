package org.openmrs.module.patientmatching;

import static org.mockito.Mockito.when;
import static org.openmrs.module.patientmatching.MatchingConstants.GP_STRATEGY;
import static org.openmrs.module.patientmatching.MatchingConstants.STRATEGY_DETERMINISTIC;
import static org.openmrs.module.patientmatching.MatchingConstants.STRATEGY_PROBABILISTIC;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class StrategyHolderTest {
	
	@Mock
	private AdministrationService mockAdminService;
	
	private StrategyHolder strategyHolder = new StrategyHolder();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(mockAdminService);
		Whitebox.setInternalState(StrategyHolder.class, "isProbabilistic", (Object) null);
	}
	
	@Test(expected = APIException.class)
	public void isProbabilistic_shouldFailIfTheGpValueIsInvalid() {
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn("bad value");
		strategyHolder.isProbabilistic();
	}
	
	@Test
	public void isProbabilistic_shouldReturnTrueIfTheGpValueIsSetToProbabilistic() {
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn(STRATEGY_PROBABILISTIC);
		Assert.assertTrue(strategyHolder.isProbabilistic());
	}
	
	@Test
	public void isProbabilistic_shouldReturnTrueIfTheGpValueIsNotSet() {
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn(null);
		Assert.assertTrue(strategyHolder.isProbabilistic());
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn("");
		Assert.assertTrue(strategyHolder.isProbabilistic());
	}
	
	@Test
	public void isProbabilistic_shouldReturnTrueIfTheGpValueIsSetToDeterministic() {
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn(STRATEGY_DETERMINISTIC);
		Assert.assertFalse(strategyHolder.isProbabilistic());
		//Should be case insensitive
		when(mockAdminService.getGlobalProperty(GP_STRATEGY)).thenReturn(STRATEGY_DETERMINISTIC.toUpperCase());
		Assert.assertFalse(strategyHolder.isProbabilistic());
	}
	
}
