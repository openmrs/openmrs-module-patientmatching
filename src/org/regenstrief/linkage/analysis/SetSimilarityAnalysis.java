package org.regenstrief.linkage.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.RecordLink;
import org.regenstrief.linkage.SameEntityRecordGroup;

/**
 * Class written to group like Records together.  If Records A, B, C, and D
 * were matched, and the matching determined that A = B and B = D, then
 * A should be grouped with D.
 * 
 * Normal matching returns a list of MatchResults.  This object takes that list
 * and returns a list of list of MatchResults, where every MatchResult from the
 * argument List is present in the return object, even if it's one element lists.
 * 
 * Where a MatchResult is A=B, a List of MatchResults like
 * {A=B,C=D,E=F,C=G}
 * would be returned as
 * {{A=B},{C=D,C=G},{E=F}}
 * 
 * 
 * @author jegg
 *
 */

public class SetSimilarityAnalysis {
	
	/**
	 * Method returns the RecordLink objects in groups based on transitive analysis
	 * 
	 * @param link	a list of links between Records
	 * @return	a list of SameEntityGroups where each group of RecordLinks refers to the same entity
	 */
	/*
	 * Currently, code just calls the old method getSimilarSets
	 */
	public List<SameEntityRecordGroup> getRecordGroups(List<RecordLink> links){
		List<SameEntityRecordGroup> ret = null;
		List<List<RecordLink>> grouped_lists = getSimilarSets(links);
		if(grouped_lists != null){
			ret = new ArrayList<SameEntityRecordGroup>();
		} else {
			return null;
		}
		int group_id = 0;
		Iterator<List<RecordLink>> it = grouped_lists.iterator();
		while(it.hasNext()){
			List<RecordLink> entity_links = it.next();
			SameEntityRecordGroup serg = new SameEntityRecordGroup(group_id++);
			Iterator<RecordLink> it_rl = entity_links.iterator();
			while(it_rl.hasNext()){
				RecordLink rl = it_rl.next();
				serg.addRecordToGroup(rl.getRecord1(), rl);
				serg.addRecordToGroup(rl.getRecord2(), rl);
			}
			ret.add(serg);
		}
		return ret;
	}
	
	/**
	 * Method returns the argument list separated out into sub lists
	 * based on similarity between both Records in the MatchResult objects
	 * 
	 * @param links	a single list of all the MatchResult objects
	 * @return	a sublists of results, where all MatchResult objects in a sublist have Records in common
	 */
	public List<List<RecordLink>> getSimilarSets(List<RecordLink> links){
		/*
		 * Method not currently implemented, just copies results to ret
		 */
		
		List<List<RecordLink>> ret = new ArrayList<List<RecordLink>>();
		Hashtable<String,List<RecordLink>> buckets = new Hashtable<String,List<RecordLink>>();
		
		Iterator<RecordLink> it = links.iterator();
		while(it.hasNext()){
			RecordLink rl = it.next();
			Record r1 = rl.getRecord1();
			Record r2 = rl.getRecord2();
			String key1 = r1.getContext() + Integer.toString(r1.getUID());
			String key2 = r1.getContext() + Integer.toString(r2.getUID());
			List<RecordLink> bucket1 = buckets.get(key1);
			List<RecordLink> bucket2 = buckets.get(key2);
			if(bucket1 == null && bucket2 == null){
				List<RecordLink> bucket = new ArrayList<RecordLink>();
				bucket.add(rl);
				buckets.put(key1, bucket);
				buckets.put(key2, bucket);
			} else if(bucket1 != null){
				bucket1.add(rl);
				if(bucket2 == null){
					buckets.put(key2, bucket1);
				}
			} else if(bucket2 != null){
				bucket2.add(rl);
				if(bucket1 == null){
					buckets.put(key1, bucket2);
				}
			}
		}
		
		Enumeration<List<RecordLink>> e = buckets.elements();
		while(e.hasMoreElements()){
			List<RecordLink> list = e.nextElement();
			if(!contains(ret, list)){
				ret.add(list);
			}
		}
		
		return ret;
	}
	
	private boolean contains(List<List<RecordLink>> parent, List<RecordLink> element) {
	    boolean found = false;
	    for (List<RecordLink> mList : parent) {
	        // only check element that have equal number of element
	        boolean equal = true;
            if(mList.size() == element.size()) {
                for (int i = 0; i < mList.size(); i++) {
                	RecordLink mListResult = mList.get(i);
                	RecordLink elementResult = element.get(i);
                    
                    int uid11 = mListResult.getRecord1().getUID();
                    int uid12 = mListResult.getRecord2().getUID();
                    
                    int uid21 = elementResult.getRecord1().getUID();
                    int uid22 = elementResult.getRecord2().getUID();
                    
                    if(!((uid11 == uid21 && uid12 == uid22) || (uid11 == uid22 && uid12 == uid21))) {
                        equal = false;
                        break;
                    }
                }
            }
            if (equal) {
                found = true;
                break;
            }
        }
	    return found;
	}
	
	
}
