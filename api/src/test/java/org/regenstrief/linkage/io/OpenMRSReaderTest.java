package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.regenstrief.linkage.Record;

public class OpenMRSReaderTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public Properties getRuntimeProperties() {
		Properties props = super.getRuntimeProperties();
		//Fixes the error reported by the h2 driver
		props.setProperty(Environment.URL, props.getProperty(Environment.URL) + ";DB_CLOSE_ON_EXIT=FALSE");
		return props;
	}
	
	@Test
	public void shouldLoadAllNonVoidedPatientsExcludingAnyVoidedDataWhenNotUsingProjections() throws Exception {
		executeDataSet("moduleTestData.xml");
		OpenMRSReader reader = new OpenMRSReader(null);
		List<Record> records = new ArrayList();
		Record expPatient2 = null;
		Record expPatient6 = null;
		Record expPatient7 = null;
		Record expPatient8 = null;
		Record expPatient1001 = null;
		Record expPatient1002 = null;
		Record expPatient1004 = null;//Patient with address or attribute
		while (reader.hasNextRecord()) {
			Record r = reader.nextRecord();
			if (r.getUID() == 2L) {
				expPatient2 = r;
			} else if (r.getUID() == 6L) {
				expPatient6 = r;
			} else if (r.getUID() == 7L) {
				expPatient7 = r;
			} else if (r.getUID() == 8L) {
				expPatient8 = r;
			} else if (r.getUID() == 1001L) {
				expPatient1001 = r;
			} else if (r.getUID() == 1002L) {
				expPatient1002 = r;
			} else if (r.getUID() == 1004L) {
				expPatient1004 = r;
			}
			records.add(r);
		}
		
		Assert.assertEquals(7, records.size());
		Assert.assertNotNull(expPatient2);
		Assert.assertNotNull(expPatient6);
		Assert.assertNotNull(expPatient7);
		Assert.assertNotNull(expPatient8);
		Assert.assertNotNull(expPatient1001);
		Assert.assertNotNull(expPatient1002);
		Assert.assertNotNull(expPatient1004);
		Assert.assertEquals("m101", expPatient1001.getDemographic("(Identifier) OpenMRS Identification Number"));
		Assert.assertEquals("", expPatient1001.getDemographic("(Identifier) Old Identification Number"));
		Assert.assertEquals("dwight", expPatient1001.getDemographic("org.openmrs.PersonName.givenName"));
		Assert.assertEquals("1101 w 11th st", expPatient1001.getDemographic("org.openmrs.PersonAddress.address1"));
		Assert.assertEquals("kampala", expPatient1001.getDemographic("(Attribute) Birthplace"));
		Assert.assertEquals("", expPatient1001.getDemographic("(Attribute) Civil Status"));
	}
	
}
