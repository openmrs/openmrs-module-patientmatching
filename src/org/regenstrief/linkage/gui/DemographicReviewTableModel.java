package org.regenstrief.linkage.gui;

import javax.swing.table.AbstractTableModel;

public class DemographicReviewTableModel extends AbstractTableModel {

	Object[][] data;
	String[] demographics;
	
	public DemographicReviewTableModel(Object[][] data, String[] demographics){
		this.data = data;
		this.demographics = demographics;
	}
	
	public int getColumnCount() {
		return data[0].length;
	}

	public int getRowCount() {
		return 2;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	public String getColumnName(int colnum){
		return demographics[colnum];
	}

}
