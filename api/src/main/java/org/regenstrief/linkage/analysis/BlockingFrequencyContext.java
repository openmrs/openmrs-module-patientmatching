package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public class BlockingFrequencyContext extends FrequencyContext {
	public BlockingFrequencyContext(final MatchingConfig mc, final LinkDataSource lds) {
		super(mc, lds);
	}
	
	public BlockingFrequencyContext(final MatchingConfig mc, final LinkDataSource lds, final DataSourceReader dsr) {
		super(mc, lds, dsr);
	}
	
	@Override
	protected final DataSourceFrequency newDataSourceFrequency() {
		return new SingleFieldDataSourceFrequency(BlockingFrequencyAnalyzer.FIELD);
	}
	
	@Override
	protected final FrequencyAnalyzer newFrequencyAnalyzer(final MatchingConfig mc, final LinkDataSource lds) {
		return new BlockingFrequencyAnalyzer(lds, mc, getFrequency());
	}
}
