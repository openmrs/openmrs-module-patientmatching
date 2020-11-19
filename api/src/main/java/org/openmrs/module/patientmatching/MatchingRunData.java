/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton class providing access to data used in current report.
 */
public class MatchingRunData {
	
	protected static final Log log = LogFactory.getLog(MatchingRunData.class);
	
	private boolean timerTaskStarted;
	
	private List<Long> proTimeList;
	
	private String fileStrat;
	
	private String runName;
	
	private static final List<String> runningTasks = Collections.synchronizedList(new ArrayList());
	
	/**
	 * default constructor, initializes variables
	 */
	private MatchingRunData() {
		timerTaskStarted = false;
	}
	
	/**
	 * Creates a MatchingRunData instance
	 *
	 * @return singleton MatchingRunData instance
	 */
	public static MatchingRunData createInstance() {
		return new MatchingRunData();
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
	
	/**
	 * Gets the runName
	 *
	 * @return the runName
	 */
	public String getRunName() {
		return runName;
	}
	
	/**
	 * Sets the runName
	 *
	 * @param runName the runName to set
	 */
	public void setRunName(String runName) {
		this.runName = runName;
	}
	
	public boolean isTimerTaskStarted() {
		return timerTaskStarted;
	}
	
	public void setTimerTaskStarted(boolean timerTaskStarted) {
		this.timerTaskStarted = timerTaskStarted;
	}
	
	/**
	 * Gets the count of the currently running patient matching tasks
	 * 
	 * @return the count of the running patient matching tasks
	 */
	public static int getRunningTaskCount() {
		return runningTasks.size();
	}
	
	/**
	 * Adds the task name to the running tasks
	 */
	public static void addTask(String name) {
		runningTasks.add(name);
		log.debug("Added " + name + " to running patient matching tasks");
	}
	
	/**
	 * Removed the task name to the running tasks
	 */
	public static void removeTask(String name) {
		runningTasks.remove(name);
		log.debug("Removed " + name + " from running patient matching tasks");
	}
	
}
