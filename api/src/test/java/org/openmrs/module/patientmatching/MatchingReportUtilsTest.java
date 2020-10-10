package org.openmrs.module.patientmatching;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mysql.jdbc.Driver;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.regenstrief.linkage.util.MatchingConfig;
import org.regenstrief.linkage.util.RecMatchConfig;
import org.regenstrief.linkage.util.XMLTranslator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, OpenmrsUtil.class, XMLTranslator.class, DriverManagerConnectionFactory.class })
public class MatchingReportUtilsTest {
	
	@Mock
	private Properties mockProperties;
	
	@Mock
	private File mockSerialFolder;
	
	@Mock
	private File mockConfigFolder;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(Context.class);
		PowerMockito.spy(OpenmrsUtil.class);
		PowerMockito.mockStatic(XMLTranslator.class);
		PowerMockito.mockStatic(DriverManager.class);
		MockitoAnnotations.initMocks(this);
		Whitebox.setInternalState(mockConfigFolder, "path", "some-path");
        when(Context.getRuntimeProperties()).thenReturn(mockProperties);
        when(mockProperties.getProperty("connection.driver_class")).thenReturn(Driver.class.getName());
		when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(MatchingConstants.CONFIG_FOLDER_NAME))
		        .thenReturn(mockConfigFolder);
		when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(MatchingConstants.SERIAL_FOLDER_NAME))
		        .thenReturn(mockSerialFolder);
		when(mockSerialFolder.listFiles()).thenReturn(new File[] {});
	}
	
	@Test
	public void ReadConfigFile_shouldUseTheMatchingConfigFromTheXmlConfigFile() {
		Map<String, Object> objects = new HashMap();
		final String strategy1 = "strategy1";
		MatchingConfig matchingConfig = new MatchingConfig(strategy1, new String[] {});
		RecMatchConfig recMatchConfig = new RecMatchConfig();
		recMatchConfig.getMatchingConfigs().add(matchingConfig);
		when(XMLTranslator.createRecMatchConfig(any())).thenReturn(recMatchConfig);
		
		Map<String, Object> result = MatchingReportUtils.ReadConfigFile(objects, new String[] { "strategy1" });
		
		Assert.assertEquals(matchingConfig, ((List) result.get("matchingConfigLists")).get(0));
	}
	
}
