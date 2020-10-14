package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.analysis.DataSourceFrequency;

public class MICalculator {
	
	private List<ColumnPair> column_pair_list = new ArrayList();
	
	public List<ColumnPair> getColumn_pair_list() {
		return column_pair_list;
	}
	
	public void setColumn_pair_list(List<ColumnPair> column_pair_list) {
		this.column_pair_list = column_pair_list;
	}
	
}
