/**
 */
package org.regenstrief.linkage.matchresult;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.patientmatching.web.RecordSerializer;
import org.regenstrief.linkage.MatchResult;
import org.regenstrief.linkage.Record;

/**
 *
 */
public class DedupMatchResultList extends MatchResultList {
    private Log log = LogFactory.getLog(this.getClass());
    
    private Map<Integer, TreeMap<Integer, Boolean>> encounteredIds;
    
    private Map<Integer, Integer> encounteredIdsOpp;
    
    public DedupMatchResultList() {
        super();
        encounteredIds = new TreeMap<Integer, TreeMap<Integer, Boolean>>();
        encounteredIdsOpp = new TreeMap<Integer, Integer>();
    }

    /**
     * @see org.regenstrief.linkage.matchresult.MatchResultList#acceptMatchResult(org.regenstrief.linkage.MatchResult)
     */
    @Override
    public synchronized void acceptMatchResult(MatchResult mr) {
    	
    	boolean flipped = false;
    	
        if (mr.getScore() > mr.getMatchingConfig().getScoreThreshold()) {
            Record r1 = mr.getRecord1();
            Record r2 = mr.getRecord2();
            
            int smallerId = r1.getUID();
            int biggerId = r2.getUID();
            if(r2.getUID() < r1.getUID()) {
            	flipped = true;
                smallerId = r2.getUID();
                biggerId = r1.getUID();
            }
            
            Integer encounteredIdOppPairs = null;
            TreeMap<Integer, Boolean> encounteredIdPairs = encounteredIds.get(new Integer(smallerId));
            if(encounteredIdPairs == null) {
            	// try the small one first
            	encounteredIdOppPairs = encounteredIdsOpp.get(new Integer(smallerId));
            	// try the other id
            	if (encounteredIdOppPairs == null) {
            		flipped = true;
            		encounteredIdOppPairs = encounteredIdsOpp.get(new Integer(biggerId));
            	} else {
            		encounteredIdPairs = encounteredIds.get(encounteredIdOppPairs);
            	}
            	// both id never been found before
            	if (encounteredIdOppPairs == null) {
                    encounteredIdPairs = new TreeMap<Integer, Boolean>();
                    encounteredIds.put(new Integer(smallerId), encounteredIdPairs);
                    try {
    					if(flipped) {
    						RecordSerializer.serialize(r1);
    					} else {
    						RecordSerializer.serialize(r2);
    					}
    				} catch (IOException e) {
    					log.info("Failed to serialize record object");
    				}
            	}
            }
            
            Boolean encountered = encounteredIdPairs.get(new Integer(biggerId));
            if(encountered == null) {
                // this id is not found yet
                try {
					if(flipped) {
						RecordSerializer.serialize(r2);
					} else {
						RecordSerializer.serialize(r1);
					}
				} catch (IOException e) {
					log.info("Failed to serialize record object");
				}
                encounteredIdPairs.put(new Integer(biggerId), new Boolean(true));
                encounteredIdsOpp.put(new Integer(biggerId), new Integer(smallerId));
            }
        }
    }
    
    public Map<Integer, TreeMap<Integer, Boolean>> getGroupedMatchResult() {
    	return encounteredIds;
    }
}
