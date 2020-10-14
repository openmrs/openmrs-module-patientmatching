package org.regenstrief.linkage.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Class stores a hashtable of Strings to represent values that should be excluded from being used
 * as a blocking value.
 * 
 * @author jegg
 */

public class BlockingExclusionList implements Cloneable {
	
	private Hashtable<String, List<String>> exclude_regexs;
	
	public BlockingExclusionList() {
		exclude_regexs = new Hashtable<String, List<String>>();
	}
	
	public void addExclusion(String demographic, String regex) {
		List<String> demographic_regexs = exclude_regexs.get(demographic);
		if (demographic_regexs == null) {
			demographic_regexs = new ArrayList<String>();
			exclude_regexs.put(demographic, demographic_regexs);
		}
		demographic_regexs.add(regex);
	}
	
	public boolean isExcludeValue(String demographic, String value) {
		List<String> demographic_regexs = exclude_regexs.get(demographic);
		if (demographic_regexs == null) {
			return false;
		}
		Iterator<String> it = demographic_regexs.iterator();
		while (it.hasNext()) {
			String regex = it.next();
			if (value.toLowerCase().matches(regex)) {
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		String ret = new String();
		
		Enumeration<String> e = exclude_regexs.keys();
		while (e.hasMoreElements()) {
			String demographic = e.nextElement();
			ret += demographic + "\n";
			List<String> demographic_regexs = exclude_regexs.get(demographic);
			Iterator<String> it = demographic_regexs.iterator();
			while (it.hasNext()) {
				String regex = it.next();
				ret += "\t" + regex + "\n";
			}
		}
		return ret;
	}
	
	public Object clone() {
		BlockingExclusionList list = null;
		
		try {
			// clone the main object
			list = (BlockingExclusionList) super.clone();
			// clone the hashtable
			list.exclude_regexs = (Hashtable<String, List<String>>) this.exclude_regexs.clone();
			// copy the hashtable value
			Enumeration<String> e = this.exclude_regexs.keys();
			while (e.hasMoreElements()) {
				String demographic = e.nextElement();
				list.exclude_regexs.put(demographic, this.exclude_regexs.get(demographic));
			}
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return list;
	}
}
