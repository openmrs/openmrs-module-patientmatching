package org.regenstrief.linkage.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ConfigFileFilter extends FileFilter {
	
	@Override
	public boolean accept(File arg0) {
		if (arg0.isDirectory()) {
			return true;
		}
		
		if (arg0.getName().endsWith(".xml")) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getDescription() {
		return ".xml files";
	}
	
}
