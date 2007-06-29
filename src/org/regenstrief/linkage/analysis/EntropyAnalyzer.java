package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Iterator;

import org.regenstrief.linkage.Record;

/**
 * Class calculates the entropy of the fields of the given Records.  It initializes
 * a hash table to keep track of frequencies and when the Record streem is
 * finished, calculates the values
 *
 */

public class EntropyAnalyzer extends Analyzer {
	// of the format table{demographic} -> table{token} -> count
	private Hashtable<String,Hashtable<String,Integer>> freq_table;
	private int total_records;
	
	public EntropyAnalyzer(){
		freq_table = new Hashtable<String,Hashtable<String,Integer>>();
		total_records = 0;
	}
	
	public void analyzeRecord(Record rec){
		total_records++;
		Iterator<String> it = rec.getDemographics().keySet().iterator();
		while(it.hasNext()){
			String demographic = it.next();
			String value = rec.getDemographic(demographic);
			Hashtable<String,Integer> demographic_token_freq = freq_table.get(demographic);
			if(demographic_token_freq == null){
				// need to create this hash table to insert to hashtable with a count of 1
				Hashtable<String,Integer> bucket = new Hashtable<String,Integer>();
				bucket.put(value, new Integer(1));
				freq_table.put(demographic, bucket);
			} else {
				// get the value and increment the number
				Integer count = demographic_token_freq.get(value);
				count++;
				demographic_token_freq.put(value, count);
			}
		}
	}
	
	/*
	 * At the point, the frequency table has been constructed and there are no more incoming
	 * Record objects
	 */
	public void finishAnalysis() {
		Iterator<String> demographic_it = freq_table.keySet().iterator();
		double entropy;
		while(demographic_it.hasNext()){
			String current_demographic = demographic_it.next();
			entropy = 0;
			
			Hashtable<String,Integer> demographic_freq_table = freq_table.get(current_demographic);
			if(demographic_freq_table != null){
				Iterator<String> it = demographic_freq_table.keySet().iterator();
				while(it.hasNext()){
					String token = it.next();
					Integer count = demographic_freq_table.get(token);
					double frequency = count.doubleValue() / total_records;
					entropy += -1 * frequency * (Math.log(frequency)/Math.log(2));
				}
			}
			
			System.out.println("column/demographic " + current_demographic + " has entroy of: " + entropy);
		}
		
		// return some sort of analysis result object
	}

}
