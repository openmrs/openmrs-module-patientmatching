package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;

/**
 * Class stores the list of Record pairs from the given FormPairs in an OpenMRS module.
 * 
 * @author jegg
 */

public class OpenMRSLookupFormPairs extends LookupFormPairs {
	
	List<long[]> ids;
	
	// place in list for getNextRecordPair() and reset methods
	int current_pair;
	
	public OpenMRSLookupFormPairs(FormPairs fp) {
		super(fp);
		ids = new ArrayList<long[]>();
		fillIDList();
		reset();
	}
	
	protected void fillIDList() {
		Record[] pair;
		while ((pair = fp.getNextRecordPair()) != null) {
			long[] id_pair = new long[2];
			id_pair[0] = pair[0].getUID();
			id_pair[1] = pair[1].getUID();
			ids.add(id_pair);
		}
	}
	
	@Override
	protected Record getRecordFromUID(long id, String context) {
		int int_id = (int) id;
		Patient p = Context.getPatientService().getPatient(int_id);
		if (p != null) {
			return LinkDBConnections.getInstance().patientToRecord(p);
		} else {
			return null;
		}
		
	}
	
	@Override
	public Record[] getRecordPair(int index) {
		Record[] ret = new Record[2];
		long[] pair = ids.get(index);
		ret[0] = getRecordFromUID(pair[0], "OpenMRS");
		ret[1] = getRecordFromUID(pair[1], "OpenMRS");
		return ret;
	}
	
	@Override
	public boolean reset() {
		current_pair = 0;
		return true;
	}
	
	@Override
	public Record[] getNextRecordPair() {
		if (current_pair < ids.size()) {
			return getRecordPair(current_pair++);
		} else {
			return null;
		}
	}
	
	public int size() {
		return ids.size();
	}
	
}
