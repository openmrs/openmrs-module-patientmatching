package org.regenstrief.linkage.analysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.MatchVector;
import org.regenstrief.linkage.NullDemographicsMatchVector;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LoggingObject;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.ScorePair;

/**
 * Class was originally extending Analyzer, but EM analyzes
 * pairs of records, not a record data source, so it
 * no longer is a subclass.  It also modifies the MatchingConfig
 * object given in the analyzeRecordPairs method with new
 * agreement and non-agreement values for included columns as
 * a side effect
 */

public class EMAnalyzer extends RecordPairAnalyzer implements LoggingObject { //extends Analyzer {
	final static double INIT_MEST = 0.9;
	final static double INIT_UEST = 0.1;
	final static int INIT_COMP = 0;
	final static double EARLY_TERMINATION_THRESHOLD = 0.00001;
	
	// use approximate values close to zero and one
	// due to the math used in the record linking
	final static double EM_ONE = 0.99999;
	final static double EM_ZERO = 0.000001;
	
	final static int MIN_ITERATIONS = 15;
	final static int MAX_ITERATIONS = 200;
	private int iterations;
	private boolean pin_u_values;
	private boolean trinomial;
	
	//private Logger log = Logger.getLogger(this.getClass() + this.toString());
	
	Hashtable<String,Double> msum;
	Hashtable<String,Double> usum;
	Hashtable<String,Double> mest;
	Hashtable<String,Double> uest;
	
	private Hashtable<MatchVector,Integer> vector_count;
	private List<NullDemographicsMatchVector> null_vectors;
	private ScorePair sp;
	int vct_count;
	double termM, termU, 	gMtemp, gUtemp, gMsum, gUsum;
	
	/**
	 * Constructor needs a name to use when created the temporary
	 * .vct file
	 * 
	 */
	public EMAnalyzer(MatchingConfig mc){
		super(mc);
		
		msum = new Hashtable<String,Double>();
		usum = new Hashtable<String,Double>();
		mest = new Hashtable<String,Double>();
		uest = new Hashtable<String,Double>();
		
		vct_count = 0;
		
		vector_count = new Hashtable<MatchVector,Integer>();
		null_vectors = new LinkedList<NullDemographicsMatchVector>();
		sp = new ScorePair(mc);
		iterations = MAX_ITERATIONS;
		/*
		 * Check the current blocking run use random sampling or not.
		 */
		pin_u_values = mc.isLockedUValues();
		trinomial = false;
	}
	
	public void setTrinomial(boolean trinomial){
		this.trinomial = trinomial;
	}
	
	public void setIterations(int iterations){
		this.iterations = iterations;
	}
	
	public boolean isUStatic(){
		return pin_u_values;
	}
	
	public void setUStatic(boolean b){
		pin_u_values = b;
	}
	
	/**
	 * Returns the logger used for analysis messages
	 * 
	 * @return	analysis logger, setup by default to just print messages to console
	 */
	public Logger getLogger(){
		return log;
	}
	
	public void analyzeRecordPair(Record[] pair){
		Record r1 = pair[0];
		Record r2 = pair[1];
		MatchResult mr = sp.scorePair(r1, r2);
		MatchVector mr_vect = mr.getMatchVector();
		//vector_list.add(mr_vect);
		
		if(trinomial && mr_vect instanceof NullDemographicsMatchVector){
			null_vectors.add((NullDemographicsMatchVector)mr_vect);
		} else {
			Integer mv_count = vector_count.get(mr_vect);
			if(mv_count == null){
				vector_count.put(mr_vect, new Integer(1));
			} else {
				vector_count.put(mr_vect, new Integer(mv_count.intValue() + 1));
			}
		}
		
	}
	
