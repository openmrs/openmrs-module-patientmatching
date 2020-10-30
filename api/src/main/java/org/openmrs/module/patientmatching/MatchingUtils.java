package org.openmrs.module.patientmatching;

import static org.openmrs.module.patientmatching.MatchingConstants.CONCATENATED_FIELD_PREFIX;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.MatchItem;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
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
	
	/**
	 * Utility method which gets a map of setIds and the List of transposable fields that belong to it
	 * which are generated from the specified {@link MatchingConfig}
	 * 
	 * @param mc the MatchConfig object to use
	 * @return a Map instance
	 */
	public static Map<String, List<String>> getSetIdAndFieldsMap(MatchingConfig mc) {
		Map<String, List<String>> mapTemp = new HashMap();
		for (MatchingConfigRow row : mc.getIncludedColumns()) {
			if (StringUtils.isNotBlank(row.getSetID()) && !"0".equals(row.getSetID())) {
				if (mapTemp.get(row.getSetID()) == null) {
					mapTemp.put(row.getSetID(), new ArrayList());
				}
				
				mapTemp.get(row.getSetID()).add(row.getName());
			}
		}
		
		Map<String, List<String>> map = new HashMap(mapTemp.size());
		//Exclude a set with less than 2 fields
		mapTemp.forEach((setId, fields) -> {
			if (fields.size() > 1) {
				map.put(setId, fields);
			}
		});
		
		return map;
	}
	
	/**
	 * Gets a mapping of the concatenated fields and their values for the specified record using the
	 * specified setId and fields map
	 * 
	 * @param record the Record instance
	 * @param setIdAndFieldsMap a map of setIds and fields that belong to it
	 * @return Map Instance
	 */
	public static Map<String, String> getConcatValueMap(Record record, Map<String, List<String>> setIdAndFieldsMap) {
		Map<String, String> concats = new HashMap();
		setIdAndFieldsMap.forEach((setId, fields) -> {
			String concatValue = "";
			for (final String demographic : fields) {
				String fieldValue = record.getDemographic(demographic);
				if (StringUtils.isNotBlank(fieldValue)) {
					concatValue += fieldValue;
				}
			}
			
			concats.put(CONCATENATED_FIELD_PREFIX + setId, concatValue);
		});
		
		return concats;
	}
	
	public static File getConfigFile() {
		String value = Context.getAdministrationService().getGlobalProperty(MatchingConstants.GP_CONFIG_FILE);
		if (StringUtils.isBlank(value)) {
			value = MatchingConstants.CONFIG_FILE_DEFAULT;
		}
		
		Path path = Paths.get(value);
		File configFile;
		if (path.isAbsolute()) {
			configFile = path.toFile();
		} else {
			File configFileDir;
			if (path.getParent() != null) {
				configFileDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(path.getParent().toString());
			} else {
				configFileDir = new File(OpenmrsUtil.getApplicationDataDirectory());
			}
			
			configFile = new File(configFileDir, path.getFileName().toString());
		}
		
		return configFile;
	}
	
	public static File getConfigFolder() {
		String value = Context.getAdministrationService().getGlobalProperty(MatchingConstants.GP_CONFIG_DIR);
		if (StringUtils.isBlank(value)) {
			value = MatchingConstants.CONFIG_DIR_DEFAULT;
		}
		
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(value);
	}
	
	public static File getSerializationFolder() {
		String value = Context.getAdministrationService().getGlobalProperty(MatchingConstants.GP_SERIAL_DIR);
		if (StringUtils.isBlank(value)) {
			value = MatchingConstants.SERIAL_DIR_DEFAULT;
		}
		
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(value);
	}
	
}
