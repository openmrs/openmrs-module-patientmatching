package org.regenstrief.linkage.entropy;

import java.io.*;
import java.util.*;

/*
 * Class takes a file, column index, and delimiter and calculates
 * the entropy of that column in the file
 */

public class ColumnEntropy {
	
	final static int SAMPLE_SIZE = 2000;
	
	boolean count_nulls;
	
	public ColumnEntropy() {
		this.count_nulls = false;
		
	}
	
	public ColumnEntropy(boolean count_nulls) {
		this.count_nulls = count_nulls;
		
	}
	
	public void setCountNulls(boolean b) {
		count_nulls = b;
	}
	
	/*
	 * Method randomly samples lines of the csv file to
	 * get an approximateion of the columns entropy
	 */
	public double guessEntropy(ColumnEntropyProvider cep) {
		double entropy = 0;
		Hashtable<String, Integer> element_count = new Hashtable<String, Integer>();
		int total_elements = 0;
		int null_count = 0;
		
		if (cep.supportsEstimate()) {
			// work out how to handle sub-classing/interfaces later
			FileEntropyProvider fep = (FileEntropyProvider) cep;
			List<String> elements = fep.getRandomSet(SAMPLE_SIZE);
			Iterator<String> it = elements.iterator();
			while (it.hasNext()) {
				String element = it.next();
				Integer count;
				if (element == null) {
					null_count++;
				} else {
					if ((count = element_count.get(element)) != null) {
						int c = count.intValue();
						element_count.put(element, new Integer(c + 1));
					} else {
						element_count.put(element, new Integer(1));
					}
					total_elements++;
				}
			}
			
			if (count_nulls) {
				total_elements += null_count;
				double frequency = null_count / total_elements;
				entropy += -1 * frequency * (Math.log(frequency) / Math.log(2));
			}
			
			// iterate over the elements in the hash table, calculating the entropy
			it = element_count.keySet().iterator();
			while (it.hasNext()) {
				String element = it.next();
				Integer count = element_count.get(element);
				double frequency = count.doubleValue() / total_elements;
				entropy += -1 * frequency * (Math.log(frequency) / Math.log(2));
			}
		}
		
		return entropy;
	}
	
	public double calculateEntropy(ColumnEntropyProvider cep) {
		double entropy = 0;
		Hashtable<String, Integer> element_count = new Hashtable<String, Integer>();
		int total_elements = 0;
		int null_count = 0;
		
		while (cep.hasNext()) {
			try {
				String element = cep.nextElement();
				Integer count;
				if (element == null) {
					null_count++;
				} else {
					if ((count = element_count.get(element)) != null) {
						int c = count.intValue();
						element_count.put(element, new Integer(c + 1));
					} else {
						element_count.put(element, new Integer(1));
					}
					total_elements++;
				}
			}
			catch (EntropyProviderException epe) {
				//System.err.println("invalid column index for line " + line);
				continue;
			}
			
		}
		// if null values are valid, then add to null_count to total_elements
		// to have an inclusive total_elements, and calculate null values' 
		// contribution to entropy
		if (count_nulls) {
			total_elements += null_count;
			double frequency = null_count / total_elements;
			entropy += -1 * frequency * (Math.log(frequency) / Math.log(2));
		}
		
		// iterate over the elements in the hash table, calculating the entropy
		Iterator<String> it = element_count.keySet().iterator();
		while (it.hasNext()) {
			String element = it.next();
			Integer count = element_count.get(element);
			double frequency = count.doubleValue() / total_elements;
			entropy += -1 * frequency * (Math.log(frequency) / Math.log(2));
		}
		
		return entropy;
	}
	
	public static void main(String[] argv) {
		if (argv.length != 3) {
			System.out.println("usage: java ColumnEntropy <file> <column index> <delim>");
			System.exit(0);
		}
		File f = new File(argv[0]);
		int index = Integer.parseInt(argv[1]);
		String d = argv[2];
		char delim;
		if (d.length() > 1) {
			System.out.println("delimiter must be one charcter long");
			System.exit(0);
		}
		delim = d.charAt(0);
		
		// create ColumnEntropy object and get entropy for column
		System.out.println("Starting at:\t" + new Date());
		FileEntropyProvider fep = new FileEntropyProvider(f, delim, index);
		ColumnEntropy ce = new ColumnEntropy();
		double entropy = ce.calculateEntropy(fep);
		System.out.println("entropy:\t" + entropy);
		entropy = ce.guessEntropy(fep);
		System.out.println("entropy:\t" + entropy);
		System.out.println("Finished at:\t" + new Date());
	}
}
