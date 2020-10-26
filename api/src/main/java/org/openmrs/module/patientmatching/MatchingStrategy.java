/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientmatching;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Super interface for strategies that can be used to match a pair of records
 */
public interface MatchingStrategy {
	
	/**
	 * Matches the specified records using the specified {@link MatchingConfig}
	 * 
	 * @param rec1 the record to match
	 * @param rec2 the other record to match
	 * @param mc the {@link MatchingConfig} instance to use
	 * @return true if the records match otherwise false
	 */
	boolean match(Record rec1, Record rec2, MatchingConfig mc);
	
}
