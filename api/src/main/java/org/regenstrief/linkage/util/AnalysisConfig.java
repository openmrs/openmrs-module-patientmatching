package org.regenstrief.linkage.util;

import java.util.HashMap;

/**
 * A simple class to store settings regarding Analyzers TODO: Document possible analyzer names
 * 
 * @author scentel
 */

public class AnalysisConfig {
	
	private HashMap<String, String> settings;
	
	public AnalysisConfig() {
		settings = new HashMap<String, String>();
	}
	
	public AnalysisConfig(String analyzer_name, String init) {
		settings = new HashMap<String, String>();
		addInitString(analyzer_name, init);
	}
	
	public boolean doesAnalysisExist(String analyzer_name) {
		return (settings.get(analyzer_name) == null);
	}
	
	public void addInitString(String analyzer_name, String init) {
		settings.put(analyzer_name, init);
	}
	
	public String getInitString(String analyzer_name) {
		return settings.get(analyzer_name);
	}
	
	public HashMap<String, String> getSettings() {
		return settings;
	}
	
}
