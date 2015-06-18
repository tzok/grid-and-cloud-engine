/**************************************************************************
Copyright (c) 2011:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on
the
copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Author: Diego Scardaci (INFN)
****************************************************************************/


package it.infn.ct.GridEngine.UsersTracking;

import it.infn.ct.GridEngine.Job.JSagaJobSubmission;
import it.infn.ct.GridEngine.Job.JobId;
import it.infn.ct.GridEngine.JobCollection.ClosedJobCollection;
import it.infn.ct.GridEngine.JobCollection.JobCollection;
import it.infn.ct.GridEngine.JobCollection.WorkflowN1;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.JobService.JobCheckStatusService;
import it.infn.ct.GridEngine.SendMail.MailUtility;
import it.infn.ct.GridEngine.SendMail.MailUtility.ContentMessage;
import it.infn.ct.ThreadPool.CheckJobStatusThreadPoolExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class UsersTrackingDBInterface {
	
	Connection conn = null;
	String URL		= "";
	String userName = "";
	String password = "";
	public boolean inAppServer;
	int jobsUpdatingInterval = 300;
	//Vector<String> jobsStatusThread = new Vector<String>();
	
	private CheckJobStatusThreadPoolExecutor threadPool = null;
	private boolean updatingJobsStatus=false;
	
	private static final Logger logger = Logger.getLogger(UsersTrackingDBInterface.class);

	/**
	 * 
	 */
	public UsersTrackingDBInterface() {
		inAppServer = true;
		try {
			threadPool = InitialContext.<CheckJobStatusThreadPoolExecutor>doLookup("GridEngine-CheckStatusPool");
		}
		catch ( NamingException ex ) {
			logger.error("Cannot get thread-pool: " + ex);
//			System.out.println("Cannot get thread-pool: " + ex);
		}
		catch(Exception ex){
			logger.error("Cannot get thread-pool: " + ex);
//			System.out.println("Cannot get thread-pool: " + ex);
		}
	}
	
	/**
	 * Constructs an objects that represents an interface to the users tracking database,
	 * where all user grid interactions are stored, specifying connection parameters.
	 * 
	 * @param url database name
	 * @param username database username
	 * @param pwd database password.
	 */
	public UsersTrackingDBInterface(String url, String username, String pwd) {
		inAppServer = false;
		URL = url;
		userName = username;
		password = pwd;
	}

	private boolean CreateDBConnection() {
		if (inAppServer){
			logger.debug("###I'm in app server###");
			return CreateDBConnectionGlassfish();
		}
		else{
			logger.debug("***I'm not in app server***");
			return CreateDBConnectionExternal();
		}
	}
	
	/** Uses JNDI and Datasource (preferred style).   */
	private boolean CreateDBConnectionGlassfish() {
		if (conn != null) return true; //Connection just created
	    
		//String DATASOURCE_CONTEXT = "java:comp/env/jdbc/blah";
		String DATASOURCE_CONTEXT = "jdbc/UserTrackingPool";

		Connection result = null;
		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource)initialContext.lookup(DATASOURCE_CONTEXT);
			if (datasource != null) {
				result = datasource.getConnection();
			}
			else {
				logger.warn("Failed to lookup datasource.");
//				System.out.println("Failed to lookup datasource.");
			}
		}
		catch ( NamingException ex ) {
			logger.error("Cannot get connection: " + ex);
//			System.out.println("Cannot get connection: " + ex);
		}
		catch(SQLException ex){
			logger.error("Cannot get connection: " + ex);
//			System.out.println("Cannot get connection: " + ex);
		}
		catch(Exception ex){
			logger.error("Cannot get connection: " + ex);
//			System.out.println("Cannot get connection: " + ex);
		}
		conn = result;
		
		if (conn==null) return false;
		
		return true;
	}

	private boolean CreateDBConnectionExternal() {
		if (conn != null) return true; //Connection just created
	        
        try
        {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (URL, userName, password);
            logger.info("Database connection established");
//            System.out.println ("Database connection established");
            return true;
        }
        catch (Exception e)
        {
        	logger.error("Cannot connect to database server: " + e.toString());
//            System.out.println(e.toString());
        	System.err.println ("Cannot connect to database server");
        }
        
        return false;
	}
	
	private boolean CloseDBConnection() {
		if (conn != null)
        {
            try
            {
                conn.close ();
                conn = null;
                logger.info("Database connection terminated");
//                System.out.println ("Database connection terminated");
                return true;
            }
            catch (Exception e) { 
            	/* ignore close errors */
            	return false;
            }
        }
		
		return true;
	}
	
	/**
	 * Returns the interval between two consecutively updating job status events.
	 * 
	 * @return the interval between two consecutively updating job status events.
	 */
	public int getJobsUpdatingInterval() {
		return jobsUpdatingInterval;
	}
	
	/**
	 * Sets the interval between two consecutively updating job status events.
	 * 
	 * @param value the interval between two consecutively updating job status events.
	 */
	public void setJobsUpdatingInterval(int value) {
		jobsUpdatingInterval = value;
	}
	
//	public boolean isInAppServer() {
//		return inAppServer;
//	}

	/**
	 * Performs a SQL INSERT INTO in the ActiveGridInteractions users tracking database table
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param gridInteraction an identifier of application in a specified portal
	 * @param gridId the grid identifier for this interaction
	 * @param robotCertificate used to submit this job
	 * @param proxyId used to submit this job
	 * @param VO
	 * @param FQAN
	 * @param userDescription a description for this job.
	 * @param jobIdCollection collection identifier, if job belongs to a collection, null otherwise 
	 * @return auto generated id of this insert.
	 */
	public int InsertActiveGridInteraction(String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, Integer jobIdCollection) {
	//********MARIO**********
		//email == eTokenServer == ""
		//return InsertGridInteraction("ActiveGridInteractions", commonName, tcpAddress, gridInteraction, gridId, robotCertificate, proxyId, VO, FQAN, userDescription);
		return InsertGridInteraction("ActiveGridInteractions", commonName, tcpAddress, gridInteraction, gridId, robotCertificate, proxyId, VO, FQAN, userDescription, "", "", jobIdCollection);
	}

	/**
	 * Performs a SQL INSERT INTO in the ActiveGridInteractions users tracking database table
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param gridInteraction an identifier of application in a specified portal
	 * @param gridId the grid identifier for this interaction
	 * @param robotCertificate used to submit this job
	 * @param proxyId used to submit this job
	 * @param VO
	 * @param FQAN
	 * @param userDescription a description for this job.
	 * @param email user email to notify that the job has been
	 * @param jobCollectionId collection identifier, if job belongs to a collection, null otherwise 
	 * @return auto generated id of this insert.
	 */
	public int InsertActiveGridInteraction(String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, String email, Integer jobCollectionId) {
		//eTokenServer == ""
		//email != ""
		return InsertGridInteraction("ActiveGridInteractions", commonName, tcpAddress, gridInteraction, gridId, robotCertificate, proxyId, VO, FQAN, userDescription, email, "", jobCollectionId);
	}
	
	/**
	 * Performs a SQL INSERT INTO in the ActiveGridInteractions users tracking database table
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param gridInteraction an identifier of application in a specified portal
	 * @param gridId the grid identifier for this interaction
	 * @param robotCertificate used to submit this job
	 * @param proxyId used to submit this job
	 * @param VO
	 * @param FQAN
	 * @param userDescription a description for this job.
	 * @param email user email to notify that the job has been
	 * @param eTokenServer
	 * @param jobIdCollection collection identifier, if job belongs to a collection, null otherwise 
	 * @return auto generated id of this insert.
	 */
	public int InsertActiveGridInteraction(String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, String email, String eTokenServer, Integer jobIdCollection) {
		//email != eTokenServer != ""
		return InsertGridInteraction("ActiveGridInteractions", commonName, tcpAddress, gridInteraction, gridId, robotCertificate, proxyId, VO, FQAN, userDescription, email, eTokenServer, jobIdCollection );
	}
	
	private int InsertGridInteraction(String DB, String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, String email, String eTokenServer, Integer jobIdCollection) {
	//***********************
	//private int InsertGridInteraction(String DB, String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription) {
		if(logger.isDebugEnabled())
			logger.debug("Input = " + DB + "," + commonName + "," + tcpAddress + "," + gridInteraction + "," + gridId + "," + robotCertificate + "," + proxyId + "," + VO + "," + FQAN);

		if (CreateDBConnection())
        {
            try
            {
            	//String query = "INSERT INTO " + DB + " (common_name, tcp_address, grid_interaction, grid_id, robot_certificate, proxy_id, virtual_organization, fqan, user_description, status, timestamp) VALUES (?,?,?,?,?,?,?,?,?,?,?) ";
            	String query = "INSERT INTO " + DB + " (common_name, tcp_address, grid_interaction, grid_id, robot_certificate, proxy_id, virtual_organization, fqan, user_description, status, timestamp, email, e_token_server, id_job_collection) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
            	PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            	pstmt.setString(1, commonName);
            	pstmt.setString(2, tcpAddress);
            	pstmt.setInt(3, gridInteraction);
            	pstmt.setString(4, gridId);
            	pstmt.setString(5, robotCertificate);
            	pstmt.setString(6, proxyId);
            	pstmt.setString(7, VO);
            	pstmt.setString(8, FQAN);
            	pstmt.setString(9, userDescription);
            	pstmt.setString(10, "SUBMITTED"); //TODO Can I leave this default value for the data management?
            	pstmt.setTimestamp(11, getCurrentUTCTimestamp());
            	//***********MARIO*************
            	pstmt.setString(12, email);
            	pstmt.setString(13, eTokenServer);
            	if(jobIdCollection!=null)
            		pstmt.setInt(14, jobIdCollection);
            	else 
            		pstmt.setNull(14, java.sql.Types.INTEGER);
            	//*****************************
            	
            	boolean flag = pstmt.execute();

            	if (flag) System.out.println("flag true");
            	
            	logger.info("Data Inserted.");
            	
            	ResultSet rs = pstmt.getGeneratedKeys();
            	if (rs.next()) {
            		int auto_id = rs.getInt(1);
            		logger.info("ID = " + auto_id);
            		
            		if (!updatingJobsStatus)
            			CloseDBConnection();
            		
                    return auto_id;
                }
            	else {
            		logger.warn("There are no generated keys.");
//            	    System.out.println("There are no generated keys.");
            	}
            	
            }
            catch (Exception e) {
            	logger.error("Cannot insert data in the DB." + e.toString());
//            	System.out.println(e.toString());
            	System.err.println ("Cannot insert data in the DB.");
            	e.printStackTrace();
            }
        }
		else{
			logger.error("Cannot insert data in the DB: DB Connection is closed.");
			System.err.println ("Cannot insert data in the DB: DB Connection is closed.");
		}
		
		if(!updatingJobsStatus)
			CloseDBConnection();
		
        return -1;
		
	}
	
