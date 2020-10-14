package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Interface defines a store of Records from a DataSourceReader that can also create a Reader to
 * access the Records it has encountered.
 * 
 * @author jegg
 */

public interface RecordStore {
	
	public boolean storeRecord(Record r);
	
	public LinkDataSource getRecordStoreLinkDataSource();
}
