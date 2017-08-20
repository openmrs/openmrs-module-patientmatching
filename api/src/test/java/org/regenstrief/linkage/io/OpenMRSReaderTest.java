package org.regenstrief.linkage.io;

import org.hibernate.Criteria;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class OpenMRSReaderTest extends BaseModuleContextSensitiveTest {

    @Before
	public void setup() throws Exception {
        executeDataSet("PatientMatchingTestDataSet.xml");
    }

    @Test
    public void createCriteria_shouldNotReturnNullAndEmpty() throws ParseException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        // Creating a collection of projections
        Collection<String> projections = Arrays.asList("org.openmrs.Patient.birthdate",
                "org.openmrs.Patient.gender","org.openmrs.Patient.patientId","org.openmrs.PersonAddress.country",
                "org.openmrs.PersonAddress.countyDistrict","org.openmrs.PersonAddress.postalCode",
                "org.openmrs.PersonAddress.stateProvince","org.openmrs.PersonName.familyName");

        // Initialize date variable for report created date
        Date dateReportCreatedOn;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateReportCreatedOn = dateFormat.parse("18-01-2006");

        // Reflection class for OpenMRSReader as the method going to be tested is a private method
        Class reflectionClass = OpenMRSReader.class;
        Object objReader = reflectionClass.newInstance();

        // Field for collection of projections
        Field fieldProjections = reflectionClass.getDeclaredField("projections");
        fieldProjections.setAccessible(true);
        fieldProjections.set(objReader,projections);

        // Field for date report created on
        Field fieldDateReportCreatedOn = reflectionClass.getDeclaredField("reportCreatedOn");
        fieldDateReportCreatedOn.setAccessible(true);
        fieldDateReportCreatedOn.set(objReader,dateReportCreatedOn);

        // Get the method createCriteria()
        Method methodCreateCriteria = reflectionClass.getDeclaredMethod("createCriteria",null);
        methodCreateCriteria.setAccessible(true);
        Object objCriteria = methodCreateCriteria.invoke(objReader);


        // criteria neither be null nor empty
        Criteria criteria = ((Criteria)objCriteria);
        Assert.assertNotNull(criteria);
        Assert.assertNotEquals(0,criteria.list().size());


    }
}
