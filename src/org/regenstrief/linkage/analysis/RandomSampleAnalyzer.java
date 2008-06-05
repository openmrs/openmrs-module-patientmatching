package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.FormPairs;
import org.regenstrief.linkage.io.OrderedDataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.StringMatch;


/**
 * Class analyzes two record sources to calculate new u values 
 * to use in matching.  To do this, it creates record pairs based on the
 * blocking columns.  It then randomly pairs records from both
 * data sources and compares the demographics, calculating the new u values
 * based on what percent the random pairings match.
 * 
 * 
 */

public class RandomSampleAnalyzer extends RecordPairAnalyzer implements LoggingObject {
	public static final int SAMPLE_SIZE = 100000;
	
	Random rand;
	
	List<int[]> index_pairs;
	Record[] recs1, recs2;
	int pair_count;
	
	/*
	 * Random sampling size as defined by the user that will replace the static value
	 */
	private int sampleSize;
	
	public RandomSampleAnalyzer(LinkDataSource lds1, LinkDataSource lds2, MatchingConfig mc){
		super(lds1, lds2, mc);
		rand = new Random();
		
		index_pairs = new ArrayList<int[]>();
		
		int recordPairCount = countRecordPairs();
		
		sampleSize = mc.getRandomSampleSize();
		
		setIndexPairs(recordPairCount);
		
		recs1 = new Record[sampleSize];
		recs2 = new Record[sampleSize];
		pair_count = 0;
	}
	
	public void analyzeRecordPair(Record[] pair){
		// need to set the corresponding value in record arrays if one of
		// the current records is set to be sampled
		for(int i = 0; i < sampleSize; i++){
			int[] set = index_pairs.get(i);
			if(set[0] == pair_count){
				recs1[i] = pair[0];
			}
			if(set[1] == pair_count){
				recs2[i] = pair[1];
			}
		}
		
		pair_count++;
	}
	
	public void finishAnalysis(){
		// iterate over the saved record pairs and determine rate of matching
		Hashtable<String,Integer> demographic_agree_count = new Hashtable<String,Integer>();
		
		for(int i = 0; i < sampleSize; i++){
			Record r1 = recs1[i];
			Record r2 = recs2[i];
			
			// compare the two records to modify u values
			HashSet<String> demographics = new HashSet<String>();
			List<MatchingConfigRow> includedColumn = mc.getIncludedColumns();
			Iterator<MatchingConfigRow> mcrIterator = includedColumn.iterator();
			while(mcrIterator.hasNext()) {
			    MatchingConfigRow mcr = mcrIterator.next();
			    demographics.add(mcr.getName());
			}
			
			Iterator<String> it = demographics.iterator();
			while(it.hasNext()){
				String demographic = it.next();
				Integer bucket = demographic_agree_count.get(demographic);
				if(bucket == null){
					// not keeping stats for this column yet
					demographic_agree_count.put(demographic, new Integer(0));
				}
				
				if(matchesOnDemographic(r1, r2, demographic, mc)){
					Integer non_match_count = demographic_agree_count.get(demographic);
					demographic_agree_count.put(demographic, new Integer(non_match_count.intValue() + 1));
				}
				
			}
		}
		
		// review totals and calculate u values
		// modify the matching config object to reflect calculated values
		Iterator<String> it = demographic_agree_count.keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			int agree_count = demographic_agree_count.get(demographic);
			double u_val = (double)agree_count/(double)sampleSize;
			if(u_val < EMAnalyzer.EM_ZERO){
				u_val = EMAnalyzer.EM_ZERO;
			} else if(u_val > EMAnalyzer.EM_ONE){
				u_val = EMAnalyzer.EM_ONE;
			}
			
			double stdDev = getStandardDeviation(sampleSize, u_val);
			double[] confidenceInterval = getConfidenceInterval(u_val, stdDev);
			
			log.info("Generated u for: " + demographic + formatOutput(u_val, stdDev, confidenceInterval));
			mc.getMatchingConfigRowByName(demographic).setNonAgreement(u_val);
		}
	}
	
	private String formatOutput(double u, double stdDev, double[] interval){
	    StringBuffer buffer = new StringBuffer();
	    buffer.append(System.getProperty("line.separator"));
	    buffer.append("\tu value: ").append(u).append(", ");
	    buffer.append(System.getProperty("line.separator"));
	    buffer.append("\tsd: ").append(stdDev).append(", ");
	    buffer.append(System.getProperty("line.separator"));
	    buffer.append("\t95% CI: ");
	    buffer.append("(").append(interval[0]).append(", ");
	    buffer.append(interval[1]).append(")");
	    return buffer.toString();
	}

    /*
     * Generate standard deviation value
     * Probably should move this to a static utility class
     */
    private double getStandardDeviation(double n, double p) {
        return Math.sqrt(n * p * (1 - p)) / n;
    }
    
    /*
     * Generate the confidence interval value.
     * Probably should move this to a static utility class
     */
    private double[] getConfidenceInterval(double p, double std) {
        double[] d = new double[2];
        d[0] = p - 2 * std;
        d[1] = p + 2 * std;
        return d;
    }
    
	private boolean matchesOnDemographic(Record r1, Record r2, String demographic, MatchingConfig mc){
		String val1 = r1.getDemographic(demographic);
		String val2 = r2.getDemographic(demographic);
		
		MatchingConfigRow mcr = mc.getMatchingConfigRowByName(demographic);
		
		boolean match = false;
		if(val1 != null && val2 != null) {
			switch(mcr.getAlgorithm()){
			case(MatchingConfig.EXACT_MATCH):
				match = StringMatch.exactMatch(val1, val2);
			break;
			case(MatchingConfig.JWC):
				match = StringMatch.JWCMatch(val1, val2);
			break;
			case(MatchingConfig.LCS):
				match = StringMatch.LCSMatch(val1, val2);
			break;
			case(MatchingConfig.LEV):
				match = StringMatch.LEVMatch(val1, val2);
			break;
			}
		}
		
		return match;
	}
	
	private int countRecordPairs(){
		ReaderProvider rp = new ReaderProvider();
		OrderedDataSourceReader reader1 = rp.getReader(lds1, mc);
		OrderedDataSourceReader reader2 = rp.getReader(lds2, mc);
		FormPairs fp = new FormPairs(reader1, reader2, mc, lds1.getTypeTable());
		int pair_count = 0;
		while(fp.getNextRecordPair() != null){
			pair_count++;
		}
		reader1.close();
		reader2.close();
		
		return pair_count;
	}
	
	private void setIndexPairs(int max_index){
		index_pairs.clear();
		
		// need to get two sets of random numbers, one for each data source
		for(int i = 0; i < sampleSize; i++){
			int[] pair = new int[2];
			pair[0] = rand.nextInt(max_index);
			pair[1] = rand.nextInt(max_index);
			index_pairs.add(pair);
		}
	}

	public Logger getLogger() {
		return log;
	}
}
