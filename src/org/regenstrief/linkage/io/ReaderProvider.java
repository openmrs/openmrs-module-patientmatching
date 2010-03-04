package org.regenstrief.linkage.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.regenstrief.linkage.Record;
import org.regenstrief.linkage.util.LinkDataSource;
import org.regenstrief.linkage.util.MatchingConfig;

/**
 * Class created to return Reader objects for a given
 * data source description in a LinkDataSource object.
 * 
 * 
 * For database readers, it uses Apache Commons DBCP to pool
 * connections per datasource.
 * 
 * @author jegg
 *
 */

/*
 * Use of apache-commons DBCP taken from sample code
 * ManualPoolingDataSourceExample.java from the apache.org
 * svn repository
 */

public class ReaderProvider {
	Hashtable<LinkDataSource, DataSource> database_pools;
	Hashtable<LinkDataSource,Hashtable<MatchingConfig,Long>> sort_times;
	private static final ReaderProvider INSTANCE = new ReaderProvider();
	
	private ReaderProvider(){
		database_pools = new Hashtable<LinkDataSource, DataSource>();
		sort_times = new Hashtable<LinkDataSource,Hashtable<MatchingConfig,Long>>();
	}
	
	public static ReaderProvider getInstance(){
		return INSTANCE;
	}
	
	public DataSourceReader getReader(LinkDataSource lds){
		if(lds.getType().equals("CharDelimFile")){
			return new CharDelimFileReader(lds);
		} else if(lds.getType().equals("DataBase")){
			Connection db = getConnection(lds);
			return new DataBaseReader(lds, db);
		} else if(lds.getType().equals("Vector")){
			return new VectorReader(lds);
		}
		return null;
	}
	
	public OrderedDataSourceReader getReader(LinkDataSource lds, MatchingConfig mc){
		if(lds.getType().equals("CharDelimFile")){
			// check if orderedCharDelimFileReader has been created before for this lds and mc
			Hashtable<MatchingConfig,Long> entry = sort_times.get(lds);
			if(entry == null){
				entry = new Hashtable<MatchingConfig,Long>();
				entry.put(mc, new Long(new Date().getTime()));
				sort_times.put(lds, entry);
				return new OrderedCharDelimFileReader(lds, mc);
			} else {
				Long sorted = entry.get(mc);
				if(sorted == null){
					entry.put(mc, new Long(new Date().getTime()));
					return new OrderedCharDelimFileReader(lds, mc);
				} else {
					return new OrderedCharDelimFileReader(lds, mc, sorted);
				}
			}
			
		} else if(lds.getType().equals("DataBase")){
			Connection db = getConnection(lds);
			return new OrderedDataBaseReader(lds, db, mc);
		} else if(lds.getType().equals("Vector")){
			return new VectorReader(lds);
		}
		return null;
	}
	
	public SubsetDataSourceReader getReader(LinkDataSource lds, MatchingConfig mc, Record test){
		if(lds.getType().equals("CharDelimFile")){
			return null;
		} else if(lds.getType().equals("DataBase")){
			Connection db = getConnection(lds);
			return new SubsetDataBaseReader(lds, db, mc, test);
		} else if(lds.getType().equals("Vector")){
			return null;
		}
		return null;
	}
	
	private Connection getConnection(LinkDataSource lds){
		DataSource db = database_pools.get(lds);
		
		// if link data source hasn't been used to get a database connection yet
		if(db == null){
			db = getDataSource(lds);
			database_pools.put(lds, db);
		}
		
		try{
			
			return db.getConnection();
		}
		catch(SQLException sqle){
			return null;
		}
		catch(NullPointerException npe){
			return null;
		}
		catch(Exception e){
			return null;
		}
	}
	
	private DataSource getDataSource(LinkDataSource lds){
		try{
			String driver, url, user, passwd;
			String[] access = lds.getAccess().split(",");
			driver = access[0];
			url = access[1];
			user = access[2];
			passwd = access[3];
			
			Class.forName(driver);
			
			ObjectPool connectionPool = new GenericObjectPool(null);
	        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, passwd);
	        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
	        PoolingDataSource data_source = new PoolingDataSource(connectionPool);
	        
	        return data_source;
		}
		catch(Exception e){
			
		}
		
		return null;
	}
}
