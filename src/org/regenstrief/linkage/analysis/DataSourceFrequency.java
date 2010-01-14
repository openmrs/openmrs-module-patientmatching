package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.util.LinkDataSource;

public abstract class DataSourceFrequency {
	protected LinkDataSource source;
	protected boolean finished;
	
	public DataSourceFrequency(LinkDataSource source){
		this.source = source;
		finished = false;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public void setFinished(boolean f){
		finished = f;
	}
	
	public abstract int getFrequency(String token);
	
	public abstract void incrementCount(String token);
}
