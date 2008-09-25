package org.openmrs.module.patientmatching;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.patientmatching.advice.PatientMatchingAdvice;
import org.regenstrief.linkage.db.RecordDBManager;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * Class implements the startup and initial methods for the module.  When
 * loading, module runs the populateMatchingTable() method to synchronize
 * the OpenMRS patient database with the record linkage table.  To do this,
 * the module needs to check that ever patient's openmrs_id exists in the table
 * and add it if it is not present.  This can take a long time.
 * 
 * @author jegg
 *
 */

/*
 * 
 * The demographic information used in the linkage process is the same:
 * 
 * 	"mrn" - medical record number
 * 	"ln"  - last name
 * 	"lny" - last name NYSIIS
 * 	"fn"  - first name
 * 	"sex"  - sex/gender
 * 	"mb" - month of birth
 * 	"db" - date of birth
 * 	"yb" - year of birth
 * 	"city" - address city
 * 	"st" - address street
 * 	"zip" - address zip code
 * 	"tel" - telephone number
 * 	"nkln" - next of kin last name
 * 	"nkfn" - next of kin first name
 * 	"drid" - Dr. ID
 * 	"drfn" - Dr. first name
 * 	"drln" - Dr. last name 
 *
 */
public class PatientMatchingActivator extends StaticMethodMatcherPointcutAdvisor implements Activator, Advisor{
	
	public static final String CREATE_METHOD = "createPatient";
	public static final String UPDATE_METHOD = "updatePatient";
	public static final String FIND_METHOD = "findPatient";
	
	public final static String CONFIG_FILE = "link_config.xml";
	public final static String MATCHING_ATTRIBUTE = "Other Matching Information";
	public final static String LINK_TABLE_KEY_DEMOGRAPHIC = "openmrs_id";
	
	// to fix the automatic startup issue, need to get this privilege
	// it's either "View Patients" or "View Cohorts"
	public final static String PRIVILEGE = "View Patients";
    public final static String PRIVILEGE2 = "View Patient Cohorts";
    public final static String PRIVILEGE3 = "View Identifier Types";
    public final static String PRIVILEGE4 = "View Person Attribute Types";
	
	protected static final Log logger = LogFactory.getLog(PatientMatchingActivator.class);
	private Log log = LogFactory.getLog(this.getClass());
	public static final String FILE_LOG = "patient_matching_file_log";
	
	/**
	 * Method calls the disconnect method in the LinkDBManager object.
	 */
	public void shutdown() {
		log.info("Shutting down Patient Matching Module");
		LinkDBConnections ldb_con = LinkDBConnections.getInstance();
		RecordDBManager link_db = ldb_con.getRecDBManager();
		link_db.disconnect();
		Context.removeProxyPrivilege(PRIVILEGE);
        Context.removeProxyPrivilege(PRIVILEGE2);
        Context.removeProxyPrivilege(PRIVILEGE3);
        Context.removeProxyPrivilege(PRIVILEGE4);
	}
	
	/**
	 * At startup, module parses the configuration file and makes sure the
	 * record linkage table is populated with the existing patients in OpenMRS.
	 */
	public void startup() {
		log.info("Starting Patient Matching Module");
		
		// to fix automatic startup, get privilege
		Context.addProxyPrivilege(PRIVILEGE);
        Context.addProxyPrivilege(PRIVILEGE2);
        Context.addProxyPrivilege(PRIVILEGE3);
        Context.addProxyPrivilege(PRIVILEGE4);
		
		log.info("Starting to populate matching table");
		if(LinkDBConnections.getInstance().getRecDBManager() != null){
			populateMatchingTable();
			log.info("Matching table populated");
		} else {
			log.warn("Error parsing config file and creating linkage objects");
		}
		
	}
	
	/**
	 * Method iterates through existing Patient objects and adds them to the linkage
	 * database for use when matching.  Method also creates the table, if needed,
	 * with the correct column taken from the LinkDataSource object.
	 *
	 */
	private void populateMatchingTable(){
		
		// get the list of patient objects
		//link_db.createTable();
		LinkDBConnections ldb_con = LinkDBConnections.getInstance();
		RecordDBManager link_db = ldb_con.getRecDBManager();
		if(!link_db.connect()){
			log.warn("Error connecting to link db when populating database");
			return;
		}
		
		// iterate through them, and if linkage tables does not contain 
		// a record with openmrs_id equal to patient.getID, then add
		PatientSetService pss = Context.getPatientSetService();
		PatientService patientService = Context.getPatientService();
		Set<Integer> patient_list = pss.getAllPatients().getMemberIds();
		Iterator<Integer> it = patient_list.iterator();
		while(it.hasNext()){
			Patient p = patientService.getPatient(it.next());
			Integer id = p.getPatientId();
			int existing_patients = link_db.getRecordCountFromDB(LINK_TABLE_KEY_DEMOGRAPHIC, id.toString());
			if(existing_patients == 0){
				if(link_db.addRecordToDB(LinkDBConnections.getInstance().patientToRecord(p))){
					if(log.isDebugEnabled()){
						log.debug("Adding patient " + p.getPatientId() + " to link DB succeeded");
					}
				} else {
					log.warn("Adding patient " + p.getPatientId() + " to link DB failed");
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
    public boolean matches(Method method, Class targetClass) {
		String method_name = method.getName();
		if(method_name.equals(CREATE_METHOD)){
			return true;
		} else if(method_name.equals(UPDATE_METHOD)){
			return true;
		} else if(method_name.equals(FIND_METHOD)){
			return true;
		}
		return false;
	}
	
	@Override
	public Advice getAdvice(){
		log.debug("Returning new advice object from " + this);
		return new PatientMatchingAdvice();
	}
	
}
