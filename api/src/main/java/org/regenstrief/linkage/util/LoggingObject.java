package org.regenstrief.linkage.util;

import org.apache.log4j.Logger;

/**
 * Interface created to allow access to an objects Logger and allow
 * adding a new appender to the logger.  This was created so the
 * GUI can capture logging output and display it for the user, especially
 * with output from an analysis.
 * 
 * @author jegg
 *
 */

public interface LoggingObject {
	public Logger getLogger();
	
}
