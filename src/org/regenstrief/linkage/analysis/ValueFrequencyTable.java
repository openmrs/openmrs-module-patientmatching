package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class stores how often a given token value is present.
 * @author jegg
 *
 */

public class ValueFrequencyTable {

	Hashtable<String,Long> frequencies;
	
	public ValueFrequencyTable(){
		frequencies = new Hashtable<String,Long>();
	}
	
	public Long getFrequency(String value){
		Long l = frequencies.get(value);
		if(l == null){
			return 0L;
		}
		return l;
	}
	
	public void setFrequency(String value, long freq){
		frequencies.put(value, freq);
	}
	
	public List<String> getValues(){
		List<String> ret = new ArrayList<String>();
		ret.addAll(frequencies.keySet());
		return ret;
	}
}
