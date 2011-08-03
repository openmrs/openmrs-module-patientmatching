package org.openmrs.module.patientmatching;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.openmrs.module.patientmatching.PatientMatchingReportMetadata;
import org.springframework.transaction.annotation.Transactional;
public interface PatientMatchingReportMetadataService {
	
    public void saveReportDetails(PatientMatchingReportMetadata pri);
    public Map<String, Object> showReportDetails(String reportName);

}
