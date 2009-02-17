package org.openmrs.module.patientmatching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.Record;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RecordSerializer {
	
	private static XStream stream = new XStream(new DomDriver("UTF-8"));
	
	private static Record record;
	
	public static void serialize(Record record) throws IOException {
		String filename = String.valueOf(record.getUID());
		
		String configLocation = MatchingConstants.SERIAL_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File serialFile = new File(configFileFolder, filename);
        
        if (!serialFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(serialFile);
    		stream.toXML(record, outputStream);
        }
	}
	
	public static Record deserialize(String xmlName) throws IOException {
		String configLocation = MatchingConstants.SERIAL_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File serialFile = new File(configFileFolder, xmlName);
        
		FileInputStream fis = new FileInputStream(serialFile);
        stream.setClassLoader(new Record(-999, "OpenMRS").getClass().getClassLoader());
        record = (Record) stream.fromXML(fis);
        return record;
	}
}
