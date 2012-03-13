package org.regenstrief.linkage.analysis;

import java.util.HashMap;
import java.util.Map;

import org.regenstrief.linkage.util.LinkDataSource;

/**
 * Class stores summay statistics for a data source.  It stores, per demographic, entropy, null counts, average frequency,
 * and number of unique values
 * 
 * @author jegg
 *
 */

public class SummaryStatisticsStore {
	LinkDataSource data;
	private Map<String,Double> entropy, max_entropy, null_counts, avg_freq, uniques;
	
	public SummaryStatisticsStore(LinkDataSource lds){
		data = lds;
		entropy = new HashMap<String,Double>();
		max_entropy = new HashMap<String,Double>();
		null_counts = new HashMap<String,Double>();
		avg_freq = new HashMap<String,Double>();
		uniques = new HashMap<String,Double>();
	}
	
	public double getEntropy(String demographic){
		if(demographic != null){
			Double e = entropy.get(demographic);
			if(e != null){
				return e.doubleValue();
			}
		}
		return 0;
	}
	
	public void setEntropy(String demographic, double value){
		entropy.put(demographic, new Double(value));
	}
	
	public double getMaxEntropy(String demographic){
		if(demographic != null){
			Double e = max_entropy.get(demographic);
			if(e != null){
				return e.doubleValue();
			}
		}
		return 0;
	}
	
	public void setMaxEntropy(String demographic, double value){
		max_entropy.put(demographic, new Double(value));
	}
	
	public double getNullCount(String demographic){
		if(demographic != null){
			Double n = null_counts.get(demographic);
			if(n != null){
				return n.doubleValue();
			}
		}
		return 0;
	}
	
	public void setNullCount(String demographic, double value){
		null_counts.put(demographic, new Double(value));
	}
	
	public double getAverageFrequency(String demographic){
		if(demographic != null){
			Double avg = avg_freq.get(demographic);
			if(avg != null){
				return avg.doubleValue();
			}
		}
		return 0;
	}
	
	public void setAverageFrequency(String demographic, double value){
		avg_freq.put(demographic, new Double(value));
	}
	
	public double getUniqueValueCount(String demographic){
		if(demographic != null){
			Double u = uniques.get(demographic);
			if(u != null){
				return u.doubleValue();
			}
		}
		return 0;
	}
	
	public void setUniqueValueCount(String demographic, double value){
		uniques.put(demographic, new Double(value));
	}
}
