package org.regenstrief.linkage.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientmatching.HibernateConnection;
import org.openmrs.module.patientmatching.LinkDBConnections;
import org.regenstrief.linkage.Record;

public class OpenMRSReader implements DataSourceReader {

    private final int DEFAULT_PAGING_SIZE = 10000; // number of patients read at a time
    
    private final int PAGING_SIZE = Context.getAdministrationService().getGlobalPropertyValue("patientmatching.scratchPageSize", Integer.valueOf(DEFAULT_PAGING_SIZE)).intValue();
    
    private final int PAGING_LIMIT = Context.getAdministrationService().getGlobalPropertyValue("patientmatching.scratchPageLimit", Integer.valueOf(-1)).intValue();
    
    private final static byte MODE_PATIENT = 0;
    
    private final static byte MODE_ARRAY = 1;
    
    private final static byte MODE_PROJECTION = 2;
    
    private final static String[][] PROJECTION_ALIASES = {
    		{ "org.openmrs.PersonName", "names" },
    		{ "org.openmrs.PersonAddress", "addresses" },
    		{ "org.openmrs.PatientIdentifier", "identifiers" }
    };

    protected final Log log = LogFactory.getLog(this.getClass());
    
    private Iterator<?> patients;

    private Criteria criteria;
    
    private int pageNumber;
    
    private byte resultMode;
    
    private boolean expand_patient;
    
    private Collection<String> projections;

    private Date reportCreatedOn;

    /**
     * 
     */
    public OpenMRSReader() {
    	this(null,null);
    }
    
    public OpenMRSReader(Collection<String> projections, Date reportCreatedOn) {
        pageNumber = 0;
        resultMode = MODE_PATIENT;
        expand_patient = true;
        if (projections != null && !projections.contains("org.openmrs.Patient.patientId")) {
        	projections.add("org.openmrs.Patient.patientId");
        }
        this.projections = projections;
        this.reportCreatedOn = reportCreatedOn;
        
    	log.info("Getting all patient records ...");
    	updatePatientList();
    	
    	log.info("Finish intialization ...");
    }
    
    public void setExpandPatient(boolean expand){
    	expand_patient = expand;
    }
    
    private Criteria createCriteria(){
        createHibernateSession().clear();
    	criteria = createHibernateSession().createCriteria(Patient.class)
    			.setMaxResults(PAGING_SIZE)
    			.setFirstResult(pageNumber * PAGING_SIZE);

    	// Add restriction to fetch the patients based on the date report created on
        if(reportCreatedOn != null)
            criteria.add(Restrictions.or(Restrictions.gt("dateCreated",reportCreatedOn),
                    Restrictions.gt("dateChanged",reportCreatedOn)));

    	if (projections != null) {
    		resultMode = MODE_PROJECTION;
    		ProjectionList projectionList = Projections.projectionList();
    		Set<String> aliases = null;
    		for (String projection : projections) {
    			if (projection.startsWith("org.openmrs.Patient")) {
    				projection = projection.substring(20);
    			} else {
	    			for (final String[] aliasDefinition : PROJECTION_ALIASES) {
	    				final String className = aliasDefinition[0];
		    			if (projection.startsWith(className)) {
		    				final String alias = aliasDefinition[1];
		    				projection = alias + projection.substring(className.length());
		    				if (aliases == null) {
		    					aliases = new HashSet<String>();
		    				}
		    				if (aliases.add(alias)) {
		    					criteria = criteria.createAlias(alias, alias);
		    				}
		    				break;
		    			}
	    			}
    			}
    			projectionList = projectionList.add(Projections.property(projection));
    		}
    		criteria = criteria.setProjection(projectionList);
    	}
    	return criteria;
    }
    
    private Session createHibernateSession() {
        HibernateConnection connection = new HibernateConnection();
        SessionFactory sessionFactory = connection.getSessionFactory();
        return sessionFactory.getCurrentSession();
    }
    
