package org.regenstrief.linkage.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;

public final class BlockingHeuristicCalculator extends FrequencyBasedCalculator {
	private final static String FIELD = BlockingFrequencyAnalyzer.FIELD;
	private final static double entropyDenominator = -Math.log(2.0);
	private final static double log10 = Math.log(10.0);
	
	private double entropy;
	private double maxEntropy;
	private int uniqueValues = 0;
	private int nullValues = 0;
	private int totalPairs = 0;
	
	public final static List<MatchingConfig> getBlockingSchemesToAnalyze(final RecMatchConfig rmConf) {
		final String possibleFieldString = System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.possibleFields");
		if (possibleFieldString == null) {
			return rmConf.getMatchingConfigs();
		}
		final int minFields = Integer.parseInt(System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.minFields"));
		final int maxFields = Integer.parseInt(System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.maxFields"));
		final String[] possibleFields = possibleFieldString.split(";");
		return getPossibleBlockingSchemes(minFields, maxFields, possibleFields);
	}
	
	public final static List<MatchingConfig> getPossibleBlockingSchemes(final int minFields, final int maxFields, final String[] possibleFields) {
		final List<MatchingConfig> mcs = new ArrayList<MatchingConfig>();
		for (int numFields = minFields; numFields <= maxFields; numFields++) {
			final String[] currentFields = new String[numFields];
			addBlockingSchemes(mcs, currentFields, 0, possibleFields, 0, numFields);
		}
		return mcs;
	}
	
	private final static void addBlockingSchemes(final List<MatchingConfig> mcs, final String[] currentFields, final int currentIndex,
	                                             final String[] possibleFields, final int possibleIndex, final int numFields) {
		final int nextIndex = currentIndex + 1;
		for (int i = possibleIndex; i < possibleFields.length; i++) {
			currentFields[currentIndex] = possibleFields[i];
			if (nextIndex < numFields) {
				addBlockingSchemes(mcs, currentFields, nextIndex, possibleFields, i + 1, numFields);
			} else {
				mcs.add(newMatchingConfig(currentFields));
			}
		}
	}
	
	private final static MatchingConfig newMatchingConfig(final String[] fields) {
		final StringBuilder b = new StringBuilder();
		for (final String field : fields) {
			if (b.length() > 0) {
				b.append('+');
			}
			b.append(field);
		}
		final MatchingConfig mc = new MatchingConfig(b.toString(), fields);
		int i = 1;
		for (final MatchingConfigRow mcr : mc.getMatchingConfigRows()) {
			mcr.setBlockOrder(i);
			i++;
		}
		return mc;
	}
	
	@Override
	public final void calculateDedup(final MatchingConfig mc, final DataSourceFrequency freq) {
		final double totalRecords = freq.getTotal();
		int totalPairs = 0, nullValues = 0;
		double entropyNumerator = 0;
		final Set<String> tokens = freq.getTokens(FIELD);
		for (final String token : tokens) {
			final int numRecords = freq.getFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += numRecords;
				continue;
			}
			
			totalPairs += countPairs(numRecords);
			
			entropyNumerator += getFieldEntropyNumerator(numRecords, totalRecords);
		}
		final int uniqueValues = tokens.size();
		final BlockingUCalculator uCalc = new BlockingUCalculator();
		uCalc.calculateDedup(mc, freq);
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator, uniqueValues, nullValues, uCalc, freq.getDataSourceName());
	}
	
