package org.regenstrief.linkage.analysis;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;

public abstract class FrequencyBasedCalculator implements LoggingObject {
	protected static final Logger log = Logger.getLogger(FrequencyBasedCalculator.class);
	
	public final void calculateDedup(final MatchingConfig mc, final FrequencyContext fc) {
		try {
			fc.analyzeData();
			calculateDedup(mc, fc.getFrequency());
		} finally {
			fc.close();
		}
	}
	
	public abstract void calculateDedup(final MatchingConfig mc, final DataSourceFrequency freq);
	
	public final void calculate(final MatchingConfig mc, final FrequencyContext fc1, final FrequencyContext fc2) {
		try {
	    	fc1.analyzeData();
	    	fc2.analyzeData();
			calculate(mc, fc1.getFrequency(), fc2.getFrequency());
		} finally {
			fc1.close();
			fc2.close();
		}
	}
	
	public abstract void calculate(final MatchingConfig mc, final DataSourceFrequency freq1, final DataSourceFrequency freq2);
	
	protected static final long countPairs(final long numRecords) {
		return (numRecords * (numRecords - 1)) / 2;
	}
	
	protected final static boolean isValidToken(final String token) {
		return (token != null) && !token.equals("");
	}
	
	@Override
	public Logger getLogger() {
		return log;
	}
}
