package org.regenstrief.linkage.analysis;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.regenstrief.linkage.util.LinkDataSource;

public class MemoryBackedDataSourceFrequency extends DataSourceFrequency {
	
	Map<String,Map<String, Integer>> frequencies;
	
	public MemoryBackedDataSourceFrequency(LinkDataSource lds){
		super(lds);
		frequencies = new Hashtable<String,Map<String,Integer>>();
	}
	
	@Override
	public int getFrequency(String field, String token) {
		if(token != null){
			Map<String,Integer> field_frequencies = frequencies.get(field);
			if(field_frequencies != null){
				Integer freq = field_frequencies.get(token);
				if(freq != null){
					return freq.intValue();
				}
			}
			
		}
		return 0;
	}

	@Override
	public void incrementCount(String field, String token) {
		if(token != null){
			Map<String,Integer> field_frequencies = frequencies.get(field);
			if(field_frequencies == null){
				field_frequencies = new Hashtable<String,Integer>();
				frequencies.put(field, field_frequencies);
			}
			Integer freq = field_frequencies.get(token);
			if(freq != null){
				field_frequencies.put(token, new Integer(freq + 1));
			} else {
				field_frequencies.put(token, new Integer(1));
			}
			Integer prev_total = totals.get(field);
			if(prev_total != null){
				totals.put(field, new Integer(prev_total + 1));
			} else {
				totals.put(field, new Integer(1));
			}
		}
	}
	
	public Set<String> getFields(){
		return frequencies.keySet();
	}
	
	public void setFrequency(String field, String token, int freq){
		Map<String,Integer> field_frequencies = frequencies.get(field);
		int prev_freq = 0;
		if(field_frequencies == null){
			field_frequencies = new Hashtable<String,Integer>();
			frequencies.put(field, field_frequencies);
		} else {
			prev_freq = field_frequencies.get(token);
		}
		field_frequencies.put(token, new Integer(freq));
		
		Integer prev_total = totals.get(field);
		if(prev_total == null){
			prev_total = 0;
		}
		
		totals.put(field, new Integer(prev_total + (freq - prev_freq)));
	}
	
	public Set<String> getTokens(String field){
		return frequencies.get(field).keySet();
	}
	
	public int getTotal(String field){
		return totals.get(field);
	}

}
