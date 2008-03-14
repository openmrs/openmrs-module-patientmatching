package org.regenstrief.linkage.analysis;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public abstract class RecordPairAnalyzer implements PairAnalyzer{
	
	LinkDataSource lds1, lds2;
	MatchingConfig mc;
	protected Logger log = Logger.getLogger(this.getClass() + this.toString());
	
	public RecordPairAnalyzer(LinkDataSource lds1, LinkDataSource lds2, MatchingConfig mc){
		this.lds1 = lds1;
		this.lds2 = lds2;
		this.mc = mc;
		
		log.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
		log.setAdditivity(false);
		log.setLevel(Level.INFO);
	}
	
	public abstract void analyzeRecordPair(Record[] pair);
	
}
