package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.io.FormPairs;

public class PairDataSourceAnalysis {
	
	List<PairAnalyzer> analyzers;
	FormPairs fp;
	
	public PairDataSourceAnalysis(FormPairs fp){
		this.fp = fp;
		analyzers = new ArrayList<PairAnalyzer>();
	}
	
	public void addAnalyzer(PairAnalyzer a){
		analyzers.add(a);
	}
	
	public List<PairAnalyzer> getAnalyzers(){
		return analyzers;
	}
	
	public void analyzeData(){
		Record[] pair;
		while((pair = fp.getNextRecordPair()) != null){
			for(int i = 0; i < analyzers.size(); i++){
				// pass Record pairs to analyzer object
				PairAnalyzer a = analyzers.get(i);
				a.analyzeRecordPair(pair);
			}
		}
		
		// reading input is finished, give analyzer objects a chance to
		// finalize analysis
		for(int i = 0; i < analyzers.size(); i++){
			analyzers.get(i).finishAnalysis();
		}
		
	}
}
