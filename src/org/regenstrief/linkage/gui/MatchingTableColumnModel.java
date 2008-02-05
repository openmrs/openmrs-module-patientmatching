package org.regenstrief.linkage.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.DataColumnIncludeComparator;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class extends the default table column model class to provide
 * a direct link between the GUI to configure the datasource and
 * the LinkDatasource object.  Where the MatchingTableModel class
 * implements the data model of the JTable and stores options of
 * data source type and raw data column positioning, this class
 * overrides methods to keep the include position field of the
 * DataColumn object current.
 * 
 * @author jegg
 *
 */

public class MatchingTableColumnModel extends DefaultTableColumnModel {
	
	protected LinkDataSource lds;
	protected Hashtable<String,TableColumn> hidden_columns;
	
	protected DataColumnIncludeComparator column_comparator;
	
	public MatchingTableColumnModel(LinkDataSource lds){
		super();
		this.lds = lds;
		
		hidden_columns = new Hashtable<String,TableColumn>();
		column_comparator = new DataColumnIncludeComparator(lds);
	}
	
	protected void removeNonIncludedColumns(){
		Iterator<DataColumn> it = lds.getDataColumns().iterator();
		while(it.hasNext()){
			DataColumn dc = it.next();
			if(dc.getIncludePosition() == DataColumn.INCLUDE_NA){
				String dc_name = dc.getName();
				Enumeration<TableColumn> e = this.getColumns();
				while(e.hasMoreElements()){
					TableColumn tc = e.nextElement();
					if(tc.getHeaderValue().equals(dc_name)){
						removeColumn(tc);
						hidden_columns.put(dc_name, tc);
					}
				}
			}
			
		}
	}
	
	/**
	 * Method calls super class addColumn, but gets the
	 * TableColumn's name in order to set the DataColumn's
	 * include position correctly.
	 */
	public void addColumn(TableColumn tc){
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		
		// set identifier object if not set yet
		if(tc.getIdentifier() == null){
			tc.setIdentifier(dc.getName());
		}
		
		if(dc.getIncludePosition() == DataColumn.INCLUDE_NA){
			hidden_columns.put(dc.getName(), tc);
		} else {
			super.addColumn(tc);
			Collections.sort(tableColumns, new DataColumnIncludeComparator(lds));
		}
	}
	
	public void unHideColumn(String col_name){
		TableColumn tc = hidden_columns.get(col_name);
		if(tc != null){
			hidden_columns.remove(col_name);
			DataColumn dc = lds.getDataColumn(tc.getModelIndex());
			dc.setIncludePosition(tableColumns.size());
			addColumn(tc);
		}
	}
	
	/**
	 * When method is called, need to update the include value
	 * for the DataColumn in the LinkDataSource to reflect the
	 * new placement.
	 */
	public void moveColumn(int original_index, int new_index){
		super.moveColumn(original_index, new_index);
		if(original_index != new_index){
			updateLDS(original_index, new_index);
		}
	}
	
	protected void updateLDS(int original_index, int new_index){
		TableColumn tc = this.getColumn(new_index);
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		dc.setIncludePosition(new_index);
		if(Math.abs(original_index - new_index) == 1){
			// two adjacent columns were swapped, so set include value for neighbor
			TableColumn tc2 = this.getColumn(original_index);
			int model_index2 = tc2.getModelIndex();
			DataColumn dc2 = lds.getDataColumn(model_index2);
			dc2.setIncludePosition(original_index);
		} else {
			syncIncludes();
		}
	}
	
	/**
	 * Method added for when non-adjacent columns get moved
	 * 
	 */
	protected void syncIncludes(){
		Enumeration<TableColumn> e = getColumns();
		while(e.hasMoreElements()){
			TableColumn tc = e.nextElement();
			int model_index = tc.getModelIndex();
			DataColumn dc = lds.getDataColumn(model_index);
			int include_index = this.getColumnIndex(tc.getIdentifier());
			dc.setIncludePosition(include_index);
		}
			
	}
	
	/**
	 * When method is called, set the include position to
	 * INCLUDE_NA in the DataColumn to update the LinkDataSource
	 * object to show column is no longer included in the analysis.
	 */
	public void hideColumn(TableColumn tc){
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		dc.setIncludePosition(DataColumn.INCLUDE_NA);
		hidden_columns.put(tc.getHeaderValue().toString(), tc);
		super.removeColumn(tc);
	}
	
	public List<String> getHiddenColumns(){
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(hidden_columns.keySet());
		return ret;
	}
}
