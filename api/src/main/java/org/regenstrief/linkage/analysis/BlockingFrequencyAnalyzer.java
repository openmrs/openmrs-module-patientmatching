package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public class BlockingFrequencyAnalyzer extends FrequencyAnalyzer {
	protected final static String FIELD = "blocking_scheme";
	
	private final String[] blockingColumns;
	private final StringBuilder stringBuilder = new StringBuilder();
	
	public BlockingFrequencyAnalyzer(final LinkDataSource lds, final MatchingConfig mc, final DataSourceFrequency dsf) {
		super(lds, mc, dsf);
		blockingColumns = mc.getBlockingColumns();
	}
	
	@Override
	protected final void incrementCount(final Record rec) {
		stringBuilder.setLength(0);
		boolean first = true;
		for (final String bc : blockingColumns) {
			if (first) {
				first = false;
			} else {
				stringBuilder.append('|');
			}
			final String token = rec.getDemographic(bc);
			if (!BlockingHeuristicCalculator.isValidToken(token)) {
				counter.incrementCount(FIELD, "");
				return;
			}
			stringBuilder.append(token);
		}
		counter.incrementCount(FIELD, stringBuilder.toString());
	}
}
