package org.regenstrief.linkage.io;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public class OrderedDataBaseReader extends DataBaseReader {
	
	private MatchingConfig mc;
	
	public OrderedDataBaseReader(LinkDataSource lds, MatchingConfig mc){
		super(lds);
		this.mc = mc;
	}
	
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
			query += incl_cols.get(i).getName() + ", ";
		}
		
		query += incl_cols.get(incl_cols.size() - 1).getName();
		query += " FROM " + data_source.getName();
		query += " ORDER BY ";
		String[] b_columns = mc.getBlockingColumns();
		for(int i = 0; i < b_columns.length - 1; i++){
			query += b_columns[i] + ", ";
		}
		query += b_columns[b_columns.length - 1];
		return query;
	}
}
