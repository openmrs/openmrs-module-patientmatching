/**
 * 
 */
package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.db.LinkDBManager;
import org.regenstrief.linkage.io.CharDelimFileReader;
import org.regenstrief.linkage.io.DataSourceReader.Job;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import java.util.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author sarpc Used to analyze character delimited files
 * 
 * TODO: Test with PostgreSQL
 */
public class CharDelimFileAnalyzer extends DataSourceAnalyzer {
	
	public CharDelimFileAnalyzer(LinkDataSource lds, MatchingConfig mc,
			LinkDBManager ldbmanager) {
		super(ldbmanager);
		reader = new CharDelimFileReader(lds, mc, Job.Analysis);
		datasource_id = reader.data_source.getDataSource_ID();
	}
	
	public void analyzeTokenFrequencies(DataColumn target_column) {
		long start = System.currentTimeMillis();
		int loop_count = 0;
		while (reader.hasNextRecord()) {
			Record current_record = reader.nextRecord();
			String column_value = current_record.getDemographic(target_column
					.getName());
			if (!column_value.equals("") && column_value != null) {
				int record_frequency = sw_connection.getTokenFrequency(
						target_column, datasource_id, column_value);
				record_frequency++;
				addOrUpdateToken(target_column, datasource_id, column_value, record_frequency);
			}
			loop_count++;
		}
		finishAnalysis();
		long end = System.currentTimeMillis();
		System.out.println("Time ellapsed: " + (end - start) + " Loop Count: "
				+ loop_count);
	}
	
	public void analyzeTokenFrequencies(DataColumn target_column, int size) {
		long start = System.currentTimeMillis();
		PriorityQueue<AnalysisObject> pq = new PriorityQueue<AnalysisObject>(
				size,AnalysisObject.FrequencyComparator);
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>(2 * size);
		int loop_count = 0;
		int hash_table = 0;
		int dirty_read = 0;
		while (reader.hasNextRecord()) {
			Record current_record = reader.nextRecord();
			String column_value = current_record.getDemographic(target_column
					.getName());
			if (!column_value.equals("") && column_value != null) {
				try {
					int frequency = ht.get(column_value);
					boolean changed = pq.remove(new AnalysisObject(column_value, frequency));
					frequency++;
					ht.remove(column_value);
					ht.put(column_value, frequency);
					pq.add(new AnalysisObject(column_value, frequency));
					hash_table++;
					
				} catch (NullPointerException e) {
					int record_frequency = sw_connection.getTokenFrequency(
							target_column, datasource_id, column_value) + 1;
					AnalysisObject ao = new AnalysisObject(column_value,
							record_frequency);
					
					int num_el = ht.size();
					if (num_el < size) {
						ht.put(column_value, record_frequency);
						pq.add(ao);
						hash_table++;
					} else {
						AnalysisObject min_freq = (AnalysisObject) pq.element();
						dirty_read++;
						if (record_frequency > min_freq.frequency) {
							ht.remove(min_freq.token);
							addOrUpdateToken(target_column, datasource_id, min_freq.token, min_freq.frequency);
							ht.put(column_value, record_frequency);
							pq.remove();
							pq.add(ao);
						}
						else {
							addOrUpdateToken(target_column, datasource_id, column_value, record_frequency);
						}
					}
				}
			}
			loop_count++;
		}
		
		for(Enumeration e = ht.keys(); e.hasMoreElements();) {
			String token = (String) e.nextElement(); 
			Integer frequency =	ht.get(token);
			addOrUpdateToken(target_column, datasource_id, token, frequency);
		}
		
		finishAnalysis();
		long end = System.currentTimeMillis();
		System.out.println("Time ellapsed: " + (end - start) + " Loop Count: "
				+ loop_count + "Hash table: " + hash_table);
	}
		
	public int getRecordCount() {
		int no_records = 0;
		while (reader.hasNextRecord()) {
			reader.nextRecord();
			no_records++;
		}
		finishAnalysis();
		return no_records;
	}
	
	public int getNonNullCount(DataColumn target_column) {
		int non_null_count = 0;
		while (reader.hasNextRecord()) {
			Record current_record = reader.nextRecord();
			String column_value = current_record.getDemographic(target_column
					.getName());
			if (!column_value.equals("") && column_value != null) {
				non_null_count++;
			}
		}
		finishAnalysis();
		return non_null_count;
	}
	
	public int getNullCount(DataColumn target_column) {
		int null_count = 0;
		while (reader.hasNextRecord()) {
			Record current_record = reader.nextRecord();
			String column_value = current_record.getDemographic(target_column
					.getName());
			if (column_value.equals("") || column_value == null) {
				null_count++;
			}
		}
		finishAnalysis();
		return null_count;
	}
	
	public void finishAnalysis() {
		reader.reset();
	}
}
