CONTENTS
    PATIENT MATCHING STRATEGY - Describes the supported patient matching strategies

	PATIENT MATCHING ATTRIBUTE - Describes format of the PersonAttributeType the modules uses
	
	STRING COMPARATORS - Lists the different comparators available when matching
	
	CONFIGURATION FILE - Describes requirements of the configuration file
	
	CONFIGURATION FILE TAGS - Explains the elements in the configuration file

	KEY MODULE CONFIGURATIONS - Explains the key module global properties to configure

PATIENT MATCHING STRATEGY
    Probabilistic
        This is the default strategy that requires training the patient matching engine how to match records, it keeps
        details about matched and unmatched fields whenever any 2 records are matched and uses it to determine scores
        for future comparisons. For example, if a certain property matches 90% of the times for true matched records
        then it ends up carrying more weight over fields that have lower true match percentages. This has a huge
        implication, it means given the same records to compare it can produce different scores potentially matching or not
        matching them for different runs depending on the current m(true) and u(false) values of the properties
        being compared but overtime it should normalize.

    Deterministic
        Because of the somewhat unpredictable nature of probabilistic strategy, some implementation find it undesirable
        especially if they have more straight forward and simpler patient matching needs to address. This strategy
        guarantees the same results given the same configuration and records to match, requires no training of the
        matching engine and is much simpler to configure.

The matching strategy can be configured via the patientmatching.strategy global property.

PATIENT MATCHING ATTRIBUTE
The module prefers to use a special matching PersonAttributeType of "Other Matching Information".  This is a list of demographic-value pairs in the form of "<demographic1>:<value1>;<demographic2>:<value2>; . . . ".  If a demographic has no value, then it can either not be present in the string of the value can be an empty string.

If there is no person attribute of that type, then the module will try to get some basic information as best it can.  Currently, this is very minimal and would not make good matches.

STRING COMPARATORS
The string comparators that can be used are:
Exact Match - case sensitive comparison for the whole string, similarity is iether 0 or 1
Levenshtein - Levenshtein edit distance / longest string length
Longest Common Substring - Regenstrief algorithm, converts to case insensitive strings in comparison
Jaro Winkler - 

The implementations for Levenshtein and Jaro Winkler comparators come from the Simmetrics library at http://www.dcs.shef.ac.uk/~sam/simmetrics.html.  The threshold for Jaro Winkler
and Longest Common Substring is a score 0.8.  The threhold for Levenshtein is 0.7.


CONFIGURATION FILE
The default name for the configuration file is "link_config.xml" in the patietmatching directory in the OpenMRS application data directory.
The JDBC driver needs to be in the classpath when the program is run if the link table is in a non Postgres or MySQL directory.

A excerpt of a valid configuration file for the probabilistic strategy is (see below for that the deterministic):
<?xml version="1.0" encoding="UTF-8" ?>
<Session>
	<datasource name="link_test" type="DataBase" access="<JDBC driver>,<database URL>,<user>,<passwd>" id="3">
		<column include_position="0" column_id="mrn" label="mrn" type="string"/>
		<column include_position="1" column_id="ln" label="ln" type="string"/>
		. . .
		<column include_position="17" column_id="openmrs_id" label="openmrs_id" type="string"/>
	</datasource>
	<analysis type="scaleweight">
	<init>DBCdriver,databaseURL,user,passwd</init>
	</analysis>
	<run estimate="true" name="conversion">
		<row name="yb">
			<BlockOrder>1</BlockOrder>
			<BlckChars>40</BlckChars>
			<Include>false</Include>
			<TAgreement>0.9</TAgreement>
			<NonAgreement>0.1</NonAgreement>
			<ScaleWeight lookup="TopN" N="100.0" buffer="500">true</ScaleWeight>
			<Algorithm>Exact Match</Algorithm>
	<SetID>0</SerID>
		</row>
		. . .
		<row name="zip">
			<BlockOrder>null</BlockOrder>
			<BlckChars>40</BlckChars>
			<Include>true</Include>
			<TAgreement>0.9</TAgreement>
			<NonAgreement>0.1</NonAgreement>
			<ScaleWeight>null</ScaleWeight>
			<Algorithm>Exact Match</Algorithm>
		<SetID>0</SerID></row>
	</run>
</Session>


Example configuration file for deterministic strategy:

