package org.regenstrief.linkage.analysis;

import java.util.Set;

import org.regenstrief.linkage.util.MatchingConfig;

public final class BlockingHeuristicCalculator extends FrequencyBasedCalculator {
	private final static String FIELD = BlockingFrequencyAnalyzer.FIELD;
	private final static double entropyDenominator = -Math.log(2.0);
	
	private int totalPairs = 0;
	private double entropy;
	
	@Override
	public final void calculateDedup(final MatchingConfig mc, final DataSourceFrequency freq) {
		final double totalRecords = freq.getTotal();
		int totalPairs = 0;
		double entropyNumerator = 0;
		for (final String token : freq.getTokens(FIELD)) {
			if (!isValidToken(token)) {
				continue;
			}
			final int numRecords = freq.getFrequency(FIELD, token);
			
			totalPairs += countPairs(numRecords);
			
			entropyNumerator += getFieldEntropyNumerator(numRecords, totalRecords);
		}
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator);
	}
	
	@Override
	public final void calculate(final MatchingConfig mc, final DataSourceFrequency freq1, final DataSourceFrequency freq2) {
		final double totalRecords = freq1.getTotal() + freq2.getTotal();
		int totalPairs = 0;
		double entropyNumerator = 0;
		final Set<String> tokens1 = freq1.getTokens(FIELD);
		for (final String token : tokens1) {
			if (!isValidToken(token)) {
				continue;
			}
			final int numRecords1 = freq1.getFrequency(FIELD, token), numRecords2 = freq2.getFrequency(FIELD, token);
			
			totalPairs += (numRecords1 * numRecords2);
			
			entropyNumerator += getFieldEntropyNumerator(numRecords1 + numRecords2, totalRecords);
		}
		for (final String token : freq2.getTokens(FIELD)) { // Unpaired values needed for entropy
			if (!isValidToken(token) || tokens1.contains(token)) {
				continue;
			}
			entropyNumerator += getFieldEntropyNumerator(freq2.getFrequency(FIELD, token), totalRecords);
		}
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator);
	}
	
	private final static double getFieldEntropyNumerator(final double num, final double totalRecords) {
		return num * Math.log(num / totalRecords);
	}
	
	private final void finishCalculation(final MatchingConfig mc, final double totalRecords, final int totalPairs, final double entropyNumerator) {
		this.totalPairs = totalPairs;
		entropy = entropyNumerator / (totalRecords * entropyDenominator);
		log.info(mc.getName() + " Heuristics");
		log.info("Total number of pairs: " + totalPairs);
		log.info("Entropy: " + entropy);
	}
	
	public final int getTotalPairs() {
		return totalPairs;
	}
	
	public final double getEntropy() {
		return entropy;
	}
}
