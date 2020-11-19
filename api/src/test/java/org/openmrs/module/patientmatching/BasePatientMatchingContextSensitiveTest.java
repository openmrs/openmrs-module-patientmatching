package org.openmrs.module.patientmatching;

import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public abstract class BasePatientMatchingContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public Properties getRuntimeProperties() {
		Properties props = super.getRuntimeProperties();
		//Fixes the error reported by the h2 driver
		props.setProperty(Environment.URL, props.getProperty(Environment.URL) + ";DB_CLOSE_ON_EXIT=FALSE");
		return props;
	}
	
}
