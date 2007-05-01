package org.regenstrief.linkage.util;
/*
 * Implements the EM algorithm.
 * 
 * Ported from C code in August, 2006
 */

import java.io.*;
import java.util.*;

public class EM {
	
	File pair_file;
	MatchingConfig mc;
	LinkDataSource lds;
	int iterations;
	
	// default values to match rates
	final static double INIT_MEST = 0.9;
	final static double INIT_UEST = 0.1;
	final static int INIT_COMP = 0;
	
	final static double META_ONE = 0.99999;
	final static double META_ZERO = 0.00001;
	
	/**
	 * Takes three arguments: record pair file name, meta-file name, and
	 * number of iterations.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3){
			System.out.println("EM ERROR:\nNot enough arguments.  Please enter:");
			System.out.println("\t1) Record Pair file name");
			System.out.println("\t2) Meta-file name");
			System.out.println("\t3) Number of iterations");
			System.exit(0);
		}
		
		/*String file = args[0];
		String meta_file = args[1];
		int iterations = Integer.parseInt(args[2]);
		
		File pair_file = new File(file);
		File meta = new File(meta_file);
		
		
		EM em = new EM(pair_file, meta, iterations);
		try{
			em.estimate();
		}
		catch(IOException ioe){
			System.err.println("IO error running estimation process: " + ioe.getMessage());
		}
		*/
	}
	
	public EM(File file, MatchingConfig mc, int iterations, LinkDataSource lds){
		pair_file = file;
		this.mc = mc;
		this.iterations = iterations;
		this.lds = lds;
	}
	
	public void estimate() throws IOException{
		int line_count;
		double gMsum, gUsum, gMtemp, gUtemp, p;
		double termM, termU;
		
		int[] included_columns = lds.getIndexesOfColumnNames(mc.getIncludedColumnsNames());
		int used_len = included_columns.length;
		int length = mc.getRowNames().length;
		
		// allocate arrays used to store values
		double[] msum = new double[used_len];
		double[] usum = new double[used_len];
		double[] mest = new double[used_len];
		double[] uest = new double[used_len];
		int[] comp = new int[used_len];
		
		// extra variable to keep track of matches, used in lieu of vct file
		//ArrayList<int[]> vect = new ArrayList<int[]>();
		
		
		// initialize estimates for EM parameters
		for(int i = 0; i < used_len; i++){
			mest[i] = INIT_MEST;
			uest[i] = INIT_UEST;
			comp[i] = INIT_COMP;
		}
		
		// reset values for another iteration
		line_count = 0;
		gMsum = 0;
		gUsum = 0;
		gMtemp = 0;
		gUtemp = 0;
		p = 0.01;
		
		System.out.println("Iteration 1");
		System.out.println(new Date());
		
		// open vector file
		File vector_file = new File(pair_file.getAbsoluteFile() + ".vct");
		
		BufferedWriter vout = new BufferedWriter(new FileWriter(vector_file));
		BufferedReader pair_in = new BufferedReader(new FileReader(pair_file));
		
		String line;
		while((line = pair_in.readLine()) != null){
			String[] data = line.split("\\|", -1);
			line_count++;
			
			termM = 1;
			termU = 1;
			gMtemp = 0;
			gUtemp = 0;
			
			for(int i = 0; i < used_len; i++){
				int used_index = included_columns[i];
				String str1 = data[used_index];
				String str2 = data[used_index + length];
				
				// need to get the matching algorith used in mc row used_index
				// and use that method to compare the two strings
				int alg = mc.getAlgorithm(used_index);
				boolean match = false;
				if(alg == MatchingConfig.EXACT_MATCH){
					match = StringMatch.exactMatch(str1, str2);
				} else if(alg == MatchingConfig.JWC){
					match = StringMatch.JWCMatch(str1, str2);
				} else if(alg == MatchingConfig.LCS){
					match = StringMatch.LCSMatch(str1, str2);
				} else if(alg == MatchingConfig.LEV){
					match = StringMatch.LEVMatch(str1, str2);
				}
				if(match){
					comp[i] = 1;
				} else {
					comp[i] = 0;
				}
				
				termM = termM * Math.pow(mest[i], comp[i]) * Math.pow(1 - mest[i], 1 - comp[i]);
				termU = termU * Math.pow(uest[i], comp[i]) * Math.pow(1 - uest[i], 1 - comp[i]);
			}
			
			// save in ArrayList for later
			int[] v = new int[used_len];
			
			// print vector to vector file
			for(int i = 0; i < used_len; i++){
				vout.write(Integer.toString(comp[i]));
				v[i] = comp[i];
			}
			vout.write("\n");
			//vect.add(v);
			
			gMtemp = (p * termM) / ((p * termM) + ((1 - p) * termU));
			gUtemp = ((1 - p) * termU) / (((1 - p) * termU) + (p * termM));
			
			// update the running sum for msum and usum
			for(int i = 0; i < used_len; i++){
				msum[i] = msum[i] + comp[i] * gMtemp;
				usum[i] = usum[i] + comp[i] * gUtemp;
			}
			
			// update the running sum for gMsum and gUsum
			gMsum = gMsum + gMtemp;
			gUsum = gUsum + gUtemp;
			
		}
		
		// close files, update p
		vout.flush();
		vout.close();
		pair_in.close();
		p = gMsum / line_count;
		System.out.println("p: " + p);
		
		// update the mest and uest values
		for(int i = 0; i < used_len; i++){
			mest[i] = msum[i] / gMsum;
			uest[i] = usum[i] / gUsum;
			System.out.println(mc.getRowName(included_columns[i]) + ":   mest: " + mest[i] + "   uest: " + uest[i]);
		}
		System.out.println();
		
		// now read the vector file
		// loop through the remaining iterations
		for(int current_iteration = 1; current_iteration < iterations; current_iteration++){
			System.out.println("Iteration " + (current_iteration + 1));
			System.out.println("time is " + new Date());
			
			line_count = 0;
			gMsum = 0;
			gUsum = 0;
			gMtemp = 0;
			gUtemp = 0;
			
			// zero out the msum and usum arrays for another iteration
			for(int i = 0; i < used_len; i++){
				msum[i] = 0;
				usum[i] = 0;
			}
			
			// open the vector file
			BufferedReader vin = new BufferedReader(new FileReader(vector_file));
			
			// get iterator for vect ArrayList which has the previous results
			//Iterator<int[]> it = vect.iterator();
			//while(it.hasNext()){
			//	comp = it.next();
			//	line_count++;
			while((line = vin.readLine()) != null){
				line_count++;
				
				// begin the EM calcluation loop for the current record pair
				termM = 1;
				termU = 1;
				gMtemp = 0;
				gUtemp = 0;
				for(int i = 0; i < used_len; i++){
					comp[i] = Integer.parseInt(line.substring(i, i + 1));
					termM = termM * Math.pow(mest[i], comp[i]) * Math.pow(1 - mest[i], 1 - comp[i]);
					termU = termU * Math.pow(uest[i], comp[i]) * Math.pow(1 - uest[i], 1 - comp[i]);
				}
				
				gMtemp = (p * termM) / ((p * termM) + ((1 - p) * termU));
				gUtemp = ((1 - p) * termU) / (((1 - p) * termU) + (p * termM)); 
				
				// update the running sum for msum and usum
				for(int i = 0; i < used_len; i++){
					msum[i] = msum[i] + comp[i] * gMtemp;
					usum[i] = usum[i] + comp[i] * gUtemp;
				}
				// update the running sum for gMsum and gUsum
				gMsum = gMsum + gMtemp;
				gUsum = gUsum + gUtemp;
				
			}
			
			// update p_est
			p = gMsum / line_count;
			
			
			// update the mest and uest values after each iteration
			for(int i = 0; i < used_len; i++){
				mest[i] = msum[i] / gMsum;
				uest[i] = usum[i] / gUsum;
				System.out.println(mc.getRowName(included_columns[i]) + ":   mest: " + mest[i] + "   uest: " + uest[i]);
			}
			vin.close();
			
		}
		
		// iterations finished, now print the results summary
		System.out.println("\nThere were " + used_len + " elements in the  \"used\" array");
		System.out.println(line_count + " record pairs were read per iteration.");
		System.out.println("gMsum: " + gMsum + " gUsum: " + gUsum);
		System.out.println("P: " + p +"\n");
		for(int i = 0; i < used_len; i++){
			System.out.println(mc.getRowName(included_columns[i]) + ":   mest: " + mest[i] + "   uest: " + uest[i]);
		}
		
		// set the calculcated values in the MatchingConfig object
		for(int i = 0; i < used_len; i++){
			int incl_col = included_columns[i];
			
			if(mest[i] > META_ONE){
				mc.setAgreementValue(incl_col, META_ONE);
			} else if(mest[i] < META_ZERO){
				mc.setAgreementValue(incl_col, META_ZERO);
			} else {
				mc.setAgreementValue(incl_col, mest[i]);
			}
			if(uest[i] > META_ONE){
				mc.setNonAgreementValue(incl_col, META_ONE);
			} else if(uest[i] < META_ZERO){
				mc.setNonAgreementValue(incl_col, META_ZERO);
			} else {
				mc.setNonAgreementValue(incl_col, uest[i]);
			}
		}
		
	}
	
	
}
