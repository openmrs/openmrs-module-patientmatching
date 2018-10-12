package org.regenstrief.linkage.analysis;

import java.util.Iterator;
import java.util.Set;

public abstract class DataSourceFrequency {
	protected boolean finished = false;
	protected int total = 0;
	protected String dataSourceName = null;
	
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
	
	public String getDataSourceName() {
		return dataSourceName;
	}
	
	public void setDataSourceName(final String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	
	public abstract int getFrequency(final String field, final String token);
	
	public abstract int removeFrequency(final String field, final String token);
	
	public abstract void setFrequency(final String field, final String token, final int freq);
	
	public abstract void incrementCount(final String field, final String token);
	
	public abstract Set<String> getTokens(final String field);
	
	public Iterator<String> getTokenIterator(final String field) {
		return getTokens(field).iterator();
	}
	
	public abstract Set<String> getFields();
	
	protected final static class Count {
		
		protected int i; // java.lang.Integer is immutable; use our own mutable wrapper so we don't need to create a new instance each time we increment
		
		protected Count(final int i) {
			this.i = i;
		}
	}
}
