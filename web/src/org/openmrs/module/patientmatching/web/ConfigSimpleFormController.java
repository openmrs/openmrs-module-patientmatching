package org.openmrs.module.patientmatching.web;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.ConfigEntry;
import org.openmrs.module.patientmatching.PatientMatchingConfig;
import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.util.DataColumn;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.MatchingConfigRow;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigSimpleFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected PatientMatchingConfig formBackingObject(HttpServletRequest request) throws Exception {
	    String fileName = request.getParameter("filename");
        PatientMatchingConfig config = new PatientMatchingConfig();
	    if (fileName != null) {
	        File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory("patient-matching");
	        log.info("Opening: " + folder.getAbsolutePath());
	        File f = new File(folder, fileName);
	        RecMatchConfig recMatchConfig = XMLTranslator.createRecMatchConfig(XMLTranslator.getXMLDocFromFile(f));
	        
	        MatchingConfig matchingConfig = recMatchConfig.getMatchingConfigs().get(0);
	        List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
            List<String> includeColumn = Arrays.asList(matchingConfig.getIncludedColumnsNames());
            List<String> blockingColumn = Arrays.asList(matchingConfig.getBlockingColumns());
	        
	        for (DataColumn column : recMatchConfig.getLinkDataSource1().getDataColumns()) {
                ConfigEntry configEntry = new ConfigEntry();
                configEntry.setFieldName(column.getColumnID());
                if (includeColumn.contains(column.getColumnID())) {
                    configEntry.setSelected(true);
                }
                if (blockingColumn.contains(column.getColumnID())) {
                    configEntry.setBlocking(true);
                }
                configEntries.add(configEntry);
            }
	        Collections.sort(configEntries);
	        config.setConfigName(matchingConfig.getName());
	        config.setRandomSampleSize(matchingConfig.getRandomSampleSize());
	        config.setUseRandomSampling(matchingConfig.isUsingRandomSampling());
	        config.setConfigEntries(configEntries);
	    } else {
	        config.setUseRandomSampling(false);
	        config.setRandomSampleSize(100000);
	        config.setConfigName("New Configuration");
	        
	        List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
	        
	        AdministrationService adminService = Context.getAdministrationService();
	        String excludedProperties = adminService.getGlobalProperty("patientmatching.excludedProperties");
	        List<String> listExcludedProperties = Arrays.asList(excludedProperties.split(","));
	        log.info("Excluded Property: " + excludedProperties);
	        
	        List<String> listPatientProperty = introspectBean(listExcludedProperties, Patient.class);
	        for (String fieldName : listPatientProperty) {
	            ConfigEntry configEntry = new ConfigEntry();
	            configEntry.setFieldName(fieldName);
	            configEntry.setSelected(new Boolean(false));
	            configEntries.add(configEntry);
	        }
	        
	        List<String> listAddressProperty = introspectBean(listExcludedProperties, PersonAddress.class);
	        for (String fieldName : listAddressProperty) {
	            ConfigEntry configEntry = new ConfigEntry();
	            configEntry.setFieldName(fieldName);
	            configEntry.setSelected(new Boolean(false));
	            configEntries.add(configEntry);
	        }
	        
	        List<String> listNameProperty = introspectBean(listExcludedProperties, PersonName.class);
	        for (String fieldName : listNameProperty) {
	            ConfigEntry configEntry = new ConfigEntry();
	            configEntry.setFieldName(fieldName);
	            configEntry.setSelected(new Boolean(false));
	            configEntries.add(configEntry);
	        }

            Collections.sort(configEntries);
	        config.setConfigEntries(configEntries);
	    }
		return config;
		
	}

    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        PatientMatchingConfig config = (PatientMatchingConfig) command;
        
        // this should be delegated to a service object
        Hashtable<String, ConfigEntry> map = new Hashtable<String, ConfigEntry>();
        for (ConfigEntry entry : config.getConfigEntries()) {
            map.put(entry.getFieldName(), entry);
        }
        
        String[] demographic = map.keySet().toArray(new String[map.keySet().size()]);
        MatchingConfig matchingConfig = new MatchingConfig(config.getConfigName(), demographic);
        matchingConfig.setUsingRandomSampling(config.isUseRandomSampling());
        matchingConfig.setRandomSampleSize(config.getRandomSampleSize());
        
        LinkDataSource linkDataSource = new LinkDataSource("dummy", "dummy", "dummy", 1);
        
        int blockOrder = 1;
        List<MatchingConfigRow> configRows = matchingConfig.getMatchingConfigRows();
        for (MatchingConfigRow matchingConfigRow : configRows) {
            ConfigEntry configEntry = map.get(matchingConfigRow.getName());
            matchingConfigRow.setInclude(configEntry.isSelected());
            if(configEntry.isBlocking()) {
                matchingConfigRow.setBlockOrder(blockOrder);
                blockOrder ++;
            }
            linkDataSource.addNewDataColumn(matchingConfigRow.getName());
        }
        
        List<MatchingConfig> matchingConfigList = new ArrayList<MatchingConfig>();
        matchingConfigList.add(matchingConfig);
        
        
        RecMatchConfig recMatchConfig = new RecMatchConfig(linkDataSource, linkDataSource, matchingConfigList);
        File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory("patient-matching");
        File f = new File(folder, config.getConfigName());
        log.info("writing to file: " + f.getAbsolutePath());
        XMLTranslator.writeXMLDocToFile(XMLTranslator.toXML(recMatchConfig), f);
        log.info("finish writing file");
        
        Map<String, String> model = new HashMap<String, String>();
//        model.put("fileName", f.getAbsolutePath());
        return new ModelAndView(getSuccessView(), model);
    }

    @SuppressWarnings("unchecked")
    private List<String> introspectBean(List<String> listExcludedProperties, Class clazz) {
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
                    if (excludedProperty.trim().isEmpty()) {
                        break;
                    }
                    if (propertyName.toUpperCase().contains(excludedProperty.toUpperCase())) {
                        exclude = true;
                        break;
                    }
                }
                if(!exclude) {
                    if (PatientIntrospector.isSimpleProperty(propertyDescriptor.getPropertyType())) {
                        list.add("patientmatching." + clazz.getName() + "." + propertyDescriptor.getName());
                    }
                }
            }
        }
        
        return list;
    }
	
}
