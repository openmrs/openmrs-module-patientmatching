package org.regenstrief.linkage.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.regenstrief.linkage.analysis.DataSourceFrequency;

public class ColumnPair {
	private Column columnA;
	private Column columnB;
	private int pairwiseCombinationCount;
	private double jointEntropy;
	private double mutualInformationScore;
	private Map<String, Integer> combinations = new HashMap<String, Integer>();

	public Column getColumnA() {
		return columnA;
	}

	public void setColumnA(Column columnA) {
		this.columnA = columnA;
	}

	public Column getColumnB() {
		return columnB;
	}

	public void setColumnB(Column columnB) {
		this.columnB = columnB;
	}

	public int getPairwiseCombinationCount() {
		return pairwiseCombinationCount;
	}

	public void setPairwiseCombinationCount(int pairwiseCombinationCount) {
		this.pairwiseCombinationCount = pairwiseCombinationCount;
	}

	public Map<String, Integer> getCombinations() {
		return combinations;
	}

	public void setCombinations(Map<String, Integer> combinations) {
		this.combinations = combinations;
	}

	public double getJointEntropy() {
		return jointEntropy;
	}

	public void setJointEntropy(double jointEntropy) {
		this.jointEntropy = jointEntropy;
	}
	
	
	public double getMutualInformationScore() {
		return mutualInformationScore;
	}

	public void setMutualInformationScore(double mutualInformationScore) {
		this.mutualInformationScore = mutualInformationScore;
	}
}
