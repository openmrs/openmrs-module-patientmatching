package org.openmrs.module.patientmatching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class PatientMatchingReportMetadata {
	private final Log log = LogFactory.getLog(this.getClass());
	private String reportName;
	private String selstrategies;
	private String createdBy;
	private String dateCreated;
	private String pNameTime;
	
	public PatientMatchingReportMetadata()
	{
		
	}
	
	
		public String getDateCreated() {
			return dateCreated;
		}
		public String getCreatedBy() {
			return createdBy;
		}
		public String getReportName() {
			return reportName;
		}
		public String getpNameTime() {
			return pNameTime;
		}
		public String getSelstrategies() {
			return selstrategies;
		}
	    public void	setDateCreated(String dateCreated)
	    {
	    this.dateCreated=dateCreated;
	    }
	    public void	setCreatedBy(String createdBy)
	    {
	    this.createdBy=createdBy;
	    }
	    public void	setReportName(String reportname)
	    {
	    this.reportName=reportname;
	    }
	    public void	setSelStrats(String selstrategies)
	    {
	    this.selstrategies=selstrategies;
	    }
	    public void	setProcessNT(String pNameTime)
	    {
	    this.pNameTime=pNameTime;
	    }
		
		

}
