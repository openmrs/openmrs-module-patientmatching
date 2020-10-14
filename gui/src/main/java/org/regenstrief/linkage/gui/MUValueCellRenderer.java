package org.regenstrief.linkage.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MUValueCellRenderer extends DefaultTableCellRenderer {
	
	public MUValueCellRenderer() {
		super();
	}
	
	public void setValue(Object value) {
		super.setValue(value);
		if (value != null && value instanceof Double) {
			Double d = (Double) value;
			setText(d.toString());
		}
		
	}
}