//	private int InsertEndGridInteraction(String DB, String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, Timestamp jobStartDataTime, String status, String gridCE, float latitude, float longitude, Timestamp jobEndDataTime) {
	private int InsertEndGridInteraction(String DB, String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, Timestamp jobStartDataTime, String status, String gridCE, float latitude, float longitude, Timestamp jobEndDataTime, Integer idClosedCollection) {
		if(logger.isDebugEnabled())
			logger.debug("Input = " + DB + "," + commonName + "," + tcpAddress + "," + gridInteraction + "," + gridId + "," + robotCertificate + "," + proxyId + "," + VO + "," + FQAN);
		if (CreateDBConnection())
        {
            try
            {
//            	String query = "INSERT INTO " + DB + " (common_name, tcp_address, grid_interaction, grid_id, robot_certificate, proxy_id, virtual_organization, fqan, user_description, status, timestamp, grid_ce, latitude, longitude, timestamp_endjob) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
            	String query = "INSERT INTO " + DB + " (common_name, tcp_address, grid_interaction, grid_id, robot_certificate, proxy_id, virtual_organization, fqan, user_description, status, timestamp, grid_ce, latitude, longitude, timestamp_endjob, id_job_collection) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
            	PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            	pstmt.setString(1, commonName);
            	pstmt.setString(2, tcpAddress);
            	pstmt.setInt(3, gridInteraction);
            	pstmt.setString(4, gridId);
            	pstmt.setString(5, robotCertificate);
            	pstmt.setString(6, proxyId);
            	pstmt.setString(7, VO);
            	pstmt.setString(8, FQAN);
            	pstmt.setString(9, userDescription);
            	pstmt.setString(10, status); //TODO Can I leave this default value for the data management?
            	pstmt.setTimestamp(11, jobStartDataTime);
            	pstmt.setString(12, gridCE);
            	pstmt.setFloat(13, latitude);
            	pstmt.setFloat(14, longitude);
            	pstmt.setTimestamp(15, jobEndDataTime);
            	if(idClosedCollection != null)
            		pstmt.setInt(16, idClosedCollection);
            	boolean flag = pstmt.execute();

            	if (flag) System.out.println("flag true");
            	logger.info("Data Inserted.");
//            	System.out.println("Data Inserted.");
            	
            	ResultSet rs = pstmt.getGeneratedKeys();
            	if (rs.next()) {
            		int auto_id = rs.getInt(1);
            		logger.info("ID = " + auto_id);
//            		System.out.println("ID = " + auto_id);
            		
            		if(!updatingJobsStatus)
            			CloseDBConnection();
                    
            		return auto_id;
                }
            	else {
            		logger.warn("There are no generated keys.");
//            	    System.out.println("There are no generated keys.");
            	}
            	
            }
            catch (Exception e) {
            	logger.error("Cannot insert data in the DB." + e.toString());
//            	System.out.println(e.toString());
            	System.err.println ("Cannot insert data in the DB.");
            }
        }
		else{
			logger.error("Cannot insert data in the DB: DB Connection is closed.");
			System.err.println ("Cannot insert data in the DB: DB Connection is closed.");
		}
		
		if(!updatingJobsStatus)
			CloseDBConnection();
        
		return -1;
        
	}
	
	/**
	 * Sets grid identifier to the active grid interaction with the specified id.
	 * 
	 * @param grid_id grid identifier
	 * @param id identifier of the active grid interaction that will be updated. 
	 * @return 0 if the update was successfully, -1 otherwise.
	 */
	public int updateGridInteraction(String grid_id, int id) {
		if (CreateDBConnection())
        {
            try
            {
            	PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE ActiveGridInteractions SET grid_id = ? WHERE id = ?");
            	
            	pstmt.setString(1, grid_id);
            	pstmt.setInt(2, id);

            	pstmt.executeUpdate();
            	
            	if(!updatingJobsStatus)
            		CloseDBConnection();
            	
            	return 0;
            	
            }
            catch (Exception e) {
            	logger.error("Cannot update  data in the DB." + e.toString());
//            	System.out.println(e.toString());
            	System.err.println ("Cannot update  data in the DB.");
            }
        }
		else{
			logger.error("Cannot update data in the DB: DB Connection is closed.");
//			System.err.println ("Cannot update data in the DB: DB Connection is closed.");
		}
		
		if(!updatingJobsStatus)
			CloseDBConnection();
    	
		return -1;
	}
	
	/**
	 * This method closes an active grid interaction by moving information from 
	 * ActiveGridIteractions table to GridInteractions.  
	 * 
	 * @param id of the closing active grid interaction.
	 * @return 0 if the moving was successfully, -1 otherwise.
	 * @see #CloseGridInteraction(int id, int idClosedCollection)
	 */
	public int CloseGridInteraction(int id){
		return CloseGridInteraction(id, -1);
	}
	
	/**
	 * This method closes an active grid interaction belonging to a collection by 
	 * moving information from ActiveGridIteractions table to GridInteractions and 
	 * setting the identifier of the closed collection.
	 * 
	 * @param id of the closing active grid interaction
	 * @param idClosedCollection identifier of the closed collection.
	 * @return 0 if the moving was successfully, -1 otherwise.
	 */
	public int CloseGridInteraction(int id, int idClosedCollection) {
		if (CreateDBConnection())
        {
            try
            {
				String query = "SELECT * FROM ActiveGridInteractions WHERE id = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
				pstmt.setInt(1, id);
				
				ResultSet rs;
				rs = pstmt.executeQuery();
				
				//if (rs==null) System.out.println("rs is null");
				
				if (rs.next()) {
					logger.info("Inserting data in GridInteractions.");
//					System.out.println("Inserting data in GridInteractions.");
					//InsertEndGridInteraction(String DB, String commonName, String tcpAddress, int gridInteraction, String gridId, String robotCertificate, String proxyId, String VO, String FQAN, String userDescription, Timestamp jobStartDataTime, String status, String gridCE, float latitude, float longitude, Timestamp jobEndDataTime) {
					String status = rs.getString(12);
					if(!status.equals("DONE"))
						status = "Aborted";
						
//					InsertEndGridInteraction("GridInteractions",rs.getString(2),rs.getString(3),rs.getInt(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getString(10),rs.getString(11),rs.getTimestamp(4),status,rs.getString(13),rs.getFloat(14),rs.getFloat(15),rs.getTimestamp(16));
					InsertEndGridInteraction("GridInteractions",rs.getString(2),rs.getString(3),rs.getInt(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9),rs.getString(10),rs.getString(11),rs.getTimestamp(4),status,rs.getString(13),rs.getFloat(14),rs.getFloat(15),rs.getTimestamp(16), idClosedCollection);
					
					if (CreateDBConnection()) {
						logger.info("Deleting data in ActiveGridInteractions.");
//						System.out.println("Deleting data in ActiveGridInteractions.");
						query = "DELETE FROM ActiveGridInteractions WHERE id = ?";
						pstmt = (PreparedStatement) conn.prepareStatement(query);
						pstmt.setInt(1, id);
						
						pstmt.execute();
						
						if(!updatingJobsStatus)
							CloseDBConnection();
						
						return 0;
					}
					else
						logger.warn("Error in delete entry in ActiveGridInteractions.");
//						System.out.println("Error in delete entry in ActiveGridInteractions.");
					
					
				}
            	
            }
            catch (Exception e) {
            	logger.error("Cannot move data in the DB." + e.toString());
//            	System.out.println(e.toString());
            	System.err.println ("Cannot move data in the DB.");
            }
        }
		else{
			logger.error("Cannot move data in the DB: DB Connection is closed.");
			System.err.println ("Cannot move data in the DB: DB Connection is closed.");
		}
		
		if(!updatingJobsStatus)
			CloseDBConnection();
		
		return -1;
	}
	
	/**
	 * Returns a {@link Vector} of {@link ActiveInteractions}
	 *
	 * @param commonName a String representing user name
	 * @return a {@link Vector} of {@link ActiveInteractions}
	 */
	public Vector<ActiveInteractions> getActiveInteractionsByName (String commonName) {  
		//l'output è un vettore di stringhe costituito da id(Activegrid..) Portal(GridOperation) descri(GO) userdesr(AcGI) timestamp(AcGI) status(AcGI)
		//In caso di collection prenod i capi d acollection attive
		
		Vector<ActiveInteractions> activeInteractions = new Vector<ActiveInteractions>();
		
		Vector<String[]> activeJobsList = getActiveJobsListByName(commonName);
//		Vector<JobCollection> activeCollections = JobCollection.getActiveJobCollectionsByName(commonName);
		HashMap<Integer, Vector<String[]>> tmp = new HashMap<Integer, Vector<String[]>>();
		
		for(String[] gridInteraction : activeJobsList){
			if(gridInteraction[6]==null){
				activeInteractions.add(new ActiveInteractions(gridInteraction, null));
			} else {
				Integer key = Integer.parseInt(gridInteraction[6]);
				if(tmp.containsKey(key))
					tmp.get(key).add(gridInteraction);
				else{
					Vector<String[]> firstSubjob = new Vector<String[]>();
					firstSubjob.add(gridInteraction);
					tmp.put(key, firstSubjob);
				}
			}
		}
		
		if(tmp!=null){
			for(Integer key : tmp.keySet()){
				JobCollection collection = JobCollection.getJobCollection(key);
				String[] collectionInfos = collection.getCollectionInfos();
				if(collection instanceof WorkflowN1){
					if(collection.getCollectionStatus().equals("SUBMITTING_FINAL_JOB") || 
							collection.getCollectionStatus().equals("RUNNING_FINAL_JOB") ||
							collection.getCollectionStatus().equals("DONE")){
						Vector<String[]> finalSubJob = new Vector<String[]>();
						for(String[] subJobsinfos : tmp.get(key))
							if(subJobsinfos[3].contains("-Final"))
								finalSubJob.add(subJobsinfos);
						tmp.put(key, finalSubJob);
					}
				} 
				collectionInfos[1] = tmp.get(key).elementAt(0)[1];
				collectionInfos[2] = tmp.get(key).elementAt(0)[2];
				activeInteractions.add(new ActiveInteractions(collectionInfos, tmp.get(key)));
			}
		}
		return activeInteractions;
	}

	@Deprecated
	public Vector<String[]> getActiveJobsListByName (String commonName) {  
		//l'output è un vettore di stringhe costituito da id(Activegrid..) Portal(GridOperation) descri(GO) userdesr(AcGI) timestamp(AcGI) status(AcGI)
		//In caso di collection prenod i capi d acollection attive
		
		
		if (CreateDBConnection())
        {
			Vector<String[]> activeJobsList = new Vector<String[]>();
			try
            {
				logger.info("Querying ActiveGridInteractions...");
//				System.out.println("querying ActiveGridInteractions...");
				String query = "SELECT * FROM ActiveGridInteractions WHERE common_name = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, commonName);
				
				ResultSet rs;
				rs = pstmt.executeQuery();
				
//				if (rs==null) {
//					System.out.println("rs is null");
//					System.out.println("jobs list is empty for " + commonName);
//					CloseDBConnection();
//					return null;
//				}
				
				while (rs.next()) {
					String[] item = new String[7];
					logger.info("querying GridOperations...");
//					System.out.println("querying GridOperations...");
					query = "SELECT * FROM GridOperations WHERE id = ?";
					PreparedStatement pstmt2 = (PreparedStatement) conn.prepareStatement(query);
					pstmt2.setInt(1, rs.getInt(5));
					
					ResultSet rsGridOp;
					rsGridOp = pstmt2.executeQuery();
					
					if ( (rsGridOp!=null) && (rsGridOp.next()) ) {
						logger.info("Getting GridOperations data...");
//						System.out.println("getting GridOperations data...");
						item[1] = rsGridOp.getString(2);
						item[2] = rsGridOp.getString(3);
						
						rsGridOp.close();
						pstmt2.close();
					}
					else {
						logger.warn("Grid Interaction not registered.");
//						System.out.println("Grid Interaction not registered.");
						item[1] = "";
						item[2] = "";
					}
					
					item[0] = new String(String.valueOf(rs.getInt(1)));
					item[3] = rs.getString(11).toString();
					item[4] = rs.getTimestamp(4).toString();
					logger.info("Querying for job status...");
//					System.out.println("querying for job status...");
					
					item[5] = rs.getString(12);
					item[6] = rs.getString(19);
					activeJobsList.add(item);
					
				}
				rs.close();
				pstmt.close();
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				
				return activeJobsList;
            }
			catch (Exception e) {
				logger.error(e.toString());
//            	System.out.println(e.toString());
            }
			if(!updatingJobsStatus)
				CloseDBConnection();
        }
		else{
			logger.info("Cannot get active jobs list from DB: DB Connection is closed.");
			System.err.println ("Cannot get active jobs list from DB: DB Connection is closed.");
		}
		return null;
	}
	
	/**
	 * Returns the geographic distribution of the jobs for a given portal and user. The geographic
	 * distribution is made of a {@link Vector} of string[] containing the following values:
	 * <ol>
	 * <li> computing element name;</li>
	 * <li> number of RUNNING jobs;</li>
	 * <li> number of DONE jobs;</li>
	 * <li> middleware name;</li>
	 * <li> computing element latitude;</li>
	 * <li> computing element longitude.</li>
	 * </ol>
	 * @param portal the given portal name
	 * @param commonName the given user name 
	 * @return the geographic distribution of the jobs for a given portal and user.
	 */
	public Vector<String[]> getCEsGeographicDistribution (String portal, String commonName) {
		//middleware
		//0 glite
		//1 unicore
		//2 wsgram
		//3 ourgrid
		//4 genesis II
		//5 gos
		if (CreateDBConnection())
		{
			Vector<String[]> cesGeographicDistribution = new Vector<String[]>();
			Hashtable<String, int[]> cesJobsCounter = new Hashtable<String, int[]>();
			try
			{
				logger.info("Querying GridOperations...");
//				System.out.println("querying GridOperations...");
				String query = "SELECT * FROM GridOperations WHERE portal = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, portal);

				ResultSet rs;
				rs = pstmt.executeQuery();

//				if (rs==null) {
//					System.out.println("rs is null");
//					System.out.println("No applications defined in portal " + portal);
//					CloseDBConnection();
//					return null;
//				}

				Vector<Integer> grid_interactions = new Vector<Integer>();

				while (rs.next()) {
					grid_interactions.add(new Integer(rs.getInt(1)));
				}

				rs.close();
				pstmt.close();
				
				Iterator<Integer> i = grid_interactions.iterator();
				
				while(i.hasNext()) {
					int gridInteractionID = ((Integer)i.next()).intValue();
					logger.info("Querying ActiveGridInteractions...");
//					System.out.println("querying ActiveGridInteractions...");
					query = "SELECT grid_ce, status, grid_id FROM ActiveGridInteractions WHERE common_name = ? AND grid_interaction = ?";
					PreparedStatement pstmtJob = (PreparedStatement) conn.prepareStatement(query);
					pstmtJob.setString(1, commonName);
					pstmtJob.setInt(2, gridInteractionID);

					ResultSet rsJob;
					rsJob = pstmtJob.executeQuery();

//					if (rsJob==null) {
//						System.out.println("rs is null");
//						System.out.println("jobs list is empty for " + commonName);
//						CloseDBConnection();
//						return null;
//					}

					while (rsJob.next()) {
						String ceName = rsJob.getString(1);
						if (!ceName.equals("")) {
							int[] jobsCounter = cesJobsCounter.get(ceName);
							if (jobsCounter==null) {
								jobsCounter = new int[3];
								if (rsJob.getString(2).equals("DONE")) {
									jobsCounter[0] = 0;
									jobsCounter[1] = 1;
								}
								else {
									jobsCounter[0] = 1;
									jobsCounter[1] = 0;
								}
								
								String adaptor = (rsJob.getString(3)).substring(1,rsJob.getString(3).indexOf(":"));
								if (adaptor.equals("glite"))
									jobsCounter[2] = 0;
								else if (adaptor.equals("unicore"))
									jobsCounter[2] = 1;
								else if (adaptor.equals("wsgram") || adaptor.equals("gatekeper") )
									jobsCounter[2] = 2;
								else if (adaptor.equals("ourgrid"))
									jobsCounter[2] = 3;
								else if (adaptor.equals("bes-genesis2"))
									jobsCounter[2] = 4;
								else if (adaptor.equals("gos"))
									jobsCounter[2] = 5;
								else if (adaptor.equals("ssh"))
									jobsCounter[2] = 6;
								else if (adaptor.equals("rocci"))
									jobsCounter[2] = 7;
								
								cesJobsCounter.put(ceName, jobsCounter);
							}
							else {
								if (rsJob.getString(2).equals("DONE")) {
									jobsCounter[1]++;
								}
								else {
									jobsCounter[0]++;
								}
								cesJobsCounter.put(ceName, jobsCounter);
							}
						}
					}

					rsJob.close();
					pstmtJob.close();
				}
				

				Enumeration<String> ces = cesJobsCounter.keys();
				while (ces.hasMoreElements()) {
					String ce = (String)ces.nextElement();
					logger.info("Querying all_ces for lat and long - ce= " + ce);
//					System.out.println("querying all_ces for lat and long - ce= " + ce);
					query = "SELECT latitude, longitude FROM all_ces WHERE hostname = ?";
					pstmt = (PreparedStatement) conn.prepareStatement(query);
					pstmt.setString(1, ce);

					rs = pstmt.executeQuery();

					if (rs.next()) {
						String ceGeogrhapicalData[] = new String[6];
						ceGeogrhapicalData[0] = ce;
						ceGeogrhapicalData[1] = String.valueOf(cesJobsCounter.get(ce)[0]);
						ceGeogrhapicalData[2] = String.valueOf(cesJobsCounter.get(ce)[1]);
						ceGeogrhapicalData[3] = String.valueOf(cesJobsCounter.get(ce)[2]);
						ceGeogrhapicalData[4] = (new Float(rs.getFloat(1))).toString();
						ceGeogrhapicalData[5] = (new Float(rs.getFloat(2))).toString();
						cesGeographicDistribution.add(ceGeogrhapicalData);
					}

					rs.close();
					pstmt.close();
				}
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				return cesGeographicDistribution;
			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
			}
			if(!updatingJobsStatus)
				CloseDBConnection();
		}

		return null;
	}
	
	public Vector<String[]> getCEsGeographicDistributionForAll (String portal) {
		//middleware
		//0 glite
		//1 unicore
		//2 wsgram
		//3 ourgrid
		//4 genesis II
		//5 gos
		if (CreateDBConnection())
		{
			Vector<String[]> cesGeographicDistribution = new Vector<String[]>();
			Hashtable<String, int[]> cesJobsCounter = new Hashtable<String, int[]>();
			try
			{
				logger.info("Querying GridOperations...");
//				System.out.println("querying GridOperations...");
				String query = "SELECT * FROM GridOperations WHERE portal = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, portal);

				ResultSet rs;
				rs = pstmt.executeQuery();

//				if (rs==null) {
//					System.out.println("rs is null");
//					System.out.println("No applications defined in portal " + portal);
//					CloseDBConnection();
//					return null;
//				}

				Vector<Integer> grid_interactions = new Vector<Integer>();

				while (rs.next()) {
					grid_interactions.add(new Integer(rs.getInt(1)));
				}

				rs.close();
				pstmt.close();
				
				Iterator<Integer> i = grid_interactions.iterator();
				
				while(i.hasNext()) {
					int gridInteractionID = ((Integer)i.next()).intValue();
					logger.info("Querying ActiveGridInteractions...");
//					System.out.println("querying ActiveGridInteractions...");
					query = "SELECT grid_ce, status, grid_id FROM ActiveGridInteractions WHERE grid_interaction = ?";
					PreparedStatement pstmtJob = (PreparedStatement) conn.prepareStatement(query);
					pstmtJob.setInt(1, gridInteractionID);

					ResultSet rsJob;
					rsJob = pstmtJob.executeQuery();

//					if (rsJob==null) {
//						System.out.println("rs is null");
//						System.out.println("jobs list is empty for " + commonName);
//						CloseDBConnection();
//						return null;
//					}

					while (rsJob.next()) {
						String ceName = rsJob.getString(1);
						if (!ceName.equals("")) {
							int[] jobsCounter = cesJobsCounter.get(ceName);
							if (jobsCounter==null) {
								jobsCounter = new int[3];
								if (rsJob.getString(2).equals("DONE")) {
									jobsCounter[0] = 0;
									jobsCounter[1] = 1;
								}
								else {
									jobsCounter[0] = 1;
									jobsCounter[1] = 0;
								}
								
								String adaptor = (rsJob.getString(3)).substring(1,rsJob.getString(3).indexOf(":"));
								if (adaptor.equals("glite"))
									jobsCounter[2] = 0;
								else if (adaptor.equals("unicore"))
									jobsCounter[2] = 1;
								else if (adaptor.equals("wsgram") || adaptor.equals("gatekeeper"))
									jobsCounter[2] = 2;
								else if (adaptor.equals("ourgrid"))
									jobsCounter[2] = 3;
								else if (adaptor.equals("bes-genesis2"))
									jobsCounter[2] = 4;
								else if (adaptor.equals("gos"))
									jobsCounter[2] = 5;
								else if (adaptor.equals("ssh"))
									jobsCounter[2] = 6;
								else if (adaptor.equals("rocci"))
									jobsCounter[2] = 7;
								
								cesJobsCounter.put(ceName, jobsCounter);
							}
							else {
								if (rsJob.getString(2).equals("DONE")) {
									jobsCounter[1]++;
								}
								else {
									jobsCounter[0]++;
								}
								cesJobsCounter.put(ceName, jobsCounter);
							}
						}
					}

					rsJob.close();
					pstmtJob.close();
				}
				

				Enumeration<String> ces = cesJobsCounter.keys();
				while (ces.hasMoreElements()) {
					String ce = (String)ces.nextElement();
					logger.info("Querying all_ces for lat and long - ce= " + ce);
//					System.out.println("querying all_ces for lat and long - ce= " + ce);
					query = "SELECT latitude, longitude FROM all_ces WHERE hostname = ?";
					pstmt = (PreparedStatement) conn.prepareStatement(query);
					pstmt.setString(1, ce);

					rs = pstmt.executeQuery();

					if (rs.next()) {
						String ceGeogrhapicalData[] = new String[6];
						ceGeogrhapicalData[0] = ce;
						ceGeogrhapicalData[1] = String.valueOf(cesJobsCounter.get(ce)[0]);
						ceGeogrhapicalData[2] = String.valueOf(cesJobsCounter.get(ce)[1]);
						ceGeogrhapicalData[3] = String.valueOf(cesJobsCounter.get(ce)[2]);
						ceGeogrhapicalData[4] = (new Float(rs.getFloat(1))).toString();
						ceGeogrhapicalData[5] = (new Float(rs.getFloat(2))).toString();
						cesGeographicDistribution.add(ceGeogrhapicalData);
					}

					rs.close();
					pstmt.close();
				}
				if(!updatingJobsStatus)
					CloseDBConnection();
				
				return cesGeographicDistribution;
			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
			}
			if(!updatingJobsStatus)
				CloseDBConnection();
		}

		return null;
	}
	
	/**
	 * Returns the total number of users with running jobs.
	 * 
	 * @return the total number of users with running jobs.
	 */
	public int getTotalNumberOfUsersWithRunningJobs() {
		if (CreateDBConnection())
        {
			try
            {
				logger.info("Querying ActiveGridInteractions for total users running jobs...");
//				System.out.println("querying ActiveGridInteractions for total users running jobs...");
				String query = "SELECT COUNT(DISTINCT common_name) FROM ActiveGridInteractions";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				
				ResultSet rs;
				rs = pstmt.executeQuery();
				
				rs.next();
				
				int usersNumbers = rs.getInt(1);
				
				rs.close();
				pstmt.close();
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				
				return usersNumbers;
            }
			catch (Exception e) {
				logger.error(e.toString());
//            	System.out.println(e.toString());
            }
			
			if(!updatingJobsStatus)
				CloseDBConnection();
        }
		else{
			logger.error("Cannot get total number of users with running jobs: DB Connection is closed.");
			System.err.println ("Cannot get total number of users with running jobs: DB Connection is closed.");
		}
		return 0;
    }
	
	/**
	 * Returns a {@link Vector} of {@link ActiveInteractions} with all completed interactions 
	 * for the specified user.
	 *
	 * @param commonName a String representing user name
	 * @return a {@link Vector} of {@link ActiveInteractions}
	 */
	public Vector<ActiveInteractions> getDoneInteractionsByName (String commonName) {  
		//l'output è un vettore di stringhe costituito da id(Activegrid..) Portal(GridOperation) descri(GO) userdesr(AcGI) timestamp(AcGI) status(AcGI)
		//In caso di collection prenod i capi d acollection attive
		
		Vector<ActiveInteractions> activeInteractions = new Vector<ActiveInteractions>();
		
		Vector<String[]> doneJobsList = getDoneJobsListByName(commonName);
//		Vector<JobCollection> activeCollections = JobCollection.getActiveJobCollectionsByName(commonName);
		HashMap<Integer, Vector<String[]>> tmp = new HashMap<Integer, Vector<String[]>>();
		
		for(String[] gridInteraction : doneJobsList){
			if(Integer.parseInt(gridInteraction[6])==-1){
				activeInteractions.add(new ActiveInteractions(gridInteraction, null));
			} else {
				Integer key = Integer.parseInt(gridInteraction[6]);
				if(tmp.containsKey(key))
					tmp.get(key).add(gridInteraction);
				else{
					Vector<String[]> firstSubjob = new Vector<String[]>();
					firstSubjob.add(gridInteraction);
					tmp.put(key, firstSubjob);
				}
			}
		}
		
		if(tmp!=null){
			for(Integer key : tmp.keySet()){
				ClosedJobCollection closedCollection = ClosedJobCollection.getClosedCollections(key);
				String[] collectionInfos = closedCollection.getCollectionInfos();
				if(closedCollection.getCollectionType().equals(WorkflowN1.class.getClass().getSimpleName())){
					if(closedCollection.getCollectionStatus().equals("DONE")){
						Vector<String[]> finalSubJob = new Vector<String[]>();
						for(String[] subJobsinfos : tmp.get(key))
							if(subJobsinfos[3].contains("-Final"))
								finalSubJob.add(subJobsinfos);
						tmp.put(key, finalSubJob);
					}
				} 
				collectionInfos[1] = tmp.get(key).elementAt(0)[1];
				collectionInfos[2] = tmp.get(key).elementAt(0)[2];
				activeInteractions.add(new ActiveInteractions(collectionInfos, tmp.get(key)));
			}
		}
		return activeInteractions;
	}
	/**
	 * @deprecated
	 * @param commonName
	 * @return
	 */
	public Vector<String[]> getDoneJobsListByName (String commonName) { //MODIFICARE CON COLLECTION
		
		if (CreateDBConnection())
        {
			Vector<String[]> doneJobsList = new Vector<String[]>();
			try
            {
				logger.info("Querying GridInteractions...");
//				System.out.println("querying GridInteractions...");
				String query = "SELECT * FROM GridInteractions WHERE common_name = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, commonName);
				
				ResultSet rs;
				rs = pstmt.executeQuery();
				
//				if (rs==null) {
//					System.out.println("rs is null");
//					System.out.println("jobs list is empty for " + commonName);
//					CloseDBConnection();
//					return null;
//				}
				
				while (rs.next()) {
					String[] item = new String[7];
					logger.info("Querying GridOperations...");
//					System.out.println("querying GridOperations...");
					query = "SELECT * FROM GridOperations WHERE id = ?";
					PreparedStatement pstmt2 = (PreparedStatement) conn.prepareStatement(query);
					pstmt2.setInt(1, rs.getInt(5));
					
					ResultSet rsGridOp;
					rsGridOp = pstmt2.executeQuery();
					
					//if ( (rsGridOp!=null) && (rsGridOp.next()) ) {
					if (rsGridOp.next()) {
						logger.info("Getting GridOperations data...");
//						System.out.println("getting GridOperations data...");
						item[1] = rsGridOp.getString(2);
						item[2] = rsGridOp.getString(3);
					}
					else {
						logger.warn("Grid Interaction not registered.");
//						System.out.println("Grid Interaction not registered.");
						item[1] = "";
						item[2] = "";
					}
					
					rsGridOp.close();
					pstmt2.close();
					
					item[0] = new String(String.valueOf(rs.getInt(1)));
					item[3] = rs.getString(11).toString();
					item[4] = rs.getTimestamp(4).toString();
					item[5] = rs.getString(12);
					item[6] = new String(String.valueOf(rs.getInt(17)));
					
					doneJobsList.add(item);
					
				}
				rs.close();
				pstmt.close();
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				return doneJobsList;
            }
			catch (Exception e) {
				logger.error(e.toString());
//            	System.out.println(e.toString());
            }
			if(!updatingJobsStatus)
				CloseDBConnection();
        }
		else{
			logger.error("Cannot get active jobs list from DB: DB Connection is closed.");
			System.err.println ("Cannot get active jobs list from DB: DB Connection is closed.");
		}
		return null;
	}

	/**
	 * 
	 * @param commonName
	 * @param jobOutputPath
	 * @param jobsUpdatingInterval
	 */
	public void updateJobsStatusAsync(final String commonName, final String jobOutputPath, final int jobsUpdatingInterval) {
		//		if (isUpdateJobsStatusAsyncRunning(portal,commonName))
		//			return;
		//
		//		final String temp = portal + "-" + commonName;
		//		jobsStatusThread.addElement(temp);
		
		final ScheduledExecutorService executor;
		
		if (threadPool != null)
			executor = Executors.newSingleThreadScheduledExecutor(threadPool.getThreadFactory());
		else
			executor = Executors.newSingleThreadScheduledExecutor();

		final UsersTrackingDBInterface dbInt = this;

		Runnable periodicTask = new Runnable() {
			public void run() {
				updatingJobsStatus = true;
				// Invoke method(s) to do the work
				//String userData = temp;
				logger.info("UpdateJobsStatusAsync running in Thread : " + Thread.currentThread());
//				System.out.println("updateJobsStatusAsync running in Thread : " + Thread.currentThread());
				final Vector<String[]> runningJobs = new Vector<String[]>();
				Vector<JobCollection> runnigJobCollections = new Vector<JobCollection>();
				Semaphore waitForThreads = null;
				
				if (CreateDBConnection())
				{
					try
					{
						logger.info("Querying ActiveGridInteractions for common_name = " + commonName);
//						System.out.println("querying ActiveGridInteractions for common_name = " + commonName);
						String query = "SELECT * FROM ActiveGridInteractions WHERE common_name = ? AND grid_id!=''";
						PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
						pstmt.setString(1, commonName);

						ResultSet rs = pstmt.executeQuery();

//						if (rs==null) {
//							System.out.println("rs is null");
//							System.out.println("jobs list is empty for " + commonName);
//							System.out.println("Closing status update thread...");
//							return;
//						}

						//if (rs!=null) {
							while (rs.next()) {
//								if ( !(rs.getString(12).equals("DONE"))) {
								if ( !(rs.getString(12).equals("DONE")) && !(rs.getString(12).equals("Aborted"))) {

									String jobData[] = new String[12];
									jobData[0] = rs.getString(6);
									jobData[1] = String.valueOf(rs.getInt(1));
									jobData[2] = rs.getString(8);
									jobData[3] = rs.getString(9);
									jobData[4] = rs.getString(10);
									jobData[5] = rs.getString(13);
									jobData[6] = rs.getString(11);
									//******MARIO***************
									jobData[7] = rs.getString(17); //get email field
									jobData[8] = rs.getString(18); //get eTokenServer field
									logger.debug("Getting GridOperations id="+rs.getInt(5)+"...");
									String query1 = "SELECT * FROM GridOperations WHERE id = ?";
									PreparedStatement pstmt1 = (PreparedStatement) conn.prepareStatement(query1);
									pstmt1.setInt(1, rs.getInt(5));

									ResultSet rs1 = pstmt1.executeQuery();
									
									if (rs1.next()) {
										jobData[9] = rs1.getString(2); //get Portal
										jobData[10] = rs1.getString(3); //get Application
										logger.debug("GridOperation: [Portal="+jobData[9]+"; Application="+jobData[10]+"]");
									} else {
										logger.warn("No operation found for id="+rs.getInt(5));
									}
									rs1.close();
									pstmt1.close();
									//**************************
									jobData[11] = rs.getString(19);
									runningJobs.add(jobData);

								}
							}

							rs.close();
						//}
						pstmt.close();
						
					}
					catch (Exception e) {
						logger.error(e.toString());
//						System.out.println(e.toString());
					}

					
					try {
						if (runningJobs.size()==0) {
							logger.info("Jobs list is empty for " + commonName + ". Closing status update thread...");
//							System.out.println("jobs list is empty for " + commonName);
//							System.out.println("Closing status update thread...");
							executor.shutdown();
							//jobsStatusThread.removeElement(userData);

							JobCheckStatusService jobCheckStatusService = null;

							try {
								jobCheckStatusService = InitialContext.<JobCheckStatusService>doLookup("JobCheckStatusService");
								logger.info("Got JobCheckStatusService...");
//								System.out.println("Got JobCheckStatusService...");
							}
							catch ( NamingException ex ) {
								logger.error("Cannot get JobCheckStatusService: " + ex);
//								System.out.println("Cannot get JobCheckStatusService: " + ex);
							}
							catch(Exception ex){
								logger.error("Cannot get JobCheckStatusService: " + ex);
//								System.out.println("Cannot get JobCheckStatusService: " + ex);
							}

							if (jobCheckStatusService==null) {
								logger.info("Get local JobCheckStatusService");
//								System.out.println("get local JobCheckStatusService");
								jobCheckStatusService = JobCheckStatusService.getInstance(URL, userName, password);
							}

							jobCheckStatusService.stopJobCheckStatusThread(commonName);
						}
						else {
							waitForThreads = new Semaphore(0);

							Thread threads[] = new Thread[runningJobs.size()];
							//running jobs check status threads... 
							for(int i=0;i<runningJobs.size();i++) {
								threads[i] = checkJobStatusThread(runningJobs.elementAt(i),jobOutputPath,waitForThreads);
							}
							logger.info("Waiting for thread...");
//							System.out.println("Waiting for thread...");

							try {
								waitForThreads.acquire(runningJobs.size());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								logger.error(e.toString());
								e.printStackTrace();
							}
							logger.info("All job check status threads exited...");
												
//							System.out.println("All job check status threads exited...");

							//						for(int i=0;i<runningJobs.size();i++) {
							//							try {
							//								System.out.println("Waiting for thread " + i + "...");
							//								threads[i].join();
							//							} catch (InterruptedException e) {
							//								System.out.println("Error in join: " + e.toString());	
							//							}
							//						}
							//
							//						System.out.println("After join...");
							
						}
							
						//Controllo delle collezioni se done o aborted (Stato finale) nel caso in cui un job è aborted
						//prendo tutte le collection ciclo per ciascuna e verifico se sono in uno stato finale

						runnigJobCollections = JobCollection.getRunningJobCollections(commonName);
						logger.debug("Running collections: "+runnigJobCollections.size());
						for(JobCollection runningCollection : runnigJobCollections ){
							//TODO In future version we consider a collection aborted if one job is in that state
							logger.debug("Running collection Status: "+runningCollection.getCollectionStatus());
							runningCollection.updateJobCollectionStatus(runningCollection.isInFinalStatus());
							logger.debug("NEW Running collection Status: "+runningCollection.getCollectionStatus());
							
							if(runningCollection.getCollectionStatus().equals("SUBMITTING_FINAL_JOB")){
								((WorkflowN1) runningCollection).submitFinalJob(dbInt);
							} else if(runningCollection.getCollectionStatus().equals("DONE")){
								logger.debug("Closing Done Collection: "+runningCollection.toString());
								runningCollection.completeCollection(runningJobs.get(0));
							} else if(runningCollection.getCollectionStatus().equals("Aborted")){
								logger.info("Closing Aborted collection: " + runningCollection.toString());
								runningCollection.abortCollection(dbInt);
							}

						}

					}
					catch (Exception e) {
						e.printStackTrace();
						logger.error(e.toString());
//						System.out.println(e.toString());
					}
					logger.info("Closing DB Connection...");
//					System.out.println("Closing DB Connection...");
					//if(!updatingJobsStatus)
						CloseDBConnection();
					
					updatingJobsStatus = false;
				}
			}
		};

		executor.scheduleWithFixedDelay(periodicTask, 0, jobsUpdatingInterval, TimeUnit.SECONDS);
	}
	
