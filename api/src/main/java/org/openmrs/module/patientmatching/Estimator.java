package org.openmrs.module.patientmatching;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;

/**
 * Class to calculate various estimations of an strategy to give a feedback
 * on the goodness of the strategy before it is used in de-duplication
 *
 * @author pulasthi
 *
 */
public class Estimator {
	private long estimatedComparisons = -1L;
	private long estimatedTimeToRun = -1L;

	private static final double RECALCULATE_IGNORE_FRACTION = 0.1;

    /**
     * Calculate the number of comparisons when the blocking field names are given
     * @param entryNames The list of the names of the blocking fields
     */
	public void doEstimationsWithBlockingFields(List<String> entryNames){
		Date startedAt = new Date();

		String select = "select count(p1) from Patient p1, Patient p2";
		String where = " where p1 <> p2 ";

		List<String> attributes = new ArrayList<String>();
		List<String> identifiers = new ArrayList<String>();
		List<String> attrTypes = new ArrayList<String>();

		for (String entryName : entryNames ) {
			if(entryName.startsWith("(Identifier) ")){
				identifiers.add(entryName.substring("(Identifier) ".length()));
			} else if(entryName.startsWith("(Attribute) ")){
				attrTypes.add(entryName.substring("(Attribute) ".length()));
			} else{
				String attribute = entryName.substring(entryName.lastIndexOf(".")+1);
				attributes.add(attribute);
			}
		}

        //Get the attributes from the Patient, Person, PersonName, PatientIdentifier classes
		Class<Patient> patient = Patient.class;
		Set<String> patientFieldNames = new HashSet<String>(patient.getDeclaredFields().length);
		for (Field f : patient.getDeclaredFields()) {
			patientFieldNames.add(f.getName());
		}

		Class<Person> person = Person.class;
		Set<String> personFieldNames = new HashSet<String>(person.getDeclaredFields().length);
		for (Field f : person.getDeclaredFields()) {
			personFieldNames.add(f.getName());
		}

		Class<PersonName> personName = PersonName.class;
		Set<String> personNameFieldNames = new HashSet<String>(personName.getDeclaredFields().length);
		for (Field f : personName.getDeclaredFields()) {
			personNameFieldNames.add(f.getName());
		}

		Class<PatientIdentifier> identifier = PatientIdentifier.class;
		Set<String> identifierFieldNames = new HashSet<String>(identifier.getDeclaredFields().length);
		for (Field f : identifier.getDeclaredFields()) {
			identifierFieldNames.add(f.getName());
		}

        //build the HQL query from the above attributes
		for (String s : attributes) {
			if (patientFieldNames.contains(s)) {
				where += " and p1." + s + " = p2." + s;
			} else if (personFieldNames.contains(s)) {
				if (!select.contains("Person ")) {
					select += ", Person person1, Person person2";
					where += " and p1.patientId = person1.personId and p2.patientId = person2.personId ";
				}
				where += " and person1." + s + " = person2." + s;
			} else if (personNameFieldNames.contains(s)) {
				if (!select.contains("PersonName")) {
					select += ", PersonName pn1, PersonName pn2";
					where += " and p1 = pn1.person and p2 = pn2.person ";
				}
				where += " and pn1." + s + " = pn2." + s;
			} else if (identifierFieldNames.contains(s)) {
				if (!select.contains("PatientIdentifier")) {
					select += ", PatientIdentifier pi1, PatientIdentifier pi2";
					where += " and p1 = pi1.patient and p2 = pi2.patient ";
				}
				where += " and pi1." + s + " = pi2." + s;
			}
		}

        //The identifiers (Patient) and the attributes(Person) are dynamic rather than static,
        //Therefore must be checked dynamically
		if(!identifiers.isEmpty()){
			if (!select.contains("PatientIdentifier ")) {
				select += ", PatientIdentifier pi1, PatientIdentifier pi2";
				where += " and p1 = pi1.patient and p2 = pi2.patient";
			}
			if (!select.contains("PatientIdentifierType")) {
				select += ", PatientIdentifierType pit1, PatientIdentifierType pit2";
				where += " and pit1 = pi1.identifierType and pit2 = pi2.identifierType and pi1.identifier = pi2.identifier" +
						" and pit1.name=pit2.name";
			}
			int count = 0;
			where += " and (";
            StringBuilder whereBuffer = new StringBuilder();
			for (String idType : identifiers) {
				if(count>0){
					whereBuffer.append(" or");
				}
				whereBuffer.append(" pit1.name = '");
                whereBuffer.append(idType);
                whereBuffer.append("'");
				count++;
			}
			where += whereBuffer.toString()+ ")";
		}

        if(!attrTypes.isEmpty()){
            //In case Person not defined previously
            if (!select.contains("Person ")) {
                select += ", Person person1, Person person2";
                where += " and p1.patientId = person1.personId and p2.patientId = person2.personId ";
            }
            if (!select.contains("PersonAttribute")){
                select += ", PersonAttribute pa1, PersonAttribute pa2";
                where += " and person1 = pa1.person and person2 = pa2.person";
            }
            if (!select.contains("PersonAttributeType")){
                select += ", PersonAttributeType pat1, PersonAttributeType pat2";
                where += " and pat1 = pa1.attributeType and pat2 = pa2.attributeType" +
                    " and pat1.name = pat2.name";
            }

            int count = 0;
            where += " and (";
            StringBuilder whereBuffer = new StringBuilder();
            for (String attrType : attrTypes) {
                if(count>0){
                    whereBuffer.append(" or");
                }
                whereBuffer.append(" pat1.name = '");
                whereBuffer.append(attrType);
                whereBuffer.append("'");
                count++;
            }
            where += whereBuffer.toString()+ ")";
        }

		select = select + where;

		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		estimatedComparisons = service.getCustomCount(select) / 2;
		Date finishedAt = new Date();
		estimatedTimeToRun = finishedAt.getTime() - startedAt.getTime();
	}

