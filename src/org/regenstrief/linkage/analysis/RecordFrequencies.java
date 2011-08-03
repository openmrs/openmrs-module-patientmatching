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
	Hashtable<String,DependentValueFrequencyTable> dependent_frequencies;
	Hashtable<String,String> dependencies;
	
	public RecordFrequencies(){
		demographic_frequencies = new Hashtable<String,ValueFrequencyTable>();
		dependencies = new Hashtable<String,String>();
		dependent_frequencies = new Hashtable<String,DependentValueFrequencyTable>();
	}
	
	public ValueFrequencyTable getDemographicFrequencies(String demographic){
		ValueFrequencyTable vft = demographic_frequencies.get(demographic);
		return vft;
	}
	
	public void setDemographicFrequencies(String demographic, ValueFrequencyTable freqs){
		demographic_frequencies.put(demographic, freqs);
	}
	
	public void setDependentDemographicFrequencies(String demographic, DependentValueFrequencyTable freqs){
		dependent_frequencies.put(demographic, freqs);
	}
	
	public List<String> getDemographics(){
		List<String> ret = new ArrayList<String>();
		ret.addAll(demographic_frequencies.keySet());
		ret.addAll(dependencies.keySet());
		return ret;
	}

	public boolean isDependent(String demographic){
		return dependencies.keySet().contains(demographic);
	}

	public String getContext(String demographic){
		return dependencies.get(demographic);
	}

	public void setDependency(String context, String demographic){
		dependencies.put(demographic, context);
	}
	
	public DependentValueFrequencyTable getDependentValueFrequencyTable(String demographic){
		return dependent_frequencies.get(demographic);
	}
}
