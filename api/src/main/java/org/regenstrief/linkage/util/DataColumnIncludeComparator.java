package org.regenstrief.linkage.util;

import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.table.TableColumn;

public class DataColumnIncludeComparator implements Comparator<TableColumn> {
	
	LinkDataSource lds;
	
	public DataColumnIncludeComparator(LinkDataSource lds) {
		this.lds = lds;
	}
	
	public int compare(TableColumn tc, TableColumn tc2) {
		Hashtable<String, DataColumn> include_columns = lds.getIncludedDataColumns();
		String header1 = tc.getHeaderValue().toString();
		String header2 = tc2.getHeaderValue().toString();
		Integer tc_include = include_columns.get(header1).getIncludePosition();
		Integer tc_include2 = include_columns.get(header2).getIncludePosition();
		
		return tc_include.compareTo(tc_include2);
	}
}
