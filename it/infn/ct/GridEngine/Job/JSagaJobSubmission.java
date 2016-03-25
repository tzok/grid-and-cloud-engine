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


package it.infn.ct.GridEngine.Job;

import fr.in2p3.jsaga.impl.job.instance.JobImpl;
import it.infn.ct.GridEngine.InformationSystem.BDII;
import it.infn.ct.GridEngine.JobCollection.JobCollection;
import it.infn.ct.GridEngine.JobResubmission.GEActiveGridInteraction;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.JobService.JobCheckStatusService;
import it.infn.ct.GridEngine.SendMail.MailUtility;
import it.infn.ct.GridEngine.UsersTracking.ActiveInteractions;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
//to be deleted only 4 genesis
//import org.ogf.saga.session.SessionFactory;
//import fr.in2p3.jsaga.impl.context.ContextImpl;

//import org.apache.commons.logging.Log; 
//import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible for submission of a job to an e-Infrastructure
 * @author mario
 *
 */
public class JSagaJobSubmission {
	/**
	 * List of possibles Adaptor
	 */
	public enum Adaptor {
	    /**
	     * WMS adaptor
	     */
		wms,
		/**
		 * WSGRAM adaptor
		 */
	    wsgram, 
	    /**
		 * GATEKEEPER adaptor
		 */
	    gatekeeper,
	    /**
	     * SSH adaptor
	     */
	    ssh,
	    /**
	     * OURGRID adaptor
	     */
	    ourgrid, 
	    /**
	     * UNICORE adaptor
	     */
	    unicore, 
	    /**
	     * GOS adaptor
	     */
	    gos, 
	    /**
	     * GENESIS Adaptor
	     */
	    besGenesis2,
	    /**
	     * ROCCI Adaptor
	     */
	    rocci
	}


	private JobServiceManager jobServiceManager = null;
	private BDII bdii = null;
	private String WMSList[] = null;
	private String resourceManagerlist[] = null;
	
	//*********MARIO************
	private String CEList[] = null;
	private String userEmail = "";
	private static final Logger logger = Logger.getLogger(JSagaJobSubmission.class);
//	private static final Logger logger = Logger.getRootLogger();
	private GEJobDescription description;
	//**************************
	
//	private String executable = "";
//	private String arguments = "";
//	private String jobQueue = "";
//	private String outputPath = "";
	private String outputPathPrefix = "";
//	private String jobOutput = "";
//	private String jobError = "";
	private String jobSandbox = "";
//	private String inputFiles[] = null;
	private String outputFiles[] = null;
	private String jobPortletId = "";

//	private String SPMDVariation = "";
//	private String NumberOfProcesses = "";
//	private String totalCPUCount = "";
//	private String JDLRequirements[] = null;
	
	private UsersTrackingDBInterface DBInterface;
	
	private ThreadPoolExecutor threadPool;
	
	private String URL		= "";
	private String userName = "";
	private String password = "";
	
	private int shallowRetry = 5;
	private int allowResub = -1;
	
	private boolean checkJobsStatus = true;
	//private static Log _log = LogFactory.getLog(JSagaJobSubmission.class);
	//private static Logger _log = Logger.getLogger(JSagaJobSubmission.class);
	private boolean randomCE = false;
	
	private String jksPath = "";
	private String jksPassword = "";
	
	private String ourgridUserName = "";
	private String ourgridPassword = "";

	private String genesisJksPath = "";
	private String genesisJksPassword = "";

	private String SSHUserName = "";
	private String SSHPassword = "";
	
	private boolean resubmitting = false;
	private Semaphore waitForResubmission  = new Semaphore(0);

	/**
	 * Constructs a {@link JSagaJobSubmission} object specifying 
	 * connection parameters to users tracking database.
	 *  
	 * @param db database name
	 * @param dbUser database username
	 * @param dbUserPwd database password.
	 */
	public JSagaJobSubmission(String db, String dbUser, String dbUserPwd) {
		//System.setProperty("JSAGA_HOME", "/opt/jsaga-0.9.15-SNAPSHOT/");
//		System.setProperty("JSAGA_HOME", "/home/mario/JSAGA/");
		System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");		
//		PropertyConfigurator.configure("log4j.properties");
//		DOMConfigurator.configure("GridEngineLogConfig.xml");
		DOMConfigurator.configure((System.getProperty("GridEngineLogConfig.path")!=null ? System.getProperty("GridEngineLogConfig.path") : "GridEngineLogConfig.xml"));
		
		URL = db;
		userName = dbUser;
		password = dbUserPwd;
				
		DBInterface = new UsersTrackingDBInterface(db,dbUser,dbUserPwd);
		//DBInterface = new UsersTrackingDBInterface("jdbc:mysql://lrt01.ct.infn.it/userstracking","tracking_user","usertracking");
		
		jobServiceManager = new JobServiceManager();
		
		try {
			threadPool = InitialContext.<ThreadPoolExecutor>doLookup("GridEngine-Pool");
		}
		catch ( NamingException ex ) {
			//System.out.println("Cannot get thread-pool: " + ex);
			logger.error("Cannot get thread-pool: " + ex);
		}
		catch(Exception ex){
			//System.out.println("Cannot get thread-pool: " + ex);
			logger.error("Cannot get thread-pool: " + ex);
		}

		//DELME
		String test1 = System.getProperty("JSAGA_HOME");
		String test3 = System.getProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
		if(logger.isDebugEnabled()){
			logger.debug("JSAGA_HOME:"+test1);
			logger.debug("saga.factory:"+test3);
//			System.out.println("JSAGA_HOME:"+test1);
//			System.out.println("saga.factory:"+test3);
		}
		description = new GEJobDescription();
	}
	
	/**
	 * Constructs a {@link JSagaJobSubmission} object without specify 
	 * connection parameters to users tracking database.
	 * 
	 */
	public JSagaJobSubmission() {
		//System.setProperty("JSAGA_HOME", "/opt/jsaga-0.9.15-SNAPSHOT/");
//		System.setProperty("JSAGA_HOME", "/home/mario/JSAGA/");
		System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
		//PropertyConfigurator.configure("log4j.properties");
//		DOMConfigurator.configure("GridEngineLogConfig.xml");
		DOMConfigurator.configure((System.getProperty("GridEngineLogConfig.path")!=null ? System.getProperty("GridEngineLogConfig.path") : "GridEngineLogConfig.xml"));
		DBInterface = new UsersTrackingDBInterface();
		
		jobServiceManager = new JobServiceManager();
		
		try {
			threadPool = InitialContext.<ThreadPoolExecutor>doLookup("GridEngine-Pool");
		}
		catch ( NamingException ex ) {
			logger.error("Cannot get thread-pool: " + ex);
//			System.out.println("Cannot get thread-pool: " + ex);
		}
		catch(Exception ex){
			logger.error("Cannot get thread-pool: " + ex);
//			System.out.println("Cannot get thread-pool: " + ex);
		}
		
		//DELME
		String test1 = System.getProperty("JSAGA_HOME");
		String test3 = System.getProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
		if(logger.isDebugEnabled()){
			logger.debug("JSAGA_HOME:"+test1);
			logger.debug("saga.factory:"+test3);
//			System.out.println("JSAGA_HOME:"+test1);
//			System.out.println("saga.factory:"+test3);
		}
		description = new GEJobDescription();
	}
	
	/**
	 * Constructs a {@link JSagaJobSubmission} specifying a connection
	 * interface to users tracking database.
	 *  
	 * @param dbInt users tracking database connection interface.
	 */
	public JSagaJobSubmission(UsersTrackingDBInterface dbInt) {

		logger.info("NEW CONSTRUCTOR");
		DBInterface = dbInt;
		
		jobServiceManager = new JobServiceManager();
		
		try {
				threadPool = InitialContext.<ThreadPoolExecutor>doLookup("GridEngine-Pool");
		}
		catch ( NamingException ex ) {
			logger.error("Cannot get thread-pool: " + ex);
		}
		catch(Exception ex){
			logger.error("Cannot get thread-pool: " + ex);
		}
		
		//DELME
		String test1 = System.getProperty("JSAGA_HOME");
		String test3 = System.getProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
		if(logger.isDebugEnabled()){
			logger.debug("JSAGA_HOME:"+test1);
			logger.debug("saga.factory:"+test3);

		}
	}
	
	/**
	 * Constructs a {@link JSagaJobSubmission} object specifying 
	 * connection parameters to users tracking database and and the description 
	 * of submitting job.
	 *  
	 * @param db database name
	 * @param dbUser database username
	 * @param dbUserPwd database password
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public JSagaJobSubmission(String db, String dbUser, String dbUserPwd, GEJobDescription description) {
		
		this(db, dbUser, dbUserPwd);
		this.description = description;
		this.description.saveJobDescription();

	}
	
	/**
	 * Constructs a {@link JSagaJobSubmission} object specifying the description
	 * for the submitting job.
	 * 
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public JSagaJobSubmission(GEJobDescription description) {
		
		this();
		this.description = description;
		this.description.saveJobDescription();

	}
	
	/**
	 * Constructs a {@link JSagaJobSubmission} specifying a connection
	 * interface to users tracking database and the grid job identifier to retrieve 
	 * its description.
	 *  
	 * @param dbInt users tracking database connection interface
	 * @param jobId grid job identifier.
	 */	
	public JSagaJobSubmission(UsersTrackingDBInterface dbInt, String jobId) {
		
		DBInterface = dbInt;
		description = new GEJobDescription();
		description = GEJobDescription.findJobDescriptionByJobId(jobId);
		jobServiceManager = new JobServiceManager();
		
		try {
				threadPool = InitialContext.<ThreadPoolExecutor>doLookup("GridEngine-Pool");
		}
		catch ( NamingException ex ) {
			logger.error("Cannot get thread-pool: " + ex);
			
		}
		catch(Exception ex){
			logger.error("Cannot get thread-pool: " + ex);

		}
		
		//DELME
		String test1 = System.getProperty("JSAGA_HOME");
		String test3 = System.getProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
		if(logger.isDebugEnabled()){
			logger.debug("JSAGA_HOME:"+test1);
			logger.debug("saga.factory:"+test3);

		}
	}

	/**
	 * Sets java keystore file path and password. 
	 * 
	 * @param jkspath keystore file path
	 * @param jkspassword keystore password.
	 */
	public void setJKS(String jkspath, String jkspassword) {
		jksPath = jkspath;
		jksPassword = jkspassword;
	}

	public void setGenesisJKS(String jkspath, String jkspassword) {
		genesisJksPath = jkspath;
		genesisJksPassword = jkspassword;
	}
	
	/**
	 * Sets OurGrid user credentials.
	 * 
	 * @param username OurGrid username
	 * @param password OurGrid password.
	 */
	public void setOurGridCredential(String username, String password) {
		ourgridUserName = username;
		ourgridPassword = password;
	}
	
	/**
	 * Sets SSH credentials.
	 * 
	 * @param username SSH username
	 * @param password SSH password.
	 */
	public void setSSHCredential(String username, String password) {
		SSHUserName = username;
		SSHPassword = password;
	}
	
	/**
	 * Sets the maximum attempts submission numbers.
	 * 
	 * @param value the maximum attempts submission numbers.
	 */
	public void setShallowRetry(int value) {
		shallowRetry = value;
	}
	
	/**
	 * Returns the maximum attempts submission numbers.
	 * 
	 * @return the maximum attempts submission numbers.
	 */
	public int getShallowRetry() {
		return shallowRetry;
	}
	
	/**
	 * If true starts after job submission starts a thread that checks 
	 * job status. By default it is true.
	 * 
	 * @param value if true starts a check job status thread, else not 
	 * 				starts thread.
	 */
	public void setCheckJobsStatus(boolean value) {
		checkJobsStatus = value;
	}
	
	/**
	 * Returns true if a check job status thread was started
	 * after job submission, false otherwise.
	 * 
	 * @return true if a check job status thread was started after job submission, false otherwise.
	 */
	public boolean getCheckJobsStatus() {
		return checkJobsStatus;
	}
	
	/**
	 * Sets if a random CE will be chosen from given CEs list or from a 
	 * bdii service.	 
	 * 
	 * @param value true if a random CE will be chosen, false otherwise. 
	 * By default it is false.
	 */
	public void setRandomCE(boolean value) {
		randomCE = value;
	}
	
	/**
	 * Returns true if a random CE will be chosen from given CEs list or from a 
	 * bdii service, false otherwise.
	 * 
	 * @return true if a random CE will be chosen from given CEs list or from a 
	 * bdii service, false otherwise
	 */
	public boolean getRandomCE() {
		return randomCE;
	}
	
	/**
	 * Sets the BDII URI service.
	 * 
	 * @param value URI service.
	 */
	public void setBDII(String value) {
		try {
			bdii = new BDII(new URI(value));
		}
		catch (Exception exc) {
			logger.error(exc.toString());
//			System.out.println(exc.toString());
		}
	}

	/**
	 * Returns a string representing BDII URI.
	 * 
	 * @return a string representing BDII URI.
	 */
	public String getBDII() {
		return bdii.getLocation().toString();
	}
	
	/**
	 * Sets WMS addresses list.
	 * 
	 * @param list a String[] consisting of WMS address.
	 */
	public void setWMSList(String list[]) {
		WMSList = list;
		if(logger.isDebugEnabled())
			for (int i=0;i<WMSList.length;i++)
				logger.debug("WMS["+i+"]=" + WMSList[i]);
	}
	
	/**
	 * Returns WMS addresses list.
	 * 
	 * @return WMS addresses list.
	 */
	public String[] getWMSList() {
		return WMSList;
	}


	public String[] getResourceManagerlist() {
		return resourceManagerlist;
	}

	public void setResourceManagerlist(String resourceManagerlist[]) {
		this.resourceManagerlist = resourceManagerlist;
		if(logger.isDebugEnabled())
			for (int i=0;i<this.resourceManagerlist.length;i++)
				logger.debug("resourceManagerlist["+i+"]=" + this.resourceManagerlist[i]);
	}
	
