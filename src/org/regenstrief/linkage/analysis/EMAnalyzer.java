package org.regenstrief.linkage.analysis;

import org.regenstrief.linkage.*;
import org.regenstrief.linkage.io.*;
import org.regenstrief.linkage.util.*;
import java.util.*;
import java.io.*;

/**
 * Class was originally extending Analyzer, but EM analyzes
 * pairs of records, not a record data source, so it
 * no longer is a subclass.  It also modifies the MatchingConfig
 * object given in the analyzeRecordPairs method with new
 * agreement and non-agreement values for included columns as
 * a side effect
 */

public class EMAnalyzer { //extends Analyzer {
	final static double INIT_MEST = 0.9;
	final static double INIT_UEST = 0.1;
	final static int INIT_COMP = 0;
	
	// use approximate values close to zero and one
	// due to the math used in the record linking
	final static double EM_ONE = 0.99999;
	final static double EM_ZERO = 0.00001;
	
	final static int ITERATIONS = 3;
	
	private int record_size;
	//private Hashtable<MatchVector,Integer> vector_count;
	private List<MatchVector> vector_list;
	
	/**
	 * Constructor needs a name to use when created the temporary
	 * .vct file
	 * 
	 */
	public EMAnalyzer(){
		//vector_count = new Hashtable<MatchVector,Integer>();
		vector_list = new ArrayList<MatchVector>();
		record_size = 0;
		
	}
	
	/**
	 * Method analyzes pairs of records and keeps a count of the
	 * number of different matching vectors
	 */
	
	public Map<String,Double> analyzeRecordPairs(org.regenstrief.linkage.io.FormPairs fp, MatchingConfig mc) throws IOException{
		ScorePair sp = new ScorePair(mc);
		Record[] pair;
		while((pair = fp.getNextRecordPair()) != null){
			Record r1 = pair[0];
			Record r2 = pair[1];
			MatchResult mr = sp.scorePair(r1, r2);
			MatchVector mr_vect = mr.getMatchVector();
			vector_list.add(mr_vect);
			/*
			Integer mv_count = vector_count.get(mr_vect);
			if(mv_count == null){
				vector_count.put(mr_vect, new Integer(1));
			} else {
				vector_count.put(mr_vect, new Integer(mv_count.intValue() + 1));
			}
			*/
		}
		Map<String,Double> values = finishAnalysis(new VectorTable(mc), mc.getIncludedColumnsNames(), mc);
		//Map<String,Double> values = finishAnalysis2(mc);
		return values;
	}
	
	/**
	 * The number of different MatchingVectors are known, so method
	 * can loop through the MatchingVector objects for ITERATIONS times
	 * updating the match and umatch values.  Values are calculated here and
	 * MatchingConfig object is modified at the end.
	 * 
	 * Currently a String array of demographics is passed in and used to
	 * initialize the hashtable with default values.  This can be removed if
	 * the VectorTable is modified to keep track of what Strings the table
	 * is matched on.
	 */
	public Map<String,Double> finishAnalysis(VectorTable vt, String[] demographics, MatchingConfig mc)  throws IOException{
		
		// values to store index by demographic name
		Hashtable<String,Double> msum = new Hashtable<String,Double>();
		Hashtable<String,Double> usum = new Hashtable<String,Double>();
		Hashtable<String,Double> mest = new Hashtable<String,Double>();
		Hashtable<String,Double> uest = new Hashtable<String,Double>();
		
		// initialize default values
		for(int i = 0; i < demographics.length; i++){
			mest.put(demographics[i], new Double(INIT_MEST));
			uest.put(demographics[i], new Double(INIT_UEST));
			msum.put(demographics[i], new Double(0));
			usum.put(demographics[i], new Double(0));
		}
		
		
		double gMsum, gUsum, gMtemp, gUtemp;
		double termM, termU;
		double p = 0.01;
		//gMsum = 0;
		//gUsum = 0;
		//gMtemp = 0;
		//gUtemp = 0;
		
		for(int i = 0; i < ITERATIONS; i++){
			gMsum = 0;
			gUsum = 0;
			gMtemp = 0;
			gUtemp = 0;
			int vct_count = 0;
			
			// zero out msum and usum arrays
			for(int k = 0; k < demographics.length; k++){
				msum.put(demographics[k], new Double(0));
				usum.put(demographics[k], new Double(0));
			}
			
			//Iterator<MatchVector> mv_it = vector_count.keySet().iterator();
			Iterator<MatchVector> mv_it = vector_list.iterator();
			while(mv_it.hasNext()){
				MatchVector mv = mv_it.next();
				//int mv_count = vector_count.get(mv).intValue();
				//vct_count += mv_count;
				vct_count++;
				//for(int j = 0; j < mv_count; j++){
					// begin the EM calculation loop for the current record pair
					termM = 1;
					termU = 1;
					gMtemp = 0;
					gUtemp = 0;
					
					for(int k = 0; k < demographics.length; k++){
						String demographic = demographics[k];
						boolean matched = mv.matchedOn(demographic);
						int comp = 0;
						if(matched){
							comp = 1;
						}
						termM = termM * Math.pow(mest.get(demographic), comp) * Math.pow(1 - mest.get(demographic), 1 - comp);
						termU = termU * Math.pow(uest.get(demographic), comp) * Math.pow(1 - uest.get(demographic), 1 - comp);
						//System.out.println(termM + "\t" + termU);
					}
					//System.out.println();
					gMtemp = (p * termM) / ((p * termM) + ((1 - p) * termU));
					gUtemp = ((1 - p) * termU) / (((1 - p) * termU) + (p * termM)); 
					//System.out.println("gMtemp: " + gMtemp);
					
					// update the running sum for msum and usum
					for(int k = 0; k < demographics.length; k++){
						String demographic = demographics[k];
						boolean matched = mv.matchedOn(demographic);
						if(matched){
							double m = msum.get(demographic);
							double u = usum.get(demographic);
							msum.put(demographic, new Double(m + gMtemp));
							usum.put(demographic, new Double(u + gUtemp));
						}
					}
					
					// update the running sum for gMsum and gUsum
					
					
					
					gMsum = gMsum + gMtemp;
					gUsum = gUsum + gUtemp;
					
				//}
				
			}
			
			// update p_est
			p = gMsum / vct_count;
			System.out.println("Iteration " + (i + 1));
			System.out.println("P: " + p);
			
			// update the mest and uest values after each iteration
			for(int j = 0; j < demographics.length; j++){
				String demographic = demographics[j];
				double mest_val = msum.get(demographic) / gMsum;
				double uest_val = usum.get(demographic) / gUsum;
				mest.put(demographic, mest_val);
				uest.put(demographic, uest_val);
			}
			System.out.println("gMsum: " + gMsum + " gUsum: " + gUsum);
			for(int j = 0; j < demographics.length; j++){
				String demographic = demographics[j];
				System.out.println(demographic + ":   mest: " + mest.get(demographic) + "   uest: " + uest.get(demographic));
			}
			System.out.println();
			
		}
		
		for(int i = 0; i < demographics.length; i++){
			String demographic = demographics[i];
			MatchingConfigRow mcr = mc.getMatchingConfigRows().get(mc.getRowIndexforName(demographic));
			mcr.setAgreement(mest.get(demographic));
			mcr.setNonAgreement(uest.get(demographic));
		}
		
		return null;
	}
	
}
