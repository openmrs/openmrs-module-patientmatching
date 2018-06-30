package org.openmrs.module.patientmatching.web.dwr;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class DWRMatchingConfigUtilitiesTest extends BaseModuleContextSensitiveTest {

    @Before
    public void setup() throws Exception {
        executeDataSet("PatientTestDataSet.xml");
    }

    @Test
    public void mergePatients_shouldMergePatientsAndShouldReturnsTrue() {
        String patientIDs = "2&6&7&8&9";
        boolean result = DWRMatchingConfigUtilities.mergePatients(patientIDs);

        // Assert
        Assert.assertEquals(result, true);

        // Getting patients
        Patient p2 = Context.getPatientService().getPatient(2);
        Patient p6 = Context.getPatientService().getPatient(6);
        Patient p7 = Context.getPatientService().getPatient(7);
        Patient p8 = Context.getPatientService().getPatient(8);
        Patient p9 = Context.getPatientService().getPatient(9);

        // Patients who are having ids 6, 7, 8, and 9 should be voided
        Assert.assertEquals(p6.isVoided(), true);
        Assert.assertEquals(p7.isVoided(), true);
        Assert.assertEquals(p8.isVoided(), true);
        Assert.assertEquals(p9.isVoided(), true);

        // Patient id = 2 should not be voided
        Assert.assertEquals(p2.isVoided(), false);

    }
}
