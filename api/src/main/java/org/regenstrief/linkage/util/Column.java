package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.regenstrief.linkage.analysis.DataSourceFrequency;

public class Column {
	private String name;
	private DataSourceFrequency dsf;
	private Map<String, Integer> frequency = new HashMap();
	private List<Token> tokenFrequency = new ArrayList();
	private double conditionalEntropy;

	public double getConditionalEntropy() {
		return conditionalEntropy;
	}

	public void setConditionalEntropy(double conditionalEntropy) {
		this.conditionalEntropy = conditionalEntropy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Integer> getFrequency() {
		return frequency;
	}

	public void setFrequency(Map<String, Integer> frequency) {
		this.frequency = frequency;
	}

	public Column(String name, DataSourceFrequency dsf1){
		this.name = name;
		this.dsf = dsf1;
	}
	
	public List<Token> getTokenFrequency() {
		return tokenFrequency;
	}

	public void setTokenFrequency(List<Token> tokenFrequency) {
		this.tokenFrequency = tokenFrequency;
	}
	
	/*public void analyse(){
		System.out.println(dsf);
	
		Set<String> tokens = dsf.getTokens(getName());
		System.out.println(tokens);
		
		for(String token: tokens){
			System.out.println("_________");
			System.out.println(token);
			System.out.println(dsf.getFrequency(getName(), token));
			frequency.put(token, dsf.getFrequency(getName(), token));
		}
	}
*/
}
