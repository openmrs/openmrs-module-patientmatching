package org.openmrs.module.patientmatching;

import java.util.Hashtable;

import org.regenstrief.linkage.MatchFinder;
import org.regenstrief.linkage.analysis.RecordFieldAnalyzer;
import org.regenstrief.linkage.db.RecordDBManager;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.RecMatchConfig;

public class LinkDBConnections {
	private static Hashtable<RecMatchConfig,MatchFinder> finders = new Hashtable<RecMatchConfig,MatchFinder>();
	private static Hashtable<LinkDataSource,RecordDBManager> link_dbs = new Hashtable<LinkDataSource,RecordDBManager>();
	
	private LinkDBConnections(){};
	
	public static synchronized MatchFinder getFinder(RecMatchConfig rmc){
		MatchFinder ret;
		if((ret = finders.get(rmc)) == null){
			ret = new MatchFinder(rmc.getLinkDataSource1(), rmc.getMatchingConfigs(), new RecordFieldAnalyzer(),MatchFinder.Scoring.BLOCKING_INCLUSIVE);
			finders.put(rmc, ret);
		}
		return ret;
	}
	
	public static synchronized RecordDBManager getLinkDBManager(LinkDataSource lds){
		RecordDBManager ret;
		if((ret = link_dbs.get(lds)) == null){
			ret = new RecordDBManager(lds);
			link_dbs.put(lds, ret);
		}
		return ret;
	}
}
