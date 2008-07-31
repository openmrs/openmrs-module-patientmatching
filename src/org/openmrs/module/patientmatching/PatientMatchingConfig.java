/**
 * Auto generated file comment
 */
package org.openmrs.module.patientmatching;

import java.util.List;

/**
 *
 */
public class PatientMatchingConfig {
    
    private String configName;
    
    private boolean useRandomSampling;
    
    private int randomSampleSize;
    
    private List<ConfigEntry> configEntries;

    /**
     * @return the configEntries
     */
    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    /**
     * @param configEntries the configEntries to set
     */
    public void setConfigEntries(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    /**
     * @return the configName
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * @param configName the configName to set
     */
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * @return the useRandomSampling
     */
    public boolean isUseRandomSampling() {
        return useRandomSampling;
    }

    /**
     * @param useRandomSampling the useRandomSampling to set
     */
    public void setUseRandomSampling(boolean useRandomSampling) {
        this.useRandomSampling = useRandomSampling;
    }

    /**
     * @return the randomSampleSize
     */
    public int getRandomSampleSize() {
        return randomSampleSize;
    }

    /**
     * @param randomSampleSize the randomSampleSize to set
     */
    public void setRandomSampleSize(int randomSampleSize) {
        this.randomSampleSize = randomSampleSize;
    }
    
    public String toString() {
        return this.configName;
    }

}
