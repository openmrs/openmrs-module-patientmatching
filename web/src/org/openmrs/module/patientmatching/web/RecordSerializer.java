package org.openmrs.module.patientmatching.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.openmrs.util.OpenmrsUtil;
import org.regenstrief.linkage.Record;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RecordSerializer {
	
	private static XStream stream = new XStream(new DomDriver());
	
	private static Record record;
	
	public static void serialize(Record record) throws IOException {
		String filename = String.valueOf(record.getUID());
		
		String configLocation = MatchingConstants.SERIAL_FOLDER_NAME;
        File configFileFolder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(configLocation);
        File serialFile = new File(configFileFolder, filename);
        
        if (!serialFile.exists()) {
    		String xml = stream.toXML(record);
            BufferedWriter writer = new BufferedWriter(new FileWriter(serialFile));
            writer.write(xml);
            writer.close();
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
