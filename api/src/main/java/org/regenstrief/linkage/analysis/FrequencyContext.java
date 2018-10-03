/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.io.DataSourceReader;
import org.regenstrief.linkage.io.ReaderProvider;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * FrequencyContext
 */
public class FrequencyContext {
	private final DataSourceAnalysis dsa;
	private final DataSourceFrequency dsf;
	private final FrequencyAnalyzer fa;
	
	public FrequencyContext(final MatchingConfig mc, final LinkDataSource lds) {
		this(mc, lds, ReaderProvider.getInstance().getReader(lds));
	}
	
	public FrequencyContext(final MatchingConfig mc, final LinkDataSource lds, final DataSourceReader dsr) {
		if (dsr == null) {
			throw new NullPointerException("Could not obtain Reader");
		}
		this.dsa = new DataSourceAnalysis(dsr);
		this.dsf = newDataSourceFrequency();
		this.fa = newFrequencyAnalyzer(mc, lds);
		this.dsf.setDataSourceName(getDataSourceName());
	}
	
	protected DataSourceFrequency newDataSourceFrequency() {
		return new MemoryBackedDataSourceFrequency();
	}
	
	protected FrequencyAnalyzer newFrequencyAnalyzer(final MatchingConfig mc, final LinkDataSource lds) {
		return new FrequencyAnalyzer(lds, mc, dsf);
	}
	
	public final void analyzeData() {
		this.dsa.addAnalyzer(this.fa);
		this.dsa.analyzeData();
	}
	
	public final DataSourceFrequency getFrequency() {
		return this.dsf;
	}
	
	public final FrequencyAnalyzer getFrequencyAnalyzer() {
		return this.fa;
	}
	
	public final String getDataSourceName() {
		return fa.lds.getName();
	}
}
