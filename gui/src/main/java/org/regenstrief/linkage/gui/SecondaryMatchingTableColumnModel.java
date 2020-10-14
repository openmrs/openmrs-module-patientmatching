package org.regenstrief.linkage.gui;

import javax.swing.table.TableColumn;

import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class created because secondary JTable of the GUI, the bottom table, has different requirements
 * when a column is dragged. The column names should be anchored to the index, but include position
 * of the LinkDataSource columns be updated.
 * 
 * @author jegg
 */

public class SecondaryMatchingTableColumnModel extends MatchingTableColumnModel {
	
	public SecondaryMatchingTableColumnModel(LinkDataSource lds) {
		super(lds);
	}
	
	protected void updateLDS(int original_index, int new_index) {
		TableColumn tc = this.getColumn(new_index);
		int model_index = tc.getModelIndex();
		DataColumn dc = lds.getDataColumn(model_index);
		dc.setIncludePosition(new_index);
		if (Math.abs(original_index - new_index) == 1) {
			// two adjacent columns were swapped, so set include value for neighbor
			TableColumn tc2 = this.getColumn(original_index);
			int model_index2 = tc2.getModelIndex();
			DataColumn dc2 = lds.getDataColumn(model_index2);
			dc2.setIncludePosition(original_index);
			
			// swap names for dc and dc2, as well as table column header values and identifiers
			String name = dc.getName();
			int type = dc.getType();
			dc.setName(dc2.getName());
			dc.setType(dc2.getType());
			dc2.setName(name);
			dc2.setType(type);
			
			tc2.setHeaderValue(dc2.getName());
			tc2.setIdentifier(dc2.getName());
			
			tc.setHeaderValue(dc.getName());
			tc.setIdentifier(dc.getName());
		} else {
			syncIncludes();
		}
	}
}
