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
	
	protected final static class Count {
		
		protected int i; // java.lang.Integer is immutable; use our own mutable wrapper so we don't need to create a new instance each time we increment
		
		protected Count(final int i) {
			this.i = i;
		}
	}
}
