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
	
	public CharDelimLDSInspector(){
		
	}
	
	public void setDefaultDataColumns(LinkDataSource lds){
		lds.getDataColumns().clear();
		File f = new File(lds.getName());
		String delim = lds.getAccess();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			String[] fields = line.split(delim, -1);
			
			for(int i = 0; i < fields.length; i++){
				DataColumn dc = new DataColumn(Integer.toString(i));
				dc.setIncludePosition(i);
				dc.setType(DataColumn.STRING_TYPE);
				dc.setName("Column" + i);
				lds.addDataColumn(dc);
			}
		}
		catch(IOException ioe){
			return;
		}
	}
}
