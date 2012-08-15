package org.openmrs.module.patientmatching;

import java.lang.reflect.Method;
import java.util.Collection;
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
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.patientmatching.advice.PatientMatchingAdvice;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;
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
public class PatientMatchingActivator extends StaticMethodMatcherPointcutAdvisor implements ModuleActivator, Advisor{
	
	/**
	 * generated serial version uid
	 */
	private static final long serialVersionUID = 3883294318400519194L;
	public static final String CREATE_METHOD = "createPatient";
	public static final String UPDATE_METHOD = "updatePatient";
	public static final String FIND_METHOD = "findPatient";
	public static final String GET_PATIENT_METHOD = "getPatientByExample";
	public static final String MERGE_METHOD = "mergePatient";
	public static final String SAVE_METHOD = "savePatient";
    private static final String extention = "patientMatching";
	public final static String CONFIG_FILE = "link_config.xml";
	public final static String MATCHING_ATTRIBUTE = "Other Matching Information";
	public final static String LINK_TABLE_KEY_DEMOGRAPHIC = "openmrs_id";
	
	public static final int DEFAULT_RECORD_MATCHING_ID = 0;
	
	protected static final Log logger = LogFactory.getLog(PatientMatchingActivator.class);
	private Log log = LogFactory.getLog(this.getClass());
	public static final String FILE_LOG = "patient_matching_file_log";
	

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
		} else if(method_name.equals(SAVE_METHOD)){
			return true;
		} else if(method_name.equals(MERGE_METHOD)){
			return true;
		} else if(method_name.equals(GET_PATIENT_METHOD)){
			return true;
		}
		return false;
	}
	
	@Override
	public Advice getAdvice(){
		log.debug("Returning new advice object from " + this);
		return new PatientMatchingAdvice();
	}

    public void willRefreshContext() {
    }

    public void contextRefreshed() {
    }

    /**
     * At startup, module parses the configuration file and makes sure the
     * record linkage table is populated with the existing patients in OpenMRS.
     */
    public void willStart() {
        log.info("Starting Patient Matching Module");

        try{
            // get privileges
            Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
            Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS);
            Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES);
            Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES);

            log.info("Starting to populate matching table");
            if(LinkDBConnections.getInstance().getRecDBManager() != null){
                populateMatchingTable();
                log.info("Matching table populated");
            } else {
                log.warn("Error parsing config file and creating linkage objects");
            }
        }
        finally{
            Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS);
            Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS);
            Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_IDENTIFIER_TYPES);
            Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_PERSON_ATTRIBUTE_TYPES);
        }
    }

    public void started() {
        ReportMigrationUtils.migrateFlatFilesToDB();
    }

    /**
     * Method calls the disconnect method in the LinkDBManager object.
     */
    public void willStop() {
        log.info("Shutting down Patient Matching Module");

        Collection<TaskDefinition> rTasks = Context.getSchedulerService().getRegisteredTasks();
        for(TaskDefinition td:rTasks){
            if(td.getName().contains(extention)){
                try {
                    Context.getSchedulerService().shutdownTask(td);
                } catch (SchedulerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        LinkDBConnections ldb_con = LinkDBConnections.getInstance();
        RecordDBManager link_db = ldb_con.getRecDBManager();
        link_db.disconnect();
    }

    public void stopped() {
    }
}
