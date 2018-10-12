package org.regenstrief.linkage.analysis;

import junit.framework.Assert;
import org.junit.Test;
import org.regenstrief.linkage.io.ArrayDataSourceReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.MatchingConfig;

public class CloseFormUCalculatorTest {
	private final static MatchingConfig mcLast = BlockingHeuristicCalculator.newMatchingConfig("LAST");
	private final static MatchingConfig mcFirst = BlockingHeuristicCalculator.newMatchingConfig("FIRST");
	
	static {
		System.setProperty("org.regenstrief.linkage.analysis.BlockingFrequencyContext.memoryBacked", Boolean.toString(true));
	}
	
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
		assertEquals(exLast, mc.getNonAgreementValue(1));
		assertEquals(exFirst, mc.getNonAgreementValue(2));
		
		runDedupHeuristic(exLast, mcLast, r);
		runDedupHeuristic(exFirst, mcFirst, r);
	}
	
	private void runDedupHeuristic(final double ex, final MatchingConfig mc, final DataSourceReader r) {
		r.reset();
		final BlockingHeuristicCalculator calc = newBlockingHeuristicCalculator();
		calc.calculateDedup(mc, new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r));
		assertEquals(ex, calc.getU());
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
		assertEquals(exLast, mc.getNonAgreementValue(1));
		assertEquals(exFirst, mc.getNonAgreementValue(2));
		
		runHeuristic(exLast, mcLast, r1, r2);
		runHeuristic(exFirst, mcFirst, r1, r2);
	}
	
	private void runHeuristic(final double ex, final MatchingConfig mc, final DataSourceReader r1, final DataSourceReader r2) {
		r1.reset();
		r2.reset();
		final BlockingHeuristicCalculator calc = newBlockingHeuristicCalculator();
		calc.calculate(mc,
			new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r1),
			new BlockingFrequencyContext(mc, ArrayDataSourceReader.SAMPLE_LINK_DATA_SOURCE, r2));
		assertEquals(ex, calc.getU());
	}
	
	@Test
	public void denominatorIsCorrect() {
		runDenominator(0.0, 0, 0);
		runDenominator(1.0, 1, 1);
		runDenominator(8.0, 2, 4);
		runDenominator(2500000000.0, 50000, 50000);
	}
	
	private void runDenominator(final double ex, final int total1, final int total2) {
		assertEquals(ex, CloseFormUCalculator.getDenominator(total1, total2));
	}
	
	private BlockingHeuristicCalculator newBlockingHeuristicCalculator() {
		final BlockingHeuristicCalculator calculator = new BlockingHeuristicCalculator();
		calculator.setFileEnabled(false);
		return calculator;
	}
	
	private final static void assertEquals(final double ex, final double ac) {
		Assert.assertEquals(ex, ac, 0.001);
	}
}
