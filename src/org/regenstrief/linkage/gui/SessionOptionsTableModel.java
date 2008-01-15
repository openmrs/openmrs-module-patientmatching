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
	
	public static final Class[] column_classes = {String.class, Integer.class, Integer.class, Boolean.class, Double.class, Double.class, String.class};
	public static final String[] column_names = {"Name", "Block Order", "Blck Chars", "Include", "T Agreement", "Non-Agreement", "Algorithm"};
	
	
	public SessionOptionsTableModel(MatchingConfig m){
		// setup table to follow the columns defined in MatchingConfig
		// rows contain the names of the rows to currently display
		mc = m;
		row_names = mc.getRowNames();
		
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
		} else if(col == 4 && value instanceof Double){
			Double d = (Double)value;
			mcr.setAgreement(d.doubleValue());
		} else if(col == 5 && value instanceof Double){
			Double d = (Double)value;
			mcr.setNonAgreement(d.doubleValue());
		} else if(col == 6){
			
		}
		fireTableCellUpdated(row, col);
		
    }
	
	public String getColumnName(int c){
		return column_names[c];
	}
	
	 public boolean isCellEditable(int row, int col){
	 	// first column is only to be viewed
	 	if(col == 0 || col == 6){
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
