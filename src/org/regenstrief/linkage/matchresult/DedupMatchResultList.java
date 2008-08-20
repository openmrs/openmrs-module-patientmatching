/**
 */
package org.regenstrief.linkage.matchresult;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

/**
 *
 */
public class DedupMatchResultList extends MatchResultList {
    
    private Hashtable<Integer, Hashtable<Integer, Boolean>> encounteredIds;
    
    public DedupMatchResultList() {
        super();
        encounteredIds = new Hashtable<Integer, Hashtable<Integer,Boolean>>();
    }

    /**
     * @see org.regenstrief.linkage.matchresult.MatchResultList#acceptMatchResult(org.regenstrief.linkage.MatchResult)
     */
    @Override
    public synchronized void acceptMatchResult(MatchResult mr) {
        if (mr.getScore() > mr.getMatchingConfig().getScoreThreshold()) {
            Record r1 = mr.getRecord1();
            Record r2 = mr.getRecord2();
            
            int smallerId = r1.getUID();
            int biggerId = r2.getUID();
            if(r2.getUID() < r1.getUID()) {
                smallerId = r2.getUID();
                biggerId = r1.getUID();
            }
            
            Hashtable<Integer, Boolean> encounteredIdPairs = encounteredIds.get(new Integer(smallerId));
            if(encounteredIdPairs == null) {
                encounteredIdPairs = new Hashtable<Integer, Boolean>();
                encounteredIds.put(new Integer(smallerId), encounteredIdPairs);
            }
            
            Boolean encountered = encounteredIdPairs.get(new Integer(biggerId));
            if(encountered == null) {
                // this id is not found yet
                super.acceptMatchResult(mr);
                encounteredIdPairs.put(new Integer(biggerId), new Boolean(true));
            }
        }
    }
    
    public List<MatchResult> getResults() {
        return results;
    }
    
    public int size() {
        return results.size();
    }
    
    public void sort() {
        Collections.sort(results, Collections.reverseOrder());
    }
}
