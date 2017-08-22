package org.openmrs.module.patientmatching;

import org.openmrs.Patient;

import java.io.Serializable;

/**
 *  This class holds the non-matching patient pairs which were stated by the reviewer
 */
public class RejectedPatientPair implements Serializable {

    private Patient patient1;
    private Patient patient2;

    /**
     * Creates a RejectedPatientPair with given patients.

     * @param patient1  Patient 1
     * @param patient2  Patient 2
     */
    public RejectedPatientPair(Patient patient1, Patient patient2) {

        // Always assign the patient having minimum id to the the variable patient1 as a convention.
        if (patient1.getPatientId() < patient2.getPatientId()) {
            this.patient1 = patient1;
            this.patient2 = patient2;

        } else {
            this.patient1 = patient2;
            this.patient2 = patient1;

        }
    }

    public RejectedPatientPair() {
    }

    public Patient getPatient1() {
        return patient1;
    }

    public Patient getPatient2() {
        return patient2;
    }

    public void setPatient1(Patient patient1) {
        this.patient1 = patient1;
    }

    public void setPatient2(Patient patient2) {
        this.patient2 = patient2;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + patient1.getPatientId();
        result = 31 * result + patient2.getPatientId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RejectedPatientPair)) {
            return false;
        }

        RejectedPatientPair rp = (RejectedPatientPair) obj;

        return (rp.patient1 == patient1 && rp.patient2 == patient2)
                || (rp.patient1 == patient2 && rp.patient2 == patient1);
    }
}
