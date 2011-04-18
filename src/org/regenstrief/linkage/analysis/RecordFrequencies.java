package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class stores a ValueFrequencyTable for each demographic in a record.
 * @author jegg
 *
 */

public class RecordFrequencies {
	
	Hashtable<String,ValueFrequencyTable> demographic_frequencies;
	
	public RecordFrequencies(){
		demographic_frequencies = new Hashtable<String,ValueFrequencyTable>();
	}
	
	public ValueFrequencyTable getDemographicFrequencies(String demographic){
		ValueFrequencyTable vft = demographic_frequencies.get(demographic);
		if(vft == null){
			vft = new ValueFrequencyTable();
		}
		return vft;
	}
	
	public void setDemographicFrequencies(String demographic, ValueFrequencyTable freqs){
		demographic_frequencies.put(demographic, freqs);
	}
	
	public List<String> getDemographics(){
		List<String> ret = new ArrayList<String>();
		ret.addAll(demographic_frequencies.keySet());
		return ret;
	}
}
