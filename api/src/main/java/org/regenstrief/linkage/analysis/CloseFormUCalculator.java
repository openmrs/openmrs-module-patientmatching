package org.regenstrief.linkage.analysis;

import java.util.Set;

import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/**
 * To calculate U values for linkage of two files: 
 * sum ( [ n * m ] )  /  [ N * M ]
 * where n is the frequency of the given token in file 1, m is the frequency of the given token in file 2,
 * N is the total number of records in file 1, and M is the total number of records in file 2
 * 
 * To calcule U values for each field when de-duplicating one file:
 * sum ( [ (n^2 - n) / 2 ] )  /  [ (N^2 - N) / 2]
 * where n is the frequency of the given token and N is the total number of records in the file
 * 
 * @author jegg
 *
 */
public class CloseFormUCalculator extends FrequencyBasedCalculator {
	private static final CloseFormUCalculator instance = new CloseFormUCalculator();
	
	protected CloseFormUCalculator() {
	}
	
	public static final CloseFormUCalculator getInstance() {
		return instance;
	}
	
	public final static double getDenominatorDedup(final DataSourceFrequency freq) {
		return countPairs(freq.getTotal());
	}
	
	@Override
	public final void calculateDedup(final MatchingConfig mc, final DataSourceFrequency freq) {
		final double denominator = getDenominatorDedup(freq);
		for (final String field : freq.getFields()) {
			long sum = 0;
			// for every frequency, calculate u rate based on frequencies in freq2
			for (final String token : freq.getTokens(field)) {
				// add if statement to ignore empty string tokens
				if (isValidToken(token)) {
					sum += countPairs(freq.getFrequency(field, token));
				}
			}
			setU(mc, field, sum, denominator);
		}
		logNonAgreement(mc);
	}
	
	public final static double getDenominator(final DataSourceFrequency freq1, final DataSourceFrequency freq2) {
		return getDenominator(freq1.getTotal(), freq2.getTotal());
	}
	
	public final static double getDenominator(final int total1, final int total2) {
		final double t1 = total1, t2 = total2;
		return t1 * t2;
	}
	
	@Override
	public final void calculate(final MatchingConfig mc, final DataSourceFrequency freq1, final DataSourceFrequency freq2) {
		final Set<String> fields2 = freq2.getFields();
		final double denominator = getDenominator(freq1, freq2);
		// iterate over common fields, calculating u-values
		for (final String field : freq1.getFields()) {
			if (!fields2.contains(field)) {
				continue;
			}
			long sum = 0;
			for (final String token : freq1.getTokens(field)) {
				if (isValidToken(token)) {
					final long count1 = freq1.getFrequency(field, token), count2 = freq2.getFrequency(field, token);
					sum += (count1 * count2);
				}
			}
			setU(mc, field, sum, denominator);
		}
		logNonAgreement(mc);
	}
	
	protected void setU(final MatchingConfig mc, final String field, final long sum, final double denominator) {
		final double numerator = sum;
		mc.setNonAgreementValue(mc.getRowIndexforName(field), numerator / denominator);
	}
	
	protected void logNonAgreement(final MatchingConfig mc) {
		log.info("calcluted values:");
		for (final MatchingConfigRow mcr : mc.getMatchingConfigRows()) {
			log.info(mcr.getName() + ":\t" + mcr.getNonAgreement());
		}
	}
}
