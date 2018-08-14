package org.regenstrief.linkage.analysis;

import junit.framework.Assert;
import org.junit.Test;
import org.regenstrief.linkage.io.ArrayDataSourceReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.MatchingConfig;

public class BlockingHeuristicCalculatorTest {
	@Test
	public void dedupAnalysisIsCorrect() {
		runDedupAnalysis(1, (log2(1.0 / 3.0) + (2.0 * log2(2.0 / 3.0))) / -3.0, ArrayDataSourceReader.getSampleDoe());
		runDedupAnalysis(0, 1.0, ArrayDataSourceReader.getSampleDoe2());
		runDedupAnalysis(0, -log2(1.0 / 5.0), ArrayDataSourceReader.getSampleMix());
		runDedupAnalysis(0, log2(1.0 / 5.0) / -5.0, ArrayDataSourceReader.getSampleNull());
	}
	
	private void runDedupAnalysis(final int exPairs, final double exEntropy, final DataSourceReader r) {
		final MatchingConfig mc = ArrayDataSourceReader.newFullNameBlockingMatchingConfig();
		final BlockingHeuristicCalculator calculator = new BlockingHeuristicCalculator();
		calculator.calculateDedup(mc, new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r));
		assertResults(exPairs, exEntropy, calculator);
	}
	
	@Test
	public void analysisIsCorrect() {
		runAnalysis(1, ((2.0 * 2.0 * log2(2.0 / 5.0)) + log2(1.0 / 5.0)) / -5.0, ArrayDataSourceReader.getSampleDoe(), ArrayDataSourceReader.getSampleDoe2());
		runAnalysis(2, ((2.0 * 2.0 * log2(2.0 / 7.0)) + (3.0 * log2(1.0 / 7.0))) / -7.0, ArrayDataSourceReader.getSampleMix(), ArrayDataSourceReader.getSampleDoe2());
		runAnalysis(1, 2.0 * log2(2.0 / 8.0) / -8.0, ArrayDataSourceReader.getSampleNull(), ArrayDataSourceReader.getSampleNull2());
	}
	
	private void runAnalysis(final int exPairs, final double exEntropy, final DataSourceReader r1, final DataSourceReader r2) {
		final MatchingConfig mc = ArrayDataSourceReader.newFullNameBlockingMatchingConfig();
		final BlockingHeuristicCalculator calculator = new BlockingHeuristicCalculator();
		calculator.calculate(mc,
			new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r1),
			new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r2));
		assertResults(exPairs, exEntropy, calculator);
	}
	
	private void assertResults(final int exPairs, final double exEntropy, final BlockingHeuristicCalculator calculator) {
		Assert.assertEquals(exPairs, calculator.getTotalPairs());
		Assert.assertEquals(exEntropy, calculator.getEntropy(), 0.001);
	}
	
	private final static double log2(final double value) {
		return Math.log(value) / Math.log(2);
	}
}
