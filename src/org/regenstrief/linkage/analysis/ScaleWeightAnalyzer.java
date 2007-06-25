package org.regenstrief.linkage.analysis;

import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.*;

/**
 * Class implements a scale-weight algorithm to analyze
 * the Records given to it
 *
 */

public class ScaleWeightAnalyzer extends Analyzer {
	
	private LinkDataSource data_source1;
	private LinkDataSource data_source2;
	
	private String[] ds1_sw_indices;
	private String[] ds2_sw_indices;
	
	public ScaleWeightAnalyzer(RecMatchConfig rmc, MatchingConfig analytics){
		data_source1 = rmc.getLinkDataSource1();
		data_source2 = rmc.getLinkDataSource2();
		
		String [] sw_column_names = analytics.getScaleWeightColumns();
		ds1_sw_indices = data_source1.getColumnIDsofColumnNames(sw_column_names);
		ds2_sw_indices = data_source2.getColumnIDsofColumnNames(sw_column_names);
		
	}
	
	
	public void analyzeRecord(Record rec){
	
	}
	
	public void finishAnalysis() {
		
	}

}