//	jobData[0] = rs.getString(6);
//	jobData[1] = rs.getString(1);
//	jobData[2] = rs.getString(8);
//	jobData[3] = rs.getString(9);
//	jobData[4] = rs.getString(10);
	private Thread checkJobStatusThread(final String jobData[], final String jobOutputPath, final Semaphore waitForThreads) {
		Thread t;
		final UsersTrackingDBInterface dbInt = this;
		t = new Thread("checkJobStatus") {
			public void run() {
				try {
					logger.info("Check job status in Thread : " + Thread.currentThread());
//					System.out.println("Check job status in Thread : " + Thread.currentThread());
					JobId jobId = new JobId(jobData[0], Integer.valueOf(jobData[1]).intValue());
//					JSagaJobSubmission jobSubmission = new JSagaJobSubmission();
					JSagaJobSubmission jobSubmission = new JSagaJobSubmission(dbInt, jobId.getGridJobId());
					String adaptor = jobData[0].substring(1, jobData[0].indexOf(":"));
					if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) ) {
						//if (jobData[2].matches("\\d+"))
						if (!jobData[2].contains("/"))
							//***********MARIO************
							if(!jobData[8].equals(""))
								jobSubmission.useRobotProxy(jobData[8].substring(0,jobData[8].indexOf(':')),jobData[8].substring(jobData[8].indexOf(':')+1), jobData[2],jobData[3],jobData[4],true, true);
							else
								jobSubmission.useRobotProxy(jobData[2],jobData[3],jobData[4],true, true);
							//****************************
						else
							jobSubmission.setUserProxy(jobData[2]);
					}
					else if (adaptor.equals("rocci")) {
						//if (jobData[2].matches("\\d+"))
						if (!jobData[2].contains("/"))
							//***********MARIO************
							if(!jobData[8].equals(""))
								jobSubmission.useRobotProxy(jobData[8].substring(0,jobData[8].indexOf(':')),jobData[8].substring(jobData[8].indexOf(':')+1), jobData[2],jobData[3],jobData[4],true,true);
							else
								jobSubmission.useRobotProxy(jobData[2],jobData[3],jobData[4],true, true);
							//****************************
						else
							jobSubmission.setUserProxy(jobData[2]);
					}
					else if (adaptor.equals("unicore")){
						String jksPath = jobData[2].substring(0,jobData[2].indexOf(":"));
						String jksPassword = jobData[2].substring(jobData[2].indexOf(":")+1);
						jobSubmission.setJKS(jksPath, jksPassword);
					}
					else if (adaptor.equals("ourgrid")){
						String ourgridUserName = jobData[2].substring(0,jobData[2].indexOf(":"));
						String ourgridPassword = jobData[2].substring(jobData[2].indexOf(":")+1);
						jobSubmission.setOurGridCredential(ourgridUserName, ourgridPassword);
					}
					else if (adaptor.equals("bes-genesis2")){
						String jksPath = jobData[2].substring(0,jobData[2].indexOf(":"));
						String jksPassword = jobData[2].substring(jobData[2].indexOf(":")+1);
						jobSubmission.setGenesisJKS(jksPath, jksPassword);
					}
					else if (adaptor.equals("gos")){
						//nothing to do
					}
					else if (adaptor.equals("ssh")){
						String sshUserName = jobData[2].substring(0,jobData[2].indexOf(":"));
						String sshPassword = jobData[2].substring(jobData[2].indexOf(":")+1);
						jobSubmission.setSSHCredential(sshUserName, sshPassword);
					}
					else
						logger.warn("Error! Adaptor: " + adaptor + " not supported!");
//						System.out.println("Error! Adaptor not supported!");
					
					jobSubmission.setOutputPath(jobOutputPath);
					String output[];
					logger.info("CE name="+jobData[5]);
//					System.out.println("CE name="+jobData[5]);
					if (jobData[5].equals("")) {
						logger.info("Getting job status and CE");
//						System.out.println("Getting job status and CE");
						output = jobSubmission.getJobStatus(jobId,true);
					}
					else {
						logger.info("Getting only job status");
//						System.out.println("Getting only job status");
						output = jobSubmission.getJobStatus(jobId,false);
					}
					
					String status = output[0];
					logger.info("Status for job " + jobId.getGridJobId() + " is " + status);
//					System.out.println("Status for job " + jobId.getGridJobId() + " is " + status);
					
					if (status.equals("DONE")) {
												
						jobSubmission.downloadJobOutput(jobId,jobData[6]);
						Timestamp endJobDateTime = getCurrentUTCTimestamp();
						try {
							PreparedStatement pstmt_status;
							pstmt_status = conn.prepareStatement("UPDATE ActiveGridInteractions SET timestamp_endjob = ? WHERE id = ?");
							pstmt_status.setTimestamp(1, endJobDateTime);	
							pstmt_status.setInt(2, Integer.valueOf(jobData[1]).intValue());
							pstmt_status.executeUpdate();
							pstmt_status.close();
						} catch (Exception e) {
							logger.error("Error in writing end job datetime: " + e.toString());
//							System.out.println("Error in writing end job datetime: " + e.toString());	
						}
						
						//********MARIO***********
						//Invio mail
//						try{
							if (!jobData[7].equals("")){
								MailUtility m = new MailUtility(jobData[7], jobData[9] ,jobData[10], jobData[6], ContentMessage.SIMPLE_JOB);
								m.sendMail();
							}
							else
								logger.info("No 'TO' mail address is set");
								
							
//						}catch(Exception ex){
//							ex.printStackTrace();
//						}
						//************************
							
						//Delete job description for completed job
						GEJobDescription jobDescription = GEJobDescription.findJobDescriptionByJobId(jobId.getGridJobId());
						jobDescription.delete();
					}
					
					if (!(status.equals(""))) {
						try {
							PreparedStatement pstmt_status;
							if (output[1].equals("")) {
								pstmt_status = conn.prepareStatement("UPDATE ActiveGridInteractions SET status = ? WHERE id = ?");
								pstmt_status.setString(1, status);	
								pstmt_status.setInt(2, Integer.valueOf(jobData[1]).intValue());
							}
							else {
								float latitude = 0;
								float longitude = 0;
								logger.info("querying all_ces for lat and long - ce= " + output[1]);
//								System.out.println("querying all_ces for lat and long - ce= " + output[1]);
								String query = "SELECT latitude, longitude FROM all_ces WHERE hostname = ?";
								PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
								pstmt.setString(1, output[1]);

								ResultSet rs = pstmt.executeQuery();
								
								//if (rs!=null) {
								if (rs.next()) {
									//rs.next();
//									System.out.println("lat col="+rs.findColumn("latitude"));
//									System.out.println("long col="+rs.findColumn("longitude"));
									latitude = rs.getFloat(1);
									longitude = rs.getFloat(2);
								}
								
								rs.close();
								pstmt.close();
								
								pstmt_status = conn.prepareStatement("UPDATE ActiveGridInteractions SET status = ?, grid_ce =?, latitude=?, longitude=? WHERE id = ?");
								pstmt_status.setString(1, status);	
								pstmt_status.setString(2, output[1]);	
								pstmt_status.setFloat(3, latitude); 
								pstmt_status.setFloat(4, longitude); 
								pstmt_status.setInt(5, Integer.valueOf(jobData[1]).intValue());
							}
							pstmt_status.executeUpdate();
							pstmt_status.close();
						} catch (Exception e) {
							logger.error("Error in updating job state: " + e.toString());
//							System.out.println("Error in updating job state: " + e.toString());	
						}
					}
					waitForThreads.release(1);
				}
				catch (Exception e) {
					logger.error("Exception in check status thread. Releasing semaphore resource...");
//					System.out.println("Exception in check status thread. Releasing semaphore resource...");
					waitForThreads.release(1);
					e.printStackTrace();

				}
			}
		};
		if (threadPool != null) threadPool.execute(t);
		else t.start();

		return t;
	}
	
	/**
	 * Returns the grid job idenfier for an active grid interaction secified by the database id.
	 * 
	 * @param DbId database id for this active grid interaction.
	 * @return the grid job idenfier.
	 */
	public String getGridJobId(int DbId) {
		String gridJobId = null;
		if (CreateDBConnection())
		{
			try
			{
				logger.info("Querying GridOperations...");
//				System.out.println("querying GridOperations...");
				String query = "SELECT * FROM ActiveGridInteractions WHERE id = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setInt(1, DbId);

				ResultSet rs;
				rs = pstmt.executeQuery();

//				if (rs==null) {
//					System.out.println("rs is null");
//					System.out.println("No job found for id  " + DbId);
//					CloseDBConnection();
//					return gridJobId;
//				}

				if (rs.next()) {
					gridJobId = rs.getString(6);

				}
//				else
//					gridJobId = rs.getString(6);
				logger.info("gridJobId = " + gridJobId);
//				System.out.println("gridJobId = " + gridJobId);

				rs.close();
				pstmt.close();

			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
			}
		}
		if(!updatingJobsStatus)
			CloseDBConnection();
		
		return gridJobId;
	}
	
	/**
	 * Returns an array of two float values representing respectively 
	 * latitude and longitude of the specified CE.
	 * 
	 * @param CE for which you want retrive coordinates.
	 * @return CE's coordinates.
	 */
	public float[] getCECoordinate(String CE) {
		float[] ceCoordinate = new float[2];
		ceCoordinate[0] = 0;
		ceCoordinate[1] = 0;

		if (CreateDBConnection())
		{
			try
			{
				logger.info("querying all_ces for lat and long - ce= " + CE);
//				System.out.println("querying all_ces for lat and long - ce= " + CE);
				String query = "SELECT latitude, longitude FROM all_ces WHERE hostname = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, CE);

				ResultSet rs = pstmt.executeQuery();
				
				//if (rs!=null) {
					rs.next();
					logger.info("lat col="+rs.findColumn("latitude"));
//					System.out.println("lat col="+rs.findColumn("latitude"));
					logger.info("long col="+rs.findColumn("longitude"));
//					System.out.println("long col="+rs.findColumn("longitude"));
					ceCoordinate[0] = rs.getFloat(1);
					ceCoordinate[1] = rs.getFloat(2);
				//}
				
				rs.close();
				pstmt.close();
			}
			catch (Exception e) {
				logger.info(e.toString());
//				System.out.println(e.toString());
			}
		}
		if(!updatingJobsStatus)
			CloseDBConnection();
		
		return ceCoordinate;
	}
	
	/**
	 * Creates an archive that contains all job outputs for a specified user. 
	 * 
	 * @param commonName a String representing user name
	 * @param jobOutputPath path where this archive is stored.
	 * @return a string that represent the complete path where the archive is located.
	 */
	public String createAllJobsArchive(String commonName, String jobOutputPath) {
		String command = "";
		Vector<Integer> jobsId = new Vector<Integer>();
		
		Vector<JobCollection> doneCollections = JobCollection.getDoneJobCollections(commonName);
		HashMap<Integer, Vector<Integer>> a = new HashMap<Integer, Vector<Integer>>();

		if (CreateDBConnection())
		{
			try
			{
				logger.info("Get all jobs id for user = " + commonName);
//				System.out.println("get all jobs id for user = " + commonName);
				String query = "SELECT id, user_description, id_job_collection FROM ActiveGridInteractions WHERE common_name = ? AND status = 'DONE' order by id";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, commonName);

				ResultSet rs = pstmt.executeQuery();
				
				while (rs.next()) {
					Integer key = rs.getInt(3);
					if(key==0){
						int dbId = rs.getInt(1);
						jobsId.add(dbId);
						command = command + JSagaJobSubmission.removeNotAllowedCharacter(rs.getString(2)) + "_" + dbId + ".tgz ";
						logger.info("Command = "+command);
					} else {
						if(a.containsKey(key))
							a.get(key).add(rs.getInt(1));
						else{
							Vector<Integer> tmp = new Vector<Integer>();
							tmp.add(rs.getInt(1));
							a.put(key, tmp);
						}
						
					}
//					System.out.println("command="+command);
				}
				
				rs.close();
				pstmt.close();
				
				Iterator<Integer> i = jobsId.iterator();
				
				while(i.hasNext()) { //Chiudo i Job semplici
					CloseGridInteraction(i.next().intValue());
				}
				
				//Chiudo collection e subjobs
				for(JobCollection doneCollection : doneCollections){
					command = command + JSagaJobSubmission.removeNotAllowedCharacter(doneCollection.getDescription()) + "_" + doneCollection.getId() + ".tgz ";
					int idClosedCollection = doneCollection.close();
					for(int idSubJob : a.get(doneCollection.getId()))
						CloseGridInteraction(idSubJob, idClosedCollection);
				
				}
			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				
				return "";
			}
		}
		
		if(!updatingJobsStatus)
			CloseDBConnection();
		String outputFileName="";
		if (jobsId.size()==0)
			if(doneCollections.size()==0)
				return "";
			else
				outputFileName = jobOutputPath + "/jobOutput/AllJobs_" + commonName + "_" + doneCollections.lastElement().getId() + ".tgz";
		else
			outputFileName = jobOutputPath + "/jobOutput/AllJobs_" + commonName + "_" + jobsId.lastElement() + ".tgz";
		
		try {
			//creating a tgz archive containing output files
			command = "tar czvf " + outputFileName + " --directory="+jobOutputPath+"/jobOutput/ " + command;
			logger.info("Creating a tgz archive containing output files...");
//			System.out.println("creating a tgz archive containing output files...");
			logger.info(command);
//			System.out.println(command);
			Process creatingTar = Runtime.getRuntime().exec(command);
			creatingTar.waitFor();
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
		}
		logger.info("OutputFileName="+outputFileName);
