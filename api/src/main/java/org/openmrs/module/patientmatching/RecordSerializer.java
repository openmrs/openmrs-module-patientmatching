package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.regenstrief.linkage.Record;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Utility class that will be used to perform serialization and de-serialization of the
 * <code>Record</code> object during the matching analysis process. By performing serialization and
 * de-serialization, the memory footprint used by the matching process will be reduced because the
 * process only hold the reference to <code>uid</code> of the <code>Record</code> instead of the
 * <code>Record</code> itself. The class utilize the XStream library to perform serialization and
 * de-serialization effectively and efficiently.
 */
public class RecordSerializer {
	
	/**
	 * Static reference to a single XStream instance. The XStream will be able to handle the UTF-8
	 * character sets by using the following constructor
	 */
	private static XStream stream = new XStream(new DomDriver("UTF-8"));
	
	/**
	 * Static reference to the currently processed <code>Record</code> object.
	 */
	private static Record record;
	
	/**
	 * Perform the serialization process of the record. The output will be stored in the predefined
	 * folders which is not visible to the users. UID of the <code>Record</code> object will be the
	 * output file name of the serialization process
	 * 
	 * @param record that will be serialized
	 * @param runName The name of the current patient matching run
	 * @throws IOException
	 */
	public static void serialize(Record record, String runName) throws SecurityException, IOException {
		String filename = String.valueOf(record.getUID());
		
		File serialFile = new File(MatchingUtils.getSerializationFolder(runName), filename);
		
		// only serialize if the same record have not been serialized before
		if (!serialFile.exists()) {
			FileOutputStream outputStream = new FileOutputStream(serialFile);
			stream.toXML(record, outputStream);
			outputStream.close();
		}
	}
	
	/**
	 * Perform the de-serialization process. This will read the xml output of the serialization process
	 * and try to reconstruct the <code>Record</code> object from the xml file.
	 * 
	 * @param xmlName the uid of the record
	 * @param runName The name of the current patient matching run
	 * @return <code>Record</code> object constructed from the xml file
	 * @throws IOException
	 */
	public static Record deserialize(String xmlName, String runName) throws FileNotFoundException, SecurityException {
		File serialFile = new File(MatchingUtils.getSerializationFolder(runName), xmlName);
		
		FileInputStream fis = new FileInputStream(serialFile);
		//hack: need to do this to let know XStream the appropriate type of object to be created
		stream.setClassLoader(new Record(-999, "OpenMRS").getClass().getClassLoader());
		record = (Record) stream.fromXML(fis);
		return record;
	}
}
