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
package org.regenstrief.linkage.io;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * ArrayDataSourceReader
 */
public class ArrayDataSourceReader implements DataSourceReader {
	
	public final static LinkDataSource SAMPLE_LINK_DATA_SOURCE = new LinkDataSource("Sample", "", "", 0);
	
	private final static ArrayDataSourceReader SAMPLE_DOE = new ArrayDataSourceReader(new String[][] {
		{ "ID", "LAST", "FIRST" },
		{ "1", "DOE", "JANE" },
		{ "2", "DOE", "JOHN" },
		{ "3", "DOE", "JANE" }
	});
	
	private final static ArrayDataSourceReader SAMPLE_DOE2 = new ArrayDataSourceReader(new String[][] {
		{ "ID", "LAST", "FIRST" },
		{ "4", "DOE", "JANET" },
		{ "5", "DOE", "JOHN" }
	});
	
	private final static ArrayDataSourceReader SAMPLE_MIX = new ArrayDataSourceReader(new String[][] {
		{ "ID", "LAST", "FIRST" },
		{ "6", "DOE", "JANET" },
		{ "7", "DOE", "JOHN" },
		{ "8", "SMITH", "JANET" },
		{ "9", "SMITH", "JOHN" },
		{ "10", "SMITH", "MARY" }
	});
	
	private final static ArrayDataSourceReader SAMPLE_NULL = new ArrayDataSourceReader(new String[][] {
		{ "ID", "LAST", "FIRST" },
		{ "11", "DOE", "JANE" },
		{ "12", "DOE", "" },
		{ "13", "DOE", null },
		{ "14", "", "" },
		{ "15", null, null }
	});
	
	private final static ArrayDataSourceReader SAMPLE_NULL2 = new ArrayDataSourceReader(new String[][] {
		{ "ID", "LAST", "FIRST" },
		{ "11", "DOE", "JANE" },
		{ "12", "DOE", "" },
		{ "13", "DOE", null }
	});
	
	private final Record[] records;
	
	private int currentIndex = 0;
	
	static{
		SAMPLE_LINK_DATA_SOURCE.setUniqueID("ID");
	}
	
	public ArrayDataSourceReader(final String[][] a) {
		final String[] header = a[0];
		final int numRows = a.length, numRecords = numRows - 1, numFields = header.length;
		this.records = new Record[numRecords];
		for (int i = 1; i < numRows; i++) {
			final Record r = new Record(i, null);
			final String[] row = a[i];
			this.records[i - 1] = r;
			for (int f = 0; f < numFields; f++) {
				r.addDemographic(header[f], row[f]);
			}
		}
	}
	
	@Override
	public int getRecordSize() {
		return records.length;
	}

	@Override
	public boolean hasNextRecord() {
		return currentIndex < records.length;
	}

	@Override
	public Record nextRecord() {
		final Record r = records[currentIndex];
		currentIndex++;
		return r;
	}

	@Override
	public boolean reset() {
		currentIndex = 0;
		return true;
	}

	@Override
	public boolean close() {
		return true;
	}
	
	public final static MatchingConfig newSampleMatchingConfig() {
		final MatchingConfig mc = new MatchingConfig("Sample", new String[] { "ID", "LAST", "FIRST" });
		mc.getMatchingConfigRowByName("LAST").setBlockOrder(1);
		mc.getMatchingConfigRowByName("FIRST").setInclude(true);
		return mc;
	}
	
	private final ArrayDataSourceReader init() {
		reset();
		return this;
	}
	
	public final static ArrayDataSourceReader getSampleDoe() {
		return SAMPLE_DOE.init();
	}
	
	public final static ArrayDataSourceReader getSampleDoe2() {
		return SAMPLE_DOE2.init();
	}
	
	public final static ArrayDataSourceReader getSampleMix() {
		return SAMPLE_MIX.init();
	}
	
	public final static ArrayDataSourceReader getSampleNull() {
		return SAMPLE_NULL.init();
	}
	
	public final static ArrayDataSourceReader getSampleNull2() {
		return SAMPLE_NULL2.init();
	}
}
