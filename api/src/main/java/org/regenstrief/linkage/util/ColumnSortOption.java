package org.regenstrief.linkage.util;
/*
 * Class holds options for the ColumnSorter object
 * Needs to keep track of the index of the column,
 * the ascending/descending order of the sort, and
 * whether the column is numerical or textual
 * 
 */

public class ColumnSortOption {
	
	public static final int NUMERIC = 0;
	
	public static final int TEXT = 1;
	
	public static final int ASCENDING = 2;
	
	public static final int DESCENDING = 3;
	
	public int column_index, sort_order, data_type;
	
	public ColumnSortOption(int ci, int order, int type) {
		column_index = ci;
		sort_order = order;
		data_type = type;
	}
	
	public void setType(int t) {
		data_type = t;
	}
	
	public void setOrder(int order) {
		sort_order = order;
	}
	
	public void setIndex(int index) {
		column_index = index;
	}
	
	public int getType() {
		return data_type;
	}
	
	public int getOrder() {
		return sort_order;
	}
	
	public int getIndex() {
		return column_index;
	}
	
	public String toString() {
		String t, o;
		if (data_type == NUMERIC) {
			t = "numeric";
		} else {
			t = "text";
		}
		if (sort_order == ASCENDING) {
			o = "ascending";
		} else {
			o = "descending";
		}
		String s = "Column index " + column_index + ", data is " + t + ", order is " + o;
		return s;
	}
	
}
