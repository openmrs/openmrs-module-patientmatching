package org.regenstrief.linkage.analysis;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;

public abstract class RecordPairAnalyzer implements PairAnalyzer, MatchingConfigAnalyzer, LoggingObject {
	
	MatchingConfig mc;
	
	protected Logger log = Logger.getLogger(this.getClass() + this.toString());
	
	public RecordPairAnalyzer(MatchingConfig mc) {
		this.mc = mc;
		
		log.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
		log.setAdditivity(false);
		log.setLevel(Level.INFO);
	}
	
	public MatchingConfig getAnalyzerMatchingConfig() {
		return mc;
	}
	
	public abstract void analyzeRecordPair(Record[] pair);
	
	public Logger getLogger() {
		return log;
	}
}
