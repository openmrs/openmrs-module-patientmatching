package org.openmrs.module.patientmatching.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class AdminList extends AdministrationSectionExt {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "patientmatching.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
        
        map.put("module/patientmatching/config.list", "patientmatching.config.view");
        map.put("module/patientmatching/schedule.list", "patientmatching.schedule.view");
        map.put("module/patientmatching/dupes.list", "patientmatching.report.view");
		
		return map;
	}
	
}