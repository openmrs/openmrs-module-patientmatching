package org.regenstrief.linkage.gui;

import javax.swing.table.AbstractTableModel;

import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;

/*
 * Used to display the session options for the 
 */

public class SessionOptionsTableModel extends AbstractTableModel{
	MatchingConfig mc;
	String row_names[];
	
	public static final Class[] column_classes = {String.class, Integer.class, Integer.class, Boolean.class, Double.class, Double.class, Object.class};
	public static final String[] column_names = {"Name", "Block Order", "Block Chars", "Include", "m-value", "u-value", "Algorithm"};
	
	
	public SessionOptionsTableModel(MatchingConfig m){
		// setup table to follow the columns defined in MatchingConfig
		// rows contain the names of the rows to currently display
		mc = m;
		if(m == null){
			row_names = null;
		} else {
			row_names = mc.getRowNames();
		}
		
	}
	
	public SessionOptionsTableModel(){
		mc = null;
		row_names = null;
	}
	
	public int getOffset(){
		// one column is displayed, but not in the table model for the name
		return 1;
	}
	
	public MatchingConfig getConfigData(){
		// gets the data behind the table model to place in list of runs or to
		// write information to a file
		return mc;
	}
	
	public void setValueAt(Object value, int row, int col) {
		MatchingConfigRow mcr = mc.getMatchingConfigRows().get(row);
		if(col == 1 && value instanceof Integer){
			Integer i = (Integer)value;
			mcr.setBlockOrder(i.intValue());
		} else if(col == 2 && value instanceof Integer){
			Integer i = (Integer)value;
			mcr.setBlockChars(i.intValue());
		} else if(col == 3 && value instanceof Boolean){
			Boolean b = (Boolean)value;
			mcr.setInclude(b.booleanValue());
			if(b){
				mcr.setBlockOrder(MatchingConfigRow.DEFAULT_BLOCK_ORDER);
			}
		} else if(col == 4 && value instanceof Double){
			Double d = (Double)value;
			if(d > 0.99999){
				d = 0.99999;
			} else if(d < 0.00001){
				d = 0.00001;
			}
			mcr.setAgreement(d.doubleValue());
		} else if(col == 5 && value instanceof Double){
			Double d = (Double)value;
			if(d > 0.99999){
				d = 0.99999;
			} else if(d < 0.00001){
				d = 0.00001;
			}
			mcr.setNonAgreement(d.doubleValue());
		} else if(col == 6){
			if(value.equals(MatchingConfig.ALGORITHMS[0])){
				mcr.setAlgorithm(0);
			} else if(value.equals(MatchingConfig.ALGORITHMS[1])){
				mcr.setAlgorithm(1);
			} else if(value.equals(MatchingConfig.ALGORITHMS[2])){
				mcr.setAlgorithm(2);
			} else if(value.equals(MatchingConfig.ALGORITHMS[3])){
				mcr.setAlgorithm(3);
			} else {
				mcr.setAlgorithm(0);
			}
			
		}
		fireTableCellUpdated(row, col);
		
    }
	
	public String getColumnName(int c){
		return column_names[c];
	}
	
	 public boolean isCellEditable(int row, int col){
	 	// first column is only to be viewed
	 	if(col == 0){// || col == 6){
	 		return false;
	 	} else {
	 		return true;
	 	}
	 }
	
	public Class getColumnClass(int c) {
        return column_classes[c];
    }
	
	public int getColumnCount(){
		return column_classes.length;
	}
	
	public Object getValueAt(int row, int c){
		MatchingConfigRow mcr = mc.getMatchingConfigRows().get(row);
		// dependong on column, will need to call different methods
		// if column:
		// 0 is name
		// 1 is block order
		// 2 is block chars
		// 3 is include
		// 4 is T Agreement
		// 5 is Non-Agreement
		// 6 is algorithm
		switch(c){
		case 0:
			return mcr.getName();
		case 1:
			return mcr.getBlockOrder();
		case 2:
			return mcr.getBlockChars();
		case 3:
			return new Boolean(mcr.isIncluded());
		case 4:
			return new Double(mcr.getAgreement());
		case 5:
			return new Double(mcr.getNonAgreement());
		case 6:
			return MatchingConfig.ALGORITHMS[mcr.getAlgorithm()];
		}
		return null;
		
	}
	
	public int getRowCount(){
		if(mc == null){
			return 0;
		} else {
			return row_names.length;
		}
	}
}
