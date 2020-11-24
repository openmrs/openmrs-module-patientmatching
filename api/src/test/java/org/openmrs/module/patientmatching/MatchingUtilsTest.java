package org.openmrs.module.patientmatching;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openmrs.module.patientmatching.MatchingConstants.SERIAL_DIR_DEFAULT;
import static org.openmrs.util.OpenmrsUtil.getDirectoryInApplicationDataDirectory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, OpenmrsUtil.class, Paths.class })
public class MatchingUtilsTest {
	
	@Mock
	private AdministrationService mockAdminService;
	
	@Mock
	private File mockFile;
	
	@Mock
	private Path mockPath;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		PowerMockito.mockStatic(OpenmrsUtil.class);
		PowerMockito.mockStatic(Paths.class);
		when(Context.getAdministrationService()).thenReturn(mockAdminService);
	}
	
	/**
	 * @see MatchingUtils#getCandidatesFromMultiFieldDemographics(String, String)
	 * @verifies return a list of all possible permutations
	 */
	@Test
	public void getCandidatesFromMultiFieldDemographics_shouldReturnAListOfAllPossiblePermutations() throws Exception {
		
		// minimum requirement for setting up a MatchingConfig
		String data1 = "101" + MatchingConstants.MULTI_FIELD_DELIMITER + "202";
		String data2 = "303" + MatchingConstants.MULTI_FIELD_DELIMITER + "404" + MatchingConstants.MULTI_FIELD_DELIMITER
		        + "505";
		
		List<String[]> expected = new ArrayList<String[]>();
		expected.add(new String[] { "101", "303" });
		expected.add(new String[] { "101", "404" });
		expected.add(new String[] { "101", "505" });
		expected.add(new String[] { "202", "303" });
		expected.add(new String[] { "202", "404" });
		expected.add(new String[] { "202", "505" });
		
		List<String[]> actual = MatchingUtils.getCandidatesFromMultiFieldDemographics(data1, data2);
		
		for (int i = 0; i < 6; i++) {
			assertEquals("permutation", expected.get(i)[0], actual.get(i)[0]);
			assertEquals("permutation", expected.get(i)[1], actual.get(i)[1]);
		}
	}
	
	@Test
	public void getConfigFolder_shouldReturnTheDefaultFolderIfNoneIsSpecified() {
		when(getDirectoryInApplicationDataDirectory(MatchingConstants.CONFIG_DIR_DEFAULT)).thenReturn(mockFile);
		assertEquals(mockFile, MatchingUtils.getConfigFolder());
	}
	
	@Test
	public void getConfigFolder_shouldReturnTheConfiguredFolderIfSpecified() {
		final String someFolderName = "someFolder";
		when(mockAdminService.getGlobalProperty(MatchingConstants.GP_CONFIG_DIR)).thenReturn(someFolderName);
		when(getDirectoryInApplicationDataDirectory(someFolderName)).thenReturn(mockFile);
		assertEquals(mockFile, MatchingUtils.getConfigFolder());
	}
	
	@Test
	public void getSerializationFolder_shouldReturnTheDefaultFolderIfNoneIsSpecified() {
		when(getDirectoryInApplicationDataDirectory(SERIAL_DIR_DEFAULT + File.separator + "manual_run"))
		        .thenReturn(mockFile);
		assertEquals(mockFile, MatchingUtils.getSerializationFolder("manual run"));
	}
	
	@Test
	public void getSerializationFolder_shouldReturnTheConfiguredFolderIfSpecified() {
		final String serialFolderName = "serialFolder";
		when(mockAdminService.getGlobalProperty(MatchingConstants.GP_SERIAL_DIR)).thenReturn(serialFolderName);
		when(getDirectoryInApplicationDataDirectory(serialFolderName + File.separator + "manual_run")).thenReturn(mockFile);
		assertEquals(mockFile, MatchingUtils.getSerializationFolder("manual run"));
	}
	
	@Test
	public void getConfigFile_shouldReturnTheDefaultFileIfNoneIsSpecified() throws Exception {
		when(Paths.get(MatchingConstants.CONFIG_FILE_DEFAULT)).thenReturn(mockPath);
		Path mockParentPath = Mockito.mock(Path.class);
		Path mockFilePath = Mockito.mock(Path.class);
		when(mockParentPath.toString()).thenReturn(MatchingConstants.CONFIG_DIR_DEFAULT);
		when(mockPath.getParent()).thenReturn(mockParentPath);
		when(mockPath.getFileName()).thenReturn(mockFilePath);
		when(mockFilePath.toString()).thenReturn(MatchingConstants.CONFIG_FILENAME_DEFAULT);
		File testConfigDir = new File("test");
		when(getDirectoryInApplicationDataDirectory(MatchingConstants.CONFIG_DIR_DEFAULT)).thenReturn(testConfigDir);
		
		File configFile = MatchingUtils.getConfigFile();
		assertEquals(testConfigDir, configFile.getParentFile());
		assertEquals(MatchingConstants.CONFIG_FILENAME_DEFAULT, configFile.getName());
	}
	
	@Test
	public void getConfigFile_shouldReturnTheConfiguredFileIfSpecified() {
		final String testConfigDirName = "test_dir";
		final String testConfigFileName = "test_config.xml";
		final String testConfigFile = testConfigDirName + File.separator + testConfigFileName;
		when(mockAdminService.getGlobalProperty(MatchingConstants.GP_CONFIG_FILE)).thenReturn(testConfigFile);
		when(Paths.get(testConfigFile)).thenReturn(mockPath);
		Path mockParentPath = Mockito.mock(Path.class);
		Path mockFilePath = Mockito.mock(Path.class);
		when(mockParentPath.toString()).thenReturn(testConfigDirName);
		when(mockPath.getParent()).thenReturn(mockParentPath);
		when(mockPath.getFileName()).thenReturn(mockFilePath);
		when(mockFilePath.toString()).thenReturn(testConfigFileName);
		File testConfigDir = new File(testConfigDirName);
		when(getDirectoryInApplicationDataDirectory(testConfigDirName)).thenReturn(testConfigDir);
		
		File configFile = MatchingUtils.getConfigFile();
		assertEquals(testConfigDir, configFile.getParentFile());
		assertEquals(testConfigFileName, configFile.getName());
	}
	
}
