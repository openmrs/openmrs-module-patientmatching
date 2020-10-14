package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Class stores how often a given token value is present.
 * 
 * @author jegg
 */

public class ValueFrequencyTable {
	
	Hashtable<String, Long> frequencies;
	
	public ValueFrequencyTable() {
		frequencies = new Hashtable<String, Long>();
	}
	
	public int getRank(String value) {
		long val_freq = getFrequency(value);
		if (val_freq == 0) {
			return 0;
		}
		
		List<Long> freqs = new ArrayList<Long>();
		Enumeration<Long> e = frequencies.elements();
		while (e.hasMoreElements()) {
			Long l = e.nextElement();
			freqs.add(l);
		}
		Collections.sort(freqs, Collections.reverseOrder());
		int rank = freqs.indexOf(val_freq) + 1;
		
		return rank;
	}
	
	public Long getFrequency(String value) {
		Long l = frequencies.get(value);
		if (l == null) {
			return 0L;
		}
		return l;
	}
	
	public int getUniqueValueCount() {
		return frequencies.keySet().size();
	}
	
	public void setFrequency(String value, long freq) {
		frequencies.put(value, freq);
	}
	
	public List<String> getValues() {
		List<String> ret = new ArrayList<String>();
		ret.addAll(frequencies.keySet());
		return ret;
	}
}
