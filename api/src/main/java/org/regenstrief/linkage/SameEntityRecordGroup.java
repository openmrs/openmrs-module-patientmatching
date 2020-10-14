package org.regenstrief.linkage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Class represents a set of Records that are all the same thing and how that decision was made. One
 * link between Records consist of just two Records, A and B. If A,B is a link, and B,C is a
 * different link, this class holds the information that A,B,C is all the same entity. The class's
 * data can be generated automatically, like from a list of RecordLink objects, or explicitly by
 * calling the addRecordToGroup method.
 */

public class SameEntityRecordGroup {
	
	private int group_id;
	
	private Hashtable<Record, List<RecordLink>> group_links;
	
	public SameEntityRecordGroup(int id) {
		group_id = id;
		group_links = new Hashtable<Record, List<RecordLink>>();
	}
	
	/**
	 * Returns the integr group ID for the set of equivalent Records. ID should be unique among groups
	 * created during matching
	 * 
	 * @return the group's ID
	 */
	public int getGroupID() {
		return group_id;
	}
	
	/**
	 * Method adds the given Record to the existing Record group
	 * 
	 * @param r the first Record of the pair
	 * @param rl the second Record of the pair
	 */
	public void addRecordToGroup(Record r, RecordLink rl) {
		List<RecordLink> record_links = group_links.get(r);
		if (record_links == null) {
			record_links = new ArrayList<RecordLink>();
			group_links.put(r, record_links);
		}
		
		if (!record_links.contains(rl)) {
			record_links.add(rl);
		}
	}
	
	/**
	 * Method returns a list of Records in the group that all represent the same thing
	 * 
	 * @return a list of Records in the group
	 */
	public List<Record> getGroupRecords() {
		List<Record> ret = new ArrayList<Record>();
		ret.addAll(group_links.keySet());
		return ret;
	}
	
	/**
	 * Method returns the RecordLink objects that link all of the Records in the group together. Every
	 * record returned by getGroupRecords should be present in one side of a RecordLink, and it should
	 * be possible to go from any Record in the group to any other by following RecordLink pairs. In
	 * other words A,B and B,C and C,D are valid RecordLinks to have, but having just A,B and C,D should
	 * not happen.
	 * 
	 * @return a list of RecordLinks describing relationships between Records in the group
	 */
	public List<RecordLink> getGroupLinks() {
		List<RecordLink> ret = new ArrayList<RecordLink>();
		Enumeration<List<RecordLink>> e = group_links.elements();
		while (e.hasMoreElements()) {
			List<RecordLink> entry = e.nextElement();
			Iterator<RecordLink> it = entry.iterator();
			while (it.hasNext()) {
				RecordLink link = it.next();
				if (!ret.contains(link)) {
					ret.add(link);
				}
			}
		}
		
		return ret;
	}
}
