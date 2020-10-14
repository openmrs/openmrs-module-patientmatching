package org.regenstrief.linkage.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class created to write MatchingConfig name in JList in SessionsPanel, since MatchingConfig's
 * toString() method returns a String too long for JList. Instead, this class will return the
 * MatchingConfig's getName() method results.
 * 
 * @author jegg
 */

public class MatchingConfigCellRenderer implements ListCellRenderer {
	
	private DefaultListCellRenderer dlcr;
	
	public MatchingConfigCellRenderer() {
		dlcr = new DefaultListCellRenderer();
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
	        boolean cellHasFocus) {
		String display;
		if (value instanceof MatchingConfig) {
			display = ((MatchingConfig) value).getName();
		} else {
			display = value.toString();
		}
		Component c = dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (c instanceof JLabel) {
			JLabel label = (JLabel) c;
			label.setText(display);
		}
		return c;
	}
	
}
