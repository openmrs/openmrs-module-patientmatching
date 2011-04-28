package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Class randomly samples a value from a ValueFrequencyTable, weighing the likelihood
 * of each result by the relative frequencies
 * @author jegg
 *
 */

public class WeightedSampler {

	private static Random rand = new Random();
	
	/**
	 * Method returns a value from the ValueFrequencyTable weighted by the values' frequencies
	 * 
	 * @param vft	the values and frequencies to sample from
	 * @return	a String value of the chosen token
	 */
	public static String weightedRandomSample(ValueFrequencyTable vft){
		List<String> values = vft.getValues();
		List<String> space = new ArrayList<String>();
		
		Iterator<String> it = values.iterator();
		long total = 0;
		while(it.hasNext()){
			String value = it.next();
			long f = vft.getFrequency(value);
			total += f;
			for(int i = 0; i < f; i++){
				space.add(value);
			}
		}
		if(space.size() == 0){
			return null;
		}
		int index = rand.nextInt(space.size());
		return space.get(index);
	}
}