	/**
	 * Sets CEs list.
	 * 
	 * @param list a String[] of CEs. 
	 */
	public void setCEList(String list[]) {
		if(list!=null){
			CEList = list;
			if (logger.isDebugEnabled())
				for (int i = 0; i < CEList.length; i++)
					logger.debug("CE[" + i + "]=" + CEList[i]);
		}
	}
	
	/**
	 * Returns CEs list.
	 * 
	 * @return CEs list.
	 */
	public String[] getCEList(){
		return CEList;
	}
	
	/**
	 * Returns a random CE from the specified CEs list.
	 * 
	 * @return a random CE from the specified CEs list, an empty 
	 * string if CE list wasn't specified.
	 */
	public String getRandomCEFromCElist() {
		if (!randomCE && CEList == null)
			return "";

		int index = (int) ((Math.random()) * (new Integer(CEList.length).doubleValue()));
		if (index == CEList.length)
			index--;
		if (logger.isDebugEnabled())
			logger.debug("CE selected = " + CEList[index]);

		return CEList[index];
	}
	
	/**
	 * Returns the specified user email.
	 * 
	 * @return the specified user email.
	 */
	public String getUserEmail() {
		return (this.userEmail.contains("|")) ? this.userEmail.substring(this.userEmail.indexOf('|')+1) : this.userEmail;
	}

	/**
	 * Sets user email to notify that the job has been successfully completed.
	 * 
	 * @param userEmail user email to notify that the job has been 
	 * successfully completed.
	 */
	public void setUserEmail(String userEmail) {
		if(!this.userEmail.equals("")){
			this.userEmail.concat("|"+userEmail);
		} else {
			this.userEmail = userEmail;
		}
	}
	
	/**
	 * Sets sender address for the notification email.
	 * 
	 * @param senderEmail sender email address.
	 */
	public void setSenderEmail(String senderEmail) {
		if(!this.userEmail.equals("")){
			String tmp = this.userEmail;
			this.userEmail = senderEmail+"|"+tmp;
		} else {
			this.userEmail = senderEmail;
		}
	}
	
	/**
	 * Returns the specified sender address.
	 * 
	 * @return the specified sender address.
	 */
	public String getSenderEmail() {
		return (this.userEmail.contains("|")) ? this.userEmail.substring(0, this.userEmail.indexOf('|')) : "";
	}

	/**
	 * Returns a WMS randomly chosen from the specified WMS list.
	 * 
	 * @return WMS randomly chosen from the specified WMS list, 
	 * an empty string if WMS list wasn't specified.
	 */
	public String getRandomWMS() {
		if (WMSList==null) return "";
		
		int index = (int)((Math.random())*(new Integer(WMSList.length).doubleValue()));
		if (index==WMSList.length) index--;
		if(logger.isDebugEnabled())
			logger.debug("WMS selected = " + WMSList[index]);
		
		return WMSList[index];
	}
	
	/**
	 * 
	 * @param etokenserver
	 * @param etokenserverport
	 * @param proxyId
	 * @param vo
	 * @param fqan
	 * @param proxyrenewal
	 */
	public void useRobotProxy(String etokenserver, String etokenserverport, String proxyId, String vo, String fqan, boolean proxyrenewal) {
		jobServiceManager.useRobotProxy(etokenserver, etokenserverport, proxyId, vo, fqan, proxyrenewal);
	}
	
	/**
	 * 
	 * @param etokenserver
	 * @param etokenserverport
	 * @param proxyId
	 * @param vo
	 * @param fqan
	 * @param proxyrenewal
	 */
	public void useRobotProxy(String etokenserver, String etokenserverport, String proxyId, String vo, String fqan, boolean proxyrenewal, boolean rfc) {
		jobServiceManager.useRobotProxy(etokenserver, etokenserverport, proxyId, vo, fqan, proxyrenewal, rfc);
	}
	
	/**
	 * 
	 * @param etokenserver
	 * @param etokenserverport
	 * @param proxyId
	 * @param vo
	 * @param fqan
	 * @param proxyrenewal
	 * @param cnLabel
	 */
	public void useRobotProxy(String etokenserver, String etokenserverport, String proxyId, String vo, String fqan, boolean proxyrenewal, boolean rfc, String cnLabel) {
		jobServiceManager.useRobotProxy(etokenserver, etokenserverport, proxyId, vo, fqan, proxyrenewal, rfc, cnLabel);
	}

	/**
	 * 
	 * @param proxyId
	 * @param vo
	 * @param fqan
	 * @param proxyrenewal
	 */
	public void useRobotProxy(String proxyId, String vo, String fqan, boolean proxyrenewal) {
		jobServiceManager.useRobotProxy(proxyId, vo, fqan, proxyrenewal);
	}
	
	/**
	 * 
	 * @param proxyId
	 * @param vo
	 * @param fqan
	 * @param proxyrenewal
	 */
	public void useRobotProxy(String proxyId, String vo, String fqan, boolean proxyrenewal, boolean rfc,  String cnLabel) {
		jobServiceManager.useRobotProxy(proxyId, vo, fqan, proxyrenewal, rfc, cnLabel);
	}
	
	/**
	 * Returns Robot Proxy informations.
	 * 
	 * @return Robot Proxy informations.
	 */
	public String getRobotProxy(){
		return jobServiceManager.etokenServer+":"+jobServiceManager.etokenServerPort;
	}
	
	/**
	 * Sets a user proxy
	 * 
	 * @param value
	 */
	public void setUserProxy(String value) {
		jobServiceManager.setUserProxy(value);
	}

	/**
	 * Returns the user proxy file path.
	 * @return the user proxy file path.
	 */
	public String getUserProxy() {
		return jobServiceManager.getUserProxy();
	}
	/**
	 * Not more available
	 * @deprecated
	 * @param value
	 */
	public void setRetryCount(String value) {
		System.out.println("setRetryCount not more available.");
	}

	/**
	 * @deprecated
	 * @return
	 */
	public String getRetryCount() {
		System.out.println("getRetryCount not more available.");
		return "";
	}

	/**
	 * Sets the executable for this job.
	 * 
	 * @param value executable for this job.
	 */
	public void setExecutable(String value) {
//		executable = value;
		description.setExecutable(value);
	}

	/**
	 * Returns the executable for this job.
	 * 
	 * @return the executable for this job.
	 */
	public String getExecutable() {
//		return executable;
		return description.getExecutable();
	}

	/**
	 * Sets the arguments for this job.
	 * 
	 * @param value arguments for this job.
	 */
	public void setArguments(String value) {
//		arguments = value;
		description.setArguments(value);
	}

	/**
	 * Returns the arguments for this job.
	 * 
	 * @return arguments for job.
	 */
	public String getArguments() {
//		return arguments;
		return description.getArguments();
	}

	/**
	 * Sets job queue for this job.
	 * 
	 * @param value job queue for this job.
	 */
	public void setJobQueue(String value) {
//		jobQueue = value;
		description.setQueue(value);
	}

	/**
	 * Returns job queue for this job.
	 * 
	 * @return job queue for this job.
	 */
	public String getJobQueue() {
		return description.getQueue();
//		return jobQueue;
	}

	/**
	 * Sets path where store output of this job.
	 * 
	 * @param value a String that represents path where store output of this job.
	 */
	public void setOutputPath(String value) {
		outputPathPrefix = value;
//		description.setOutputPath(description.getOutputPath()+"/jobOutput/");
//		outputPath = value + "/jobOutput/";

		logger.info("outputPathPrefix: "+ value);
		if(description.getOutputPath() == null)
			description.setOutputPath(value);
		
		File outputDir = new File(description.getOutputPath());
			
		if (!outputDir.exists()) {
			boolean success = outputDir.mkdir();
			if (!success)
				logger.warn("Error in creating Output Dir");

		}
		logger.info("outputPath: "+description.getOutputPath());
	}

	/**
	 * Returns path where output files of this job are stored.
	 * 
	 * @return path where output files of this job are stored.
	 */
	public String getOutputPath() {
		return description.getOutputPath();
//		return outputPath;
	}

	/**
	 * Sets output file name of this Job.  
	 * 
	 * @param value a String that represents output file name of this Job.
	 */
	public void setJobOutput(String value) {
//		jobOutput = value;
		description.setOutput(value);
	}

	/**
	 * Returns output file name of this Job.
	 * 
	 * @return output file name of this Job.
	 */
	public String getJobOutput() {
//		return jobOutput;
		return description.getOutput();
	}

	/**
	 * Sets error file name of this Job.  
	 * 
	 * @param value a String that represents error file name of this Job.
	 */
	public void setJobError(String value) {
		description.setError(value);
//		jobError = value;
	}
	
	/**
	 * Returns error file name of this Job.
	 * 
	 * @return error file name of this Job.
	 */
	public String getJobError() {
		return description.getError();
//		jobError = value;
	}

	
	public void setOutputFiles(String value) {
//		outputFiles = value.split(",");
		description.setOutputFiles(value);
	}

	public String[] getOutputFiles() {
		return description.getOutputFiles().split(",");
//		return outputFiles;
	}

	/**
	 * Sets a list of input file of this job.
	 * 
	 * @param value a comma separated string that represents list of input file of this job.
	 */
	public void setInputFiles(String value) {
//		inputFiles = value.split(",");
		description.setInputFiles(value);
	}

	/**
	 * Returns a string array that represents a list of input files for this job.
	 * 
	 * @return list of input files for this job.
	 */
	public String[] getInputFiles() {
		return description.getInputFiles().split(",");
//		return inputFiles;
	}

	private void setJobPortletId(String value) {
		jobPortletId = value;
	}

//FUTURE USE
//	private String getJobPortletId() {
//		return jobPortletId;
//	}

	/**
	 * Sets total number of cpus requested for this job.
	 * 
	 * @param value total number of cpus requested for this job.
	 */
	public void setTotalCPUCount(String value) {
		description.setTotalCPUCount(value);
//		totalCPUCount = value;
	}

	/**
	 * Returns total number of cpus requested for this job.
	 * 
	 * @return total number of cpus requested for this job.
	 */
	public String getTotalCPUCount() {
//		return totalCPUCount;
		return description.getTotalCPUCount();
	}

	/**
	 * Sets total number of processes to be started.
	 * 
	 * @param value total number of processes to be started.
	 */
	public void setNumberOfProcesses(String value) {
		description.setNumberOfProcesses(value);
//		NumberOfProcesses = value;
	}

	/**
	 * Returns total number of processes to be started.
	 * 
	 * @return total number of processes to be started.
	 */
	public String getNumberOfProcesses() {
		return description.getNumberOfProcesses();
//		return NumberOfProcesses;
	}

	/**
	 * 
	 * @param value
	 */
	public void setSPMDVariation(String value) {
		description.setSPDMVariation(value);		
//		SPMDVariation = value;
	}

	/**
	 * Returns the parallel job type. 
	 * @return the parallel job type.
	 */
	public String getSPMDVariation() {
		return description.getSPDMVariation();
//		return SPMDVariation;
	}

	
	/**
	 * Sets the job requirements for a gLite based job.
	 * @param value a String[] containing job requirements.
	 */
	public void setJDLRequirements(String[] value) {
		String s = "";
		for (int i = 0; i < value.length; i++) {
			if(i != 0)
				s += ";";
			s += value[i];
		}
		description.setJDLRequirements(s);
//		JDLRequirements = value;
	}

	/**
	 * Returns the job requirements for a gLite based job.
	 * @return a String[] containing job requirements.
	 */
	public String[] getJDLRequirements() {
		if(description.getJDLRequirements()!=null)
			return description.getJDLRequirements().split(";");
		else
			return null;
//		return JDLRequirements;
	}


	private String createJobSandbox() {

		jobSandbox = "";
		
//		if (inputFiles!=null) {
//			for (int i=0;i<inputFiles.length;i++) {
//				jobSandbox = jobSandbox + inputFiles[i] + ">" + inputFiles[i].substring(inputFiles[i].lastIndexOf('/')+1) +  ",";
//			}
//		}

		if ((description.getInputFiles()!=null) && (!description.getInputFiles().equals(""))) {
			String[] inputFiles = description.getInputFiles().split(",");
			for (int i=0;i<inputFiles.length;i++) {
				jobSandbox = jobSandbox + inputFiles[i] + ">" + inputFiles[i].substring(inputFiles[i].lastIndexOf('/')+1) +  ",";
			}
		}
//		if (!jobOutput.equals(""))
//			jobSandbox = jobSandbox + outputPath + jobPortletId + "/<" + jobOutput + ",";
//			//jobSandbox = jobSandbox + outputPath + jobPortletId + "/"+ jobOutput + "<" + jobOutput + ",";
		
		if ((description.getOutput()!=null) && (!description.getOutput().equals("")))
			jobSandbox = jobSandbox + description.getOutputPath() + jobPortletId + "/<" + description.getOutput() + ",";

//		if (!jobError.equals(""))
//			jobSandbox = jobSandbox + outputPath + jobPortletId + "/<" + jobError + ",";
//			//jobSandbox = jobSandbox + outputPath + jobPortletId + "/"+ jobError + "<" + jobError + ",";

		if ((description.getError()!=null) && (!description.getError().equals("")))
			jobSandbox = jobSandbox + description.getOutputPath() + jobPortletId + "/<" + description.getError() + ",";
		
//		if (outputFiles!=null) {
//			for (int i=0;i<outputFiles.length;i++) {
//				jobSandbox = jobSandbox + outputPath + jobPortletId + "/<" + outputFiles[i] + ",";
//			}
//		}
//		if (outputFiles!=null) {
//			for (int i=0;i<outputFiles.length;i++) {
//				jobSandbox = jobSandbox + description.getOutputPath() + jobPortletId + "/<" + outputFiles[i] + ",";
//			}
//		}
		
		if ((description.getOutputFiles()!=null) && (!description.getOutputFiles().equals(""))) {
			String[] outputFiles= description.getOutputFiles().split(",");
			for (int i=0;i<outputFiles.length;i++) {
				jobSandbox = jobSandbox + description.getOutputPath() + jobPortletId + "/<" + outputFiles[i] + ",";
			}
		}

		if (jobSandbox.length()>0)
			jobSandbox = jobSandbox.substring(0,jobSandbox.length()-1);
		
		logger.info("jobSandbox:"+jobSandbox);
//		System.out.println("jobSandbox:"+jobSandbox);		
		return jobSandbox;
	}

