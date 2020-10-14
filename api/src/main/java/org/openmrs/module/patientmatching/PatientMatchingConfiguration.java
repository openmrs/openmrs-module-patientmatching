/**
 *
 */
package org.openmrs.module.patientmatching;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A single class that represent a configuration that will be displayed in the page to create a
 * matching configuration. This class will be used to read from the configuration file in the file
 * system and display it in the appropriate web page and vice versa. A single configuration is the
 * elements inside the "run" tag in the actual configuration file saved in the file system.
 */
public class PatientMatchingConfiguration implements Comparable<PatientMatchingConfiguration> {
	
	/**
	 * A unique configuration Id to identify each PatientMatchingConfiguration.
	 */
	private int configurationId;
	
	/**
	 * Current configuration name. Correspond to the "name" attribute of the "run" tag in the
	 * configuration file.
	 */
	private String configurationName;
	
	/**
	 * Define whether current configuration will perform random sampling from all record before
	 * performing the EM analysis. Correspond to the "random-sample" attribute of the "run" tag in the
	 * configuration file.
	 */
	private boolean usingRandomSample;
	
	/**
	 * Define the size of the random sample that will be used in the random sample analysis. Correspond
	 * to the "sample-size" attribute of the "run" tag in the configuration file.
	 */
	private int randomSampleSize;
	
	/**
	 * The estimated number of pairs that the strategy will produce. The data is saved based on the
	 * calculations by the time of strategy creation.
	 */
	private long estimatedPairs;
	
	/**
	 * A rough estimation of the time that the strategy will take to run
	 */
	private long estimatedTime;
	
	/**
	 * Total number of records of the database at the time the strategy is saved This will be taken into
	 * consideration to recalculate the estimations if the total number of records has been changed when
	 * the strategy is used.
	 */
	private long totalRecords;
	
	/**
	 * Define the list of configuration entries for the configuration. Correspond to each element inside
	 * the "run" tag in the configuration file.
	 */
	private SortedSet<ConfigurationEntry> configurationEntries;
	
	/**
	 * Default no argument constructor
	 */
	public PatientMatchingConfiguration() {
		super();
	}
	
	/**
	 * Return the list of <code>ConfigurationEntry</code> elements for the current matching
	 * configuration page. Correspond to each element in the "run" tag of the the configuration file.
	 * 
	 * @return the configEntries
	 */
	public SortedSet<ConfigurationEntry> getConfigurationEntries() {
		if (configurationEntries == null)
			configurationEntries = new TreeSet<ConfigurationEntry>();
		return configurationEntries;
	}
	
	/**
	 * Set the current list of <code>ConfigurationEntry</code> elements for the current matching page.
	 * Correspond to each element in the "run" tag of the the configuration file.
	 * 
	 * @param newEntries the configEntries to set
	 */
	public void setConfigurationEntries(Set<ConfigurationEntry> newEntries) {
		if (this.configurationEntries == null) {
			this.configurationEntries = (SortedSet<ConfigurationEntry>) newEntries;
		} else {
			this.configurationEntries.retainAll(newEntries);
			this.configurationEntries.addAll(newEntries);
		}
	}
	
	public int getConfigurationId() {
		return configurationId;
	}
	
	public void setConfigurationId(int configurationId) {
		this.configurationId = configurationId;
	}
	
	/**
	 * Return the current configuration element name. Correspond to the "name" attribute of the "run"
	 * tag in the configuration file.
	 * 
	 * @return the configName
	 */
	public String getConfigurationName() {
		return configurationName;
	}
	
	/**
	 * Change the current configuration element name. Correspond to the "name" attribute of the "run"
	 * tag in the configuration file.
	 * 
	 * @param configurationName the configName to set
	 */
	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}
	
	/**
	 * Check to see if the current configuration will use random sampling process. Correspond to the
	 * "random-sample" attribute of the "run" tag in the configuration file.
	 * 
	 * @return the useRandomSampling
	 */
	public boolean isUsingRandomSample() {
		return usingRandomSample;
	}
	
	/**
	 * Change the flag whether current configuration will use random sampling process or not. Correspond
	 * to the "random-sample" attribute of the "run" tag in the configuration file.
	 * 
	 * @param usingRandomSample the useRandomSampling to set
	 */
	public void setUsingRandomSample(boolean usingRandomSample) {
		this.usingRandomSample = usingRandomSample;
	}
	
	/**
	 * Return the random sample size that will be used if current configuration will perform random
	 * sampling process. Correspond to the "sample-size" attribute of the "run" tag in the configuration
	 * file.
	 * 
	 * @return the randomSampleSize
	 */
	public int getRandomSampleSize() {
		return randomSampleSize;
	}
	
	/**
	 * Change the random sample size for the random sampling process. Correspond to the "sample-size"
	 * attribute of the "run" tag in the configuration file.
	 * 
	 * @param randomSampleSize the randomSampleSize to set
	 */
	public void setRandomSampleSize(int randomSampleSize) {
		this.randomSampleSize = randomSampleSize;
	}
	
	/**
	 * Return the estimated number of pairs that the strategy will produce
	 * 
	 * @return the estimated number of pairs
	 */
	public long getEstimatedPairs() {
		return estimatedPairs;
	}
	
	/**
	 * Sets the estimated number of pairs that the strategy will produce
	 * 
	 * @param estimatedPairs The estimated number of pairs
	 */
	public void setEstimatedPairs(long estimatedPairs) {
		this.estimatedPairs = estimatedPairs;
	}
	
	/**
	 * Returns the estimated time (in milliseconds) that the strategy will take to run
	 * 
	 * @return the time taken to run the strategy in ms
	 */
	public long getEstimatedTime() {
		return estimatedTime;
	}
	
	/**
	 * Sets the estimated time to run the strategy in milliseconds
	 * 
	 * @param estimatedTime time that will take to run the strategy
	 */
	public void setEstimatedTime(long estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
	
	/**
	 * Returns the total number of records in the database by the strategy creation time
	 * 
	 * @return the total number of records in the database
	 */
	public long getTotalRecords() {
		return totalRecords;
	}
	
	/**
	 * Sets the total number of patient records in the database by the strategy creation time
	 * 
	 * @param totalRecords the total number of records in the database
	 */
	public void setTotalRecords(long totalRecords) {
		this.totalRecords = totalRecords;
	}
	
	/**
	 * Return <code>String</code> representation of the <code>PatientMatchingConfiguration</code> object
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		return this.configurationName;
	}
	
	public int compareTo(PatientMatchingConfiguration o) {
		return 1;
	}
}