<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Session>
    <datasource access="com.mysql.jdbc.Driver,jdbc:mysql://localhost:3306/openmrs_merge,username,password" header="false" id="1" name="patient" type="DataBase">
        <column column_id="patient_id" include_position="0" label="org.openmrs.Patient.patientId" type="number" />
        <column column_id="gender" include_position="1" label="org.openmrs.Patient.gender" type="string" />
        <column column_id="birthdate" include_position="2" label="org.openmrs.Patient.birthdate" type="string" />
        <column column_id="family_name" include_position="3" label="org.openmrs.PersonName.familyName" type="string" />
        <column column_id="given_name" include_position="4" label="org.openmrs.PersonName.givenName" type="string" />
    </datasource>
    <run estimate="true" name="default-config">
        <row name="org.openmrs.Patient.patientId">
            <BlockOrder>null</BlockOrder>
            <BlckChars>40</BlckChars>
            <Include>false</Include>
        </row>
        <row name="org.openmrs.Patient.gender">
            <BlockOrder>1</BlockOrder>
            <BlckChars>40</BlckChars>
            <Include>true</Include>
        </row>
        <row name="org.openmrs.Patient.birthdate">
            <BlockOrder>2</BlockOrder>
            <BlckChars>40</BlckChars>
            <Include>true</Include>
        </row>
        <row name="org.openmrs.PersonName.familyName">
            <BlockOrder>null</BlockOrder>
            <BlckChars>40</BlckChars>
            <Include>true</Include>
            <Threshold>0.75</Threshold>
        </row>
        <row name="org.openmrs.PersonName.givenName">
            <BlockOrder>null</BlockOrder>
            <BlckChars>40</BlckChars>
            <Include>true</Include>
            <Threshold>0.75</Threshold>
        </row>
    </run>
</Session>


CONFIGURATION FILE TAGS
The description of the elements and attributes of the xml configuration file is:
Session � the root element
Datasource � a source of Record objects
	Name � for file sources, give the path, for data bases, gives the table name
	Type � type of datasource: CharDelimFile, DataBase, Vector
	Access � how to access the datasource.  For a character delimted file, it�s the delimiter.  For a database, it�s a String holding connection information
	ID - a numeric unique identifier for the data source
		Column � one column of fields in the datasource
		Include_position � if column is a part of the analysis, what order it is.  Zero indexed
		Column_id � name of the column.  For a character delimited file, it�s an index.  For a database table, it�s the column name
		Label � the name used by the linkage program and that appears in the �run� section.  It should be the demographics that appear in the matching person attribute
		Type � either is �string� or �numeric� and used in sorting and comparisons
Run � a set of link options to use with the datasources
	Estimate � Whether to use EM to modify values
	Name � a label for this configuration
Row � the options for a field in the Record
	Name � the name of the field, must match the label in the Datasource element
BlockOrder � if the field is a blocking field, then uniquely number this starting with 1
BlckChars � the number of characters to block on if the field is a blocking field
Include � indicates if the field will be compared between records
TAgreement � the true agreement value
NonAgreement � the non agreement value
ScaleWeight - true for enabling weight scaling, null for disabling
	lookup - Determines the tokens that will be loaded to the lookup table. Possible values are: TopN, TopNPercent, AboveN, BelowN, BottomNPercent, BottomN
	N - Defines the size of the lookup table, must be a decimal number, use a number between 0.0 and 1.0 for percentages 
	buffer - Number of records that will be stored in memory during analysis (no need to exceed the number of unique tokens)
Algorithm � the comparator to use for this field.  Options are Exact Match, LEV, LCS, and JWC
SetID - if the field is set as transposed,then the setId will be a number(the value of the set).if the field is not transposed by default it will be 0

KEY MODULE CONFIGURATIONS
    patientmatching.strategy
        The patient matching strategy to use, allowed values are probabilistic and deterministic, defaults to
        probabilistic. With deterministic strategy m and u values are ignored therefore any 2 records are considered to
        match if all their demographics match based on their respective algorithms.

    patientmatching.configDirectory
        The location of the patient matching configuration folder, can be absolute or relative path, defaults to
        patientmatching in the OpenMRS application data directory.

    patientmatching.linkConfigFile
        The location of the patient matching configuration file, can be absolute or relative path, defaults to
        patientmatching/link_config.xml in the OpenMRS application data directory.

    patientmatching.serializationDirectory
        The location of the serialization folder to be used when matching patients, can be absolute or relative path,
        defaults to patientmatching/serial in the OpenMRS application data directory.

Please refer to the OpenMRS settings page in the application for a complete list of all global properties for the module.