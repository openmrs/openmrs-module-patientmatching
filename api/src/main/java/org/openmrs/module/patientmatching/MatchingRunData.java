/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.patientmatching;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton class providing access to data used in current report.
 */
public class MatchingRunData {

	protected final Log log = LogFactory.getLog(getClass());

	private boolean timerTaskStarted;
	private List<Long> proTimeList;
	private String fileStrat;
	private String rptname;

	/**
	 * default constructor, initializes variables
	 */
	private MatchingRunData() {
		timerTaskStarted = false;
	}

	/**
	 * Bill Pugh form of lazy initialization for singleton instances
	 */
	private static class MatchingRunDataSingleton {
		public static final MatchingRunData instance = new MatchingRunData();
	}

	/**
	 * getter for singleton instance of MatchingRunData
	 *
	 * @return singleton MatchingRunData instance
	 */
	public static MatchingRunData getInstance() {
		return MatchingRunDataSingleton.instance;
	}

	public String getFileStrat() {
		return fileStrat;
	}

	public void setFileStrat(String fileStrat) {
		this.fileStrat = fileStrat;
	}

	public List<Long> getProTimeList() {
		return proTimeList;
	}

	public void setProTimeList(List<Long> proTimeList) {
		this.proTimeList = proTimeList;
	}

	public String getRptname() {
		return rptname;
	}

	public void setRptname(String rptname) {
		this.rptname = rptname;
	}

	public boolean isTimerTaskStarted() {
		return timerTaskStarted;
	}

	public void setTimerTaskStarted(boolean timerTaskStarted) {
		this.timerTaskStarted = timerTaskStarted;
	}
}
