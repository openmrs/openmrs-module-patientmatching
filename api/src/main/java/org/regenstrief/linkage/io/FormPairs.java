package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.BlockingExclusionList;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class takes a MatchingConfig and returns Record pairs. Subclasses will need to define data source
 * types and implement getNextRecordPair().
 * 
 * @author jegg
 */

public abstract class FormPairs {
	
	protected MatchingConfig mc;
	
	protected BlockingExclusionList bel;
	
	public FormPairs(MatchingConfig mc) {
		this.mc = mc;
	}
	
	public BlockingExclusionList getBlockingExclutionList() {
		return bel;
	}
	
	public void setBlockingExclusionList(BlockingExclusionList b) {
		bel = b;
	}
	
	public abstract Record[] getNextRecordPair();
	
	public MatchingConfig getMatchingConfig() {
		return mc;
	}
}
