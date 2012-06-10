package org.openmrs.module.patientmatching;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;

public class Estimator {
	private long estimatedComparisons = -1L;
	
	public void doEstimations(Set<ConfigurationEntry> configurationEntries) {
		String select = "select count(distinct p1) from Patient p1, Patient p2";
		String where = " where p1 <> p2 ";
		String orderBy = " order by ";
		
		List<String> attributes = new ArrayList<String>();
		
		for (ConfigurationEntry blockingEntry : getBlockingEntries(configurationEntries)) {
			String fieldName = blockingEntry.getFieldName();
			String className = fieldName.substring(0,fieldName.lastIndexOf("."));
			String attribute = fieldName.substring(fieldName.lastIndexOf(".")+1);
			attributes.add(attribute);
		}
		
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
	
		for (String s : attributes) {
			if (patientFieldNames.contains(s)) {
				where += " and p1." + s + " = p2." + s;
				orderBy += "p1." + s + ", ";
			} else if (personFieldNames.contains(s)) {
				if (!select.contains("Person ")) {
					select += ", Person person1, Person person2";
					where += " and p1.patientId = person1.personId and p2.patientId = person2.personId ";
				}
				where += " and person1." + s + " = person2." + s;
				orderBy += "person1." + s + ", ";
			} else if (personNameFieldNames.contains(s)) {
				if (!select.contains("PersonName")) {
					select += ", PersonName pn1, PersonName pn2";
					where += " and p1 = pn1.person and p2 = pn2.person ";
				}
				where += " and pn1." + s + " = pn2." + s;
				orderBy += "pn1." + s + ", ";
			} else if (identifierFieldNames.contains(s)) {
				if (!select.contains("PatientIdentifier")) {
					select += ", PatientIdentifier pi1, PatientIdentifier pi2";
					where += " and p1 = pi1.patient and p2 = pi2.patient ";
				}
				where += " and pi1." + s + " = pi2." + s;
				orderBy += "pi1." + s + ", ";
			}
		}
		
		int index = orderBy.lastIndexOf(", ");
		orderBy = orderBy.substring(0, index);
		
		select = select + where + orderBy;
		
		PatientMatchingReportMetadataService service = Context.getService(PatientMatchingReportMetadataService.class);
		estimatedComparisons = service.getCustomCount(select);
	}
	
	private Set<ConfigurationEntry> getBlockingEntries(Set<ConfigurationEntry> allEntries){
		Set<ConfigurationEntry> blockingEntries = new TreeSet<ConfigurationEntry>();
		for (ConfigurationEntry configurationEntry : allEntries) {
			if(configurationEntry.isBlocking()){
				blockingEntries.add(configurationEntry);
			}
		}
		return blockingEntries;
	}

	public long getEstimatedComparisons() {
		//TODO check whether the estimations are done before calling this
		return estimatedComparisons;
	}
	
}
