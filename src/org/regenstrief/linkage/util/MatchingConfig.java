package org.regenstrief.linkage.util;
/*
 * Code refactored in February 2007.  The MatchingConfig object
 * was disentangled from the GUI and other classes added to hold
 * the 
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;


public class MatchingConfig implements Cloneable {
	
	// options for the algorithms
	public static final String[] ALGORITHMS = {"Exact Match", "JWC", "LCS", "LEV"};
	public static final int EXACT_MATCH = 0;
	public static final int JWC = 1;
	public static final int LCS = 2;
	public static final int LEV = 3;
	
	// threshold that a pair of records, compared using this schema,
	// have to be considered a true positive
	public static final double DEFAULT_SCORE_THRESHOLD = 0;
	private double score_threshold;
	
	// Indicates whether one or more rows have weight scaling
	private boolean is_scale_weight = false;
	
	// datatype constants
	public static final int NUMERIC_TYPE = 0;
	public static final int STRING_TYPE = 1;
	
	private NumberFormat double_format;
	private static final int DOUBLE_SIG_FIGS = 5;
	
	final static double META_ONE = 0.99999;
	final static double META_ZERO = 0.00001;
	
	private List<MatchingConfigRow> row_options;
	
	private String name;
	private boolean estimate;
	private BlockingExclusionList bel;
	
	/*
	 * boolean value to determine whether current blocking run use random
	 * sampling to get the u values or not
	 */
	private boolean usingRandomSampling;
	/*
	 * sample size of the random sampling process
	 */
	private int randomSampleSize;
	/*
	 * boolean value to determine whether the user want to use current random sampling
	 */
	private boolean lockedUValues;
	
	public MatchingConfig(String name, String[] rn){
		row_options = new ArrayList<MatchingConfigRow>();
		for(int i = 0; i < rn.length; i++){
			MatchingConfigRow mcr = new MatchingConfigRow(rn[i]);
			row_options.add(mcr);
		}
		this.name = name;
		estimate = false;
		double_format = NumberFormat.getInstance();
		double_format.setMaximumFractionDigits(DOUBLE_SIG_FIGS);
		score_threshold = DEFAULT_SCORE_THRESHOLD;
		bel = null;
	}
	
	public MatchingConfig(String name, MatchingConfigRow[] mcrs){
		row_options = new ArrayList<MatchingConfigRow>();
		this.name = name;
		estimate = false;
		for(int i = 0; i < mcrs.length; i++){
			row_options.add(mcrs[i]);
		}
		double_format = NumberFormat.getInstance();
		double_format.setMaximumFractionDigits(DOUBLE_SIG_FIGS);
		score_threshold = DEFAULT_SCORE_THRESHOLD;
		bel = null;
	}
	
	public MatchingConfigRow getMatchingConfigRowByName(String name) {
		Iterator<MatchingConfigRow> it = row_options.iterator();
		while(it.hasNext()) {
			MatchingConfigRow mcr = it.next();
			if(mcr.getName().equals(name)) {
				return mcr;
			}
		}
		return null;
	}
	
	public boolean isUsingRandomSampling() {
		return usingRandomSampling;
	}

	public void setUsingRandomSampling(boolean usingRandomSampling) {
		this.usingRandomSampling = usingRandomSampling;
	}

	public int getRandomSampleSize() {
		return randomSampleSize;
	}

	public void setRandomSampleSize(int randomSampleSize) {
		this.randomSampleSize = randomSampleSize;
	}

    public boolean isLockedUValues() {
        return lockedUValues;
    }

    public void setLockedUValues(boolean lockedUValues) {
        this.lockedUValues = lockedUValues;
    }

    public void setBlockingExclusionList(BlockingExclusionList b){
		bel = b;
	}
	
	public BlockingExclusionList getBlockingExclusionList(){
		return bel;
	}
	
	public List<MatchingConfigRow> getMatchingConfigRows(){
		return row_options;
	}
	
	public void setEstimate(boolean b){
		estimate = b;
	}
	
	public boolean isEstimated(){
		return estimate;
	}
	
	public double getScoreThreshold(){
		return score_threshold;
	}
	
	public void setScoreThreshold(double score_threshold){
		this.score_threshold = score_threshold;
	}
	
	public String[] getRowNames(){
		String[] ret = new String[row_options.size()];
		for(int i = 0; i < row_options.size(); i++){
			ret[i] = row_options.get(i).getName();
		}
		return ret;
	}
	
	public String getRowName(int index){
		return row_options.get(index).getName();
	}
	
	/*
	 * Method created for FormPairs and when comparing string values to determine
	 * whether null values should be compared as empty strings or thrown away as
	 * not enough information.  The default is true, to ignore lines containing null values
	 */
	public boolean ignoreNullValues(int index){
		return true;
	}
	
	public int getAlgorithm(int row_index){
		return row_options.get(row_index).getAlgorithm();
	}
	
	public void setAlgorithm(int row_index, int algorithm){
		row_options.get(row_index).setAlgorithm(algorithm);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String str){
		name = str;
	}
	
	public double getAgreementValue(int row_index){
		return row_options.get(row_index).getAgreement();
	}
	
	public void setAgreementValue(int row_index, double value){
		row_options.get(row_index).setAgreement(value);
	}
	
	public double getNonAgreementValue(int row_index){
		return row_options.get(row_index).getNonAgreement();
	}
	
	public void setNonAgreementValue(int row_index, double value){
		row_options.get(row_index).setNonAgreement(value);
	}
	
	public int getRowIndexforName(String name){
		// returns the row that has name as the name
		for(int i = 0; i < row_options.size(); i++){
			if(row_options.get(i).getName().equals(name)){
				// found the index
				return i;
			}
		}
		
		// it was not found, return -1 to signify error
		return -1;
	}
	
	public List<MatchingConfigRow> getIncludedColumns(){
		ArrayList<MatchingConfigRow> ret = new ArrayList<MatchingConfigRow>();
		Iterator<MatchingConfigRow> it = row_options.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			if(mcr.isIncluded()){
				ret.add(mcr);
			}
		}
		return ret;
	}
	
	public List<MatchingConfigRow> getScaleWeightColumns() {
		ArrayList<MatchingConfigRow> ret = new ArrayList<MatchingConfigRow>();
		for(MatchingConfigRow mcr : row_options) {
			if(mcr.isIncluded() && mcr.isScaleWeight()) {
				ret.add(mcr);
			}
		}
		return ret;
	}
	

	/** 
	 * @return indexed by column name, value indicates if the column is included and has weight scaling,
	 * null if no fields require weight scaling
	 */
	
	public Hashtable<String,Boolean> getScaleWeightorNotTable() {
		int num_rows = row_options.size();
		Hashtable<String,Boolean> scale_weight_columns = new Hashtable<String,Boolean>(num_rows*2);
		Iterator<MatchingConfigRow> it = row_options.iterator();
		// find rows in the config that have specified weight scaling
		while(it.hasNext()) {
			MatchingConfigRow mcr = it.next();
			String col_name = mcr.getName();
			if(mcr.isScaleWeight()){// && mcr.isIncluded()) {
				scale_weight_columns.put(col_name, true);
			}
			else {
				scale_weight_columns.put(col_name, false);
			}
		}
	
		if(scale_weight_columns.size() == 0){
			return null;
		}
		else {
			return scale_weight_columns;
		}
	}
	
	/**
	 * Returns the indexes of the columns included in the matching run
	 * 
	 * @return an array of the column names included in the analysis,
	 * including blocking columns
	 */
	public String[] getIncludedColumnsNames(){
		int include_count = 0;
		Iterator<MatchingConfigRow> it = row_options.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			if(mcr.isIncluded()){
				include_count++;
			}
		}
		if(include_count == 0){
			return null;
		}
		String[] ret = new String[include_count];
		int ret_index = 0;
		for(int i = 0; i < row_options.size(); i++){
			MatchingConfigRow mcr = row_options.get(i);
			if(mcr.isIncluded()){
				ret[ret_index] = mcr.getName();
				ret_index++;
			}
		}
		return ret;
	}
	
	/**
	 * Method returns the names of the columns used in the comparison, excluding 
	 * blocking columns.
	 * 
	 * @return
	 */
	public String[] getLinkComparisonColumns(){
		int include_count = 0;
		Iterator<MatchingConfigRow> it = row_options.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			if(mcr.isIncluded() && mcr.getBlockOrder() != MatchingConfigRow.DEFAULT_BLOCK_ORDER){
				include_count++;
			}
		}
		if(include_count == 0){
			return null;
		}
		String[] ret = new String[include_count];
		int ret_index = 0;
		for(int i = 0; i < row_options.size(); i++){
			MatchingConfigRow mcr = row_options.get(i);
			if(mcr.isIncluded() && mcr.getBlockOrder() != MatchingConfigRow.DEFAULT_BLOCK_ORDER){
				ret[ret_index] = mcr.getName();
				ret_index++;
			}
		}
		return ret;
	}
	
	public String[] getBlockingColumns(){
		int blocking_count = 0;
		Iterator<MatchingConfigRow> it = row_options.iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			if(mcr.getBlockOrder() != MatchingConfigRow.DEFAULT_BLOCK_ORDER){
				blocking_count++;
			}
		}
		if(blocking_count == 0){
			return null;
		}
		String[] ret = new String[blocking_count];
		for(int i = 0; i < row_options.size(); i++){
			MatchingConfigRow mcr = row_options.get(i);
			int order = mcr.getBlockOrder();
			if(order != MatchingConfigRow.DEFAULT_BLOCK_ORDER){
				ret[order - 1] = mcr.getName();
			}
		}
		return ret;
	}
	
	public String toString(){
		String ret = new String();
		Iterator<MatchingConfigRow> it = getMatchingConfigRows().iterator();
		while(it.hasNext()){
			MatchingConfigRow mcr = it.next();
			ret += mcr + "\n";
		}
		return ret;
	}

	public boolean get_is_scale_weight() {
		return is_scale_weight;
	}

	public void make_scale_weight() {
		this.is_scale_weight = true;
	}
	
	private String getBlockingDescriptionString(){
		String ret = "";
		String[] blocking_fields = getBlockingColumns();
		for(int i = 0; i < blocking_fields.length; i++){
			MatchingConfigRow mcr = this.getMatchingConfigRowByName(blocking_fields[i]);
			ret += blocking_fields[i] + mcr.getBlockChars();
		}
		return ret;
	}
	
	public String getBlockingHash(){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			String blocking_string = getBlockingDescriptionString();
			
			byte[] hash = md.digest(blocking_string.getBytes());
			String ret = Base64.encodeBase64URLSafeString(hash);
			
			// remove trailing line feed from returned String
			ret = ret.substring(0, 22);
			return ret;
		}
		catch(NoSuchAlgorithmException nsae){
			return null;
		}
		
	}
	
	public Object clone() {
	    MatchingConfig config = null;
	    
	    try {
	        config = (MatchingConfig) super.clone();
	        
	        if(this.bel != null) {
	            config.bel = (BlockingExclusionList) this.bel.clone();
	        }
            
	        config.double_format = (NumberFormat) this.double_format.clone();
            
            config.row_options = new ArrayList<MatchingConfigRow>();
            for (MatchingConfigRow mcr : this.row_options) {
                MatchingConfigRow row = (MatchingConfigRow) mcr.clone();
                config.row_options.add(row);
            }
            
	    } catch (CloneNotSupportedException e) {
	        e.printStackTrace();
	    }
	    
	    return config;
	}
}
