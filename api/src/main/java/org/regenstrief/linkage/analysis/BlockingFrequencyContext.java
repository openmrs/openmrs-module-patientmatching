package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

public class BlockingFrequencyContext extends FrequencyContext {
	
	private List<SingleFieldSqliteDataSourceFrequency> listToClose = null;
	
	public BlockingFrequencyContext(final MatchingConfig mc, final LinkDataSource lds) {
		super(mc, lds);
	}
	
	public BlockingFrequencyContext(final MatchingConfig mc, final LinkDataSource lds, final DataSourceReader dsr) {
		super(mc, lds, dsr);
	}
	
	@Override
	protected final DataSourceFrequency newDataSourceFrequency() {
		if ("true".equalsIgnoreCase(
		    System.getProperty("org.regenstrief.linkage.analysis.BlockingFrequencyContext.memoryBacked"))) {
			return new SingleFieldDataSourceFrequency(BlockingFrequencyAnalyzer.FIELD);
		} else {
			final SingleFieldSqliteDataSourceFrequency freq = new SingleFieldSqliteDataSourceFrequency(
			        BlockingFrequencyAnalyzer.FIELD);
			if (listToClose == null) {
				listToClose = new ArrayList<SingleFieldSqliteDataSourceFrequency>(2);
			}
			listToClose.add(freq);
			return freq;
		}
	}
	
	@Override
	protected final FrequencyAnalyzer newFrequencyAnalyzer(final MatchingConfig mc, final LinkDataSource lds) {
		return new BlockingFrequencyAnalyzer(lds, mc, getFrequency());
	}
	
	@Override
	public final void close() {
		if (listToClose != null) {
			for (final SingleFieldSqliteDataSourceFrequency freq : listToClose) {
				freq.close();
			}
		}
	}
}
