package org.openmrs.module.patientmatching;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.regenstrief.linkage.MatchItem;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.StringMatch;

/**
 * Utility class to perform various task related to the creating matching configuration.
 */
public class MatchingUtils {
	
	/**
	 * Inspect a class and retrieve all readable and writable properties of that class excluding
	 * properties defined in the input parameter <code>listExcludedProperties</code>. This method call
	 * will return only simple properties defined in the {@link MatchingIntrospector
	 * MatchingIntrospector} class
	 * 
	 * @param listExcludedProperties list of properties that shouldn't be returned
	 * @param clazz class that will be introspected for the properties
	 * @return properties list of the class excluding all properties defined in the
	 *         <code>listExcludedProperties</code>
	 * @see MatchingIntrospector
	 */
	@SuppressWarnings("unchecked")
	public static List<String> introspectBean(List<String> listExcludedProperties, Class clazz) {
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		
		List<String> propertyList = new ArrayList<String>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null) {
				boolean exclude = false;
				String propertyName = propertyDescriptor.getName();
				if (propertyName != null) {
					Iterator<String> exclusions = listExcludedProperties.iterator();
					while (!exclude && exclusions.hasNext()) {
						String excludedProperty = exclusions.next().trim().toUpperCase();
						exclude = StringUtils.isNotBlank(excludedProperty)
						        && propertyName.toUpperCase().contains(excludedProperty);
					}
				}
				if (!exclude) {
					if (MatchingIntrospector.isSimpleProperty(propertyDescriptor.getPropertyType())) {
						propertyList.add(clazz.getName() + "." + propertyDescriptor.getName());
					}
				}
			}
		}
		
		return propertyList;
	}
	
	/**
	 * Create a list of {@link ConfigurationEntry ConfigurationEntry} for a class simple properties. The
	 * list will later be used to create the <code>PatientMatchingConfiguration</code> object. The
	 * simple properties are defined in the {@link MatchingIntrospector MatchingIntrospector}
	 * 
	 * @param listExcludedProperties list of properties that needs to be skipped
	 * @param clazz class that will be extracted for the properties
	 * @return list of <code>ConfigurationEntry</code> for class in the input parameter
	 * @see ConfigurationEntry
	 * @see MatchingIntrospector
	 */
	@SuppressWarnings("unchecked")
	public static List<ConfigurationEntry> generateProperties(List<String> listExcludedProperties, Class clazz) {
		List<ConfigurationEntry> configurationEntries = new ArrayList<ConfigurationEntry>();
		List<String> listNameProperty = introspectBean(listExcludedProperties, clazz);
		for (String fieldName : listNameProperty) {
			ConfigurationEntry configurationEntry = new ConfigurationEntry();
			configurationEntry.setFieldName(fieldName);
			configurationEntry.setFieldViewName("patientmatching." + fieldName);
			configurationEntry.setIgnored();
			configurationEntries.add(configurationEntry);
		}
		return configurationEntries;
	}
	
	/**
	 * Matches the specified strings using the specified algorithm and threshold
	 * 
	 * @param algorithm the algorithm to use
	 * @param threshold the threshold to use
	 * @param s1 the string to compare
	 * @param s2 the other string to compare
	 * @return {@link MatchItem} instance
	 */
	public static MatchItem match(int algorithm, double threshold, String s1, String s2) {
		if (s1 == null || s2 == null) {
			return new MatchItem(0, false);
		}
		
		final float similarity;
		final boolean match;
		switch (algorithm) {
			case (MatchingConfig.EXACT_MATCH):
				match = StringMatch.exactMatch(s1, s2);
				similarity = match ? 1 : 0;
				break;
			
			case (MatchingConfig.JWC):
				similarity = StringMatch.getJWCMatchSimilarity(s1, s2);
				match = similarity > threshold;
				break;
			
			case (MatchingConfig.LCS):
				similarity = StringMatch.getLCSMatchSimilarity(s1, s2);
				match = similarity > threshold;
				break;
			
			case (MatchingConfig.LEV):
				similarity = StringMatch.getLEVMatchSimilarity(s1, s2);
				match = similarity > threshold;
				break;
			
			case (MatchingConfig.DICE):
				similarity = StringMatch.getDiceMatchSimilarity(s1, s2);
				match = similarity > threshold;
				break;
			
			default:
				throw new IllegalArgumentException("Unexpected algorithm: " + algorithm);
		}
		
		return new MatchItem(similarity, match);
	}
	
	/**
	 * Returns a list of all possible combinations of candidates from multiple field demographics
	 *
	 * @param data1
	 * @param data2
	 * @return a list of all possible permutations
	 */
	public static List<String[]> getCandidatesFromMultiFieldDemographics(String data1, String data2) {
		String[] a = data1.split(MatchingConstants.MULTI_FIELD_DELIMITER);
		String[] b = data2.split(MatchingConstants.MULTI_FIELD_DELIMITER);
		
		List<String[]> res = new ArrayList();
		for (String i : a) {
			for (String j : b) {
				res.add(new String[] { i, j });
			}
		}
		
		return res;
	}
	
}
