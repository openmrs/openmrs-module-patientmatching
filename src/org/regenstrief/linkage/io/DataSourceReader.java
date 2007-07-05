package org.regenstrief.linkage.io;

/**
 * Class returns Record objects from the data source in order as sorted
 * on blocking columns defined in the MatchingConfig
 */

import org.regenstrief.linkage.util.*;
import org.regenstrief.linkage.*;
import java.util.*;

public abstract class DataSourceReader {
	
	public LinkDataSource data_source;
	
	public DataSourceReader(LinkDataSource lds){
		this.data_source = lds;
	}
	
	/**
	 * 
	 * @return	the number of fields in the Record that is included in this record linking
	 */
	public int getRecordSize(){
		int ret = 0;
		List<DataColumn> columns = data_source.getDataColumns();
		Iterator<DataColumn> it = columns.iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(!dc.getName().equals(DataColumn.INCLUDE_NA)){
				ret++;
			}
		}
		return ret;
	}
	

	
	/**
	 * 
	 * @return	if the data source has more Records left
	 */
	public abstract boolean hasNextRecord();
	
	/**
	 * 
	 * @return the next Record object from the LinkDataSource
	 */
	public abstract Record nextRecord();
	
	/**
	 * Resets the DataSourceReader to start returning Records in order from the
	 * first Record.
	 * 
	 * @return	true if the reset was successful, false if otherwise
	 */
	public abstract boolean reset();

}
