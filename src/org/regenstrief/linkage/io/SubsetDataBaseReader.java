package org.regenstrief.linkage.io;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class implements a special case of reading from a database where the
 * records in the database will only form pairs against one Record.  Instead
 * of sorting the records by the blocking columns, an SQL query can retrieve
 * only the records that match the blocking columns of the record we're matching
 * against.
 * 
 * @author jegg
 *
 */

public class SubsetDataBaseReader extends OrderedDataBaseReader implements SubsetDataSourceReader{
	
	protected Record to_match;
	protected List<String> blocking_values;
	
	public SubsetDataBaseReader(LinkDataSource lds, Connection db, MatchingConfig mc, Record rec){
		super(lds, db, mc);
		to_match = rec;
		blocking_values = new ArrayList<String>();
	}
	
	protected void getResultSet(){
		try{
			if(!ready){
				ready = true;
				query = constructQuery();
				pstmt = db.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				
				// extra for loop in order to set ?'s in the query string
				for(int i = 0; i < blocking_values.size(); i++){
					String value = blocking_values.get(i);
					if(value == null){
						value = "";
					}
					pstmt.setString(i+1, value);
				}
				
			}
			//log.error("preparing query of " + pstmt);
			data = pstmt.executeQuery();
			if(data.next()){
				parseDataBaseRow();
			} else {
				next_record = null;
			}
			
		}
		catch(SQLException se){
			db = null;
		}
	}
	
	/**
	 * Overridden method adds a "WHERE" clause to the SQL to only get
	 * rows that match the blocking column values of the to_match Record.
	 */
	public String constructQuery(){
		String query = new String("SELECT ");
		incl_cols = new ArrayList<DataColumn>();
		Iterator<DataColumn> it = data_source.getDataColumns().iterator();
		String[] block_cols = mc.getBlockingColumns();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() != DataColumn.INCLUDE_NA){
				incl_cols.add(dc);
			}
		}
		
		for(int i = 0; i < incl_cols.size() - 1; i++){
			query += incl_cols.get(i).getName() + ", ";
		}
		
		query += incl_cols.get(incl_cols.size() - 1).getName();
		query += " FROM " + data_source.getName();
		
		query += " WHERE ";
		for(int i = 0; i < block_cols.length; i++){
			query += block_cols[i] + " = ? ";
			blocking_values.add(to_match.getDemographic(block_cols[i]));
			if(i < block_cols.length - 1){
				query += "AND ";
			}
		}
		
		query += " ORDER BY ";
		//String[] b_columns = mc.getBlockingColumns();
		for(int i = 0; i < block_cols.length - 1; i++){
			query += block_cols[i] + ", ";
		}
		query += block_cols[block_cols.length - 1];
		return query;
	}
}