	/**
	 * Calculates the estimated comparisons that the strategy will run using the configurations user has specified.
	 *
	 * @param configurationEntries The set of configuration entries user has selected
	 */
	public void doEstimations(Set<ConfigurationEntry> configurationEntries) {
		Set<ConfigurationEntry> blockingEntries = getBlockingEntries(configurationEntries);
		List<String> blockingEntryNames = new ArrayList<String>(blockingEntries.size());
		for (ConfigurationEntry entry : blockingEntries) {
			blockingEntryNames.add(entry.getFieldName());
		}
		doEstimationsWithBlockingFields(blockingEntryNames);
	}

	/**
	 * Returns the "blocking" entries of a given set of configuration entries
	 * @param allEntries The set of all configuration entries
	 * @return The set of blocking entries out of the input entries
	 */
	private Set<ConfigurationEntry> getBlockingEntries(Set<ConfigurationEntry> allEntries){
		Set<ConfigurationEntry> blockingEntries = new TreeSet<ConfigurationEntry>();
		for (ConfigurationEntry configurationEntry : allEntries) {
			if(configurationEntry.isBlocking()){
				blockingEntries.add(configurationEntry);
			}
		}
		return blockingEntries;
	}

	/**
	 * Get the estimated number of comparisons that the strategy would do.
	 * @return the estimated number of comparisons
	 */
	public long getEstimatedComparisons() {
		//TODO check whether the estimations have been done before calling this
		return estimatedComparisons;
	}

	/**
	 * Get the time taken to run the comparisons
	 * @return Time taken to run the comparisons
	 */
	public long getEstimatedTimeToRun() {
		//TODO check whether the estimations are done before calling this
		return estimatedTimeToRun;
	}

	/**
	 * Get the number of patients in the database at the moment
	 * @return number of patients in the database at the moment
	 */
	public long getTotalRecords() {
		String query = "select count(p1) from Patient p1";
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		return service.getCustomCount(query);
	}

	/**
	 * Update the estimation information of given list of PatientMatchingConfiguration
	 * This update is done only if the total patient records is changed by
	 * RECALCULATE_IGNORE_FRACTION (currently 0.1) of the number of records at the last time
	 * of the strategy update. Else it is kept unchanged to minimize the calculations.
	 * @param configList The list of configurations to be updated
	 */
	public void updateStrategies(List<PatientMatchingConfiguration> configList){
		long totalRecords = getTotalRecords();
		for (PatientMatchingConfiguration configuration : configList) {
			long configPreTotalRecs = configuration.getTotalRecords();
			long recordDif = Math.abs(totalRecords-configPreTotalRecs);
			if(recordDif > configPreTotalRecs * RECALCULATE_IGNORE_FRACTION){
				doEstimations(configuration.getConfigurationEntries());
				configuration.setEstimatedPairs(estimatedComparisons);
				configuration.setEstimatedTime(estimatedTimeToRun);
				configuration.setTotalRecords(totalRecords);
				MatchingConfigurationUtils.savePatientMatchingConfig(configuration);
			}
		}
	}
}
