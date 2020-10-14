package org.openmrs.module.patientmatching;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.regenstrief.linkage.Record;

public class LinkDBConnectionsTest extends BaseModuleContextSensitiveTest {
	
	/**
	 * @see LinkDBConnections#patientToRecord(Patient)
	 * @verifies encode more than one identifier of the same type properly
	 */
	@Test
	public void patientToRecord_shouldEncodeMoreThanOneIdentifierOfTheSameTypeProperly() throws Exception {
		
		LinkDBConnections linkage = LinkDBConnections.getInstance();
		
		Patient p = Context.getPatientService().getPatient(2);
		
		// add another identifier of an existing type
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifierType(Context.getPatientService().getPatientIdentifierType(2));
		pi.setIdentifier("555");
		pi.setLocation(new Location(1));
		p.addIdentifier(pi);
		Context.getPatientService().savePatient(p);
		
		// check to see if the identifiers are serialized properly
		Record r = linkage.patientToRecord(p);
		String actual = r.getDemographic("(Identifier) Old Identification Number");
		Assert.assertEquals("101,555", actual);
	}
}
