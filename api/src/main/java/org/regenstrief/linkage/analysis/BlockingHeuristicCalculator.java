package org.regenstrief.linkage.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;

public final class BlockingHeuristicCalculator extends FrequencyBasedCalculator {
	private final static String FIELD = BlockingFrequencyAnalyzer.FIELD;
	private final static double entropyDenominator = -Math.log(2.0);
	private final static double log10 = Math.log(10.0);
	
	private boolean fileEnabled = true;
	private double entropy;
	private double maxEntropy;
	private long uniqueValues = 0;
	private long nullValues = 0;
	private double u;
	private long totalPairs = 0;
	
	public final static List<MatchingConfig> getBlockingSchemesToAnalyze(final RecMatchConfig rmConf) {
		final String specificSchemeString = System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.specificSchemes");
		if (specificSchemeString != null) {
			return getSpecificBlockingSchemes(specificSchemeString);
		}
		final String possibleSchemeString = System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.possibleSchemes");
		if (possibleSchemeString != null) {
			return getPossibleBlockingSchemeIntersections(possibleSchemeString);
		}
		final String possibleFieldString = System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.possibleFields");
		if (possibleFieldString == null) {
			return rmConf.getMatchingConfigs();
		}
		final int minFields = Integer.parseInt(System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.minFields"));
		final int maxFields = Integer.parseInt(System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.maxFields"));
		final String[] possibleFields = possibleFieldString.split(";");
		return getPossibleBlockingSchemes(minFields, maxFields, possibleFields);
	}
	
	public final static List<MatchingConfig> getSpecificBlockingSchemes(final String specificSchemeString) {
		final String[][] specificCombinations = parseBlockingSchemes(specificSchemeString);
		final List<MatchingConfig> list = new ArrayList<MatchingConfig>(specificCombinations.length);
		for (final String[] specificCombination : specificCombinations) {
			list.add(newMatchingConfig(specificCombination));
		}
		return list;
	}
	
	private final static String[][] parseBlockingSchemes(final String possibleSchemeString) {
		final String[] possibleSchemes = possibleSchemeString.split(";");
		final int size = possibleSchemes.length;
		final String[][] possibleCombinations = new String[size][];
		for (int i = 0; i < size; i++) {
			possibleCombinations[i] = possibleSchemes[i].split("\\+");
		}
		return possibleCombinations;
	}
	
	public final static List<MatchingConfig> getPossibleBlockingSchemeIntersections(final String possibleSchemeString) {
		final String[][] possibleCombinations = parseBlockingSchemes(possibleSchemeString);
		final Set<Set<String>> intersections = new LinkedHashSet<Set<String>>();
		for (final String[] combination : possibleCombinations) {
			addIntersections(intersections, possibleCombinations, new TreeSet<String>(Arrays.asList(combination)));
		}
		final String minFieldsValue = System.getProperty("org.regenstrief.linkage.analysis.BlockingHeuristicCalculator.minFields");
		if (minFieldsValue != null) {
			final int minFields = Integer.parseInt(minFieldsValue);
			final Iterator<Set<String>> iter = intersections.iterator();
			while (iter.hasNext()) {
				if (iter.next().size() < minFields) {
					iter.remove();
				}
			}
		}
		System.out.println("Calculating heuristics for:\n" + intersections);
		final List<MatchingConfig> list = new ArrayList<MatchingConfig>(intersections.size());
		for (final Set<String> intersection : intersections) {
			list.add(newMatchingConfig(intersection));
		}
		return list;
	}
	
	private final static void addIntersections(final Set<Set<String>> intersections, final String[][] possibleCombinations, final Set<String> base) {
		final int baseSize = base.size();
		for (final String[] combination : possibleCombinations) {
			final Set<String> intersection = new TreeSet<String>(Arrays.asList(combination));
			intersection.addAll(base);
			if (intersection.size() == baseSize) {
				continue;
			} else if (!intersections.add(intersection)) {
				continue;
			}
			addIntersections(intersections, possibleCombinations, intersection);
		}
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
	
	public final static MatchingConfig newMatchingConfig(final Collection<String> fields) {
		return newMatchingConfig(fields.toArray(new String[fields.size()]));
	}
	
	public final static MatchingConfig newMatchingConfig(final String... fields) {
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
		final double uDenominator = CloseFormUCalculator.getDenominatorDedup(freq);
		long totalPairs = 0, nullValues = 0;
		double entropyNumerator = 0;
		long uniqueValues = 0;
		final Iterator<String> tokens = freq.getTokenIterator(FIELD);
		while (tokens.hasNext()) {
			final String token = tokens.next();
			uniqueValues++;
			final long numRecords = freq.getFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += numRecords;
				continue;
			}
			totalPairs += countPairs(numRecords);
			entropyNumerator += getFieldEntropyNumerator(numRecords, totalRecords);
		}
		final double uNumerator = totalPairs, u = uNumerator / uDenominator;
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator, uniqueValues, nullValues, u, getOutName(freq.getDataSourceName()));
	}
	
	@Override
	public final void calculate(final MatchingConfig mc, final DataSourceFrequency freq1, final DataSourceFrequency freq2) {
		final double totalRecords = freq1.getTotal() + freq2.getTotal();
		final double uDenominator = CloseFormUCalculator.getDenominator(freq1, freq2);
		long totalPairs = 0, nullValues = 0;
		double entropyNumerator = 0;
		long uniqueValues = 0;
		final Iterator<String> tokens1 = freq1.getTokenIterator(FIELD);
		while (tokens1.hasNext()) {
			final String token = tokens1.next();
			uniqueValues++;
			final long numRecords1 = freq1.getFrequency(FIELD, token), numRecords2 = freq2.removeFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += (numRecords1 + numRecords2);
				continue;
			}
			totalPairs += (numRecords1 * numRecords2);
			entropyNumerator += getFieldEntropyNumerator(numRecords1 + numRecords2, totalRecords);
		}
		final Iterator<String> tokens2 = freq2.getTokenIterator(FIELD);
		while (tokens2.hasNext()) { // Unpaired values needed for entropy
			final String token = tokens2.next();
			uniqueValues++;
			final long numRecords2 = freq2.getFrequency(FIELD, token);
			if (!isValidToken(token)) {
				nullValues += numRecords2;
				continue;
			}
			entropyNumerator += getFieldEntropyNumerator(numRecords2, totalRecords);
		}
		final double uNumerator = totalPairs, u = uNumerator / uDenominator;
		finishCalculation(mc, totalRecords, totalPairs, entropyNumerator, uniqueValues, nullValues, u, getOutName(freq1.getDataSourceName(), freq2.getDataSourceName()));
	}
	
