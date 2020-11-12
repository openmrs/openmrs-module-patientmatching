package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Collection;
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
		final String openmrsId = "(Identifier) OpenMRS Identification Number";
		final String oldId = "(Identifier) Old Identification Number";
		final String birthPlace = "(Attribute) Birthplace";
		final String civilStatus = "(Attribute) Civil Status";
		Collection<String> projections = new ArrayList();
		projections.add("(Identifier) Old Identification Number");
		projections.add("(Attribute) Birthplace");
		projections.add("(Attribute) Civil Status");
		OpenMRSReader reader = new OpenMRSReader(null);
		List<Record> records = new ArrayList();
		Record testPatient = null;
		while (reader.hasNextRecord()) {
			Record r = reader.nextRecord();
			if (r.getUID() == 1001L) {
				testPatient = r;
			}
			records.add(r);
		}
		
		Assert.assertEquals(6, records.size());
		Assert.assertNotNull(testPatient);
		Assert.assertEquals("m101", testPatient.getDemographic(openmrsId));
		Assert.assertEquals("", testPatient.getDemographic(oldId));
		Assert.assertEquals("dwight", testPatient.getDemographic("org.openmrs.PersonName.givenName"));
		Assert.assertEquals("1101 w 11th st", testPatient.getDemographic("org.openmrs.PersonAddress.address1"));
		Assert.assertEquals("kampala", testPatient.getDemographic(birthPlace));
		Assert.assertEquals("", testPatient.getDemographic(civilStatus));
	}
	
	@Test
	public void shouldLoadAllNonVoidedPatientsExcludingAnyVoidedDataWhenUsingProjections() throws Exception {
		executeDataSet("moduleTestData.xml");
		Collection<String> projections = new ArrayList();
		projections.add("org.openmrs.PatientIdentifier.identifier");
		projections.add("org.openmrs.PersonName.givenName");
		projections.add("org.openmrs.PersonAddress.address1");
		projections.add("org.openmrs.PersonAttribute.value");
		OpenMRSReader reader = new OpenMRSReader(projections);
		List<Record> records = new ArrayList();
		Record testPatient = null;
		while (reader.hasNextRecord()) {
			Record r = reader.nextRecord();
			if (r.getUID() == 1001L) {
				testPatient = r;
			}
			records.add(r);
		}
		
		Assert.assertEquals(6, records.size());
		Assert.assertNotNull(testPatient);
		Assert.assertEquals("m101", testPatient.getDemographic("org.openmrs.PatientIdentifier.identifier"));
		Assert.assertEquals("dwight", testPatient.getDemographic("org.openmrs.PersonName.givenName"));
		Assert.assertEquals("1101 w 11th st", testPatient.getDemographic("org.openmrs.PersonAddress.address1"));
		Assert.assertEquals("kampala", testPatient.getDemographic("org.openmrs.PersonAttribute.value"));
	}
	
}
