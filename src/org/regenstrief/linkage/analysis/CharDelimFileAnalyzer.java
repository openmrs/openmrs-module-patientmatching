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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author sarpc
 * Used to analyze character delimited files
 * 
 * TODO: Add getNonNullCount, getNullCount
 */
public class CharDelimFileAnalyzer extends DataSourceAnalyzer {
	
	private CharDelimFileReader reader;
	
	public CharDelimFileAnalyzer(LinkDataSource lds, MatchingConfig mc, LinkDBManager ldbmanager) {
		super(ldbmanager);
		reader = new CharDelimFileReader(lds,mc,Job.Analysis);
	}
	
	// TODO: Find how to reset a CharDelimFileReader
	public int getNumberOfRecords() {
		int no_records = 0;
		while(reader.hasNextRecord()) {
			reader.nextRecord();
			no_records++;
		}
		reader.reset();
		return no_records;
	}
	
	/*
	 * This method calculates frequencies of the tokens in a given column, and sets
	 * the number of null and non-null values in the given DataColumn, as well as the
	 * total record count in the given DataSource
	 * 
	 * @return Returned table is indexed by value in the given column,
	 * and the value in the hashtable is the frequency  
	 */
	public Hashtable<String,Integer> getTokenFrequencies(DataColumn target_column){
		int null_count = 0;
		int total_count = 0;
		Hashtable<String,Integer> frequencies = new Hashtable<String,Integer>();
		while(reader.hasNextRecord()) {
			Record current_record = reader.nextRecord();
			String column_value = current_record.getDemographic(target_column.getName());
			if(column_value.equals("") || column_value == null){
				null_count++;
				total_count++;
			}
			int record_frequency;
			try {
				record_frequency = frequencies.get(column_value);
			}
			catch(NullPointerException ex){
				record_frequency = 0;
			}
			record_frequency++;
			frequencies.put(column_value,record_frequency);
		}
		target_column.setNonNullCont(total_count-null_count);
		target_column.setNullCount(null_count);
		reader.data_source.setRecordCount(total_count);
		
		Enumeration freq_enum = frequencies.keys();
		// store the frequencies in a relational database
		while(freq_enum.hasMoreElements()) {
			String value = (String) freq_enum.nextElement();
			int frequency = frequencies.get(value);
			System.out.println(value + ": " + frequency);
			sw_connection.insertToken(target_column,reader.data_source.getDataSource_ID() ,value,frequency);
		}
		
		return frequencies;
	}
}
