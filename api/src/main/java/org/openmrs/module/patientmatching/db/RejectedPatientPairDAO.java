package org.openmrs.module.patientmatching.db;

import org.openmrs.module.patientmatching.RejectedPatientPair;

import java.util.List;

/**
 * The DAO class to access database objects
 */
public interface RejectedPatientPairDAO {

    /**
     * Persists the patient pair to the database
     *
     * @param pair {@link RejectedPatientPair} which should be excluded from report generation process
     */
    void saveRejectedPatientPair(RejectedPatientPair pair);

    /**
     * Persists list of {@link RejectedPatientPair} to the database
     *
     * @param pairs list of {@link RejectedPatientPair}
     */
    void saveRejectedPatientPairList(List<RejectedPatientPair> pairs);

    /**
     * Retrieve the {@link RejectedPatientPair} list
     *
     * @return a list of {@link RejectedPatientPair}
     */
    List<RejectedPatientPair> getRejectedPatientPairs();
}
