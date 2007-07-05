package org.regenstrief.linkage.io;

import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.*;
import java.util.*;

/**
 * This DataSourceReader holds one Record that is explicitly given
 * in the LinkDataSource object.
 *
 */

public class VectorReader extends DataSourceReader {
	
	private boolean accessed;
	private Record data;
	
	/**
	 * This constructor uses the same format as the DataSourceReader
	 * superclass and uses the information held within the LinkDataSource
	 * object to get field values directly.
	 * 
	 * @param lds	the LinkDataSource object with field values
	 * @param mc	the MatchingConfigObject to determine sort order
	 */
	public VectorReader(LinkDataSource lds, MatchingConfig mc){
		super(lds);
		accessed = false;
		data = buildRecord(lds);
	}
	
	public VectorReader(LinkDataSource lds){
		super(lds);
		accessed = false;
		data = buildRecord(lds);
	}
	
	/**
	 * An alternate method of creating the VectorReader object that will return
	 * the given Record instead of constructing it from a LinkDataSource object.
	 * 
	 * @param r	the Record to be returned when nextRecord() is called
	 */
	public VectorReader(Record r){
		super(null);
		accessed = false;
		data = r;
	}
	
	/**
	 * Returns the number of fields within the Record object.
	 */
	public int getRecordSize(){
		return data.getDemographics().size();
	}
	
	/*
	 * Gets the values from the LinkDataSource object and makes a Record object
	 * from it. 
	 * 
	 * @param lds	LinkDataSource with the field names and values
	 * @return	the Record holding the LinkDataSource information
	 */
	private Record buildRecord(LinkDataSource lds){
		Record data = new Record();
		Iterator<DataColumn> it = lds.getDataColumns().iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				data.addDemographic(dc.getName(), dc.getColumnID());
			}
		}
		
		return data;
	}
	
	/*
	 * can just use a flag to see if the one Record has been returned yet
	 */
	public boolean hasNextRecord() {
		return !accessed;
	}
	
	public Record nextRecord() {
		if(!accessed){
			accessed = true;
			return data;
		}
		return null;
	}
	
	public boolean reset() {
		accessed = false;
		return true;
	}

}
