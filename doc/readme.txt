CONTENTS
	PATIENT MATCHING ATTRIBUTE - Describes format of the PersonAttributeType the modules uses
	
	STRING COMPARATORS - Lists the different comparators available when matching
	
	CONFIGURATION FILE - Describes requirements of the configuration file
	
	CONFIGURATION FILE TAGS - Explains the elements in the configuration file


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
The default name for the configuration file is "link_config.xml" in the current working directory.  For an OpenMRS module, this would be the Tomcat directory, such as "C:\Program Files\Apache Software Foundation\Tomcat 6.0" on Windows.  The JDBC driver needs to be in the classpath when the program is run if the link table is in a non Postgres or MySQL directory.

A excerpt of a valid configuration file is:
<?xml version="1.0" encoding="UTF-8" ?>
<Session>
	<datasource name="link_test" type="DataBase" access="<JDBC driver>,<database URL>,<user>,<passwd>" n_records="-1">
		<column include_position="0" column_id="mrn" label="mrn" type="string" n_non_null="-1" n_null="-1"/>
		<column include_position="1" column_id="ln" label="ln" type="string" n_non_null="-1" n_null="-1"/>
		. . .
		<column include_position="17" column_id="openmrs_id" label="openmrs_id" type="string" n_non_null="-1" n_null="-1"/>
	</datasource>
	<scaleweight tokentable="<NameOfTheTableWhereTokensAreStored>" access="<JDBC driver>,<database URL>,<user>,<passwd>"/>
	<run estimate="true" name="conversion">
		<row name="yb">
			<BlockOrder>1</BlockOrder>
			<BlckChars>40</BlckChars>
			<Include>false</Include>
			<TAgreement>0.9</TAgreement>
			<NonAgreement>0.1</NonAgreement>
			<ScaleWeight>null</ScaleWeight>
			<Algorithm>Exact Match</Algorithm>
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
		</row>
	</run>
</Session>


CONFIGURATION FILE TAGS
The description of the elements and attributes of the xml configuration file is:
Session – the root element
Datasource – a source of Record objects
	Name – for file sources, give the path, for data bases, gives the table name
	Type – type of datasource: CharDelimFile, DataBase, Vector
	Access – how to access the datasource.  For a character delimted file, it’s the delimiter.  For a database, it’s a String holding connection information
	N_records – number of records in the datasource
	Column – one column of fields in the datasource
	Include_position – if column is a part of the analysis, what order it is.  Zero indexed
	Column_id – name of the column.  For a character delimited file, it’s an index.  For a database table, it’s the column name
	Label – the name used by the linkage program and that appears in the “run” section.  It should be the demographics that appear in the matching person attribute
	Type – either is “string” or “numeric” and used in sorting and comparisons
	N_non_null – number of records in data where the field is not null
	N_null – number of records in data where the field is null
Scaleweight - (Optional) Stores location of where token frequencies are stored
	Tokentable - name of the table where token frequencies will be inserted into
	Access - How to access the relational database, it’s a String holding connection information 	
Run – a set of link options to use with the datasources
	Estimate – Whether to use EM to modify values
	Name – a label for this configuration
Row – the options for a field in the Record
	Name – the name of the field, must match the label in the Datasource element
BlockOrder – if the field is a blocking field, then uniquely number this starting with 1
BlckChars – the number of characters to block on if the field is a blocking field
Include – indicates if the field will be compared between records
TAgreement – the true agreement value
NonAgreement – the non agreement value
ScaleWeight - true for enabling weight scaling, null for disabling
	lookup - Determines the tokens that will be loaded to the lookup table. Possible values are: TopN, TopNPercent, AboveN, BelowN, BottomNPercent, BottomN
	N - Defines the size of the lookup table, must be a decimal number, use a number between 0.0 and 1.0 for percentages 
Algorithm – the comparator to use for this field.  Options are Exact Match, LEV, LCS, and JWC