package org.regenstrief.linkage.analysis;

import junit.framework.Assert;
import org.junit.Test;
import org.regenstrief.linkage.io.ArrayDataSourceReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.MatchingConfig;

public class CloseFormUCalculatorTest {
	@Test
	public void dedupAnalysisIsCorrect() {
		runDedupAnalysis(1, (1.0 / 3.0), ArrayDataSourceReader.getSampleDoe());
		runDedupAnalysis(1, 0, ArrayDataSourceReader.getSampleDoe2());
		runDedupAnalysis((4.0 / 10.0), (2.0 / 10.0), ArrayDataSourceReader.getSampleMix());
		runDedupAnalysis((3.0 / 10.0), 0, ArrayDataSourceReader.getSampleNull());
	}
	
	private void runDedupAnalysis(final double exLast, final double exFirst, final DataSourceReader r) {
		final MatchingConfig mc = ArrayDataSourceReader.newSampleMatchingConfig();
		CloseFormUCalculator.getInstance().calculateDedup(mc, new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r));
		Assert.assertEquals(exLast, mc.getNonAgreementValue(1), 0.001);
		Assert.assertEquals(exFirst, mc.getNonAgreementValue(2), 0.001);
	}
	
	@Test
	public void analysisIsCorrect() {
		runAnalysis(1, (1.0 / 6.0), ArrayDataSourceReader.getSampleDoe(), ArrayDataSourceReader.getSampleDoe2());
		runAnalysis((4.0 / 10.0), (4.0 / 10.0), ArrayDataSourceReader.getSampleMix(), ArrayDataSourceReader.getSampleDoe2());
		runAnalysis((9.0 / 15.0), (1.0 / 15.0), ArrayDataSourceReader.getSampleNull(), ArrayDataSourceReader.getSampleNull2());
	}
	
	private void runAnalysis(final double exLast, final double exFirst, final DataSourceReader r1, final DataSourceReader r2) {
		final MatchingConfig mc = ArrayDataSourceReader.newSampleMatchingConfig();
		CloseFormUCalculator.getInstance().calculate(mc,
			new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r1),
			new FrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r2));
		Assert.assertEquals(exLast, mc.getNonAgreementValue(1), 0.001);
		Assert.assertEquals(exFirst, mc.getNonAgreementValue(2), 0.001);
	}
}
