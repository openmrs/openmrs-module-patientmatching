package org.regenstrief.linkage.io;

/*
 * Class stores the logic for incrementing among the link data sources when
 * called to get the next pair.  It assumes that the link data sources
 * give records sorted according to blocking order.
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.BlockingExclusionList;
import org.regenstrief.linkage.util.ComparisonException;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class makes pairs between two DataSourceReaders.  The expectation
 * that the DataSourceReaders will return the Records in order when
 * sorted on the blocking variables allows this class to find pairs
 * of Records while iterating through the readers without storing
 * previous Records.
 *
 */
public class OrderedDataSourceFormPairs extends FormPairs{
	private OrderedDataSourceReader dsr1;
	private OrderedDataSourceReader dsr2;
	private MatchingConfig mc;
	private Hashtable<String,Integer> type_table;
	
	private List<Record> dsr2_buffer;
	private Record dsr2_next, dsr1_next;
	private int buffer_read_index;
	
	private BlockingExclusionList bel;
	
	private static final int GREATER_THAN = 1;
	private static final int EQUAL = 0;
	private static final int LESS_THAN = -1;
	
	/**
	 * 
	 * 
	 * @param dsr1	the first source of Record objects
	 * @param dsr2	the second source of Record objects
	 * @param mc	stores information on blocking variables
	 * @param type_table	indexes Record field names and holds what type (String or numeric) of data they are
	 */
	public OrderedDataSourceFormPairs(OrderedDataSourceReader dsr1, OrderedDataSourceReader dsr2, MatchingConfig mc, Hashtable<String,Integer> type_table){
		super(mc);
		
		this.dsr1 = dsr1;
		this.dsr2 = dsr2;
		this.type_table = type_table;
		dsr2_buffer = new ArrayList<Record>();
		dsr2_next = dsr2.nextRecord();
		dsr1_next = dsr1.nextRecord();
		fillDSR2Buffer();
		bel = mc.getBlockingExclusionList();
	}
	
	/**
	 * Returns the next pair of Record objects from the two DataSourceReaders that have
	 * equivalent blocking variable values.  If there are no more pairs, it returns null.
	 * 
	 * @return	a 2 length array of Records that have equivalent values for blocking fields
	 */
	/*
	 * Method returns the next record pair that has equal blocking variables.
	 * For the Record from data source 2, get the Record at
	 * buffer_read_index from dsr2_buffer
	 * 
	 * The Record from data source 1 is in dsr1_next
	 * 
	 * If buffer_read_index is past the length of dsr2_buffer, then a 
	 * new Record from data source 1 needs to be read in to dsr1_next,
	 * buffer_read_index needs to be reset to zero, 
	 * and comparisons performed to see if the two are equal
	 * 
	 * If they are not equal, then the data sources must be read until
	 * the record objects returned are equal
	 */
	public Record[] getNextRecordPair(){
		Record[] ret = new Record[2];
		
		Record dsr1_rec = dsr1_next;
		if(buffer_read_index == dsr2_buffer.size()){
			buffer_read_index = 0;
			//dsr1_rec = dsr1.nextRecord();
			//System.out.println(c1++);
		}
		
		Record dsr2_rec;
		if(dsr2_buffer.size() == 0){
			dsr2_rec = null;
		} else {
			dsr2_rec = dsr2_buffer.get(buffer_read_index++);
		}
		
		if(dsr1_rec == null || dsr2_rec == null){
			return null;
		}
		
		int comparison = compareRecords(dsr1_rec, dsr2_rec);
		
		while(comparison != EQUAL){
			if(comparison == LESS_THAN){
				dsr1_rec = dsr1.nextRecord();
				dsr1_next = dsr1_rec;
			}else if(comparison == GREATER_THAN){
				fillDSR2Buffer();
				if(dsr2_buffer.size() == 0){
					// end of second data source
					return null;
				}
				dsr2_rec = dsr2_buffer.get(buffer_read_index++);
			}
			if(dsr1_rec == null || dsr2_rec == null){
				// end of records reached in one of the sources
				return null;
			}
			comparison = compareRecords(dsr1_rec, dsr2_rec);
			
		}
		
		ret[0] = dsr1_rec;
		ret[1] = dsr2_rec;
		
		if(buffer_read_index == dsr2_buffer.size()){
			dsr1_next = dsr1.nextRecord();
		}
		
		return ret;
	}
	
	/*
	 * Read from dsr2 as long as the blocking variable values are
	 * EQUAL
	 */
	private void fillDSR2Buffer(){
		dsr2_buffer.clear();
		if(dsr2_next != null){
			Record buffer_record = dsr2_next;
			dsr2_buffer.add(buffer_record);
			Record test_record = dsr2.nextRecord();
			
			while(test_record != null && compareRecords(buffer_record,test_record) == EQUAL){
				dsr2_buffer.add(test_record);
				test_record = dsr2.nextRecord();
			}
			dsr2_next = test_record;
		}
		buffer_read_index = 0;
		
	}
	
	/*
	 * Need to compare the blocking variables in order to determine if
	 * the set of strings is equal, greater than, or less than
	 */
	private int compareRecords(Record rec1, Record rec2){
		String[] block_cols = mc.getBlockingColumns();
		int ret = EQUAL;
		for(int i = 0; block_cols != null && i < block_cols.length; i++){
			String comp = block_cols[i];
			String str1 = rec1.getDemographic(comp);
			String str2 = rec2.getDemographic(comp);
			
			if(str1 == null){
				str1 = "";
			}
			if(str2 == null){
				str2 = "";
			}
			
			try{
				if(bel != null){
					// check if either value matches a regex to skip as a blocking value
					if(bel.isExcludeValue(comp, str1)){
						return LESS_THAN;
					} else if(bel.isExcludeValue(comp, str2)){
						return GREATER_THAN;
					}
				}
				ret = compareString(str1, str2, type_table.get(comp).intValue());
			}
			catch(ComparisonException ce){
				//System.err.println("comparison exception with " + rec1 + " and " + rec2);
			}
			if(ret != EQUAL){
				return ret;
			}
		}
		
		return EQUAL;
	}
	
	/*
	 * If the type is numeric, cast to a double and compare, else use the
	 * String object's comparison to determine equivalence.
	 */
	private int compareString(String str1, String str2, int type) throws ComparisonException{
		int ret;
		double d1, d2;
		
		if(str1.equals("") && str2.equals("")){
			// setting two null values as unequal prevents forming pairs of records
			// on empty blocking columns
			ret = LESS_THAN;
		} else if(type == MatchingConfig.NUMERIC_TYPE){
			try{
				d1 = Double.parseDouble(str1);
			}
			catch(NumberFormatException nfe){
				throw new ComparisonException("Number format exception");
			}
			try{
				d2 = Double.parseDouble(str2);
			}
			catch(NumberFormatException nfe){
				throw new ComparisonException("Number format exception");
			}
			if(d1 < d2){
				ret = LESS_THAN;
			} else if(d1 == d2){
				ret = EQUAL;
			} else {
				ret = GREATER_THAN;
			}
		} else if(type == MatchingConfig.STRING_TYPE){
			int comp = str1.compareToIgnoreCase(str2);
			if(comp < 0){
				ret = LESS_THAN;
			} else if(comp == 0){
				ret = EQUAL;
			} else {
				ret = GREATER_THAN;
			}
		} else {
			throw new ComparisonException("unknown type given when comparing Strings " + str1 + ", " + str2);
		}
		
		return ret;
	}
}
