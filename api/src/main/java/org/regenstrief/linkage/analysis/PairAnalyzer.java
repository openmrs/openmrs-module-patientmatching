package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.Record;

public interface PairAnalyzer extends Analyzer {
	
	public void analyzeRecordPair(Record[] pair);
}