	public void finishAnalysis(){
		String[] bcs = mc.getBlockingColumns();
		String[] demographics = mc.getIncludedColumnsNames();
		//System.out.print("Blocking columns: ");
		log.info("Blocking columns: ");
		for(int i = 0; i < bcs.length; i++){
			String block_col_name = bcs[i];
			//System.out.print(" " + block_col_name);
			log.info(" " + block_col_name);
		}
		
		// initialize default values
		for(int i = 0; i < demographics.length; i++){
			mest.put(demographics[i], new Double(INIT_MEST));
			if(pin_u_values){
				uest.put(demographics[i], mc.getMatchingConfigRowByName(demographics[i]).getNonAgreement());
			} else {
				uest.put(demographics[i], new Double(INIT_UEST));
			}
			StringBuffer logBuffer = new StringBuffer();
			logBuffer.append("Initializing demographic: ")
			         .append(demographics[i])
			         .append(" u = ")
			         .append(uest.get(demographics[i]))
			         .append(" and m = ")
			         .append(mest.get(demographics[i]));
			log.info(logBuffer.toString());
			msum.put(demographics[i], new Double(0));
			usum.put(demographics[i], new Double(0));
		}
		
		
		double p = 0.01;
		double prev_p = 0.01;
		boolean break_early = false;
		
		for(int i = 0; i < iterations; i++){
			gMsum = 0;
			gUsum = 0;
			vct_count = 0;
			
			// zero out msum and usum arrays
			for(int k = 0; k < demographics.length; k++){
				msum.put(demographics[k], new Double(0));
				usum.put(demographics[k], new Double(0));
			}
			
			Iterator<MatchVector> mv_it = vector_count.keySet().iterator();
			while(mv_it.hasNext()){
				MatchVector mv = mv_it.next();
				int mv_count = vector_count.get(mv).intValue();
				modifyMU(mv, p, mv_count);
				
			}
			
			// iterate over NullSetMatchVectors in null_vectors
			Iterator<NullDemographicsMatchVector> it = null_vectors.iterator();
			while(it.hasNext()){
				NullDemographicsMatchVector nsmv = it.next();
				
				modifyMU(nsmv, p, 1);
			}
			
			// update p_est
			p = gMsum / vct_count;
			if(Math.abs(p - prev_p) < EARLY_TERMINATION_THRESHOLD && i > MIN_ITERATIONS){
				break_early = true;
			}
			
			prev_p = p;
			log.info("Iteration " + (i + 1));
			log.info("P: " + p);
			
			// update the mest and uest values after each iteration
			for(int j = 0; j < demographics.length; j++){
				String demographic = demographics[j];
				double mest_val = msum.get(demographic) / gMsum;
				double uest_val = usum.get(demographic) / gUsum;
				mest.put(demographic, mest_val);
				if(!pin_u_values){
					uest.put(demographic, uest_val);
				}
			}
			
			for(int j = 0; j < demographics.length; j++){
				String demographic = demographics[j];
				log.info(demographic + ":   mest: " + mest.get(demographic) + "   uest: " + uest.get(demographic));
			}
			if(break_early){
				log.info("Terminating early due to P value converging");
				break;
			}
			
		}
		
		// print basic information about analysis
		log.info("\nBlocking columns: ");
		for(int i = 0; i < bcs.length; i++){
			String block_col_name = bcs[i];
			log.info(" " + block_col_name);
		}
		
		log.info("P:\t" + p);
		mc.setP(p);
		int total_pairs = 0;
		Enumeration<MatchVector> e = vector_count.keys();
		while(e.hasMoreElements()){
			MatchVector mv = e.nextElement();
			total_pairs += vector_count.get(mv);
		}
		mc.setNPairs(total_pairs);
		double true_matches = total_pairs * p;
		double non_matches = total_pairs * (1 - p);
		
		log.info("Total pairs processed:\t" + total_pairs);
		log.info("Estimated true matches:\t" + true_matches);
		log.info("Estimated non matches:\t" + non_matches);
		
		for(int i = 0; i < demographics.length; i++){
			String demographic = demographics[i];
			MatchingConfigRow mcr = mc.getMatchingConfigRows().get(mc.getRowIndexforName(demographic));
			double mest_val = mest.get(demographic);
			double uest_val = uest.get(demographic);
			
			if(mest_val > EM_ONE){
				mest_val = EM_ONE;
			}
			if(mest_val < EM_ZERO){
				mest_val = EM_ZERO;
			}
			if(uest_val > EM_ONE){
				uest_val = EM_ONE;
			}
			if(uest_val < EM_ZERO){
				uest_val = EM_ZERO;
			}
			
			mcr.setAgreement(mest_val);
			mcr.setNonAgreement(uest_val);
			
		}
		// change the score threshold to one calculated from estimated true matches
		log.info("previous score threshold:\t" + mc.getScoreThreshold());
		VectorTable vt = new VectorTable(mc);
		
		// get sorted list of MatchResult objects to go with the MatchVector objects
		Iterator<MatchVector> it = vector_count.keySet().iterator();
		List<MatchResult> mrs = new Vector<MatchResult>();
		while(it.hasNext()){
			MatchVector mv = it.next();
			mrs.add(new MatchResult(vt.getScore(mv),vt.getInclusiveScore(mv),vt.getMatchVectorFalseProbability(mv),vt.getMatchVectorTrueProbability(mv),vt.getSensitivity(mv),vt.getSpecificity(mv),mv,null, null,null,mc));
		}
		
		// sort list based on score
		Collections.sort(mrs, new Comparator<MatchResult>() {
		    public int compare(MatchResult mr1, MatchResult mr2) {
		    	double diff = mr2.getScore() - mr1.getScore();
		        int ret;
		        if(diff == 0){
		        	ret = 0;
		        } else if(diff > 0){
		        	ret = 1;
		        } else {
		        	ret = -1;
		        }
		        return ret;
		    }});
		
		int result_place = 0;
		Iterator<MatchResult> mr_it = mrs.iterator();
		double new_threshold = 0;
		while(mr_it.hasNext() && result_place < true_matches){
			MatchResult mr = mr_it.next();
			new_threshold = mr.getScore();
			result_place += vector_count.get(mr.getMatchVector());
		}
		mc.setScoreThreshold(new_threshold);
		
		log.info("new score threshold:\t" + mc.getScoreThreshold());
	}
	
	/**
	 * Method modifies the demographic's m and u values based on the given MatchVector
	 * 
	 * @param mv	the agreement and disagreement to use in the calculations
	 * @param p	p value at beginning of calculation
	 * @param count	the number of times the given MatchVector should be applied
	 */
	protected void modifyMU(MatchVector mv, double p, int count){
		vct_count += count;
		termM = 1;
		termU = 1;
		gMtemp = 0;
		gUtemp = 0;
		List<String> demographics = mv.getDemographics();
		
		Iterator<String> it = demographics.iterator();
		while(it.hasNext()){
			String demographic = it.next();
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
		
		gMtemp = (double)count * gMtemp;
		gUtemp = (double)count * gUtemp;
		
		it = demographics.iterator();
		while(it.hasNext()){
			String demographic = it.next();
			boolean matched = mv.matchedOn(demographic);
			if(matched){
				double m = msum.get(demographic);
				double u = usum.get(demographic);
				msum.put(demographic, new Double(m + gMtemp));
				usum.put(demographic, new Double(u + gUtemp));
			}
		}
		
		gMsum = gMsum + gMtemp;
		gUsum = gUsum + gUtemp;
	}
	
}
