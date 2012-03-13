package org.regenstrief.linkage.io;

/**
 * Class returns Record objects from the data source in order as sorted
 * on blocking columns defined in the MatchingConfig
 */

import org.regenstrief.linkage.Record;

public interface DataSourceReader {
	
	
	/**
	 * 
	 * @return	the number of fields in the Record that is included in this record linking
	 */
	public int getRecordSize();
	
	
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
	
	/**
	 * Method releases resources held by the reader, such as
	 * file handles or database connections.  As written, once closed
	 * a DataSourceReader cannot be reopened.
	 * 
	 * @return
	 */
	public abstract boolean close();

}
