package org.regenstrief.linkage.io;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class extends DataBaseReader by taking a MatchingConfig object in the 
 * constructor providng Record order information in its blocking variable
 * information.  A different result set is obtained by overriding the
 * constructQuery() method.
 *
 */

public class OrderedDataBaseReader extends DataBaseReader  implements OrderedDataSourceReader{
	
	protected MatchingConfig mc;
	
	/**
	 * Constructs a reader, but returns the Records in order specified by the
	 * blocking variables.
	 * 
	 * @param lds	the description of the data
	 * @param mc	information on the record linkage options, containing blocking variable order (sort order)
	 */
	public OrderedDataBaseReader(LinkDataSource lds, Connection db, MatchingConfig mc){
		super(lds, db);
		this.mc = mc;
	}
	
	/**
	 * Overridden method adds an "ORDER BY" clause to the SQL to return the
	 * records ordered by blocking column.
	 */
	public String constructQuery(){
		String query = new String("SELECT ");
		incl_cols = new ArrayList<DataColumn>();
		Iterator<DataColumn> it = data_source.getDataColumns().iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				incl_cols.add(dc);
			}
		}
		
		for(int i = 0; i < incl_cols.size() - 1; i++){
			query += quote_string + incl_cols.get(i).getName() + quote_string + ", ";
		}
		
		query += incl_cols.get(incl_cols.size() - 1).getName();
		query += " FROM " + data_source.getName();
		query += " ORDER BY ";
		String[] b_columns = mc.getBlockingColumns();
		for(int i = 0; i < b_columns.length - 1; i++){
			query += quote_string + b_columns[i] + quote_string + ", ";
		}
		query += b_columns[b_columns.length - 1];
		return query;
	}
}