	public JobId submitJob(String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		return submitJob(commonName, tcpAddress, GridInteractionId, "", userDescription);
	}
	
	/**
	 * This method allows to submit a job of a collection in an asynchronous way. It starts a 
	 * separated thread responsible for the submission of this job.
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param GridInteractionId an identifier of application in a specified portal
	 * @param userDescription a description for this job
	 * @param jobCollectionId job collection identifier.
	 */
	//TO SUBMIT COLLECTIONS
	public void submitJobAsync(String commonName, String tcpAddress, int GridInteractionId, String userDescription, int jobCollectionId) {
		submitJobAsync(commonName, tcpAddress, GridInteractionId, "", userDescription, jobCollectionId );
	}
	
	/**
	 * This method allows to submit a job in an asynchronous way to a specified resource manager. 
	 * It starts a separated thread responsible for the submission of this job.
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param GridInteractionId an identifier of application in a specified portal
	 * @param resourceManager to which are submitting this job
	 * @param userDescription a description for this job.
	 */
	//TO SUBMIT Simple Jobs
	public void submitJobAsync(String commonName, String tcpAddress, int GridInteractionId, String resourceManager, String userDescription) {
		submitJobAsync(commonName, tcpAddress, GridInteractionId, resourceManager, userDescription, null );
	}
	
	/**
	 * This method allows to submit a job in an asynchronous way. 
	 * It starts a separated thread responsible for the submission of this job.
	 * 
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param GridInteractionId an identifier of application in a specified portal
	 * @param userDescription a description for this job.
	 */
	//TO SUBMIT A JOB WITHOUT SPECIFYING A DETERMINATE RESOURCE MANAGER
	public void submitJobAsync(String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		submitJobAsync(commonName, tcpAddress, GridInteractionId, "", userDescription, null );
	}

	private JobId submitJob(String commonName, String tcpAddress, int GridInteractionId, String resourceManager, String userDescription) {
		try {
			
			URL serviceURL;
			boolean getRandomWMS = false;

			if (resourceManager.equals("")) {
				getRandomWMS = true;
				if ( (WMSList==null) && (bdii==null)) { 
					System.out.println("Error: you have to specify a WMS list or a BDII");
					return null; //TODO manage error
				}
			}

			if (getRandomWMS) {
				String selectedResourceManager = "";
				if (WMSList!=null)
					selectedResourceManager = getRandomWMS();
				else
					selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
				System.out.println("Resource Manager selected: " + selectedResourceManager);
				serviceURL = URLFactory.createURL(selectedResourceManager);
			}
			else
				serviceURL = URLFactory.createURL(resourceManager);
			
			System.out.println("resourceManager:"+resourceManager);
			
			Job job = null;
			JobDescription desc = JobFactory.createJobDescription();

			if (getExecutable().equals("")) {
				return null; //TODO manage error
			}
			
			int count = 0;
			
			System.out.println("getting jobService...");
			JobService service = jobServiceManager.getJobService(serviceURL);
			while ((service==null) && (count < shallowRetry)) {
				count++;
				//sleep 1 sec between each re-submission
				try {
					Thread.sleep(1000);
				}
				catch (Exception e) {}
				if (getRandomWMS) {
					String selectedResourceManager = "";
					if (WMSList!=null)
						selectedResourceManager = getRandomWMS();
					else
						selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
					System.out.println("Resource Manager selected: " + selectedResourceManager);
					
					serviceURL = URLFactory.createURL(selectedResourceManager);
				}
				service = jobServiceManager.getJobService(serviceURL);
			}
			
			if (service==null)
				return null;
			
			//creating db entry...
			//int dbId = DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getProxyId(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription);
			int dbId = 0;
			if (jobServiceManager.getUserProxy().equals(""))
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getProxyId(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription, null);
			else
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getUserProxy(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription, null);
		
			setJobPortletId(removeNotAllowedCharacter(userDescription) + "_" + (new Integer(dbId).toString()));
			//setJobPortletId(userDescription.replaceAll(" ", "") + "_" + (new Integer(dbId).toString()));
	
			desc.setAttribute(JobDescription.EXECUTABLE, getExecutable());


			if ((getArguments()!=null) && (!getArguments().equals("")))
				desc.setVectorAttribute(JobDescription.ARGUMENTS, getArguments().split(","));

			if ((getJobOutput()!=null) && (!getJobOutput().equals("")))
				desc.setAttribute(JobDescription.OUTPUT, getJobOutput());

			if ((getJobError()!=null) && (!getJobError().equals("")))
				desc.setAttribute(JobDescription.ERROR, getJobError());

			if ((getJobQueue()!=null) && (!getJobQueue().equals("")))
				desc.setAttribute(JobDescription.QUEUE, getJobQueue());
			else if (randomCE) {
				if (bdii!=null) {
					String randomQueue = bdii.getRandomCE(jobServiceManager.getUserVO());
					System.out.println("CE selected:"+randomQueue);
					desc.setAttribute(JobDescription.QUEUE, randomQueue);
				}
			}

			desc.setVectorAttribute(JobDescription.FILETRANSFER, createJobSandbox().split(","));

			//MPI
			if ((getTotalCPUCount()!=null) && (!getTotalCPUCount().equals("")))
				desc.setAttribute(JobDescription.TOTALCPUCOUNT,getTotalCPUCount());

//FUTURE USE
//			if (!(SPMDVariation.equals("")))
//				desc.setAttribute(JobDescription.SPMDVARIATION,SPMDVariation);
//
//			if (!(NumberOfProcesses.equals("")))
//				desc.setAttribute(JobDescription.NUMBEROFPROCESSES, NumberOfProcesses);

			//Requirements
			if (getJDLRequirements()!=null)
				desc.setVectorAttribute("Extension", getJDLRequirements());

			System.out.println("JobDescription Created...");
			
			job = service.createJob(desc);
			
			// create the job
			boolean jobSubmitted = false;
			while ( (!jobSubmitted) && (count < shallowRetry)) {
				try {
					// submit
					System.out.println("Job submitting...");
				
					job.run();
					
					jobSubmitted = true;
				}
				catch(Exception exc) {
					System.out.println(exc.toString());
					count ++;
					//sleep 1 sec between each re-submission
					try {
						Thread.sleep(1000);
					}
					catch (Exception e) {}
					
					if (randomCE) {
						if (bdii!=null) {
							String randomQueue = bdii.getRandomCE(jobServiceManager.getUserVO());
							System.out.println("CE selected:"+randomQueue);
							desc.setAttribute(JobDescription.QUEUE, randomQueue);
							job = service.createJob(desc);
						}
					}
					
					if (getRandomWMS) {
						String selectedResourceManager = "";
						if (WMSList!=null)
							selectedResourceManager = getRandomWMS();
						else
							selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
						System.out.println("Resource Manager selected: " + selectedResourceManager);
						
						URL newServiceURL = URLFactory.createURL(selectedResourceManager);
						if (newServiceURL != serviceURL) {
							serviceURL = newServiceURL;
							jobServiceManager.closeSession();
							service = jobServiceManager.getJobService(serviceURL);
							job = service.createJob(desc);
						}
					}
					
				}
			}

			if (!jobSubmitted) {
				//TODO deleting line from DB
				jobServiceManager.closeSession();
				return null;
			}
				
			String jobId = job.getAttribute(Job.JOBID);
			System.out.println(jobId);

			int err = DBInterface.updateGridInteraction(jobId, dbId);
			
			if (err!=0) System.out.println("Error in update grid interaction");
		
			jobServiceManager.closeSession();
			
			return new JobId(jobId, dbId);
		}
		catch (Exception e) {
			jobServiceManager.closeSession();
			System.out.println(e.toString());
		}
		
		return null;
		
	}
	
//	public void submitJobAsync(final String commonName, final String tcpAddress, final int GridInteractionId, final String resourceManager, final String userDescription, final Integer jobCollectionId, final Long descriptionToUpdate) {
	private void submitJobAsync(final String commonName, final String tcpAddress, final int GridInteractionId, final String resourceManager, final String userDescription, final Integer jobCollectionId) {
		Thread t;
		t = new Thread("submitJob") {
		public void run() {
		try {

			logger.info("Submitting job in Thread : " + Thread.currentThread());
//			System.out.println("Submitting job in Thread : " + Thread.currentThread());
			
			URL serviceURL;
			boolean getRandomWMS = false;
			String adaptor = "";
			logger.info("random resource manager error check");
//			System.out.println("random resource manager error check");
			if (resourceManager.equals("")) {
				getRandomWMS = true;
				if ( (WMSList==null) && (bdii==null)) {
					logger.warn("You have to specify a WMS list or a BDII");
					//System.out.println("Error: you have to specify a WMS list or a BDII");
					if (resubmitting) waitForResubmission.release();
					return; //TODO manage error
				}
			}
			
			if (getRandomWMS) {
				logger.info("Getting random resource manager...");
//				System.out.println("getting random resource manager...");
				String selectedResourceManager = "";
				if (WMSList!=null)
					selectedResourceManager = getRandomWMS();
				else
					selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
				logger.info("Resource Manager selected: " + selectedResourceManager);
//				System.out.println("Resource Manager selected: " + selectedResourceManager);
				
				serviceURL = URLFactory.createURL(selectedResourceManager);
			}
			else {
				logger.info("creating serviceurl...");
//				System.out.println("creating serviceurl...");
				serviceURL = URLFactory.createURL(resourceManager);
			}
//			logger.debug("Service URL: "+serviceURL);
			logger.info("Getting adaptor name...");
//			System.out.println("getting adaptor name...");
			
			//System.out.println("resourceManager:"+resourceManager);
			adaptor = serviceURL.toString().substring(0, serviceURL.toString().indexOf(":"));
			logger.info("Using adaptor: " + adaptor);
//			System.out.println("Using adaptor:"+adaptor);
			
			Job job = null;
			JobDescription desc = JobFactory.createJobDescription();
//			GEJobDescription jobDesc=null;
//			if(descriptionToUpdate==null){
//				jobDesc = new GEJobDescription();
//			} else {
//				jobDesc = GEJobDescription.findJobDescriptionById(descriptionToUpdate);
//			}
			
//			if (executable.equals("")) {
//				return; //TODO manage error
//			}
			if(getExecutable().equals("")) {
				if (resubmitting) waitForResubmission.release();
				return;
			}
			
			int count = 0;
			
			logger.info("Getting jobService...");
//			System.out.println("getting jobService...");
			
			JobService service = null;
			if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
					service = jobServiceManager.getJobService(serviceURL);
			}
			else if (adaptor.equals("unicore")) {
				service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
			}
			else if (adaptor.equals("ourgrid")) {
				service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
			}
			else if (adaptor.equals("bes-genesis2")) {
				service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
			}
			else if (adaptor.equals("gos")) {
				service = jobServiceManager.getGOSJobService(serviceURL);
			}
			else if (adaptor.equals("ssh")) {
				service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
			}
			
			while ((service==null) && (count < shallowRetry)) {
				count++;
				//sleep 1 sec between each re-submission
				try {
					Thread.sleep(1000);
				}
				catch (Exception e) {}
				if (getRandomWMS) {
					String selectedResourceManager = "";
					if (WMSList!=null)
						selectedResourceManager = getRandomWMS();
					else
						selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
					logger.info("Resource Manager selected: " + selectedResourceManager);
//					System.out.println("Resource Manager selected: " + selectedResourceManager);
					
					adaptor = selectedResourceManager.substring(0, selectedResourceManager.indexOf(":"));
					logger.info("Using adaptor: " + adaptor);
//					System.out.println("Using adaptor:"+adaptor);
					
					serviceURL = URLFactory.createURL(selectedResourceManager);
				}
				
				if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
					service = jobServiceManager.getJobService(serviceURL);
				}
				else if (adaptor.equals("unicore")) {
					service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
				}
				else if (adaptor.equals("ourgrid")) {
					service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
				}
				else if (adaptor.equals("bes-genesis2")) {
					service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
				}
				else if (adaptor.equals("gos")) {
					service = jobServiceManager.getGOSJobService(serviceURL);
				}
				else if (adaptor.equals("ssh")) {
					service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
				}
			}
			
			if (service==null){
				deleteSubmittingDescription(userDescription, 0);
				if (resubmitting) waitForResubmission.release();
				return;
			}
				
			
			//creating db entry...
			int dbId = 0;
			
			if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
				if (jobServiceManager.getUserProxy().isEmpty()){
					//dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getProxyId(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription);
					dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getProxyId(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription, userEmail, jobServiceManager.etokenServer+":"+jobServiceManager.etokenServerPort, jobCollectionId);
				}
				else{
					//dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getUserProxy(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription);
					dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), jobServiceManager.getUserProxy(), jobServiceManager.getUserVO(), jobServiceManager.getUserFQAN(), userDescription, userEmail, jobCollectionId);
				}
//				jobDesc.setProxyRenewal(jobServiceManager.ProxyRenewal);
				description.setProxyRenewal(jobServiceManager.ProxyRenewal);
			}
			else if (adaptor.equals("unicore")) {
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), (jobServiceManager.getJKSPath()+":"+jobServiceManager.getJKSPassword()), "", "", userDescription, userEmail, jobCollectionId);
			}
			else if (adaptor.equals("ourgrid")) {
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", "", (jobServiceManager.getOurGridUserName()+":"+jobServiceManager.getOurGridPassword()), "", "", userDescription, userEmail, jobCollectionId);
			}
			else if (adaptor.equals("bes-genesis2")) {
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", jobServiceManager.getUserDN(), (jobServiceManager.getGenesisJKSPath()+":"+jobServiceManager.getGenesisJKSPassword()), "", "", userDescription, userEmail, jobCollectionId);
			}
			else if (adaptor.equals("gos")) {
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", "gos", "", "", "", userDescription, userEmail, jobCollectionId);
			}
			else if (adaptor.equals("ssh")) {
				dbId= DBInterface.InsertActiveGridInteraction(commonName, tcpAddress, GridInteractionId, "", "", (jobServiceManager.getSSHUserName()+":"+jobServiceManager.getSSHPassword()), "", "", userDescription, userEmail, jobCollectionId);
			}
			
			
			setJobPortletId(removeNotAllowedCharacter(userDescription) + "_" + (new Integer(dbId).toString()));
			//setJobPortletId(userDescription.replaceAll(" ", "") + "_" + (new Integer(dbId).toString()));
			
			desc.setAttribute(JobDescription.EXECUTABLE, getExecutable());
