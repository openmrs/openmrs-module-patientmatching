package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.*;
import java.util.*;

/**
 * Class analyzes a data source by iterating through the Records
 * and passing each to the Analyzer objects it has
 * 
 */

public class DataSourceAnalysis {
	List<DataSourceAnalyzer> analyzers;
	DataSourceReader data_reader;
	
	/**
	 * Constructor initializes an empty List of Analyzer objects
	 * 
	 * @param data_reader	the source of Records that will be analyzed
	 */
	public DataSourceAnalysis(DataSourceReader data_reader){
		this.data_reader = data_reader;
		analyzers = new ArrayList<DataSourceAnalyzer>();
	}
	
	/**
	 * Adds the given Analyzer object to the list of Analyzers
	 * 
	 * @param a	an Analyzer object that will analyze the records of the data source
	 */
	public void addAnalyzer(DataSourceAnalyzer a){
		analyzers.add(a);
	}
	
	/**
	 * Returns the list of Analyzers the DataSourceAnalysis has
	 * 
	 * @return	the List of Analyzers
	 */
	public List<DataSourceAnalyzer> getAnalyzers(){
		return analyzers;
	}
	
	/**
	 * Method iterates through the data source reader and for each
	 * Record read, passes it to the Analyzers it has.  When all
	 * Record objects have been read it calls the finishAnalysis()
	 * method for each Analyzer to complete the analysis
	 *
	 */
	public void analyzeData(){
		while(data_reader.hasNextRecord()){
			Record r = data_reader.nextRecord();
			for(int i = 0; i < analyzers.size(); i++){
				// pass Record object to analyzer object
				DataSourceAnalyzer a = analyzers.get(i);
				a.analyzeRecord(r);
			}
		}
		
		// reading input is finished, give analyzer objects a chance to
		// finalize analysis
		for(int i = 0; i < analyzers.size(); i++){
			analyzers.get(i).finishAnalysis();
		}
		
	}
	
}