//		System.out.println("outputFileName="+outputFileName);
		return outputFileName;
		
	}
	
	/**
	 * @deprecated
	 * @param commonName
	 * @param description
	 * @param jobOutputPath
	 * @return
	 */
	public String createAllJobsFromDescriptionArchive(String commonName, String description, String jobOutputPath) { //MODIFICATA QUERY, NON CONSIDERA I JOB CHE APPARTENGONO A COLLEZIONI.
        String command = "";
        Vector<Integer> jobsId = new Vector<Integer>();
		
		if (CreateDBConnection())
		{
			try
			{
				logger.info("Get all jobs id for user = " + commonName);
//				System.out.println("get all jobs id for user = " + commonName);
//				String query = "SELECT id, user_description FROM ActiveGridInteractions WHERE common_name = ? AND user_description = ? AND status = 'DONE' order by id";
				String query = "SELECT id, user_description FROM ActiveGridInteractions WHERE common_name = ? AND user_description = ? AND status = 'DONE' AND id_job_collection IS NULL order by id";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setString(1, commonName);
				pstmt.setString(2, description);

				ResultSet rs = pstmt.executeQuery();
				
				while (rs.next()) {
					int dbId = rs.getInt(1);
					jobsId.add(dbId);
					command = command + jobOutputPath + "/jobOutput/" + JSagaJobSubmission.removeNotAllowedCharacter(rs.getString(2)) + "_" + dbId + ".tgz ";
					logger.info("Command = "+command);
//					System.out.println("command="+command);
				}
				
				rs.close();
				pstmt.close();
				
				Iterator<Integer> i = jobsId.iterator();
				
				while(i.hasNext()) {
					CloseGridInteraction(i.next().intValue());
				}
				
			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
				
				if(!updatingJobsStatus)
					CloseDBConnection();
				
				return "";
			}
		}
		if(!updatingJobsStatus)
			CloseDBConnection();
		
		if (jobsId.size()==0)
			return "";
		
		
		String outputFileName = jobOutputPath + "/jobOutput/AllJobsForDesc_" + commonName + "_" + jobsId.lastElement() + ".tgz";
		try {
			//creating a tgz archive containing output files
			command = "tar czvf " + outputFileName + " " + command;
			logger.info("Creating a tgz archive containing output files...");
//			System.out.println("creating a tgz archive containing output files...");
			logger.info(command);
			System.out.println(command);
			Process creatingTar = Runtime.getRuntime().exec(command);
			creatingTar.waitFor();
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
		}
		logger.info("outputFileName="+outputFileName);
//		System.out.println("outputFileName="+outputFileName);
		return outputFileName;	
	}
	
	/**
	 * Returns the user description for a specified active grid interacion.
	 *  
	 * @param DbId database id for active grid interaction you want retrive user description.
	 * @return user description for this inteeraction. 
	 */
	public String getJobUserDescription(int DbId) {
		String userDescription = "";
		if (CreateDBConnection())
		{
			try
			{
				logger.info("Querying GridOperations...");
//				System.out.println("querying GridOperations...");
				String query = "SELECT * FROM ActiveGridInteractions WHERE id = ?";
				PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
				pstmt.setInt(1, DbId);

				ResultSet rs;
				rs = pstmt.executeQuery();


				if (rs.next())
					userDescription = rs.getString(11);
				logger.info("UserDescription = " + userDescription);
//				System.out.println("userDescription = " + userDescription);

				rs.close();
				pstmt.close();

			}
			catch (Exception e) {
				logger.error(e.toString());
//				System.out.println(e.toString());
			}
		}
		if(!updatingJobsStatus)
			CloseDBConnection();
		
		return userDescription;
	}
	
	private Timestamp getCurrentUTCTimestamp() {
		Calendar calendar = Calendar.getInstance();
		java.util.Date date = calendar.getTime();
		TimeZone tz = calendar.getTimeZone();
		
		long msFromEpochGMT = date.getTime();
		
		int offsetFromUTC = tz.getOffset(msFromEpochGMT);
		
		Calendar UTCCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		UTCCalendar.setTime(date);
		UTCCalendar.add(Calendar.MILLISECOND, -offsetFromUTC);
		
		Timestamp currentTimestamp = new Timestamp(UTCCalendar.getTimeInMillis());
		return currentTimestamp;
	}
	
	/**
	 * @param args
	 */
	///*
	public static void main(String[] args) {

		UsersTrackingDBInterface DBInterface = new UsersTrackingDBInterface("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
//		for(int i=0; i<3; i++){
//			int x = DBInterface.insertMailAddress(i, "marius83@alice.it");
//			System.out.println("Error code: " +x);
//		}
		String CE = "ce-02.roma3.infn.it";
		//String CE = "grid012.ct.infn.it";
		float coord[] = DBInterface.getCECoordinate(CE);
		
		System.out.println("Lat="+coord[0]+"  Long="+coord[1]);
		
		
		
		Vector<String[]> jobsRunning = DBInterface.getCEsGeographicDistribution ("ViralGrid", "scardaci");
		
		if (jobsRunning!=null) {
			for (int i=0;i<jobsRunning.size();i++) {
				System.out.println(" Scardaci CE=" + jobsRunning.elementAt(i)[0] + " Jobs Running=" + jobsRunning.elementAt(i)[1] + " Jobs Done=" + jobsRunning.elementAt(i)[2] + " Middleware=" + jobsRunning.elementAt(i)[3] + " lat=" + jobsRunning.elementAt(i)[4] + " long=" + jobsRunning.elementAt(i)[5]);
			}
		}
		
		jobsRunning = DBInterface.getCEsGeographicDistribution ("ViralGrid", "test2");
		
		if (jobsRunning!=null) {
			for (int i=0;i<jobsRunning.size();i++) {
				System.out.println("Test2 CE=" + jobsRunning.elementAt(i)[0] + " Jobs Running=" + jobsRunning.elementAt(i)[1] + " Jobs Done=" + jobsRunning.elementAt(i)[2] + " Middleware=" + jobsRunning.elementAt(i)[3] + " lat=" + jobsRunning.elementAt(i)[4] + " long=" + jobsRunning.elementAt(i)[5]);
			}
		}
		
		jobsRunning = DBInterface.getCEsGeographicDistribution ("ViralGrid", "test1");
		
		if (jobsRunning!=null) {
			for (int i=0;i<jobsRunning.size();i++) {
				System.out.println("Test1 CE=" + jobsRunning.elementAt(i)[0] + " Jobs Running=" + jobsRunning.elementAt(i)[1] + " Jobs Done=" + jobsRunning.elementAt(i)[2] + " Middleware=" + jobsRunning.elementAt(i)[3] + " lat=" + jobsRunning.elementAt(i)[4] + " long=" + jobsRunning.elementAt(i)[5]);
			}
		}
		
		Vector<String[]> jobsRunningForAll = DBInterface.getCEsGeographicDistributionForAll("ViralGrid");
		
		if (jobsRunningForAll!=null) {
			for (int i=0;i<jobsRunningForAll.size();i++) {
				System.out.println("ALL CE=" + jobsRunningForAll.elementAt(i)[0] + " Jobs Running=" + jobsRunningForAll.elementAt(i)[1] + " Jobs Done=" + jobsRunningForAll.elementAt(i)[2] + " Middleware=" + jobsRunningForAll.elementAt(i)[3] + " lat=" + jobsRunningForAll.elementAt(i)[4] + " long=" + jobsRunningForAll.elementAt(i)[5]);
			}
		}
//		for (int i=0;i<10;i++) {
//			int id = DBInterface.InsertActiveGridInteraction("scardaci", "192.168.1.4:8080", 1, "[wms://gilda-wms-02.ct.infn.it:7443/glite_wms_wmproxy_server]-[https://gilda-lb-01.ct.infn.it:9000/HTkbs4F6HBEZQGyxnTR3kg]", "/C=IT/O=INFN/OU=Personal Certificate/L=Catania/CN=Diego Scardaci", "100", "gilda", "gilda", "job1");
//			id = DBInterface.InsertActiveGridInteraction("scardaci", "192.168.1.4:8080", 2, "[wms://gilda-wms-02.ct.infn.it:7443/glite_wms_wmproxy_server]-[https://gilda-lb-01.ct.infn.it:9000/HTkbs4F6HBEZQGyxnTR3kg]", "/C=IT/O=INFN/OU=Personal Certificate/L=Catania/CN=Diego Scardaci", "100", "gilda", "gilda", "job2");
//			id = DBInterface.InsertActiveGridInteraction("pippo", "192.168.1.4:8080", 2, "[wms://gilda-wms-02.ct.infn.it:7443/glite_wms_wmproxy_server]-[https://gilda-lb-01.ct.infn.it:9000/HTkbs4F6HBEZQGyxnTR3kg]", "/C=IT/O=INFN/OU=Personal Certificate/L=Catania/CN=Diego Scardaci", "100", "gilda", "gilda", "job3");
//			System.out.println("Id="+id);
//		}
//
//		Vector<String[]> jobList = DBInterface.getActiveJobsListByName("scardaci");
//
//		if (jobList!=null) {
//			for (int i=0;i<jobList.size();i++) {
//				System.out.println("Portal = " + jobList.elementAt(i)[0] + " - Application = " + jobList.elementAt(i)[1] + " - Timestamp = " + jobList.elementAt(i)[2] + " - Status = " + jobList.elementAt(i)[3] );
//			}
//		}

	}
}
