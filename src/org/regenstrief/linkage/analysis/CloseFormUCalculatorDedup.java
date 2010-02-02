package org.regenstrief.linkage.analysis;

import java.util.HashMap;
import java.util.Iterator;

import org.regenstrief.linkage.util.MatchingConfig;

/**
 * 
 *  Equation for calculating U values for each field:
 * sum ( [ (n^2 - n) / 2 ] )  /  [ (S^2 - S) / 2]
 * where n is the frequency of the given token and S is the sum of all elements in the frequency vector
 * 
 * @author jegg
 *
 */

public class CloseFormUCalculatorDedup {
	private MatchingConfig mc;
	private DataSourceFrequency freq1;
	
	public CloseFormUCalculatorDedup(MatchingConfig mc, DataSourceFrequency freq1){
		this.mc = mc;
		this.freq1 = freq1;
	}
	
	public void calculateUValues(){
		Iterator<String> it1 = freq1.getFields().iterator();
		HashMap<String,Double> u_values = new HashMap<String,Double>();
		while(it1.hasNext()){
			// initialize zero values for each field
			u_values.put(it1.next(), new Double(0));
		}
		
		it1 = freq1.getFields().iterator();
		while(it1.hasNext()){
			String field = it1.next();
			int total = freq1.getTotal(field);
			
			// for every frequency, calculate u rate based on frequencies in freq2
			Iterator<String> it2 = freq1.getTokens(field).iterator();
			while(it2.hasNext()){
				String token2 = it2.next();
				int freq = freq1.getFrequency(field, token2);
				
				// add if statement to ignore empty string tokens
				if(!token2.equals("")){
					double numerator = (Math.pow(freq, 2) - freq) / 2;
					double denominator = (Math.pow(total, 2) - total)/ 2;
					double increment =  numerator / denominator;
					u_values.put(field, u_values.get(field) + increment);
				}
				
			}
			
			System.out.println("total for field " + field + ":\t" + u_values.get(field));
			mc.setNonAgreementValue(mc.getRowIndexforName(field), u_values.get(field));
		}
		
		
	}
}
