package org.regenstrief.linkage.analysis;

import java.util.Set;

public abstract class DataSourceFrequency {
	protected boolean finished = false;
	protected int total = 0;
	
	public boolean isFinished() {
		return finished;
	}
	
	public void setFinished(boolean f) {
		finished = f;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void incrementTotal() {
		total++;
	}
	
	public abstract int getFrequency(String field, String token);
	
	public abstract void setFrequency(String field, String token, int freq);
	
	public abstract void incrementCount(String field, String token);
	
	public abstract Set<String> getTokens(String field);
	
	public abstract Set<String> getFields();
}
