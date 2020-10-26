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

import java.io.IOException;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Subclasses of this interface act as both a {@link MatchingStrategy} and a
 * {@link MatchedRecordsStore}
 */
public interface MatchingStrategyAndStore extends MatchingStrategy, MatchedRecordsStore {
	
	/**
	 * Matches the specified records, if they match then it stores them as a pair in its
	 * {@link MatchedRecordsStore}
	 * 
	 * @see MatchingStrategy#match(Record, Record, MatchingConfig)
	 * @see MatchedRecordsStore#storePair(Record, Record)
	 */
	default void matchAndStore(Record rec1, Record rec2, MatchingConfig mc) throws IOException {
		if (match(rec1, rec2, mc)) {
			storePair(rec1, rec2);
		}
	}
	
}
