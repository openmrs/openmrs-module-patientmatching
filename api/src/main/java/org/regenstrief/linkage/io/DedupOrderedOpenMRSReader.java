package org.regenstrief.linkage.io;

import org.hibernate.SessionFactory;
import org.regenstrief.linkage.util.MatchingConfig;

public class DedupOrderedOpenMRSReader extends OrderedOpenMRSReader {
	
	public DedupOrderedOpenMRSReader(MatchingConfig mc, SessionFactory session_factory) {
		super(mc, session_factory);
	}
}
