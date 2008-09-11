package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.PatientMatchingActivator;
import org.regenstrief.linkage.Record;

/**
 * Class stores the list of Record pairs from the given FormPairs in an OpenMRS
 * module.
 * @author jegg
 *
 */

public class OpenMRSLookupFormPairs extends LookupFormPairs {
	
	List<int[]> ids;
	
	// place in list for getNextRecordPair() and reset methods
	int current_pair;
	
	public OpenMRSLookupFormPairs(FormPairs fp){
		super(fp);
		ids = new ArrayList<int[]>();
		fillIDList();
		reset();
	}
	
	protected void fillIDList(){
		Record[] pair;
		while((pair = fp.getNextRecordPair()) != null){
			int[] id_pair = new int[2];
			id_pair[0] = pair[0].getUID();
			id_pair[2] = pair[1].getUID();
			ids.add(id_pair);
		}
	}
	
	@Override
	protected Record getRecordFromUID(int id, String context) {
		Patient p = Context.getPatientService().getPatient(id);
		if(p != null){
			return PatientMatchingActivator.patientToRecord(p);
		} else {
			return null;
		}
		
	}

	@Override
	public Record[] getRecordPair(int index) {
		Record[] ret = new Record[2];
		int[] pair = ids.get(index);
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
		if(current_pair < ids.size()){
			return getRecordPair(current_pair++);
		} else {
			return null;
		}
	}
	
	public int size(){
		return ids.size();
	}

}
