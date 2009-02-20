package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class defines a FormPairs implementation that returns pairs from a single
 * OrderedDataSourceReader for the purposes of deduplication.  Since pairs of Records
 * will come from the same place, the object only requires one reader in the constructor.
 * 
 * @author jegg
 *
 */

public class DedupOrderedDataSourceFormPairs extends FormPairs{
	
	protected Hashtable<String,Integer> type_table;
	protected OrderedDataSourceReader reader;
	
	protected Record next_blocking_record;
	protected List<Record[]> blocking_set;
	protected String[] comp_columns;
	
	public DedupOrderedDataSourceFormPairs(OrderedDataSourceReader dsr1, MatchingConfig mc, Hashtable<String,Integer> type_table){
		super(mc);
		this.type_table = type_table;
		reader = dsr1;
		
		blocking_set = new ArrayList<Record[]>();
		next_blocking_record = dsr1.nextRecord();
		comp_columns = mc.getBlockingColumns();
	}
	
	protected void fillSet(){
		// read the Records until the next Record is different in blocking
		List<Record> blocking_equivalent = new ArrayList<Record>();
		Record next_record;
		do{
			blocking_equivalent.clear();
			blocking_equivalent.add(next_blocking_record);
			while((next_record = reader.nextRecord()) != null && equalColumnValues(next_blocking_record, next_record)){
				// add next record to list of equal records
				blocking_equivalent.add(next_record);
			}
			
			// save next set of blocking values for next time to fill
			next_blocking_record = next_record;
		}while(next_record != null && blocking_equivalent.size() < 2);
		
		// create all pairs of Records in blocking_set from the Records saved in blocking_equivalent
		while(blocking_equivalent.size() > 1){
			Record left = blocking_equivalent.remove(0);
			for(int i = 0; i < blocking_equivalent.size(); i++){
				Record right = blocking_equivalent.get(i);
				Record[] pair = {left, right};
				blocking_set.add(pair);
			}
		}
	}
	
	protected boolean equalColumnValues(Record r1, Record r2){
		for(int i = 0; i < comp_columns.length; i++){
			String comp_demographic = comp_columns[i];
			String val1 = r1.getDemographic(comp_demographic);
			String val2 = r2.getDemographic(comp_demographic);
			int block_chars = mc.getMatchingConfigRowByName(comp_demographic).getBlockChars();
			if(type_table.get(comp_demographic) == DataColumn.NUMERIC_TYPE){
				try{
					double d1 = Double.parseDouble(val1);
					double d2 = Double.parseDouble(val2);
					if(d1 != d2){
						return false;
					}
				}
				catch(NumberFormatException nfe){
					return false;
				}
			} else {
				if(val1.length() > block_chars){
					val1 = val1.substring(0, block_chars);
				}
				if(val2.length() > block_chars){
					val2 = val2.substring(0, block_chars);
				}
				if(!val1.equals(val2)){
					return false;
				}
			}
		}
		return true;
	}
	
	protected Record[] returnedRecords;
	
	public Record[] getNextRecordPair(){
		returnedRecords = null;
		if(blocking_set.size() > 0){
			returnedRecords = blocking_set.remove(0);
		} else if(next_blocking_record != null){
			fillSet();
			if(blocking_set.size() > 0){
				returnedRecords = blocking_set.remove(0);
			}
		}
		return returnedRecords;
	}
}
