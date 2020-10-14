package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DependentValueFrequencyTable {
	
	private Hashtable<String, ValueFrequencyTable> frequencies;
	
	public DependentValueFrequencyTable() {
		frequencies = new Hashtable<String, ValueFrequencyTable>();
	}
	
	public void setValueFrequencyTable(String context, ValueFrequencyTable vft) {
		frequencies.put(context, vft);
	}
	
	public List<String> getContextValues(String context) {
		List<String> ret = new ArrayList<String>();
		ret.addAll(frequencies.keySet());
		return ret;
	}
	
	public ValueFrequencyTable getValueFrequencyTable(String context) {
		return frequencies.get(context);
	}
}
