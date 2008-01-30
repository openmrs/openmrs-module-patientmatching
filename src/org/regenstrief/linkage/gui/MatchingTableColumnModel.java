package org.regenstrief.linkage.gui;

import java.util.Enumeration;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.regenstrief.linkage.util.DataColumn;
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
	
	LinkDataSource lds;
	
	public MatchingTableColumnModel(LinkDataSource lds){
		super();
		this.lds = lds;
	}
	
	/**
	 * Method calls super class addColumn, but gets the
	 * TableColumn's name in order to set the DataColumn's
	 * include position correctly.
	 */
	public void addColumn(TableColumn tc){
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		super.addColumn(tc);
		dc.setIncludePosition(this.getColumnIndex(tc.getIdentifier()));
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
	public void removeColumn(TableColumn tc){
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		dc.setIncludePosition(DataColumn.INCLUDE_NA);
		super.removeColumn(tc);
	}
}