	private final static String getOutName(final String loc1, final String loc2) {
		return getOutName(loc1) + "_" + getBaseName(loc2);
	}
	
	private final static String getOutName(final String loc) {
		final File f = new File(loc), p = f.getParentFile();
		return (p == null) ? loc : (p.getAbsolutePath() + System.getProperty("file.separator") + getBaseName(f));
	}
	
	private final static String getBaseName(final String loc) {
		return getBaseName(new File(loc));
	}
	
	private final static String getBaseName(final File f) {
		final String name = f.getName();
		final int dot = name.lastIndexOf('.');
		return (dot > 0) ? name.substring(0, dot) : null;
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
	
	private final void finishCalculation(final MatchingConfig mc, final double totalRecords, final long totalPairs, final double entropyNumerator,
	                                     final long uniqueValues, final long nullValues, final double u, final String dataSourceName) {
		entropy = divide(entropyNumerator, (totalRecords * entropyDenominator));
		maxEntropy = -Math.log(uniqueValues) / entropyDenominator;
		final double entropyPercentage = divide(entropy, maxEntropy);
		this.uniqueValues = uniqueValues;
		final double ratio = divide(totalRecords, uniqueValues);
		this.nullValues = nullValues;
		final double nullPercentage = divide(nullValues, totalRecords);
		this.u = u;
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
		if (!fileEnabled) {
			return;
		}
		final String outName = dataSourceName + ".blocking.stats.txt";
		System.out.println("Appending to " + outName);
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
	
	public final long getUniqueValues() {
		return uniqueValues;
	}
	
	public final long getNullValues() {
		return nullValues;
	}
	
	public final double getU() {
		return u;
	}
	
	public final long getTotalPairs() {
		return totalPairs;
	}
	
	protected final void setFileEnabled(final boolean fileEnabled) {
		this.fileEnabled = fileEnabled;
	}
}