    private void updatePatientList() {
    	List<?> list;
        try {
        	resultMode = MODE_PATIENT;
            list = createCriteria().list();
        } catch (Exception e) {
        	if (projections != null) {
        		projections = null;
        		updatePatientList();
        		return;
        	}
            log.info("Iterating one by one on patient records ...");
            List<Object[]> patients = new ArrayList<Object[]>();
            list = patients;
            createHibernateSession().clear();
            
            String sql = getPatientQuery();
            
            List<PatientIdentifierType> idTypes = LinkDBConnections.getInstance().getPatientIdentifierTypes();
            List<PersonAttributeType> attTypes = LinkDBConnections.getInstance().getPersonAttributeTypes();
            
            String sqlPatientId = "select patient.patientId from Patient as patient  where patient.voided = false order by patient.patientId asc";
            Query queryPatientId = createHibernateSession().createQuery(sqlPatientId)
                                .setMaxResults(PAGING_SIZE).setFirstResult(PAGING_SIZE * pageNumber);
            Iterator<?> patientIds = queryPatientId.iterate();
            
            Integer currPatientId = null;
            while (patientIds.hasNext()) {
                try {
                    currPatientId = (Integer) patientIds.next();
                    Query query = createHibernateSession().createQuery(sql)
					        .setParameter("patientId", currPatientId, StandardBasicTypes.INTEGER);
                    Iterator<?> patientIter = query.iterate();
                    List<Object> objList = new ArrayList<Object>();
                    
                    int max_rows = 1;
                    if (expand_patient){
                    	max_rows = Integer.MAX_VALUE;
                    }
                    int patient_rows = 0;
                    while (patientIter.hasNext() && patient_rows < max_rows) {
                    	Object[] objects = (Object[]) patientIter.next();
                    	objList.clear();
                    	objList.addAll(Arrays.asList(objects));
                    	
                    	currPatientId = (Integer) objects[0];

                    	String sqlIdentitifier = "select patient.patientId, id.identifier, idType.name from Patient as patient join patient.identifiers as id " +
                    	"join id.identifierType as idType where patient.patientId = :patientId order by patient.patientId asc, idType.name asc";
                    	Query queryIdentifier = createHibernateSession().createQuery(sqlIdentitifier)
						        .setParameter("patientId", currPatientId, StandardBasicTypes.INTEGER);
                    	Iterator<?> iterIdentifier = queryIdentifier.iterate();

                    	Map<String, String> mapId = new HashMap<String, String>();
                    	while (iterIdentifier.hasNext()) {
                    		Object[] oId = (Object[]) iterIdentifier.next();
                    		mapId.put(String.valueOf(oId[2]), String.valueOf(oId[1]));
                    	}
                    	for (PatientIdentifierType idType : idTypes) {
                    		String value = mapId.get(idType.getName());
                    		if (value != null) {
                    			objList.add(value);
                    		} else {
                    			objList.add("");
                    		}
                    	}

                    	String sqlAttribute = "select patient.patientId, attr.value, attrType.name from Patient as patient join patient.attributes as attr " +
                    	"join attr.attributeType as attrType where patient.patientId = :patientId order by patient.patientId asc, attrType.name asc";
                    	Query queryAttribute = createHibernateSession().createQuery(sqlAttribute)
						        .setParameter("patientId", currPatientId, StandardBasicTypes.INTEGER);
                    	Iterator<?> iterAttribute = queryAttribute.iterate();

                    	Map<String, String> mapAtt = new HashMap<String, String>();
                    	while (iterAttribute.hasNext()) {
                    		Object[] oAtt = (Object[]) iterAttribute.next();
                    		mapAtt.put(String.valueOf(oAtt[2]), String.valueOf(oAtt[1]));
                    	}
                    	for (PersonAttributeType attType : attTypes) {
                    		String value = mapAtt.get(attType.getName());
                    		if (value != null) {
                    			objList.add(value);
                    		} else {
                    			objList.add("");
                    		}
                    	}
                    	patients.add(objList.toArray());
                    	patient_rows++;
                    }


                } catch (HibernateException hex) {
                	log.info("Exception caught during iterating patient ... Skipping ...");
                	log.info("Cause: " + e.getCause());
                	log.info("Message: " + e.getMessage());
                	throw new RuntimeException(hex);
                }
            }
        }
        patients = list.iterator();
    }

    private String getPatientQuery() {
        StringBuffer selectClause = new StringBuffer();
        for (String patientProperty: LinkDBConnections.getInstance().getPatientPropertyList()) {
            String classProperty = patientProperty.substring(patientProperty.lastIndexOf(".") + 1);
            if (!classProperty.equals("patientId")) {
                selectClause.append("patient.").append(classProperty).append(",");
            }
        }
        
        for (String nameProperty: LinkDBConnections.getInstance().getNamePropertyList()) {
            String classProperty = nameProperty.substring(nameProperty.lastIndexOf(".") + 1);
            selectClause.append("name.").append(classProperty).append(",");
        }
        
        for (String addressProperty: LinkDBConnections.getInstance().getAddressPropertyList()) {
            String classProperty = addressProperty.substring(addressProperty.lastIndexOf(".") + 1);
            selectClause.append("address.").append(classProperty).append(",");
        }
        
        String select = selectClause.substring(0, selectClause.toString().length() - 1);
        
        resultMode = MODE_ARRAY;
        String sql = "select patient.patientId, " + select +
                            " from Patient as patient join patient.names as name join patient.addresses as address " +
                            " where patient.patientId = :patientId and name.voided = false and address.voided = false" +
                            " order by patient.patientId asc, name.preferred asc, address.preferred asc";
        return sql;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#close()
     */
    @Override
    public boolean close() {
        createHibernateSession().clear();
        //createHibernateSession().close();     //Causing a session closed exception in report generation
    	patients = null;
    	return true;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#getRecordSize()
     */
    @Override
    public int getRecordSize() {
        return -999;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#hasNextRecord()
     */
    @Override
    public boolean hasNextRecord() {
    	if (patients == null) {
    		return false;
    	} else if (!patients.hasNext() && ((PAGING_LIMIT <= 0) || ((pageNumber + 1) < PAGING_LIMIT))) {
    		pageNumber++;
    		updatePatientList();
    	}

    	return patients.hasNext();
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#nextRecord()
     */
    @Override
    public Record nextRecord() {
        Record r;
        if (hasNextRecord()) {
        	Object o = patients.next();
            if (resultMode == MODE_PATIENT) {
                Patient p = (Patient) o;
                r = LinkDBConnections.getInstance().patientToRecord(p);
            } else if (resultMode == MODE_ARRAY) {
                Object[] objs = (Object[]) o;
                r = LinkDBConnections.getInstance().patientToRecord(objs);
            } else {
            	Object[] objs = (Object[]) o;
            	int patientIdIndex = 0;
            	for (final String projection : projections) {
            		if (projection.endsWith("patientId")) {
            			break;
            		}
            		patientIdIndex++;
            	}
            	final long uid = ((Number) objs[patientIdIndex]).longValue();
            	r = new Record(uid, LinkDBConnections.UID_CONTEXT);
            	int i = 0;
            	for (final String projection : projections) {
            		r.addDemographic(projection, String.valueOf(objs[i]));
            		i++;
            	}
            }
        } else {
        	r = null;
        }
        return r;
    }

    /**
     * 
     * @see org.regenstrief.linkage.io.DataSourceReader#reset()
     */
    @Override
    public boolean reset() {
    	pageNumber = 0;
    	
    	updatePatientList();
        
        return (patients != null);
    }
}
