/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.regenstrief.linkage.analysis;

import junit.framework.Assert;
import org.junit.Test;
import org.regenstrief.linkage.io.ArrayDataSourceReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * CloseFormUCalculatorTest
 */
public class CloseFormUCalculatorTest {
	
	@Test
	public void dedupAnalysisIsCorrect() {
		runDedupAnalysis(1, (1.0 / 3.0), ArrayDataSourceReader.getSampleDoe());
		runDedupAnalysis(1, 0, ArrayDataSourceReader.getSampleDoe2());
		runDedupAnalysis((4.0 / 10.0), (2.0 / 10.0), ArrayDataSourceReader.getSampleMix());
	}
	
	private void runDedupAnalysis(final double exLast, final double exFirst, final DataSourceReader r) {
		final MatchingConfig mc = ArrayDataSourceReader.newSampleMatchingConfig();
		CloseFormUCalculator.getInstance().calculateUValuesDedup(mc, new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r));
		Assert.assertEquals(exLast, mc.getNonAgreementValue(1), 0.001);
		Assert.assertEquals(exFirst, mc.getNonAgreementValue(2), 0.001);
	}
	
	@Test
	public void analysisIsCorrect() {
		runAnalysis(1, (1.0 / 6.0), ArrayDataSourceReader.getSampleDoe(), ArrayDataSourceReader.getSampleDoe2());
		runAnalysis((4.0 / 10.0), (4.0 / 10.0), ArrayDataSourceReader.getSampleMix(), ArrayDataSourceReader.getSampleDoe2());
	}
	
	private void runAnalysis(final double exLast, final double exFirst, final DataSourceReader r1, final DataSourceReader r2) {
		final MatchingConfig mc = ArrayDataSourceReader.newSampleMatchingConfig();
		CloseFormUCalculator.getInstance().calculateUValues(mc,
			new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r1),
			new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r2));
		Assert.assertEquals(exLast, mc.getNonAgreementValue(1), 0.001);
		Assert.assertEquals(exFirst, mc.getNonAgreementValue(2), 0.001);
	}
}