//			jobDesc.setExecutable(executable);

			if  ((getArguments()!=null) && (!getArguments().equals(""))){
				desc.setVectorAttribute(JobDescription.ARGUMENTS, getArguments().split(","));
//				jobDesc.setArguments(getArguments());
			}
			
			if ((getJobOutput()!=null) && (!getJobOutput().equals(""))){
				desc.setAttribute(JobDescription.OUTPUT, getJobOutput());
//				jobDesc.setOutput(jobOutput);
			}
			
			if ((getJobError()!=null) && (!getJobError().equals(""))){
				desc.setAttribute(JobDescription.ERROR, getJobError());
//				jobDesc.setError(jobError);
			}
			
			if ((getJobQueue()!=null) && (!getJobQueue().equals(""))){
				desc.setAttribute(JobDescription.QUEUE, getJobQueue());
//				jobDesc.setQueue(jobQueue);
			}
			else if ((randomCE) && (adaptor.equals("wms"))) {
				if (bdii!=null) {
					String randomQueue = bdii.getRandomCE(jobServiceManager.getUserVO());
					logger.info("CE selected:"+randomQueue);
//					System.out.println("CE selected:"+randomQueue);
					desc.setAttribute(JobDescription.QUEUE, randomQueue);
				}
				//************MARIO***************************
				else{
					String randomQueue=getRandomCEFromCElist();
					logger.info("CE selected:"+randomQueue);
					desc.setAttribute(JobDescription.QUEUE, randomQueue);
				}
				//********************************************
//				jobDesc.setQueue(desc.getAttribute(JobDescription.QUEUE));
				setJobQueue(desc.getAttribute(JobDescription.QUEUE));
			}

			if (!adaptor.equals("bes-genesis2")) {
				desc.setVectorAttribute(JobDescription.FILETRANSFER, createJobSandbox().split(","));
//				jobDesc.setFileTransfer(createJobSandbox());
			}
			else {
				desc.setVectorAttribute(JobDescription.FILETRANSFER, ("scp://vm20.ct.infn.it:4422/home/chaindemo/stdout<stdout").split(","));
//				jobDesc.setFileTransfer("scp://vm20.ct.infn.it:4422/home/chaindemo/stdout<stdout");
			}
			
			//MPI
			if ((getTotalCPUCount()!=null) && (!getTotalCPUCount().equals(""))){
				desc.setAttribute(JobDescription.TOTALCPUCOUNT,getTotalCPUCount());
//				jobDesc.setTotalCPUCount(totalCPUCount);
			}
			
			if ( (!adaptor.equals("wms")) && (!adaptor.equals("ourgrid")) && (!adaptor.equals("bes-genesis2")) && (!adaptor.equals("gos")) && (!adaptor.equals("ssh")) && (!adaptor.equals("rocci"))) {
				if ((getSPMDVariation()!=null) && (!getSPMDVariation().equals(""))){
					desc.setAttribute(JobDescription.SPMDVARIATION, getSPMDVariation());
//					jobDesc.setSPDMVariation(SPMDVariation);
				}
				if ((getNumberOfProcesses()!=null) && (!getNumberOfProcesses().equals(""))){
					desc.setAttribute(JobDescription.NUMBEROFPROCESSES, getNumberOfProcesses());
//					jobDesc.setNumberOfProcesses(NumberOfProcesses);
				}
			}
			
			//Requirements
			if (adaptor.equals("wms")) {
				if (getJDLRequirements()!=null){
					desc.setVectorAttribute("Extension", getJDLRequirements());
					String req="";
					for (int i = 0; i < getJDLRequirements().length; i++) {
						if(i != 0)
							req += ";";
						req += getJDLRequirements()[i];
					}
					description.setJDLRequirements(req);
				}
			}
			logger.info("JobDescription Created...");
			
			job = service.createJob(desc);
			
//			try {
//				Thread.sleep(10000);
//			}
//			catch (Exception e) {}
			
			// create the job
			boolean jobSubmitted = false;
			
			while ( (!jobSubmitted) && (count < shallowRetry)) {
				try {
					// submit
					logger.info("Submitting job...");
//					System.out.println("Submitting job...");
				
					if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
						Context[] contexts = service.getSession().listContexts();
						
						logger.info("Using proxy="+contexts[0].getAttribute(Context.USERPROXY));
//						System.out.println("Using proxy="+contexts[0].getAttribute(Context.USERPROXY));
					}
					
					if (adaptor.equals("bes-genesis2")) {
						System.setProperty("javax.net.ssl.keyStore","/tmp/genesis-robot-keys.jks");
						System.getProperty("javax.net.ssl.keyStorePassword","chaindemo");
						logger.info("Personal keystore path = " + System.getProperty("javax.net.ssl.keyStore"));
//						System.out.println("personal keystore path = " + System.getProperty("javax.net.ssl.keyStore"));
						logger.info("Personal keystore pwd = " + System.getProperty("javax.net.ssl.keyStorePassword"));
//						System.out.println("personal keystore pwd = " + System.getProperty("javax.net.ssl.keyStorePassword"));
						logger.info("truststore path = " + System.getProperty("javax.net.ssl.trustStore"));
//						System.out.println("truststore path = " + System.getProperty("javax.net.ssl.trustStore"));
						logger.info("truststore pwd = " + System.getProperty("javax.net.ssl.trustStorePassword"));
//						System.out.println("truststore pwd = " + System.getProperty("javax.net.ssl.trustStorePassword"));
					}
					
					if ( (adaptor.equals("ssh")) || (adaptor.equals("rocci"))) {
						synchronized (JSagaJobSubmission.class) {job.run();}
					}
					else
						job.run();
					
					jobSubmitted = true;

				}
				catch(Exception exc) {
					exc.printStackTrace();
					logger.error(exc.toString());
//					System.out.println(exc.toString());
					count ++;
					//sleep 1 sec between each re-submission
					try {
						Thread.sleep(1000);
					}
					catch (Exception e) {}
					
					if (getRandomWMS) {
						String selectedResourceManager = "";
						if (WMSList!=null)
							selectedResourceManager = getRandomWMS();
						else
							selectedResourceManager = bdii.getRandomWMS(jobServiceManager.getUserVO());
						logger.info("Resource Manager selected: " + selectedResourceManager);
//						System.out.println("Resource Manager selected: " + selectedResourceManager);
						
						adaptor = selectedResourceManager.substring(0, selectedResourceManager.indexOf(":"));
						logger.info("Using adaptor: " + adaptor);
//						System.out.println("Using adaptor:"+adaptor);
						
						URL newServiceURL = URLFactory.createURL(selectedResourceManager);
						if (newServiceURL!=serviceURL) {
							serviceURL = newServiceURL;
							jobServiceManager.closeSession();
							
							if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
								service = jobServiceManager.getJobService(serviceURL);
							}
							else if (adaptor.equals("unicore")) {
								service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
							}
							else if (adaptor.equals("ourgrid")) {
								service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
							}
							else if (adaptor.equals("bes-genesis2")) {
								service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
							}
							else if (adaptor.equals("gos")) {
								service = jobServiceManager.getGOSJobService(serviceURL);
							}
							else if (adaptor.equals("ssh")) {
								service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
							}
							
							job = service.createJob(desc);
						}
					}
					
				}
			}
		
			if (!jobSubmitted) {
				deleteSubmittingDescription(userDescription, dbId);
				jobServiceManager.closeSession();
				if (resubmitting) waitForResubmission.release();
				return;
			}
//			Session s =service.getSession();
//			for (Context c : s.listContexts()) {
//				for (String attr : c.getVectorAttribute("JobServiceAttributes")) {
//					System.out.println("****ATTRIBUTI: " + attr);
//				}
//			}
			String jobId = job.getAttribute(Job.JOBID);
			logger.info("Job Submitted: "+jobId);
			
			//Store jobDescription
//			GEJobDescription geJobDescr = new GEJobDescription(desc);
//			jobDesc.setOutputPath(outputPath);
//			String files="";
//			for(int i = 0; i<getInputFiles().length; i++){
//				if(i != 0)
//					files+=","+getInputFiles()[i];
//				else
//					files=getInputFiles()[i];
//			}				
//			jobDesc.setInputFiles(files);
//			jobDesc.setjobId(jobId);//.saveJobDescription(jobId);
//			
			
			if(allowResub==-1){
				logger.debug("###jobServiceManager.getResubNumber()= "+jobServiceManager.getResubNumber()+" ###");
				allowResub=jobServiceManager.getResubNumber();
			}

			description.setResubmitCount(allowResub);
//			jobDesc.setResubmitCount(allowResub);
			description.setjobId(jobId);
			description.saveJobDescription();
			logger.debug("Description inserted or update for jobId: "+ description.getjobId());
			//End store jobDescrption
			
//			System.out.println(jobId);
			
//			String executionHosts[];
//			try {
//				executionHosts = job.getVectorAttribute(Job.EXECUTIONHOSTS);
//			} catch (IncorrectStateException e) {
//				executionHosts = new String[]{"[not initialized yet]"};
//			} catch (NotImplementedException e) {
//				executionHosts = new String[]{"[not supported for this backend]"};
//			}
//			System.out.println("executionHosts.length="+executionHosts.length);
//			for (int i=0;i<executionHosts.length;i++)
//				System.out.println("executionHosts="+executionHosts[i]);

			int err = DBInterface.updateGridInteraction(jobId, dbId);
			
			if (err!=0)
				logger.error("Error in update grid interaction");
//				System.out.println("Error in update grid interaction");
		
			jobServiceManager.closeSession();

			if (checkJobsStatus) {
				JobCheckStatusService jobCheckStatusService = null;

				try {
					jobCheckStatusService = InitialContext.<JobCheckStatusService>doLookup("JobCheckStatusService");
					logger.info("Got JobCheckStatusService...");
//					System.out.println("Got JobCheckStatusService...");
				}
				catch ( NamingException ex ) {
					logger.error("Cannot get JobCheckStatusService: " + ex);
//					System.out.println("Cannot get JobCheckStatusService: " + ex);
				}
				catch(Exception ex){
					logger.error("Cannot get JobCheckStatusService: " + ex);
//					System.out.println("Cannot get JobCheckStatusService: " + ex);
				}

				if (jobCheckStatusService==null) {
					logger.info("Get local JobCheckStatusService");
//					System.out.println("get local JobCheckStatusService");
//					jobCheckStatusService = JobCheckStatusService.getInstance(URL, userName, password);
					if(!DBInterface.inAppServer){
						logger.info("Creating JobCheckStatusService with local connection parameters.");
						jobCheckStatusService = JobCheckStatusService.getInstance(URL, userName, password);
					}
					else {
						logger.info("Creating JobCheckStatusService with datasource paramters.");
						jobCheckStatusService =  JobCheckStatusService.getInstance();
					}
				}

				jobCheckStatusService.startJobCheckStatusThread(commonName, outputPathPrefix);
			}
			
			if (resubmitting) waitForResubmission.release();
		
		}
		catch (Exception e) {
			System.out.println("Exception serviceURL="+e.getMessage());
			e.printStackTrace();
	
			jobServiceManager.closeSession();
			logger.error(e.toString());
//			System.out.println(e.toString());
			if (resubmitting) waitForResubmission.release();
		}
		
		}

		};
		if (threadPool != null) threadPool.execute(t);
		else t.start();
	}
	
