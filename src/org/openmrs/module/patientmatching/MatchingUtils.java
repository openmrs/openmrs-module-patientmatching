package org.openmrs.module.patientmatching;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Utility class to perform various task related to the creating matching
 * configuration.
 */
public class MatchingUtils {

    /**
     * Introspect a class and retrieve all readable and writable properties of
     * that class without including properties defined in the input parameter 
     * <code>listExcludedProperties</code>. This method call will return only
     * simple properties defined in the {@link MatchingIntrospector MatchingIntrospector} class
     * 
     * @param listExcludedProperties list of properties that shouldn't be returned
     * @param clazz class that will be introspected for the properties
     * @return properties list of the class excluding all properties defined in the <code>listExcludedProperties</code>
     * @see MatchingIntrospector
     */
    @SuppressWarnings("unchecked")
    public static List<String> introspectBean(List<String> listExcludedProperties, Class clazz) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
    
        List<String> list = new ArrayList<String>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getReadMethod() != null &&
                    propertyDescriptor.getWriteMethod() != null) {
                boolean exclude = false;
                String propertyName = propertyDescriptor.getName();
                for (String excludedProperty : listExcludedProperties) {
                    if (propertyName == null) {
                        break;
                    }
                    if (excludedProperty.trim().length() == 0) {
                        break;
                    }
                    if (propertyName.toUpperCase().contains(excludedProperty.toUpperCase())) {
                        exclude = true;
                        break;
                    }
                }
                if(!exclude) {
                    if (MatchingIntrospector.isSimpleProperty(propertyDescriptor.getPropertyType())) {
                        list.add(clazz.getName() + "." + propertyDescriptor.getName());
                    }
                }
            }
        }
        
        return list;
    }

    /**
     * Create a list of {@link ConfigurationEntry ConfigurationEntry} for a
     * class simple properties. The list will later be used to create the
     * <code>PatientMatchingConfiguration</code> object.
     * 
     * The simple properties are defined in the {@link MatchingIntrospector MatchingIntrospector}
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

}
