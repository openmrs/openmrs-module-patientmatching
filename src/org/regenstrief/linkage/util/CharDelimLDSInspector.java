package org.regenstrief.linkage.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class analyzes a character delimited file link data source
 * and creates default DataColumns for the LinkDataSource object.
 * 
 * This is useful since a LinkDataSource object is only guaranteed
 * to have the file name and delimiter character.
 * 
 * @author jegg
 *
 */

public class CharDelimLDSInspector implements LinkDataSourceInspector{
	
	boolean has_header;
	
	public CharDelimLDSInspector(boolean header){
		has_header = header;
	}
	
	public void setDefaultDataColumns(LinkDataSource lds){
		lds.getDataColumns().clear();
		File f = new File(lds.getName());
		String delim = lds.getAccess();
		// convert delimiter character to hex string
		delim = getHexString(delim.charAt(0));
		try{
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			String[] fields = line.split(delim, -1);
			
			for(int i = 0; i < fields.length; i++){
				DataColumn dc = new DataColumn(Integer.toString(i));
				dc.setIncludePosition(i);
				dc.setType(DataColumn.STRING_TYPE);
				if(has_header){
					dc.setName(fields[i]);
				} else {
					dc.setName("Column" + i);
				}
				lds.addDataColumn(dc);
			}
		}
		catch(IOException ioe){
			return;
		}
	}
	
	private String getHexString(char c){
		int i = Integer.valueOf(c);
		String hex = Integer.toHexString(i);
		while(hex.length() < 4){
			hex = "0" + hex;
		}
		hex = "\\u" + hex;
		return hex;
	}
}
