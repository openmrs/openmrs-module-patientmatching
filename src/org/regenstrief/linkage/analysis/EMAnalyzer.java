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
	
	final static int ITERATIONS = 1;
	
	private int record_size;
	private Hashtable<MatchVector,Integer> vector_count;
	
	/**
	 * Constructor needs a name to use when created the temporary
	 * .vct file
	 * 
	 * @param name a unique name for the EMAnalyzer
	 */
	public EMAnalyzer(){
		vector_count = new Hashtable<MatchVector,Integer>();
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
			
			Integer mv_count = vector_count.get(mr_vect);
			if(mv_count == null){
				vector_count.put(mr_vect, new Integer(1));
			} else {
				vector_count.put(mr_vect, new Integer(mv_count.intValue() + 1));
			}
			
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
		gMsum = 0;
		gUsum = 0;
		gMtemp = 0;
		gUtemp = 0;
		
		for(int i = 0; i < ITERATIONS; i++){
			int vct_count = 0;
			Iterator<MatchVector> mv_it = vector_count.keySet().iterator();
			while(mv_it.hasNext()){
				MatchVector mv = mv_it.next();
				int mv_count = vector_count.get(mv).intValue();
				vct_count += mv_count;
				for(int j = 0; j < mv_count; j++){
					// begin the EM calcluation loop for the current record pair
					termM = 1;
					termU = 1;
					gMtemp = 0;
					gUtemp = 0;
					
					/* original, array based method of calculating terms
					for(int j = 0; j < used_len; j++){
						comp[j] = Integer.parseInt(line.substring(j, j + 1));
						termM = termM * Math.pow(mest[j], comp[j]) * Math.pow(1 - mest[j], 1 - comp[j]);
						termU = termU * Math.pow(uest[j], comp[j]) * Math.pow(1 - uest[j], 1 - comp[j]);
					}*/
					
					List<String> mv_demographics = mv.getDemographics();
					Iterator<String> d_it = mv_demographics.iterator();
					while(d_it.hasNext()){
						String demographic = d_it.next();
						boolean matched = mv.matchedOn(demographic);
						int comp = 0;
						if(matched){
							comp = 1;
						}
						termM = termM * Math.pow(mest.get(demographic), comp) * Math.pow(1 - mest.get(demographic), 1 - comp);
						termU = termU * Math.pow(uest.get(demographic), comp) * Math.pow(1 - uest.get(demographic), 1 - comp);
						
					}
					
					gMtemp = (p * termM) / ((p * termM) + ((1 - p) * termU));
					gUtemp = ((1 - p) * termU) / (((1 - p) * termU) + (p * termM)); 
					
					// update the running sum for msum and usum
					d_it = mv_demographics.iterator();
					while(d_it.hasNext()){
						String demographic = d_it.next();
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
				}
				
			}
			
			// update p_est
			p = gMsum / vct_count;
			System.out.println(p);
			// update the mest and uest values after each iteration
			for(int j = 0; j < demographics.length; j++){
				String demographic = demographics[j];
				mest.put(demographic, msum.get(demographic) / gMsum);
				uest.put(demographic, usum.get(demographic) / gMsum);
			}
			
		}
		
		for(int i = 0; i < demographics.length; i++){
			String demographic = demographics[i];
			MatchingConfigRow mcr = mc.getMatchingConfigRows().get(mc.getRowIndexforName(demographic));
			mcr.setAgreement(mest.get(demographic));
			mcr.setNonAgreement(uest.get(demographic));
		}
		
		return null;
	}
	
	/*
	 * The second implemntation of this algorithm in Java and the first implementation
	 * in this class.  It was written when a temporary file was being created of
	 * 1's and 0's in each row indicating whether that record pair had a match
	 * on that particular field.  When the temp file was removed and calculations no
	 * longer indexed on position, the method stopped being used.
	 */
	public Map<String,Double> finishAnalysis2(MatchingConfig mc) throws IOException{
		int used_len = mc.getIncludedColumnsNames().length;
		int[] comp = new int[used_len];
		double[] msum = new double[used_len];
		double[] usum = new double[used_len];
		double[] mest = new double[used_len];
		double[] uest = new double[used_len];
		
		File vector_file = new File("test_EM.vct");
		
		double gMsum, gUsum, gMtemp, gUtemp;
		double termM, termU;
		double p = 0.01;
		gMsum = 0;
		gUsum = 0;
		gMtemp = 0;
		gUtemp = 0;
		
		for(int i = 0; i < used_len; i++){
			mest[i] = INIT_MEST;
			uest[i] = INIT_UEST;
		}
		
		for(int i = 0; i < ITERATIONS; i++){
			BufferedReader vct_in = new BufferedReader(new FileReader(vector_file));
			String line;
			int line_count = 0;
			while((line = vct_in.readLine()) != null){
			
				line_count++;
				
				// begin the EM calcluation loop for the current record pair
				termM = 1;
				termU = 1;
				gMtemp = 0;
				gUtemp = 0;
				for(int j = 0; j < used_len; j++){
					comp[j] = Integer.parseInt(line.substring(j, j + 1));
					termM = termM * Math.pow(mest[j], comp[j]) * Math.pow(1 - mest[j], 1 - comp[j]);
					termU = termU * Math.pow(uest[j], comp[j]) * Math.pow(1 - uest[j], 1 - comp[j]);
				}
				
				gMtemp = (p * termM) / ((p * termM) + ((1 - p) * termU));
				gUtemp = ((1 - p) * termU) / (((1 - p) * termU) + (p * termM)); 
				
				// update the running sum for msum and usum
				for(int j = 0; j < used_len; j++){
					msum[j] = msum[j] + comp[j] * gMtemp;
					usum[j] = usum[j] + comp[j] * gUtemp;
				}
				// update the running sum for gMsum and gUsum
				gMsum = gMsum + gMtemp;
				gUsum = gUsum + gUtemp;
			}
			// update p_est
			p = gMsum / line_count;
			
			// update the mest and uest values after each iteration
			for(int j = 0; j < used_len; j++){
				mest[j] = msum[j] / gMsum;
				uest[j] = usum[j] / gUsum;
			}
			vct_in.close();
			
		}
		
		// modify the given matching config object
		//Hashtable<String,Double> ret = new Hashtable<String,Double>();
		String[] demographics = mc.getIncludedColumnsNames();
		for(int i = 0; i < demographics.length; i++){
			String demographic = demographics[i];
			
			MatchingConfigRow mcr = mc.getMatchingConfigRows().get(mc.getRowIndexforName(demographic));
			mcr.setAgreement(mest[i]);
			mcr.setNonAgreement(i);
		}
		
		return null;
	}

}
