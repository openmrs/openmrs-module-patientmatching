package org.regenstrief.linkage.util;

/*
 * Class represents the matching options for one column of the data file.
 * The options are presented in one row in the GUI, where the class gets
 * its name.
 */

public class MatchingConfigRow implements Cloneable {
	// Determines if bottom/top N frequencies will be loaded
	public enum ScaleWeightSetting {
		TopN, BottomN, TopNPercent, BottomNPercent, AboveN, BelowN
	}

	private String name;
	private int block_order;
	private int block_chars;
	private boolean include;
	private double agreement;
	private double non_agreement;
	private boolean scale_weight;
	private boolean trinomial_EM;
	private ScaleWeightSetting sw_settings;
	private int buffer_size;
	private float sw_number;
	private int algorithm;
	private double threshold;
	private String setID;

	public static final int DEFAULT_BLOCK_ORDER = 0;
	public static final int DEFAULT_BLOCK_CHARS = 40;
	public static final boolean DEFAULT_INCLUDE = false;
	public static final double DEFAULT_AGREEMENT = 0.9;
	public static final double DEFAULT_NON_AGREEMENT = 0.1;
	public static final boolean DEFAULT_SCALE_WEIGHT = false;
	public static final boolean DEFAULT_TRINOMIAL_EM = false;
	public static final boolean DEFAULT_FLAG = false;
	public static final int DEFAULT_ALGORITHM = MatchingConfig.EXACT_MATCH;
	// Load all tokens by default
	public static final Float DEFAULT_SW_NUMBER = new Float(1.0);
	public static final ScaleWeightSetting DEFAULT_SW_SETTING = ScaleWeightSetting.TopNPercent;
	public static final int DEFAULT_BUFFER_SIZE = 500;
	public static final String NO_SET_ID = "0";

	/*
	 * Requires the name of the row at least. Initialize other fields with
	 * default values
	 */
	public MatchingConfigRow(String name) {
		this.name = name;
		block_order = DEFAULT_BLOCK_ORDER;
		block_chars = DEFAULT_BLOCK_CHARS;
		include = DEFAULT_INCLUDE;
		agreement = DEFAULT_AGREEMENT;
		non_agreement = DEFAULT_NON_AGREEMENT;
		scale_weight = DEFAULT_SCALE_WEIGHT;
		algorithm = DEFAULT_ALGORITHM;
		sw_settings = DEFAULT_SW_SETTING;
		sw_number = DEFAULT_SW_NUMBER;
		buffer_size = DEFAULT_BUFFER_SIZE;
		trinomial_EM = DEFAULT_TRINOMIAL_EM;
		setID = NO_SET_ID;
	}

	/**
	 * This method returns the Interchangeable (setID) value
	 * 
	 * @return
	 */
	public String getSetID() {
		return setID;
	}

	/**
	 * This method sets the Interchangeable (setID) value
	 * 
	 * @param id
	 */
	public void setSetID(String id) {
		setID = id;
	}

	public String getName() {
		return name;
	}

	public double getAgreement() {
		return agreement;
	}

	public void setName(String new_name) {

		name = new_name;
	}

	public void setAgreement(double agreement) {

		this.agreement = agreement;
	}

	public void setTrinomialEM(boolean tri) {
		trinomial_EM = tri;
	}

	public boolean isTrinomialEM() {
		return trinomial_EM;
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
		if (algorithm == MatchingConfig.JWC) {
			threshold = StringMatch.JWC_THRESH;
		} else if (algorithm == MatchingConfig.LCS) {
			threshold = StringMatch.LCS_THRESH;
		} else if (algorithm == MatchingConfig.LEV) {
			threshold = StringMatch.LEV_THRESH;
		} else if (algorithm == MatchingConfig.DICE) {
			threshold = StringMatch.DICE_THRESH;
		}
	}
	
	public double getThreshold() {
		return threshold;
	}
	
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getBlockChars() {
		return block_chars;
	}

	public void setBlockChars(int block_chars) {

		this.block_chars = block_chars;
	}

	public int getBlockOrder() {
		return block_order;
	}

	public void setBlockOrder(int block_order) {

		this.block_order = block_order;
	}

	public boolean isIncluded() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}

	public double getNonAgreement() {
		return non_agreement;
	}

	public void setNonAgreement(double non_agreement) {
		this.non_agreement = non_agreement;

	}

	public boolean isScaleWeight() {
		return scale_weight;
	}

	public void setScaleWeight(boolean scale_weight) {
		this.scale_weight = scale_weight;
	}

	public String toString() {
		String ret = new String();
		ret += name + ":\n";
		ret += "block order: " + block_order + "\n";
		ret += "blocking charcters: " + block_chars + "\n";
		ret += "include in scoring: " + include + "\n";
		ret += "agreement weight: " + agreement + "\n";
		ret += "non-agreement weight: " + non_agreement + "\n";
		ret += "scale weight: " + scale_weight + "\n";
		ret += "algorithm: " + MatchingConfig.ALGORITHMS[algorithm] + "\n";
        ret += "threshold: " + threshold + "\n";
        ret += "setID: " + setID + "\n";
		return ret;
	}

	/**
	 * Returns a string representing the old C program meta file format. This
	 * was used as the toString() method before being updated to something that
	 * is read more easily
	 * 
	 * @return a String with the options in a .meta file format
	 */
	public String getMetaString() {
		String ret = new String();
		ret += name + "," + block_order + "," + block_chars + "," + include
				+ "," + agreement + "," + non_agreement + "," + scale_weight
				+ "," + algorithm;
		return ret;
	}

	public Float getSw_number() {
		return sw_number;
	}

	public void setSw_number(float sw_number) {
		this.sw_number = sw_number;
	}

	public ScaleWeightSetting getSw_settings() {
		return sw_settings;
	}

	public void setSw_settings(ScaleWeightSetting sw_settings) {
		this.sw_settings = sw_settings;
	}

	public int getBuffer_size() {
		return buffer_size;
	}

	public void setBuffer_size(int buffer_size) {
		this.buffer_size = buffer_size;
	}

	public Object clone() {
		MatchingConfigRow configRow = null;

		try {
			configRow = (MatchingConfigRow) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return configRow;
	}
}