	@Override
	public final void calculate(final MatchingConfig mc, final DataSourceFrequency freq1, final DataSourceFrequency freq2) {
		final double totalRecords = freq1.getTotal() + freq2.getTotal();
		int totalPairs = 0, nullValues = 0;
		double entropyNumerator = 0;
		final Set<String> tokens1 = freq1.getTokens(FIELD);
		for (final String token : tokens1) {
			final int numRecords1 = freq1.getFrequency(FIELD, token), numRecords2 = freq2.getFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += (numRecords1 + numRecords2);
				continue;
			}
			
			totalPairs += (numRecords1 * numRecords2);
			
			entropyNumerator += getFieldEntropyNumerator(numRecords1 + numRecords2, totalRecords);
		}
		int uniqueValues = tokens1.size();
		for (final String token : freq2.getTokens(FIELD)) { // Unpaired values needed for entropy
			if (tokens1.contains(token)) {
				continue;
			}
			uniqueValues++;
			final int numRecords2 = freq2.getFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += numRecords2;
				continue;
			}
			entropyNumerator += getFieldEntropyNumerator(numRecords2, totalRecords);
		}
		final BlockingUCalculator uCalc = new BlockingUCalculator();
		uCalc.calculate(mc, freq1, freq2);
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator, uniqueValues, nullValues, uCalc, freq1.getDataSourceName() + "_" + freq2.getDataSourceName());
	}
	
	private final static double getFieldEntropyNumerator(final double num, final double totalRecords) {
		return num * Math.log(num / totalRecords);
	}
	
	private final static double divide(final double n, final double d) {
		return (d == 0) ? 0 : (n / d);
	}
	
	private final static double round(final double d) {
		final double t = Math.round(d * 1000.0);
		return t / 1000.0;
	}
	
	private final void finishCalculation(final MatchingConfig mc, final double totalRecords, final int totalPairs, final double entropyNumerator,
	                                     final int uniqueValues, final int nullValues, final BlockingUCalculator uCalc, final String dataSourceName) {
		entropy = divide(entropyNumerator, (totalRecords * entropyDenominator));
		maxEntropy = -Math.log(uniqueValues) / entropyDenominator;
		final double entropyPercentage = divide(entropy, maxEntropy);
		this.uniqueValues = uniqueValues;
		final double ratio = divide(totalRecords, uniqueValues);
		this.nullValues = nullValues;
		final double nullPercentage = divide(nullValues, totalRecords);
		final double u = uCalc.u;
		this.totalPairs = totalPairs;
		final double logTotalPairs = (totalPairs == 0) ? 0 : (Math.log(totalPairs) / log10);
		final double dispEntropy = round(entropy), dispMaxEntropy = round(maxEntropy), dispEntropyPercentage = round(entropyPercentage);
		final double dispRatio = round(ratio), dispNullPercentage = round(nullPercentage), dispU = round(u), dispLog = round(logTotalPairs);
		log.info(mc.getName() + " Heuristics");
		log.info("Entropy: " + dispEntropy);
		log.info("Maximum entropy: " + dispMaxEntropy);
		log.info("Entropy percentage: " + dispEntropyPercentage);
		log.info("Number of unique values: " + uniqueValues);
		log.info("Total/unique:" + dispRatio);
		log.info("Number of null values: " + nullValues);
		log.info("Percentage of null values: " + dispNullPercentage);
		log.info("u: " + dispU);
		log.info("Total number of pairs: " + totalPairs);
		log.info("Log: " + dispLog);
		final String outName = dataSourceName + ".blocking.stats.txt";
		final File outFile = new File(outName);
		final boolean headerNeeded = !outFile.exists();
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(outFile, true));
			if (headerNeeded) {
				out.println("NAME|H|HMAX|H%|UNIQUE|UNIQUERATIO|NULL|NULL%|U|PAIRS|LOGPAIRS");
			}
			out.print(mc.getName());
			out.print('|');
			out.print(dispEntropy);
			out.print('|');
			out.print(dispMaxEntropy);
			out.print('|');
			out.print(dispEntropyPercentage);
			out.print('|');
			out.print(uniqueValues);
			out.print('|');
			out.print(dispRatio);
			out.print('|');
			out.print(nullValues);
			out.print('|');
			out.print(dispNullPercentage);
			out.print('|');
			out.print(dispU);
			out.print('|');
			out.print(totalPairs);
			out.print('|');
			out.print(dispLog);
			out.println();
			out.flush();
			out.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public final double getEntropy() {
		return entropy;
	}
	
	public final double getMaxEntropy() {
		return maxEntropy;
	}
	
	public final int getUniqueValues() {
		return uniqueValues;
	}
	
	public final int getNullValues() {
		return nullValues;
	}
	
	public final int getTotalPairs() {
		return totalPairs;
	}
	
	private final static class BlockingUCalculator extends CloseFormUCalculator {
		
		private double u = 0;
		
		@Override
		protected final void setU(final MatchingConfig mc, final String field, final int sum, final double denominator) {
			u = sum / denominator;
		}
		
		@Override
		protected final void logNonAgreement(final MatchingConfig mc) {
		}
	}
}
