package org.regenstrief.linkage.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MatchResultReviewTableCellRenderer extends DefaultTableCellRenderer {
	
	private boolean[] matches;
	
	public static Color MATCH_COLOR = new Color(224, 241, 224);
	
	public static Color NONMATCH_COLOR = new Color(255, 242, 242);
	
	public MatchResultReviewTableCellRenderer(boolean[] matches) {
		this.matches = matches;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
	        int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (matches[column]) {
			c.setBackground(MATCH_COLOR);
		} else {
			c.setBackground(NONMATCH_COLOR);
		}
		return c;
	}
}
