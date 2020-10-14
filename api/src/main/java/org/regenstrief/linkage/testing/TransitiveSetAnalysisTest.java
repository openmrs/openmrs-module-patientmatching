package org.regenstrief.linkage.testing;

import java.util.ArrayList;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.analysis.SetSimilarityAnalysis;

public class TransitiveSetAnalysisTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// create test Records
		Record r1 = new Record(1, "test");
		r1.addDemographic("name", "first");
		r1.addDemographic("age", "10");
		r1.addDemographic("sex", "female");
		
		Record r2 = new Record(2, "test");
		r2.addDemographic("name", "second");
		r2.addDemographic("age", "20");
		r2.addDemographic("sex", "female");
		
		Record r3 = new Record(3, "test");
		r3.addDemographic("name", "third");
		r3.addDemographic("age", "30");
		r3.addDemographic("sex", "female");
		
		Record r4 = new Record(4, "test");
		r4.addDemographic("name", "fourth");
		r4.addDemographic("age", "40");
		r1.addDemographic("sex", "female");
		
		Record r5 = new Record(5, "test");
		r5.addDemographic("name", "fifth");
		r5.addDemographic("age", "50");
		r5.addDemographic("sex", "female");
		
		// create MatchResult objects for analysis
		MatchResult mr1 = new MatchResult(10, 0, 0, 0, 0, 0, null, null, r1, r2, null);
		MatchResult mr2 = new MatchResult(5, 0, 0, 0, 0, 0, null, null, r3, r1, null);
		MatchResult mr3 = new MatchResult(15, 0, 0, 0, 0, 0, null, null, r4, r5, null);
		
		// put MatchResult objects in a List, as ScorePairs would create
		List<MatchResult> results = new ArrayList<MatchResult>();
		results.add(mr1);
		results.add(mr2);
		results.add(mr3);
		
		// test transitive set analysis object and methods
		SetSimilarityAnalysis ssa = new SetSimilarityAnalysis();
		//List<List<Record>> sets = ssa.getSimilarRecords(ssa.getSimilarSets(results));
		/*
		for(int i = 0; i < sets.size(); i++){
			System.out.println("Group " + i);
			List<Record> set = sets.get(i);
			for(int j = 0; j < set.size(); j++){
				Record r = set.get(j);
				System.out.println("\tRecord name:\t" + r.getDemographic("name"));
			}
		}*/
	}
	
}
