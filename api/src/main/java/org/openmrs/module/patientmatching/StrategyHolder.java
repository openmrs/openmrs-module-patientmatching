package org.openmrs.module.patientmatching;

import static org.openmrs.module.patientmatching.MatchingConstants.GP_STRATEGY;
import static org.openmrs.module.patientmatching.MatchingConstants.STRATEGY_DETERMINISTIC;
import static org.openmrs.module.patientmatching.MatchingConstants.STRATEGY_PROBABILISTIC;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

/**
 * An instance of this class is used to determine configured patient matching strategy.
 */
@Component("patientMatchingStrategyHolder")
public class StrategyHolder implements GlobalPropertyListener {
	
	private static Boolean isProbabilistic;
	
	/**
	 * Checks whether the module is configured to use probabilistic strategy when matching patients,
	 * defaults to true if the GP value is not set.
	 *
	 * @return true for probabilistic strategy otherwise false
	 */
	public boolean isProbabilistic() {
		if (isProbabilistic == null) {
			String value = Context.getAdministrationService().getGlobalProperty(GP_STRATEGY);
			if (StringUtils.isNotBlank(value) && !STRATEGY_PROBABILISTIC.equalsIgnoreCase(value)
			        && !STRATEGY_DETERMINISTIC.equalsIgnoreCase(value)) {
				
				throw new APIException("Invalid patient matching strategy " + value);
			}
			
			isProbabilistic = !STRATEGY_DETERMINISTIC.equalsIgnoreCase(value);
		}
		
		return isProbabilistic;
	}
	
	/**
	 * {@link GlobalPropertyListener#supportsPropertyName(String)}
	 */
	@Override
	public boolean supportsPropertyName(String gpName) {
		return GP_STRATEGY.equals(gpName);
	}
	
	/**
	 * {@link GlobalPropertyListener#globalPropertyChanged(GlobalProperty)}
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty gp) {
		if (GP_STRATEGY.equals(gp.getProperty())) {
			isProbabilistic = null;
		}
	}
	
	/**
	 * {@link GlobalPropertyListener#globalPropertyDeleted(String)}
	 */
	@Override
	public void globalPropertyDeleted(String gpName) {
		if (GP_STRATEGY.equals(gpName)) {
			isProbabilistic = null;
		}
	}
	
}
