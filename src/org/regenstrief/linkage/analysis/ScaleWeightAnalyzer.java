package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.ScaleWeightDBManager;
import org.regenstrief.linkage.db.ScaleWeightDBManager.CountType;
import org.regenstrief.linkage.util.*;

/**
 * Class implements a scale-weight algorithm to analyze
 * the Records given to it
 *
 *@author scentel
 *
 */

public class ScaleWeightAnalyzer extends Analyzer {
	
	private MatchingConfig config;
	private LinkDataSource lds;
	private static ScaleWeightDBManager sw_connection;

	private String datasource_id;

	// Scale weight columns indexed by column label
	private Hashtable <String, DataColumn> sw_columns;
	private List<MatchingConfigRow> sw_rows;

	// Indexed by column label
	private Hashtable <String, Integer> null_counter;
	private Hashtable <String, Integer> non_null_counter;
	private Hashtable <String, Hashtable<String, Integer>> frequencies;
	private Hashtable <String, PriorityQueue<AnalysisObject>> min_priority_queues;
	
	public ScaleWeightAnalyzer(LinkDataSource lds, MatchingConfig mc, String db_access){
		this.config = mc;
		this.lds = lds;
		this.datasource_id = "" + lds.getDataSource_ID();
		
		sw_rows = mc.getScaleWeightColumns();
		
		// Set up database connection
		String [] access = db_access.split(",");
		if(sw_connection == null) {
			sw_connection = new ScaleWeightDBManager(access[0], access[1], access[2], access[3]);
			sw_connection.connect();
		}

		// Initialize hash tables
		sw_columns = lds.getScaleWeightDataColumns(mc);
		int column_count = sw_columns.size();
		null_counter = new Hashtable<String, Integer>(2*column_count);
		non_null_counter = new Hashtable<String, Integer>(2*column_count);
		frequencies = new Hashtable<String, Hashtable<String,Integer>>(2*column_count);
		min_priority_queues = new Hashtable<String, PriorityQueue<AnalysisObject>>(2*column_count);
	}
	
	private void incrementHashtableValue(Hashtable<String,Integer> table, String demographic) {
		// Learn what the frequency was before
		Integer col_frequency = table.get(demographic);
		// this is the first null we see
		if(col_frequency == null) {
			table.put(demographic, Integer.valueOf(1));
		}
		// just increment it
		else {
			col_frequency++;
			table.put(demographic, col_frequency);
		}
	}

	public void analyzeRecord(Record rec){
		for(MatchingConfigRow mcr : sw_rows) {
			String current_demographic = mcr.getName();
			String dem_value = rec.getDemographic(current_demographic);
			int buffer_size = mcr.getBuffer_size();
			
			// Null value 
			if(dem_value == null || dem_value.equals("")) {
				incrementHashtableValue(null_counter, current_demographic);
			}
			// Non-null
			else {
				incrementHashtableValue(non_null_counter, current_demographic);
				Hashtable<String, Integer> frequency_table;
				PriorityQueue<AnalysisObject> min_pq;

				// Retrieve frequency table for current demographic
				frequency_table = frequencies.get(current_demographic);
				// Create it if it does not exist
				if(frequency_table == null) {
					frequency_table = new Hashtable<String, Integer>(2*buffer_size);
					frequencies.put(current_demographic, frequency_table);
				}

				// Retrieve priority queue for current demographic
				min_pq = min_priority_queues.get(current_demographic);
				// Create it if it does not exist
				if(min_pq == null) {
					min_pq = new PriorityQueue<AnalysisObject>(buffer_size, AnalysisObject.frequencyComparator);
					min_priority_queues.put(current_demographic, min_pq);
				}
				DataColumn current_column = sw_columns.get(current_demographic);

				// See if a frequency for our token exists in memory
				try {
					int frequency = frequency_table.get(dem_value);
					// Have to update the frequency in the queue and hashtable
					boolean changed = min_pq.remove(new AnalysisObject(dem_value, frequency));
					frequency++;
					frequency_table.put(dem_value, frequency);
					min_pq.add(new AnalysisObject(dem_value, frequency));

				} catch (NullPointerException e) {
					// Frequency is not stored in memory, have to check it from the database
					int new_frequency = sw_connection.getTokenFrequencyFromDB(current_column, datasource_id, dem_value) + 1;
					AnalysisObject ao = new AnalysisObject(dem_value, new_frequency);
					// If hashtable is not full, we better store this frequency in memory
					int num_el = frequency_table.size();
					if (num_el < buffer_size) {
						frequency_table.put(dem_value, new_frequency);
						min_pq.add(ao);
					} else {
						// Hashtable is full, but we may want to replace an element in the hashtable with this one
						// That happens only if this token's frequency exceeds the minimum frequency token in the hashtable
						AnalysisObject min_freq = (AnalysisObject) min_pq.element();
						if (new_frequency > min_freq.frequency) {
							frequency_table.remove(min_freq.token);
							// Have to store the replaced token in the database, otherwise it will be lost
							sw_connection.addOrUpdateToken(current_column, datasource_id, min_freq.token, min_freq.frequency);
							frequency_table.put(dem_value, new_frequency);
							min_pq.remove();
							min_pq.add(ao);
						}
						else {
							// I still don't want to store this element in memory, so update its frequency in the database
							sw_connection.addOrUpdateToken(current_column, datasource_id, dem_value, new_frequency);
						}
					}
				}

			}
		}
	}

	public void finishAnalysis() {
		Enumeration<String> demographics = sw_columns.keys();
		// for all scale weight columns
		while(demographics.hasMoreElements()) {
			// column label
			String current_demographic = demographics.nextElement();
			DataColumn current_column = sw_columns.get(current_demographic);

			int n_non_null, n_null;
			
			try {
				n_non_null  = non_null_counter.get(current_demographic);
				// It will give an exception if there is no entry for this column
			} catch(NullPointerException e) {
				n_non_null = 0;
			}

			try {
				n_null = null_counter.get(current_demographic);
				// It will give an exception if there is no entry for this column
			} catch(NullPointerException e) {
				n_null = 0;
			}

			// set null and non-null counts
			sw_connection.setCount(CountType.Null, current_column, datasource_id, n_null);
			sw_connection.setCount(CountType.NonNull, current_column, datasource_id, n_non_null);

			Hashtable<String, Integer> ht = frequencies.get(current_demographic);
			// transfer frequencies stored in memory to the database
			for(Enumeration<String> e = ht.keys(); e.hasMoreElements();) {
				String token = e.nextElement(); 
				Integer frequency =	ht.get(token);
				sw_connection.addOrUpdateToken(current_column, datasource_id, token, frequency);
			}

			sw_connection.setCount(CountType.Unique, current_column, datasource_id, sw_connection.getDistinctRecordCount(current_column, datasource_id));
		}
	}

	public LinkDataSource getLinkDataSource() {
		return lds;
	}
	
	public MatchingConfig getMatchingConfig() {
		return config;
	}

	public static ScaleWeightDBManager getSw_connection() {
		return sw_connection;
	}
	
}
	

