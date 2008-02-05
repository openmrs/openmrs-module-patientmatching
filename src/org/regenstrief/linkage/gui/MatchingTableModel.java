package org.regenstrief.linkage.gui;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.BasicCharDelimFileReader;
import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

/*
 * First ten lines of files are displayed,
 * to give user idea of the data to set the
 * options for the program
 */

public class MatchingTableModel extends AbstractTableModel {
	
	public static final int STRING = DataColumn.STRING_TYPE;
	public static final int NUMBER = DataColumn.NUMERIC_TYPE;
	
	private LinkDataSource lds;
	private int raw_lines;
	
	// display data stored in a hash where first key is row number, second demographic of
	// data column
	private Hashtable<Integer, Hashtable<Integer, String>> display_data;
	
	public MatchingTableModel(LinkDataSource lds){
		this.lds = lds;
		display_data = new Hashtable<Integer, Hashtable<Integer, String>>();
		raw_lines = 0;
		getLines();
	}
	
	private void getLines(){
		ReaderProvider rp = new ReaderProvider();
		// need to get a BasicCharDelimFileReader instead of a CharDelimFileReader
		// to get more information about raw data
		DataSourceReader dsr;
		if(lds.getType().equals("CharDelimFile")){
			dsr = new BasicCharDelimFileReader(lds);
		} else {
			dsr = rp.getReader(lds);
		}
		
		List<DataColumn> dcs = lds.getDataColumns();
		Iterator<DataColumn> it = dcs.iterator();
		String[] dc_names = new String[lds.getDataColumns().size()];
		int col = 0;
		while(it.hasNext()){
			DataColumn dc = it.next();
			dc_names[col] = dc.getName();
			col++;
		}
		
		int read_lines = 0;
		while(dsr.hasNextRecord() && read_lines < DataPanel.ROWS_IN_TABLE){
			Record r = dsr.nextRecord();
			for(int i = 0; i < dc_names.length; i++){
				String col_id = dc_names[i];
				String demographic = r.getDemographic(col_id);
				Hashtable<Integer, String> row_hash = display_data.get(new Integer(read_lines));
				if(row_hash == null){
					row_hash = new Hashtable<Integer,String>();
					display_data.put(new Integer(read_lines), row_hash);
				}
				row_hash.put(i, demographic);
			}
			read_lines++;
		}
		raw_lines = read_lines;
	}
	
	public String getColumnName(int c){
		// if it's been set, use the user-defined name
		// else, return the default of super class
		
		DataColumn dc = lds.getDataColumn(c);
		if(dc == null){
			return super.getColumnName(c);
		} else {
			String name = dc.getName();
			if(name == null){
				return super.getColumnName(c);
			}
			else return name;
		}
		
	}
	
	public void setColumnName(String s, int col){
		if(s == null){
			// grab name from super class, and use this default in 
			// LinkDataSource object as well
			s = super.getColumnName(col);
		}
		DataColumn dc = lds.getDataColumn(col);
		dc.setName(s);
	}
	
	public Object getValueAt(int row, int c){
		Hashtable<Integer,String> row_hash = display_data.get(new Integer(row));
		String entry = row_hash.get(new Integer(c));
		
		return entry;
	}
	
	public void setHidden(int col, boolean hide){
		lds.getDataColumn(col).setIncludePosition(DataColumn.INCLUDE_NA);
	}
	
	public boolean isHidden(int col){
		return lds.getDataColumn(col).getIncludePosition() == DataColumn.INCLUDE_NA;
	}
	
	public void setDataType(int c, int type){
		lds.getDataColumn(c).setType(type);
	}
	
	public int getDataType(int col){
		return lds.getDataColumn(col).getType();
	}
	
	public boolean isNumberType(int col){
		return lds.getDataColumn(col).getType() == DataColumn.NUMERIC_TYPE;
	}
	
	public boolean isStringType(int col){
		return lds.getDataColumn(col).getType() == DataColumn.STRING_TYPE;
	}
	
	public Class getColumnClass(int c) {
        return new String().getClass(); 
    }
	
	public int getRowCount(){
		return raw_lines;
	}
	
	public int getColumnCount(){
		return lds.getDataColumns().size();
	}
	
	 public boolean isCellEditable(int row, int col){
	 	// data is only to be viewed, not manipulated for now
	 	return false;
	 }
}
