package org.regenstrief.linkage.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.regenstrief.linkage.util.MatchingConfig;

/**
 *To calculate U values, For linkage of two files (A -> B), first some preliminaries: 
 * Assume 'm' is the set of unique values for a given field in data set A, and 
 * 'n' is the set of unique values for a given field in data set B. 'Q' is the set of unique 
 * values found in both data sets -- the intersection ( Q = m /\ n ). 
 * The total number of unique values in Q is 'j'. Form a 2-d matrix with dimensions ( j x j ). 
 * The cells represent the product of the frequencies of each unique value in data set A and data set B. 
 * The numerator for the u-value estimator will be the sum of the diagonal cells. 
 * The denominator for the u-value estimator will be the sum of all cells.
 * 
 * @author jegg
 *
 */

public class CloseFormUCalculator {
	
	private MatchingConfig mc;
	private DataSourceFrequency freq1, freq2;
	
	public CloseFormUCalculator(MatchingConfig mc, DataSourceFrequency freq1, DataSourceFrequency freq2){
		this.mc = mc;
		this.freq1 = freq1;
		this.freq2 = freq2;
	}
	
	public void calculateUValues(){
		HashMap<String,Double> u_values = new HashMap<String,Double>();
		
		// find fields that are common between the two data sources
		Set<String> fields_a = freq1.getFields();
		Set<String> fields_b = freq2.getFields();
		
		Set<String> intersection = new HashSet<String>(fields_a);
		intersection.retainAll(fields_b);
		
		// iterate over common fields, calculating u-values
		Iterator<String> it = intersection.iterator();
		while(it.hasNext()){
			String field = it.next();
			// initialize zero values for each field
			u_values.put(field, new Double(0));
			
			Set<String> m = freq1.getTokens(field);
			Set<String> n = freq2.getTokens(field);
			
			Set<String> Q = new HashSet<String>(m);
			Q.retainAll(n);
			
			Iterator<String> q_it1 = Q.iterator();
			double field_total = 0;
			double diagonal_total = 0;
			
			while(q_it1.hasNext()){
				String s1 = q_it1.next();
				int f1 = freq1.getFrequency(field, s1);
				
				Iterator<String> q_it2 = Q.iterator();
				while(q_it2.hasNext()){
					String s2 = q_it2.next();
					
					int f2 = freq2.getFrequency(field, s2);
					double increment = (double)f1 * (double)f2;
					field_total += increment;
					if(s1.equals(s2) && !s1.equals("")){
						diagonal_total += increment;
					}
				}
			}
			u_values.put(field, (diagonal_total/field_total));
			
			System.out.println("total for field " + field + ":\t" + u_values.get(field));
			mc.setNonAgreementValue(mc.getRowIndexforName(field), u_values.get(field));
		}
	}
	
}
