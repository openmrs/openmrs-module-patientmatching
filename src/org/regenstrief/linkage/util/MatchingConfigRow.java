package org.regenstrief.linkage.util;

/*
 * Class represents the matching options for one column of the data file.
 * The options are presented in one row in the GUI, where the class gets
 * its name.
 */

public class MatchingConfigRow {
	private String name;
	private int block_order;
	private int block_chars;
	private boolean include;
	private double agreement;
	private double non_agreement;
	private boolean scale_weight;
	private int algorithm;
	
	public static final int DEFAULT_BLOCK_ORDER = 0;
	public static final int DEFAULT_BLOCK_CHARS = 40;
	public static final boolean DEFAULT_INCLUDE = false;
	public static final double DEFAULT_AGREEMENT = 0.9;
	public static final double DEFAULT_NON_AGREEMENT = 0.1;
	public static final boolean DEFAULT_SCALE_WEIGHT = false;
	public static final int DEFAULT_ALGORITHM = MatchingConfig.EXACT_MATCH;
	
	/*
	 * Requires the name of the row at least.
	 * Initialize other fields with default values
	 */
	public MatchingConfigRow(String name){
		this.name = name;
		block_order = DEFAULT_BLOCK_ORDER;
		block_chars = DEFAULT_BLOCK_CHARS;
		include = DEFAULT_INCLUDE;
		agreement = DEFAULT_AGREEMENT;
		non_agreement = DEFAULT_NON_AGREEMENT;
		scale_weight = DEFAULT_SCALE_WEIGHT;
		algorithm = DEFAULT_ALGORITHM;
	}
	
	public String getName(){
		return name;
	}
	
	public double getAgreement() {
		return agreement;
	}



	public void setAgreement(double agreement) {
		this.agreement = agreement;
	}



	public int getAlgorithm() {
		return algorithm;
	}



	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
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



	public String toString(){
		String ret = new String();
		ret += name + "," + block_order + "," + block_chars + "," + include + "," + agreement + "," + non_agreement + "," + scale_weight + "," + algorithm;
		return ret;
	}
}
