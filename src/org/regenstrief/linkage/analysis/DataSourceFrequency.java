package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.regenstrief.linkage.util.LinkDataSource;

public abstract class DataSourceFrequency {
	protected boolean finished;
	protected Map<String,Integer> totals;
	
	public DataSourceFrequency(){
		finished = false;
		totals = new Hashtable<String,Integer>();
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public void setFinished(boolean f){
		finished = f;
	}
	
	public int getFrequenciesSum(String field){
		return totals.get(field);
	}
	
	public abstract int getTotal(String field);
	
	public abstract int getFrequency(String field, String token);
	
	public abstract void setFrequency(String field, String token, int freq);
	
	public abstract void incrementCount(String field, String token);
	
	public abstract Set<String> getTokens(String field);
	
	public abstract Set<String> getFields();
}
