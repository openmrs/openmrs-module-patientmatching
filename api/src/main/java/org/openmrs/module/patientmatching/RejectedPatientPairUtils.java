package org.openmrs.module.patientmatching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to perform various task related to the {@link RejectedPatientPair}
 */
public class RejectedPatientPairUtils {

    /**
     * This method will create a Set<Long> of patient IDs.
     * For example if there are non-matching pairs like (20,30), (20,45), (20, 89), and (76,56)
     * This method will create set(20,30,45,89) and set(76,56)
     *
     * @param pairs list of non-matching patient pairs
     * @return a List of patient ID sets
     */
    public static List<Set<Long>> getRejectedPatientPairSets(List<RejectedPatientPair> pairs) {

        if (pairs == null || pairs.isEmpty()) {
            return null;
        }

        Set<Set<Long>> setRejectedPatientPairs = new HashSet<Set<Long>>();

        for (int i = 0; i < pairs.size(); i++) {

            Set<Long> setPairs = new HashSet<Long>();

            // Add the first appearing RejectedPatientPair's ids to the set
            RejectedPatientPair rp = pairs.get(i);
            setPairs.add(new Long(rp.getPatient1().getPatientId()));
            setPairs.add(new Long(rp.getPatient2().getPatientId()));

            for (int j = i + 1; j < pairs.size(); j++) {

                // From the two RejectedPatientPairs if there is at least one common patient
                if (pairs.get(i).getPatient1() == pairs.get(j).getPatient1()
                        || pairs.get(i).getPatient1() == pairs.get(j).getPatient2()
                        || pairs.get(i).getPatient2() == pairs.get(j).getPatient1()
                        || pairs.get(i).getPatient2() == pairs.get(j).getPatient2()) {

                    setPairs.add(new Long(pairs.get(j).getPatient1().getPatientId()));
                    setPairs.add(new Long(pairs.get(j).getPatient2().getPatientId()));
                }
            }

            boolean isASubset = false;
            for (Set<Long> existingSet : setRejectedPatientPairs) {
               if (isASubset = existingSet.containsAll(setPairs)) {
                   break;
               }
            }
            if (!isASubset) {
                setRejectedPatientPairs.add(setPairs);
            }
        }
        return new ArrayList<Set<Long>>(setRejectedPatientPairs);
    }
}