//	public String getJobStatus(JobId jobId) {
//		return (getJobStatus(jobId,false))[0];
//	}
	/**
	 * This method returns a String array consists of two elements in the following order:
	 * <ul>
	 * 	<li><b>return[0]</b> current status for this job;</li>
	 * 	<li><b>return[1]</b> compute element (CE) where job are running.</li>
	 * </ul>
	 * 
	 * @param jobId the grid job identifier for which want retrieve status
	 * @param getCE <b>true</b> if you want also retrieve the compute element, <b>false</b> othewise.
	 * @return a strings array consists of: status for this job and compute element in the above specified order.
	 */
	public String[] getJobStatus(JobId jobId, boolean getCE) {

		String[] output = new String[2];
		for (int i=0;i<2;i++)
			output[i] = "";
		
		String nativeJobId = getNativeJobId(jobId.getGridJobId());
		URL serviceURL = getServiceURL(jobId.getGridJobId());
		if ((nativeJobId==null) || (serviceURL==null)) return output; //TODO manage error

		String adaptor = (serviceURL.toString()).substring(0, (serviceURL.toString()).indexOf(":"));
		logger.info("Using adaptor:"+adaptor);
//		System.out.println("Using adaptor:"+adaptor);
		
		if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("gos")) || (adaptor.equals("rocci"))) {
		}
		else if (adaptor.equals("unicore")) {
			if ( (jksPath.equals("")) || (jksPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No JKS defined.");
//				System.out.println("Error in getJobStatus - No JKS defined.");
				return output;
			}
		}
		else if (adaptor.equals("ourgrid")) {
			if ( (ourgridUserName.equals("")) || (ourgridPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No ourgrid credentials defined.");
//				System.out.println("Error in getJobStatus - No ourgrid credentials defined.");
				return output;
			}
		}
		else if (adaptor.equals("bes-genesis2")) {
			if ( (genesisJksPath.equals("")) || (genesisJksPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No Genesis JKS defined.");
//				System.out.println("Error in getJobStatus - No Genesis JKS defined.");
				return output;
			}
		}
		else if (adaptor.equals("ssh")) {
//			if ( (SSHUserName.equals("")) || (SSHPassword.equals("")) ) {
			if ( (SSHUserName.equals(""))){
				logger.error("Error in getJobStatus - No SSH credentials defined.");
				return output;
			}
		}
		else {
			logger.error("Error in getJobStatus - Adaptor not supported.");
//			System.out.println("Error in getJobStatus - Adaptor not supported.");
			return output;
		}
		
		try {
			logger.info("Getting JobService...");
//			System.out.println("Getting JobService...");

			JobService service = null;
			if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
				service = jobServiceManager.getJobService(serviceURL);
			}
			else if (adaptor.equals("unicore")) {
				service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
			}
			else if (adaptor.equals("ourgrid")) {
				service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
			}
			else if (adaptor.equals("bes-genesis2")) {
				service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
				System.setProperty("javax.net.ssl.keyStore","/tmp/genesis-robot-keys.jks");
				System.getProperty("javax.net.ssl.keyStorePassword","chaindemo");
			}
			else if (adaptor.equals("gos")) {
				service = jobServiceManager.getGOSJobService(serviceURL);
			}
			else if (adaptor.equals("ssh")) {
				service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
			}
			
			logger.info("Getting Job...");
//			System.out.println("Getting Job...");
			Job job = service.getJob(nativeJobId);
//			System.out.println("Getting Job Description...");
//			JobDescription descr = null;
//			try {
//				descr = job.getJobDescription();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (descr!=null) {
//			System.out.println("EXECUTABLE="+descr.getAttribute(JobDescription.EXECUTABLE));
//			System.out.println("INPUT="+descr.getAttribute(JobDescription.INPUT));
//			System.out.println("OUTPUT="+descr.getAttribute(JobDescription.OUTPUT));
//			System.out.println("ARGUMENTS="+descr.getAttribute(JobDescription.ARGUMENTS));
//			System.out.println("QUEUE="+descr.getAttribute(JobDescription.QUEUE));
//			}
			logger.info("Getting Job State...");
//			System.out.println("Getting Job State...");
			//State state = job.getState();
			State state;
			
			if ( (adaptor.equals("ssh")) || (adaptor.equals("rocci"))) {
				synchronized (JSagaJobSubmission.class) {state = job.getState();}
			}
			else
				state = job.getState();
			
			if (getCE) {
				//
				// GET CE
				//
				logger.info("Getting CE...");
//				System.out.println("Getting CE...");
				
				if ( (adaptor.equals("wms")) || (adaptor.equals("ourgrid")) || (adaptor.equals("ssh")) || (adaptor.equals("rocci"))) {
					String executionHosts[];
					try {
						executionHosts = job.getVectorAttribute(Job.EXECUTIONHOSTS);
						if (!executionHosts[0].equals("")) {
							if (adaptor.equals("wms")) {
								logger.info("PRE CE ="+executionHosts[0]);
//								System.out.println("PRE CE ="+executionHosts[0]);
								//executionHosts[0] = executionHosts[0].substring(executionHosts[0].indexOf("/")+2);
								executionHosts[0] = executionHosts[0].substring(0, executionHosts[0].indexOf(":"));
							}
							logger.info("CE ="+executionHosts[0]);
//							System.out.println("CE ="+executionHosts[0]);
							output[1] = executionHosts[0];
						
//						String ceForBDII = "ldap://" + executionHosts[0] + ":2170";
//						System.out.println("CE for BDII="+ceForBDII);
//						setBDII(executionHosts[0]);
//						String CECoordinate[] = bdii.queryCECoordinate(ceForBDII);
//
//						if (CECoordinate!=null) {
//							output[2] = CECoordinate[0];
//							output[3] = CECoordinate[1];
//						}
//						else {
//							output[2] = "0";
//							output[3] = "0";
//						}
							
						}
					} catch (IncorrectStateException e) {
						executionHosts = new String[]{"[not initialized yet]"};
					} catch (NotImplementedException e) {
						executionHosts = new String[]{"[not supported for this backend]"};
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if ( (adaptor.equals("unicore")) || adaptor.equals("wsgram") || (adaptor.equals("gatekeeper")) || adaptor.equals("bes-genesis2")) {
					// unicore://zam052v01.zam.kfa-juelich.de:8080
					// wsgram://xn03.ctsf.cdacb.in:8443/GW
					// bes-genesis2://xcg-server1.uvacse.virginia.edu:20443/axis/services/GeniiBESPortType?genii-container-id=93B641B7-9422-EA4C-A90B-CA6A9D98E344
					output[1] = (serviceURL.toString()).substring((serviceURL.toString()).indexOf("/")+2);
					output[1] = output[1].substring(0,output[1].indexOf(":"));
				}
				else if (adaptor.equals("gos")) {
					// gos://124.205.18.242
					output[1] = (serviceURL.toString()).substring((serviceURL.toString()).indexOf("/")+2);
				}
//				else if (adaptor.equals("wsgram")) {
//					//TODO temporary we have only a unique wsgram ce
//					output[1] = "xn03.ctsf.cdacb.in";
//				}
//				System.out.println("executionHosts.length="+executionHosts.length);
//				for (int i=0;i<executionHosts.length;i++)
//					System.out.println("executionHosts="+executionHosts[i]);
				
			}

			jobServiceManager.closeSession();
			double x = Math.random();
//			if(x<0.55){
//				System.out.println("---x: "+x+"---");
//				state = State.FAILED;
//			}
//			else
//				System.out.println("---x: "+x+"---");
			// display status
			if (State.RUNNING.compareTo(state) == 0) {
				output[0] = "RUNNING";
				return output;
			} 
			else if (State.SUSPENDED.compareTo(state) == 0) {
				output[0] = "SUSPENDED";
				return output;
			} else if (State.DONE.compareTo(state) == 0) {
				output[0] = "DONE";
				return output;
			}
			else {
//				DBInterface.CloseGridInteraction(jobId.getDbId());
				
				if (State.CANCELED.compareTo(state) == 0) {
					output[0] = "CANCELED";
					return output;
				} else if (State.FAILED.compareTo(state) == 0) {
					//TODO review
					try {
//						String exitCode = job.getAttribute(Job.EXITCODE);
//						logger.warn("Job failed with exit code: "+exitCode);
//						System.out.println("Job failed with exit code: "+exitCode);
//						GEActiveGridInteraction jobAborted = new GEActiveGridInteraction();
//						jobAborted=Prova.deleteActiveGridInteraction(jobId.getGridJobId());
//						jobAborted=Prova.getActiveGridInteraction(jobId.getGridJobId());
						GEActiveGridInteraction jobAborted = new GEActiveGridInteraction();
						description = GEJobDescription.findJobDescriptionByJobId(jobId.getGridJobId());
//						allowResub = abortedJobDesc.getResubmitCount() - 1;
						allowResub = description.getResubmitCount() - 1;
						if(allowResub >= 0){
//							Prova.deleteActiveGridInteraction(jobId.getGridJobId());
//							GEActiveGridInteraction jobAborted = new GEActiveGridInteraction();
//							jobAborted = GEActiveGridInteraction.findActiveGridInteractionByJobId(jobId.getGridJobId());
							jobAborted = GEActiveGridInteraction.findActiveGridInteractionByJobId(Long.valueOf(jobId.getDbId()));
							jobAborted.delete();
//							resubmitJob(jobAborted, abortedJobDesc);
							resubmitJob(jobAborted);
						}
						else{
							logger.warn("Cannot resubmit job: "+ jobId.getGridJobId() +". Max resubmission count exceeded");
							description.delete();
//							jobAborted = GEActiveGridInteraction.findActiveGridInteractionByJobId(jobId.getGridJobId());
							jobAborted = GEActiveGridInteraction.findActiveGridInteractionByJobId(Long.valueOf(jobId.getDbId()));
							if(jobAborted.getIdJobCollection() == null)
								DBInterface.CloseGridInteraction(jobId.getDbId());
						}
						output[0] = "Aborted";
						return output;
					} catch(Exception e) {
						logger.warn("Job failed: "+e.toString());
//						System.out.println("Job failed.");
						
						output[0] = "Aborted";
						return output;
					} 
//					catch(NotImplementedException e) {
//						logger.warn("Job failed: "+e.toString());
////						System.out.println("Job failed.");
//						
//						output[0] = "Aborted";
//						return output;
//					}
				} else {
					output[0] = "Unexpected state";
					return output;
				}
			}
			
		}
		catch (Exception e) {
			jobServiceManager.closeSession();
			logger.error(e.toString());
//			System.out.println(e.toString());
		}

		output[0] = "Error in getting job state";
		return output;
	}

	private void resubmitJob(GEActiveGridInteraction jobAborted) {
		//Preparo l'oggetto per la sottomissione
		
		logger.debug("RESUBMITTING JOB: " +jobAborted.getJobId());
		resubmitting = true;
//		setExecutable(abortedJobDesc.getExecutable());
//		setArguments(abortedJobDesc.getArguments());
//		setJobOutput(abortedJobDesc.getOutput());
//		setJobError(abortedJobDesc.getError());
//		if(abortedJobDesc.getOutputPath().contains("/jobOutput/"));
//			abortedJobDesc.setOutputPath(abortedJobDesc.getOutputPath().replace("/jobOutput/", ""));
//		setOutputPath(abortedJobDesc.getOutputPath());
//		setInputFiles(abortedJobDesc.getInputFiles());
		setUserEmail(jobAborted.getEmail());

		String tmp = description.getjobId().substring(1, description.getjobId().indexOf(']'));
		String resourceManager=tmp.replace("[", "");
		resourceManager=resourceManager.replace("]", "");
		
		String adaptor = resourceManager.toString().substring(0, resourceManager.toString().indexOf(":"));
		
		logger.debug("Switching Adaptor...");
		switch(Adaptor.valueOf(adaptor)){
			case wms:
			case wsgram:
			case gatekeeper:
			case rocci:
				logger.debug("Adaptor is WMS or WSGRAM or GATEKEEPER");
				if(!jobAborted.getProxyId().contains("/")){
					logger.debug("Using Robot Proxy...");
					if(!jobAborted.geteTokenServer().equals("")){
						String eTokenServerTmp = jobAborted.geteTokenServer();
						useRobotProxy(eTokenServerTmp.substring(0,eTokenServerTmp.indexOf(':')), eTokenServerTmp.substring(eTokenServerTmp.indexOf(':')+1), jobAborted.getProxyId(), jobAborted.getVO(), jobAborted.getFqan(), description.isProxyRenewal(), true, jobAborted.getCommonName());
					}else{
						useRobotProxy(jobAborted.getProxyId(), jobAborted.getVO(), jobAborted.getFqan(), description.isProxyRenewal(),true, jobAborted.getCommonName());
					}
				}else{
					logger.debug("Using User Proxy...");
					setUserProxy(jobAborted.getProxyId());
				}
				break;
			case ssh:
				logger.debug("Adaptor is SSH");
				String sshUserName = jobAborted.getProxyId().substring(0,jobAborted.getProxyId().indexOf(":"));
				String sshPassword = jobAborted.getProxyId().substring(jobAborted.getProxyId().indexOf(":")+1);
				logger.debug("sshUserName: " + sshUserName + " sshPassword: "+ sshPassword);
				setSSHCredential(sshUserName, sshPassword);
				break;
			case ourgrid:
				logger.debug("Adaptor is OURGRID");
				String ourgridUserName = jobAborted.getProxyId().substring(0,jobAborted.getProxyId().indexOf(":"));
				String ourgridPassword = jobAborted.getProxyId().substring(jobAborted.getProxyId().indexOf(":")+1);
				setOurGridCredential(ourgridUserName, ourgridPassword);
				break;
			case unicore:
				logger.debug("Adaptor is UNICORE");
				String jksPath = jobAborted.getProxyId().substring(0,jobAborted.getProxyId().indexOf(":"));
				String jksPassword = jobAborted.getProxyId().substring(jobAborted.getProxyId().indexOf(":")+1);
				setJKS(jksPath, jksPassword);
				break;
			case gos:
				logger.debug("Adaptor is GOS");
				break;
			case besGenesis2:
				logger.debug("Adaptor is BESGENESIS2");
				String genesisJksPath = jobAborted.getProxyId().substring(0,jobAborted.getProxyId().indexOf(":"));
				String genesisjksPassword = jobAborted.getProxyId().substring(jobAborted.getProxyId().indexOf(":")+1);
				setGenesisJKS(genesisJksPath, genesisjksPassword);
				break;
			default:
				logger.error("Adaptor undefined.");
				return;
		}
		
		submitJobAsync(jobAborted.getCommonName(), jobAborted.getTcpAddress(), jobAborted.getGridInteraction(), resourceManager, jobAborted.getUserDescription(), jobAborted.getIdJobCollection());
		
		try {
			waitForResubmission.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error("Exception during waiting for re-submission");
			e.printStackTrace();
		}
		
		resubmitting = false;
	}

	public void submitFinalJob(GEActiveGridInteraction finalJob, GEJobDescription finalJobDesc) {
		//Preparo l'oggetto per la sottomissione
		
		logger.debug("SUBMITTING FINAL JOB FOR COLLECTION: " + finalJob.getIdJobCollection());

		description = finalJobDesc;
//		setExecutable(finalJobDesc.getExecutable());
//		setArguments(finalJobDesc.getArguments());
//		setJobOutput(finalJobDesc.getOutput());
//		setJobError(finalJobDesc.getError());
//		if(finalJobDesc.getOutputPath().contains("/jobOutput/"));
//			finalJobDesc.setOutputPath(finalJobDesc.getOutputPath().replace("/jobOutput/", ""));
//		setOutputPath(finalJobDesc.getOutputPath());
//		setInputFiles(finalJobDesc.getInputFiles());
		//setUserEmail(finalJob.getEmail()); NO uso email della collection

		String tmp = finalJob.getJobId().substring(1, finalJob.getJobId().indexOf(']'));
		String resourceManager=tmp.replace("[", "");
		resourceManager=resourceManager.replace("]", "");
		
		String adaptor = resourceManager.toString().substring(0, resourceManager.toString().indexOf(":"));
		
		logger.debug("Switching Adaptor...");
		switch(Adaptor.valueOf(adaptor)){
			case wms:
			case wsgram:
			case gatekeeper:
			case rocci:
				logger.debug("Adaptor is WMS or WSGRAM or GATEKEEPER");
				if(!finalJob.getProxyId().contains("/")){
					logger.debug("Using Robot Proxy...");
					if(!finalJob.geteTokenServer().equals("")){
						String eTokenServerTmp = finalJob.geteTokenServer();
						useRobotProxy(eTokenServerTmp.substring(0,eTokenServerTmp.indexOf(':')), eTokenServerTmp.substring(eTokenServerTmp.indexOf(':')+1), finalJob.getProxyId(), finalJob.getVO(), finalJob.getFqan(), finalJobDesc.isProxyRenewal(), true,finalJob.getCommonName());
					}else{
						useRobotProxy(finalJob.getProxyId(), finalJob.getVO(), finalJob.getFqan(), finalJobDesc.isProxyRenewal(), true, finalJob.getCommonName());
					}
				}else{
					logger.debug("Using User Proxy...");
					setUserProxy(finalJob.getProxyId());
				}
				break;
			case ssh:
				logger.debug("Adaptor is SSH");
				String sshUserName = finalJob.getProxyId().substring(0,finalJob.getProxyId().indexOf(":"));
				String sshPassword = finalJob.getProxyId().substring(finalJob.getProxyId().indexOf(":")+1);
				logger.debug("sshUserName: " + sshUserName + " sshPassword: "+ sshPassword);
				setSSHCredential(sshUserName, sshPassword);
				break;
			case ourgrid:
				logger.debug("Adaptor is OURGRID");
				String ourgridUserName = finalJob.getProxyId().substring(0,finalJob.getProxyId().indexOf(":"));
				String ourgridPassword = finalJob.getProxyId().substring(finalJob.getProxyId().indexOf(":")+1);
				setOurGridCredential(ourgridUserName, ourgridPassword);
				break;
			case unicore:
				logger.debug("Adaptor is UNICORE");
				String jksPath = finalJob.getProxyId().substring(0,finalJob.getProxyId().indexOf(":"));
				String jksPassword = finalJob.getProxyId().substring(finalJob.getProxyId().indexOf(":")+1);
				setJKS(jksPath, jksPassword);
				break;
			case gos:
				logger.debug("Adaptor is GOS");
				break;
			case besGenesis2:
				logger.debug("Adaptor is BESGENESIS2");
				String genesisJksPath = finalJob.getProxyId().substring(0,finalJob.getProxyId().indexOf(":"));
				String genesisjksPassword = finalJob.getProxyId().substring(finalJob.getProxyId().indexOf(":")+1);
				setGenesisJKS(genesisJksPath, genesisjksPassword);
				break;
			default:
				logger.error("Adaptor undefined.");
				return;
		}
		StringTokenizer st = new StringTokenizer(finalJob.getUserDescription(), "_");
		
		submitJobAsync(finalJob.getCommonName(), finalJob.getTcpAddress(), finalJob.getGridInteraction(), resourceManager, st.nextToken()+"-FinalJob", finalJob.getIdJobCollection());
	}
	
	public String getJobOutput(int DbId) {
		System.out.println("DbId = "+ DbId);
		String outputPath = "";
		String gridJobId = DBInterface.getGridJobId(DbId);
		System.out.println("gridJobId2 = "+ gridJobId);
		
		outputPath = getJobOutput(new JobId(gridJobId,DbId));
		
		return outputPath;
	}

	public String getCollectionOutput(int DbId) {
		System.out.println("DbId = "+ DbId);
		JobCollection jc = JobCollection.getJobCollection(DbId);
		String outputPath = "/jobOutput/" + removeNotAllowedCharacter(jc.getDescription()) + "_" + jc.getId() + ".tgz";
		int doneCollectionId = jc.close();
		List<GEActiveGridInteraction> subJobs = GEActiveGridInteraction.findActiveJobForJobCollection(DbId);
		for (GEActiveGridInteraction geActiveGridInteraction : subJobs) {
			DBInterface.CloseGridInteraction(geActiveGridInteraction.getId().intValue(), doneCollectionId);
		}
		
		return outputPath;
	}
	
	public String getJobOutput(JobId jobId) { //SPostare in UsertrackingDBInt
		String jobOutputPath = "/jobOutput/" + removeNotAllowedCharacter(DBInterface.getJobUserDescription(jobId.getDbId())) + "_" + jobId.getDbId() + ".tgz";
		//String jobOutputPath = "/jobOutput/" + (DBInterface.getJobUserDescription(jobId.getDbId())).replaceAll(" ", "") + "_" + jobId.getDbId() + ".tgz";
		DBInterface.CloseGridInteraction(jobId.getDbId());
		System.out.println("getJobOutput=" + jobOutputPath);
		return jobOutputPath;
		//return "/jobOutput/" + (DBInterface.getJobUserDescription(jobId.getDbId())).replaceAll(" ", "") + "_" + jobId.getDbId() + ".tgz";
	}
	
	/**
	 * Performs the output download for the specified job.
	 * 
	 * @param jobId the grid job identifier for which want download output
	 * @param description a string represent the user description for this job.
	 */
	public void downloadJobOutput(JobId jobId, String description) {
		logger.info("Downloading Job output...");
		String nativeJobId = getNativeJobId(jobId.getGridJobId());
		URL serviceURL = getServiceURL(jobId.getGridJobId());
		if ((nativeJobId==null) || (serviceURL==null)) return; //TODO manage error
		
		String adaptor = (serviceURL.toString()).substring(0, (serviceURL.toString()).indexOf(":"));
		logger.info("Using adaptor:"+adaptor);
//		System.out.println("Using adaptor:"+adaptor);
		
		if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("gos")) || (adaptor.equals("rocci")) ) {
		}
		else if (adaptor.equals("unicore")) {
			if ( (jksPath.equals("")) || (jksPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No JKS defined.");
//				System.out.println("Error in getJobStatus - No JKS defined.");
				return;
			}
		}
		else if (adaptor.equals("ourgrid")) {
			if ( (ourgridUserName.equals("")) || (ourgridPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No ourgrid credentials defined.");
//				System.out.println("Error in getJobStatus - No ourgrid credentials defined.");
				return;
			}
		}
		else if (adaptor.equals("bes-genesis2")) {
			if ( (genesisJksPath.equals("")) || (genesisJksPassword.equals("")) ) {
				logger.error("Error in getJobStatus - No Genesis JKS defined.");
//				System.out.println("Error in getJobStatus - No Genesis JKS defined.");
				return;
			}
		}
		else if (adaptor.equals("ssh")) {
//			if ( (SSHUserName.equals("")) || (SSHPassword.equals("")) ) {
			if ( (SSHUserName.equals(""))) {
				logger.error("Error in getJobStatus - No SSH credentials defined.");
				return;
			}
		}
		else {
			logger.error("Error in getJobStatus - Adaptor not supported.");
//			System.out.println("Error in getJobStatus - Adaptor not supported.");
			return;
		}

		try{
			logger.info("Creating JobService...");
//			System.out.println("Creating JobService...");

			JobService service = null;
			if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
				service = jobServiceManager.getJobService(serviceURL);
			}
			else if (adaptor.equals("unicore")) {
				service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
			}
			else if (adaptor.equals("ourgrid")) {
				service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
			}
			else if (adaptor.equals("bes-genesis2")) {
				service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
			}
			else if (adaptor.equals("gos")) {
				service = jobServiceManager.getGOSJobService(serviceURL);
			}
			else if (adaptor.equals("ssh")) {
				service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
			}
			
			logger.info("Getting Job...");
//			System.out.println("Getting Job...");
			Job job = service.getJob(nativeJobId);
			
//			JobDescription desc = job.getJobDescription();
//			if (desc==null) System.out.println("*** E' NULLO!***");
//			else desc.setVectorAttribute(JobDescription.FILETRANSFER,"/home/diego/mpitest/start.sh>start.sh,/home/diego/mpitest/output.txt<output.txt,/home/diego/mpitest/error.txt<error.txt".split(","));
//			if (filetransfer==null) System.out.println("*** E' NULLO!***");
//			else {
//				for (int i=0;i<filetransfer.length;i++)
//					System.out.println("filetransfer["+i+"]="+filetransfer[i]);
//			}
			
			
			//creating output directory
			logger.info("Creating output directory " + getOutputPath() + removeNotAllowedCharacter(description) + "_" + jobId.getDbId() + "/");
//			System.out.println("creating output directory " + getOutputPath() + removeNotAllowedCharacter(description) + "_" + jobId.getDbId() + "/");
			File outputDir = new File(getOutputPath() + removeNotAllowedCharacter(description) + "_" + jobId.getDbId() + "/");
//			System.out.println("creating output directory " + getOutputPath() + description.replaceAll(" ", "") + "_" + jobId.getDbId() + "/");
//			File outputDir = new File(getOutputPath() + description.replaceAll(" ", "") + "_" + jobId.getDbId() + "/");
			boolean success = outputDir.mkdir();
			if (!success) return; //TODO manage error
			logger.info("output directory created.");
//			System.out.println("output directory created.");

			// execute post-staging and cleanup
			try {
				if ( (adaptor.equals("ssh")) || (adaptor.equals("rocci"))) {
					synchronized (JSagaJobSubmission.class) {((JobImpl)job).postStagingAndCleanup();}
				}
				else
					((JobImpl)job).postStagingAndCleanup();
				if (adaptor.equals("bes-genesis2")) {
				
					Session session = SessionFactory.createSession(false);
					Context context = ContextFactory.createContext("UserPass");
					context.setAttribute("UserID", "chaindemo");
					context.setAttribute("UserPass", "chaindemo");
					session.addContext(context);
				
					org.ogf.saga.file.File file = FileFactory.createFile(session, URLFactory.createURL("sftp2://vm20.ct.infn.it:4422/home/chaindemo/stdout"), Flags.READ.getValue()); 
					logger.info("saving file...:"+file.getSize());
//					System.out.println("saving file...:"+file.getSize());
					file.copy(URLFactory.createURL("file://" + outputDir.getPath() + "/stdout"),Flags.NONE.getValue());
					file.close();
					session.close();
					
				}
				logger.info("Job output have been retrieved successfully (if it exists)");
//				System.out.println("Job output have been retrieved successfully (if it exists)");
			}
			catch(Exception exception) {
				logger.error("Error downloading output" + exception.toString());
//				System.out.println(exception.toString());
				exception.printStackTrace();
			}
			jobServiceManager.closeSession();
			logger.info("Current Dir=" + System.getProperty("user.dir"));
//			System.out.println("Current Dir=" + System.getProperty("user.dir"));

			GEActiveGridInteraction activeGridInteraction = GEActiveGridInteraction.findActiveGridInteractionByJobId(Long.valueOf(jobId.getDbId()));
			if(activeGridInteraction.getIdJobCollection()==null){
				//creating a tgz archive containing output files
				logger.info("creating a tgz archive containing output files...");
	//			System.out.println("creating a tgz archive containing output files...");
				logger.info("tar czvf "+ getOutputPath() + outputDir.getName()+".tgz --directory=" + getOutputPath() + " " + outputDir.getName());
	//			System.out.println("tar czvf "+ getOutputPath() + outputDir.getName()+".tgz " + getOutputPath() + outputDir.getName());
				Process creatingTar = Runtime.getRuntime().exec("tar czvf "+ getOutputPath() + outputDir.getName()+".tgz --directory=" + getOutputPath() + " " + outputDir.getName());
				creatingTar.waitFor();
				
				//deleting folder...
				String command = "rm -Rf " + getOutputPath() + outputDir.getName();
				logger.info(command);
	//			System.out.println(command);
				Process deleteFolder = Runtime.getRuntime().exec(command);
				deleteFolder.waitFor();
			}else{
				
				JobCollection activeCollection = JobCollection.getJobCollection(activeGridInteraction.getIdJobCollection());
				String outputCollectionDirName = removeNotAllowedCharacter(activeCollection.getDescription());
				File outputCollectionDir = new File(getOutputPath() + outputCollectionDirName + "_" + activeCollection.getId() +"/");
				if(!outputCollectionDir.exists()){
					logger.info("Creating a folder containing output directories...");
					boolean created = outputCollectionDir.mkdir();
					if (!created) 
						logger.error("Problems in creating folder: " + outputCollectionDir);
				}
				//moving folder...
				String command = "mv " + getOutputPath() + outputDir.getName() + " "+ getOutputPath() + outputCollectionDirName + "_" +activeCollection.getId() +"/";
				logger.info(command);
	//			System.out.println(command);
				Process moveFolder = Runtime.getRuntime().exec(command);
				moveFolder.waitFor();
			}
			
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
			jobServiceManager.closeSession();
			return;// "Unexpected state";
		}
		
	}
	
	/**
	 * Cancels a specified job execution.
	 * @param jobId grid job identifier for cancelling job.
	 * @return job status.
	 */
	public String cancelJob(JobId jobId) {
		String nativeJobId = getNativeJobId(jobId.getGridJobId());
		URL serviceURL = getServiceURL(jobId.getGridJobId());
		if ((nativeJobId==null) || (serviceURL==null)) return ""; //TODO manage error

		String adaptor = (serviceURL.toString()).substring(0, (serviceURL.toString()).indexOf(":"));
		logger.info("Using adaptor:"+adaptor);
//		System.out.println("Using adaptor:"+adaptor);
		
		if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram"))|| (adaptor.equals("gatekeeper")) || (adaptor.equals("gos")) || (adaptor.equals("rocci")) ) {
		}
		else if (adaptor.equals("unicore")) {
			if ( (jksPath.equals("")) || (jksPassword.equals("")) ) {
				logger.error("Error in cancelJob - No JKS defined.");
//				System.out.println("Error in getJobStatus - No JKS defined.");
				return "";
			}
		}
		else if (adaptor.equals("ourgrid")) {
			if ( (ourgridUserName.equals("")) || (ourgridPassword.equals("")) ) {
				logger.error("Error in cancelJob - No ourgrid credentials defined.");
//				System.out.println("Error in getJobStatus - No ourgrid credentials defined.");
				return "";
			}
		}
		else if (adaptor.equals("bes-genesis2")) {
			if ( (genesisJksPath.equals("")) || (genesisJksPassword.equals("")) ) {
				logger.error("Error in cancelJob - No Genesis JKS defined.");
//				System.out.println("Error in cancelJob - No Genesis JKS defined.");
				return "";
			}
		}
		else if (adaptor.equals("ssh")) {
//			if ( (SSHUserName.equals("")) || (SSHPassword.equals("")) ) {
			if ( (SSHUserName.equals(""))) {
				logger.error("Error in cancelJob - No SSH credentials defined.");
				return "";
			}
		}
		else {
			logger.error("Error in cancelJob - Adaptor not supported.");
//			System.out.println("Error in getJobStatus - Adaptor not supported.");
			return "";
		}
		
		try{
			logger.info("Getting JobService...");
//			System.out.println("Getting JobService...");

			JobService service = null;
			if ( (adaptor.equals("wms")) || (adaptor.equals("wsgram")) || (adaptor.equals("gatekeeper")) || (adaptor.equals("rocci"))) {
				service = jobServiceManager.getJobService(serviceURL);
			}
			else if (adaptor.equals("unicore")) {
				service = jobServiceManager.getJobService(jksPath,jksPassword,serviceURL);
			}
			else if (adaptor.equals("ourgrid")) {
				service = jobServiceManager.getOurGridJobService(ourgridUserName,ourgridPassword,serviceURL);
			}
			else if (adaptor.equals("bes-genesis2")) {
				service = jobServiceManager.getGenesisJobService(genesisJksPath,genesisJksPassword,serviceURL);
			}
			else if (adaptor.equals("gos")) {
				service = jobServiceManager.getGOSJobService(serviceURL);
			}
			else if (adaptor.equals("ssh")) {
				service = jobServiceManager.getSSHJobService(SSHUserName,SSHPassword,serviceURL);
			}
			
			logger.info("Getting Job...");
//			System.out.println("Getting Job...");
			Job job = service.getJob(nativeJobId);
			logger.info("Cancelling Job " + jobId.getGridJobId());
//			System.out.println("Cancelling Job " + jobId.getGridJobId());
			State state = null;
			if ( (adaptor.equals("ssh")) || (adaptor.equals("rocci"))) {
				synchronized (JSagaJobSubmission.class) {job.cancel(); 	job.waitFor(); job.getState();}
			}
			else {
				job.cancel();
				job.waitFor();
				job.getState();
			}

			// get status
			//State state = job.getState();

			jobServiceManager.closeSession();
		
			// display status
			if (State.RUNNING.compareTo(state) == 0) {
				return "RUNNING";
			} 
			else if (State.SUSPENDED.compareTo(state) == 0) {
				return "SUSPENDED";
			} else if (State.DONE.compareTo(state) == 0) {
				return "DONE";
			}
			else {
				DBInterface.CloseGridInteraction(jobId.getDbId());
				
				if (State.CANCELED.compareTo(state) == 0) {
					return "CANCELED";
				} else if (State.FAILED.compareTo(state) == 0) {
					//TODO review
					try {
						String exitCode = job.getAttribute(Job.EXITCODE);
						logger.info("Job failed with exit code: "+exitCode);
//						System.out.println("Job failed with exit code: "+exitCode);
						return "Aborted";
					} catch(NotImplementedException e) {
						logger.error("Job failed.");
//						System.out.println("Job failed.");
						return "Aborted";
					}
				} else {
					return "Unexpected state";
				}
			}
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
			return "";
		}

	}
	
	/**
	 * Returns service's URL for a spcified job.  
	 * 
	 * @param jobId the grid job identifier for which want retrieve service's URL.
	 * @return service's URL.
	 */
	public URL getServiceURL(String jobId) {
		URL serviceURL;
		Pattern pattern = Pattern.compile("\\[(.*)\\]-\\[(.*)\\]");
		Matcher matcher = pattern.matcher(jobId);
		try {
			if (matcher.find()) {
				serviceURL = URLFactory.createURL(matcher.group(1));
				logger.info("ServiceURL = "+serviceURL);
//				System.out.println("serviceURL="+serviceURL);
			} else {
				return null;
			}
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
			return null;
		}

		return serviceURL;
	}

	/**
	 * Returns job's native identifier.  
	 * 
	 * @param jobId the grid job identifier for which want retrieve job's native idefier.
	 * @return job's native idefier.
	 */
	public String getNativeJobId(String jobId) {
		String nativeJobId = "";
		Pattern pattern = Pattern.compile("\\[(.*)\\]-\\[(.*)\\]");
		Matcher matcher = pattern.matcher(jobId);
		try {
			if (matcher.find()) {
				nativeJobId = matcher.group(2);
				logger.info("nativeJobId="+nativeJobId);
//				System.out.println("nativeJobId="+nativeJobId);
			} else {
				return null;
			}
		}
		catch (Exception e) {
			logger.error(e.toString());
//			System.out.println(e.toString());
			return null;
		}

		return nativeJobId;
	}
	
	/**
	 * This method removes characthers that aren't allowed for the creation of a file system 
	 * path from the user description.
	 * 
	 * @param name user description of this job used to create a directory where store job output.
	 * @return string without not allowed characters.
	 */
	public static String removeNotAllowedCharacter(String name)
	{
		if(logger.isDebugEnabled())
			logger.debug("name1="+name);
//		System.out.println("name1="+name);
		name = name.replaceAll(" ", "");
		if(logger.isDebugEnabled())
			logger.debug("name2="+name);
//		System.out.println("name2="+name);
		name = name.replaceAll("[^A-Za-z0-9_.]*", "");
		if(logger.isDebugEnabled())
			logger.debug("name3="+name);
//		System.out.println("name3="+name);
		
		return name;
	}
	
	private void deleteSubmittingDescription(String userDescription, int dbId) {
		MailUtility m = null;
		logger.debug("Cannot submit job: "
				+ description.toString()
				+ ", deleteting descrisption if exits.");
//		if (jobCollectionId != null) {
//			JobCollection jc = JobCollection
//					.getJobCollection(jobCollectionId);
//			List<GEActiveGridInteraction> tmp = GEActiveGridInteraction.findActiveJobForJobCollection(jobCollectionId.intValue());
//			System.out.println("AAAAAAAAAAAAAA"+tmp.size());
//			if(tmp.size() == 1){
//				if (jc instanceof WorkflowN1) {
//					long idFinalJob = ((WorkflowN1) jc).getIdFinalJob();
//					GEJobDescription tmpDescr = new GEJobDescription();
//					tmpDescr = GEJobDescription.findJobDescriptionById(idFinalJob);
//					if (tmpDescr.getId() != null)
//						tmpDescr.delete();
//				}
//				if(!jc.getUserEmail().equals(""))
//					m = new MailUtility(jc.getUserEmail(), "", "", jc.getDescription(),  MailUtility.ContentMessage.SUBMISSION_ERROR);
//				jc.delete();
//			}
//		}
		
		if(!getUserEmail().equals("")){
			String address = getUserEmail();
			if(!getSenderEmail().equals("")){
				address = getSenderEmail()+"|"+getUserEmail();
			}
			m = new MailUtility(address, "", "", userDescription,  MailUtility.ContentMessage.SUBMISSION_ERROR);
		}
		
		if(m!=null)
			m.sendMail();
		description.delete();
		
		if(dbId!=0){
			GEActiveGridInteraction activeGridInteraction = GEActiveGridInteraction.findActiveGridInteractionByJobId((long)dbId);
			activeGridInteraction.delete();
		}
		
	}
	///*
	public static void main(String [] args)
	{
		//JSagaJobSubmission tmpJSaga1 = new JSagaJobSubmission("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
		//tmpJSaga1.removeNotAllowedCharacter("Job-1");
		//tmpJSaga1.removeNotAllowedCharacter("Stre:ss:: te%st job n. 1");
		
		int num_job = 1;
		//String bdiiCometa = "ldap://infn-bdii-01.ct.pi2s2.it:2170"; 
		String bdiiCometa = "ldap://gridit-bdii-01.cnaf.infn.it:2170"; 
		//String bdiiCometa = "ldap://bdii.eela.ufrj.br:2170";
		String wmsList[] = {"wms://wms-4.dir.garr.it:7443/glite_wms_wmproxy_server"//,
				//"wms://wms005.cnaf.infn.it:7443/glite_wms_wmproxy_server"//,
//			"wms://gridit-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server",
//			"wms://egee-rb-09.cnaf.infn.it:7443/glite_wms_wmproxy_server"};//,
//			"wms://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server",
//			/*"wms://wms013.cnaf.infn.it:7443/glite_wms_wmproxy_server",*/
//			"wms://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server"
				};
		String EUMEDwmsList[] = {"wms://wms.ulakbim.gov.tr:7443/glite_wms_wmproxy_server"};
		String bdiiEumed = "ldap://bdii.eumedgrid.eu:2170";
		String sshList[] = {"ssh://api.ct.infn.it"};
		//String CEs[] = {"grisuce.scope.unina.it", "ce-02.roma3.infn.it", "gridce3.pi.infn.it"};
		String CEs[] = {//"ce-02.roma3.infn.it:8443/cream-pbs-grid"
				//"grisuce.scope.unina.it:8443/cream-pbs-grisu_short", 
				//"cccreamceli09.in2p3.fr:8443/cream-sge-long"
				"ce-01.roma3.infn.it:8443/cream-pbs-grid"
				};
		
//		String OCCI_ENDPOINT_HOST = "rocci://carach5.ics.muni.cz";
//		String OCCI_ENDPOINT_PORT = "11443";        
//		String OCCI_AUTH = "x509";
        
        // Possible RESOURCE values: 'os_tpl', 'resource_tpl', 'compute'
//		String OCCI_RESOURCE = "compute";
		//String OCCI_RESOURCE_ID = "https://carach5.ics.muni.cz:11443/compute/a0ad539e-ad17-4309-bc9c-4f9f91aecbaa";
//		String OCCI_VM_TITLE = "MyDebianROCCITest";
        
        // Possible OCCI_OS values: 'debianvm', 'octave', 'r' and 'generic_www'
//		String OCCI_OS = "debianvm";        
//		String OCCI_FLAVOUR = "small";                

        // Possible ACTION values: 'list', 'describe', 'create' and 'delete'
//		String OCCI_ACTION = "create";    
//        String OCCI_PUBLIC_KEY = "/home/diego/.ssh/id_rsa.pub";
//        String OCCI_PRIVATE_KEY = "/home/diego/.ssh/id_rsa";
//        
//		String rOCCIURL = OCCI_ENDPOINT_HOST + ":" + 
//                 OCCI_ENDPOINT_PORT + 
//                 System.getProperty("file.separator") + "?" +
//                 "action=" + OCCI_ACTION + 
//                 "&resource=" + OCCI_RESOURCE +
//                 "&attributes_title=" + OCCI_VM_TITLE +
//                 "&mixin_os_tpl=" + OCCI_OS +
//                 "&mixin_resource_tpl=" + OCCI_FLAVOUR +
//                 "&auth=" + OCCI_AUTH +
//                 "&publickey_file=" + OCCI_PUBLIC_KEY +                                     
//                 "&privatekey_file=" + OCCI_PRIVATE_KEY;
//		
//		String rOCCIResourcesList[] = {rOCCIURL};
		//String wmsList[] = {"wms://infn-wms-01.ct.pi2s2.it:7443/glite_wms_wmproxy_server"//,
			//	"unicore://zam052v01.zam.kfa-juelich.de:8080/?Target=EMI-UNICOREX"
		//};
		
				//JSagaJobSubmission tmpJSaga = new JSagaJobSubmission();
		//JSagaJobSubmission tmpJSaga = new JSagaJobSubmission("jdbc:mysql://10.70.1.99/userstracking","tracking_user","usertracking");
		//String bdiiGilda = "ldap://egee-bdii.cnaf.infn.it:2170"; 
		//tmpJSaga.setBDII(bdiiGilda);
		//tmpJSaga.useRobotProxy("101", "gilda", "gilda", true);
//		tmpJSga.setJobOutput("myOutput.txt");
//		tmpJSaga.setJobError("myError.txt");
		System.out.println("#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#1#");
//		JobId[] newJobsId = new JobId[num_job];

//		/*SUBMISSION SPECIFYING JOB DESCRIPTION
		for(int i = 0; i<num_job; i++){
			GEJobDescription description = new GEJobDescription();
			description.setExecutable("/bin/sh");
			description.setArguments("hostname.sh");
			description.setInputFiles("/home/mario/Documenti/hostname.sh");
			description.setOutputFiles("output.README");
			description.setOutputPath("/tmp");
			description.setOutput("Output.txt");
			description.setError("Error.txt");
//			String jdlRequirements = "JDLRequirements=(Member(\"MPI-START\",other.GlueHostApplicationSoftwareRunTimeEnvironment));JDLRequirements=(Member(\"MPICH\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
//			description.setJDLRequirements(jdlRequirements);
			JSagaJobSubmission tmpJSaga = new JSagaJobSubmission("jdbc:mysql://localhost/userstracking","tracking_user","usertracking", description);
			tmpJSaga.setUserEmail("mario.torrisi@ct.infn.it");
			tmpJSaga.setSenderEmail("mario.torrisi@ct.infn.it");
//			tmpJSaga.setWMSList(rOCCIResourcesList);
//			tmpJSaga.useRobotProxy("etokenserver.ct.infn.it", "8082", "332576f78a4fe70a52048043e90cd11f", "fedcloud.egi.eu", "fedcloud.egi.eu", true);
//			tmpJSaga.setWMSList(sshList);
//			tmpJSaga.setWMSList(wmsList);
			tmpJSaga.setWMSList(EUMEDwmsList);
//			tmpJSaga.setCEList(CEs);
//			tmpJSaga.setRandomCE(true);
//			tmpJSaga.setSSHCredential("liferayadmin", "liferayadmin");
//			tmpJSaga.setSSHCredential("root", "Passw0rd!");
			tmpJSaga.useRobotProxy("etokenserver2.ct.infn.it", "8082", "bc779e33367eaad7882b9dfaa83a432c", "eumed", "eumed", true, true, "test");
//			tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "ssh://gilda-liferay-vm-06.ct.infn.it:5000", "SSH - Stress test job - "+i);
//			tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "ssh://api.ct.infn.it", "SSH - Stress test job - "+i);
//			tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "wms://wms-4.dir.garr.it:7443/glite_wms_wmproxy_server", "WMS - Stress test job - "+i);
			tmpJSaga.submitJobAsync("test", "193.206.208.183:8162", 1, "test job n. "+i);
		}
//			
		/*SUBMISSION WITHOUT SPECIFYING JOB DESCRIPTION
		for (int i=0;i<num_job;i++) {
			JSagaJobSubmission tmpJSaga = new JSagaJobSubmission("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
			tmpJSaga.setUserEmail("diego.scardaci@ct.infn.it");
		
			//tmpJSaga.setSSHCredential("root", "Passw0rd!");
			tmpJSaga.setExecutable("/bin/sh");
			tmpJSaga.setArguments("hostname.sh");
			tmpJSaga.setInputFiles("/home/mario/Documenti/hostname.sh");
			tmpJSaga.setOutputFiles("output.README");
			tmpJSaga.setOutputPath("/tmp");
			tmpJSaga.setJobOutput("myOutput.txt");
			tmpJSaga.setJobError("myError.txt");
			tmpJSaga.setWMSList(wmsList);
			tmpJSaga.useRobotProxy("etokenserver.ct.infn.it", "8082", "332576f78a4fe70a52048043e90cd11f", "gridit", "gridit", true);
			tmpJSaga.setJDLRequirements(new String[] {"JDLRequirements=(Member(\"MPI-START\",other.GlueHostApplicationSoftwareRunTimeEnvironment))","JDLRequirements=(Member(\"MPICH\", other.GlueHostApplicationSoftwareRunTimeEnvironment))" });
			tmpJSaga.submitJobAsync("test", "193.206.208.183:8162", 1, "test job n. "+i);
//			tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "ssh://api.ct.infn.it", "SSH - Stress test job - "+i);
		}
//		*/
//			tmpJSaga.setRandomCE(true);
			
//			//tmpJSaga.setUserEmail("mario.torrisi@ct.infn.it");
//			//tmpJSaga.setJobQueue("infn-ce-01.ct.pi2s2.it:8443/cream-lsf-short");
//			//tmpJSaga.setBDII(bdiiCometa);
//			//tmpJSaga.setWMSList(wmsList);
//			//tmpJSaga.setCEList(CEs);
//			//String selectedCE=tmpJSaga.getRandomCEFromCElist();
//			//tmpJSaga.setUserProxy("/tmp/proxy");
//			
//			//tmpJSaga.useRobotProxy("etokenserver.ct.infn.it", "8082", "332576f78a4fe70a52048043e90cd11f", "gridit", "gridit", true);
//			//tmpJSaga.setJKS("/home/diego/UnicoreOutput/robot2012.jks", "robot2012");
//			//tmpJSaga.setGenesisJKS("/home/diego/genesisII/genesis-keys.jks", "srt5mInfn");
//			//tmpJSaga.useRobotProxy("myproxy.ct.infn.it", "8082", "22002", "gridit", "gridit", true);
//			//tmpJSaga.useRobotProxy("myproxy.ct.infn.it", "8082", "21873", "prod.vo.eu-eela.eu", "prod.vo.eu-eela.eu", true);
//			//tmpJSaga.setOurGridCredential("diego", "scardaci");
////			tmpJSaga.setSPMDVariation("OpenMPI");
////			tmpJSaga.setTotalCPUCount("4");
////			tmpJSaga.setNumberOfProcesses("4");
////			tmpJSaga.setExecutable("diego_mpi_xn03");
////			tmpJSaga.setInputFiles("/home/diego/mpitest/diego_mpi_xn03");
//			
////		
//			tmpJSaga.setExecutable("lsf.sh");
//			tmpJSaga.setArguments("hostname.job");
//			tmpJSaga.setOutputPath("/tmp");
//			
////			tmpJSaga.setExecutable("/bin/hostname");
////			tmpJSaga.setArguments("-f");
////			tmpJSaga.setOutputPath("/tmp");
////			tmpJSaga.setJobOutput("myOutput-" + i + ".txt");
////			tmpJSaga.setJobError("myError-" + i + ".txt");
//			tmpJSaga.setJobOutput("output.txt");
//			tmpJSaga.setJobError("error.txt");
//			tmpJSaga.setInputFiles("/home/mario/Documenti/ls.sh,/home/mario/Documenti/hostname.sh,/home/mario/Documenti/pwd.sh");
//			//tmpJSaga.setOutputFiles("output.txt,error.txt");//,/home/diego/Enea/output/job.output<job.output,/home/diego/Enea/output/job.error<job.error,/home/diego/Enea/output/pwd.out<pwd.out");
////			tmpJSaga.setCheckJobsStatus(false);
//			
////			String jdlRequirements[] = new String[1];
////			jdlRequirements[0] = "JDLRequirements=(Member(\"Rank\", other.GlueCEStateFreeCPUs))";
////			tmpJSaga.setJDLRequirements(jdlRequirements);
//
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wms://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wms://prod-wms-01.pd.infn.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wms://infn-wms-01.ct.pi2s2.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wms://wms013.cnaf.infn.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wms://wms.eela.ufrj.br:7443/glite_wms_wmproxy_server", "Job-"+i);
////		    String descr = "paperino";
////			if ((i==1) || (i==3) || (i==4))
////				descr = "pippo";
////			tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "unicore://zam052v01.zam.kfa-juelich.de:8080/?Target=EMI-UNICOREX", "UNICORE - Stress test job n. "+i);
//			//tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "bes-genesis2://xcg-server1.uvacse.virginia.edu:20443/axis/services/GeniiBESPortType?genii-container-id=93B641B7-9422-EA4C-A90B-CA6A9D98E344", "GenesisII - Stress test job n. "+i);
//				
//			//tmpJSaga.submitJob("scardaci", "193.206.208.183:8162", 1, "gLite - Stress test job n. "+i);
////			if ((i%3)==0)
//				//tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "gLite - Stress test job n. "+i);
////			else if ((i%3)==1)
////				tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "wsgram://xn03.ctsf.cdacb.in:8443/PBS", "GARUDA - Stress test job n. "+i);
////			else if ((i%3)==2)
////				tmpJSaga.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "ourgrid://api.ourgrid.org", "OurGrid - Stress test job n. "+i);
//			
//	//		tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "ssh://cresco1-f1.portici.enea.it", "SSH - Stress test job n. "+i);
//			tmpJSaga.submitJobAsync("mtorrisi", "193.206.208.183:8162", 1, "ssh://gilda-liferay-vm-06.ct.infn.it:5000", "SSH - Stress test job n. "+i, null, null);
//			
////			try {
////				Thread.sleep(180000);
////			}
////			catch (Exception e) {}
//			//newJobsId[i] = tmpJSaga.submitJob("ricceri", "193.206.208.183:8162", 1, "Job-"+i);
//			//newJobsId[i] = tmpJSaga.submitJob("scardaci", "193.206.208.183:8162", 1, "Job-"+i);
//			//newJobsId[i] = tmpJSaga.submitJob("scardaci", "193.206.208.183:8162", 1, "wms://gilda-wms-02.ct.infn.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//newJobsId[i] = tmpJSaga.submitJob("scardaci", "193.206.208.183:8162", 1, "wms://infn-wms-01.ct.pi2s2.it:7443/glite_wms_wmproxy_server", "Job-"+i);
//			//System.out.println("#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#2#");
//			//System.out.println(newJobsId[i].getGridJobId());
//			//System.out.println(newJobsId[i].getDbId());
////			try {
////				Thread.sleep(10000);
////			}
////			catch (Exception e) {}
////			
//		}
		
		System.out.println("Vado in sleep...");
		try {
			Thread.sleep(60000);
		}
		catch (Exception e) {}
		
		System.out.println("Checking jobs...");
		
		UsersTrackingDBInterface DBInterface = new UsersTrackingDBInterface("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
		DBInterface.setJobsUpdatingInterval(30);
		String status = "RUNNING";
		Vector<ActiveInteractions> jobList = null;
//		Vector<String[]> cesListwithCoord = null;
		
		
//		if (!DBInterface.isUpdateJobsStatusAsyncRunning("ViralGrid", "scardaci")) {
//			System.out.println("1- Running checking thread...");
//			DBInterface.updateJobsStatusAsync2("ViralGrid","scardaci","/tmp");
//		}
//		
//		if (!DBInterface.isUpdateJobsStatusAsyncRunning("ViralGrid", "scardaci")) {
//			System.out.println("2- Running checking thread...");
//			DBInterface.updateJobsStatusAsync2("ViralGrid","scardaci","/tmp");
//		}
		
		while(status.equals("RUNNING")) {
			
			
			//DBInterface.updateJobsStatus("ViralGrid","scardaci");
			
			try {
				Thread.sleep(15000);
			}
			catch (Exception e) {}
		
			status = "DONE";
			jobList = DBInterface.getActiveInteractionsByName("test");
			
			if (jobList != null) {
				for (int i = 0; i < jobList.size(); i++) {
					
					if(jobList.get(i).getSubJobs()==null){
					
					System.out.println("DBID = " + jobList.elementAt(i).getInteractionInfos()[0]
							+ " Portal = " + jobList.elementAt(i).getInteractionInfos()[1]
							+ " - Application = " + jobList.elementAt(i).getInteractionInfos()[2]
							+ " - Description = " +jobList.elementAt(i).getInteractionInfos()[3]
							+ " - Timestamp = " + jobList.elementAt(i).getInteractionInfos()[4]
							+ " - Status = " + jobList.elementAt(i).getInteractionInfos()[5]);
					
					} else {
						System.out.println("***COLLECTION INFOS*** DBID = " + jobList.elementAt(i).getInteractionInfos()[0]
								+ " Portal = " + jobList.elementAt(i).getInteractionInfos()[1]
								+ " - Application = " + jobList.elementAt(i).getInteractionInfos()[2]
								+ " - Description = " +jobList.elementAt(i).getInteractionInfos()[3]
								+ " - Timestamp = " + jobList.elementAt(i).getInteractionInfos()[4]
								+ " - Status = " + jobList.elementAt(i).getInteractionInfos()[5]);
						Vector<String[]> subJobs = jobList.get(i).getSubJobs();
						for(String[] subJobInfos : subJobs)
							System.out.println("\t|_***SUBJOB INFOS*** DBID = " + subJobInfos[0]
									+ " Portal = " + subJobInfos[1]
									+ " - Application = " + subJobInfos[2]
									+ " - Description = " + subJobInfos[3]
									+ " - Timestamp = " + subJobInfos[4]
									+ " - Status = " + subJobInfos[5]);
					}
					if (!jobList.elementAt(i).getInteractionInfos()[5].equals("DONE"))
						status = "RUNNING";
				}
			}
			
//			cesListwithCoord = DBInterface.getCEsGeographicDistribution("ViralGrid","scardaci");
//			
//			if (cesListwithCoord!=null) {
//				for (int i=0;i<cesListwithCoord.size();i++) {
//					System.out.println("CE = " + cesListwithCoord.elementAt(i)[0] + " Num Jobs = " + cesListwithCoord.elementAt(i)[1] + " - Lat = " + cesListwithCoord.elementAt(i)[2] + " - Long = " + cesListwithCoord.elementAt(i)[3]);
//				}
//			}
			
			
//			if (!DBInterface.isUpdateJobsStatusAsyncRunning("ViralGrid", "scardaci")) {
//				System.out.println("3- Running checking thread...");
//				DBInterface.updateJobsStatusAsync2("ViralGrid","scardaci","/tmp");
//			}
			
			if (status.equals("RUNNING")) {
				try {
					Thread.sleep(15000);
				}
				catch (Exception e) {}
			}
			
		}
		
		String allTarPath = DBInterface.createAllJobsArchive("mtorrisi","/tmp");
		System.out.println("allTarPath="+allTarPath);
//		String allTarPathForDesc = DBInterface.createAllJobsFromDescriptionArchive("scardaci","pippo","/tmp");
//		System.out.println("allTarPathForDesc="+allTarPathForDesc);
		
//		for (int i=0;i<num_job;i++) {
//			String outputFile = "";
//			outputFile = tmpJSaga.getJobOutput(newJobsId[i].getDbId());
//			System.out.println("outputFile="+outputFile);
//		}
		
		Vector<ActiveInteractions> jobList1 = null;
		jobList1 = DBInterface.getActiveInteractionsByName("mtorrisi");
//		for (int i=0;i<jobList1.size();i++) {
//			JSagaJobSubmission tmpJSaga = new JSagaJobSubmission("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
//			//tmpJSaga.useRobotProxy("21174", "cometa", "cometa", true);
//			tmpJSaga.setOutputPath("/tmp/");
//			String outputFile = "";
//			outputFile = tmpJSaga.getJobOutput(new Integer(jobList.elementAt(i)[0]).intValue());
//			System.out.println("outputFile="+outputFile);
//		}
		
		if (jobList1.size()==0) System.out.println("No jobs for user mtorrisi");
//		for (int i=0;i<jobList1.size();i++) {
//			System.out.println("Portal = " + jobList1.elementAt(i)[1] + " - Application = " + jobList1.elementAt(i)[2] + " - Description = " + jobList1.elementAt(i)[3] + " - Timestamp = " + jobList1.elementAt(i)[4] + " - Status = " + jobList1.elementAt(i)[5] );
//		}
		
//		if (!DBInterface.isUpdateJobsStatusAsyncRunning("ViralGrid", "scardaci")) {
//			System.out.println("4- Running checking thread...");
//			DBInterface.updateJobsStatusAsync2("ViralGrid","scardaci","/tmp");
//		}
		
//		Vector<String[]> jobList2 = null;
//		jobList2 = DBInterface.getDoneJobsListByName("scardaci");
//		for (int i=0;i<jobList2.size();i++) {
//			System.out.println("DONE JOB - Portal = " + jobList2.elementAt(i)[1] + " - Application = " + jobList2.elementAt(i)[2] + " - Description = " + jobList2.elementAt(i)[3] + " - Timestamp = " + jobList2.elementAt(i)[4]);
//		}
		System.exit(0);
	}

}

