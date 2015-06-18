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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import it.infn.ct.GridEngine.JobService.JobServicesDispatcher;

import org.apache.log4j.Logger;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;

public class JobServiceManager {

	// Session session = null;
	private JobServicesDispatcher jobServicesDispatcher;// =
														// JobServicesDispatcher.getInstance();
	private URL ResourceManager = null;
	private boolean usingRobotProxy = false;
	private boolean usingJKS = false;
	private boolean usingOurGrid = false;
	private boolean usingGenesisJKS = false;
	private boolean usingGOS = false;
	private boolean usingSSH = false;
	private String userProxy = ""; // for example /tmp/x509up_u500

	String etokenServer = "";
	String etokenServerPort = "";
	private String ProxyId = "";
	private String DN = "";
	private String VO = "";
	private String FQAN = "";
	boolean ProxyRenewal = true;
	boolean RFC = false;

	private String JKSPath = "";
	private String JKSPassword = "";

	private String OurGridUserName = "";
	private String OurGridPassword = "";

	private String genesisJKSPath = "";
	private String genesisJKSPassword = "";

	private String SSHUserName = "";
	private String SSHPassword = "";

	private int resubNumber = 0;

	private static final Logger logger = Logger
			.getLogger(JobServiceManager.class);

	protected JobServiceManager() {
		try {
			jobServicesDispatcher = InitialContext
					.<JobServicesDispatcher> doLookup("JobServices-Dispatcher");
			logger.info("Got JobServices-Dispatcher...");

		} catch (NamingException ex) {
			logger.info("Cannot get JobServices-Dispatcher: " + ex);

		} catch (Exception ex) {
			logger.info("Cannot get JobServices-Dispatcher: " + ex);

		}

		if (jobServicesDispatcher == null) {
			logger.info("Get local JobServicesDispatcher");

			jobServicesDispatcher = JobServicesDispatcher.getInstance();
		}

		setResubNumber(jobServicesDispatcher.getResubNumber());
	}

	/**
	 * Returns the maximum attempts re-submission numbers.
	 * 
	 * @return the maximum attempts re-submission numbers.
	 */
	protected int getResubNumber() {
		return resubNumber;
	}

	/**
	 * Sets the maximum attempts re-submission numbers.
	 * 
	 * @param value
	 *            the maximum attempts re-submission numbers.
	 */
	protected void setResubNumber(int resubNumber) {
		this.resubNumber = resubNumber;
	}

	/**
	 * This method specifies that for interaction will be used the specified
	 * robot proxy.
	 * 
	 * @param etokenserver
	 *            proxy robot host
	 * @param etokenserverport
	 *            proxy robot port
	 * @param proxyId
	 *            proxy robot identifier
	 * @param vo
	 *            proxy robot virtual organization
	 * @param fqan
	 *            proxy robot roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 */
	protected void useRobotProxy(String etokenserver, String etokenserverport,
			String proxyId, String vo, String fqan, boolean proxyrenewal) {
		usingRobotProxy = true;

		useRobotProxy(etokenserver,etokenserverport,proxyId,vo,fqan,proxyrenewal,false);
	}
	
	/**
	 * This method specifies that for interaction will be used the specified
	 * robot proxy.
	 * 
	 * @param etokenserver
	 *            proxy robot host
	 * @param etokenserverport
	 *            proxy robot port
	 * @param proxyId
	 *            proxy robot identifier
	 * @param vo
	 *            proxy robot virtual organization
	 * @param fqan
	 *            proxy robot roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	protected void useRobotProxy(String etokenserver, String etokenserverport,
			String proxyId, String vo, String fqan, boolean proxyrenewal, boolean rfc) {
		usingRobotProxy = true;

		etokenServer = etokenserver;
		etokenServerPort = etokenserverport;
		ProxyId = proxyId;
		VO = vo;
		FQAN = fqan;
		ProxyRenewal = proxyrenewal;
		RFC = rfc;
	}

	/**
	 * This method specifies that for interaction will be used the specified
	 * robot proxy.
	 * 
	 * @param proxyId
	 *            proxy robot identifier
	 * @param vo
	 *            proxy robot virtual organization
	 * @param fqan
	 *            proxy robot roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 */
	protected void useRobotProxy(String proxyId, String vo, String fqan,
			boolean proxyrenewal) {
		usingRobotProxy = true;

		useRobotProxy(proxyId,vo,fqan,proxyrenewal,false);
	}

	/**
	 * This method specifies that for interaction will be used the specified
	 * robot proxy.
	 * 
	 * @param proxyId
	 *            proxy robot identifier
	 * @param vo
	 *            proxy robot virtual organization
	 * @param fqan
	 *            proxy robot roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	protected void useRobotProxy(String proxyId, String vo, String fqan,
			boolean proxyrenewal, boolean rfc) {
		usingRobotProxy = true;

		ProxyId = proxyId;
		VO = vo;
		FQAN = fqan;
		ProxyRenewal = proxyrenewal;
		RFC = rfc;
	}

	/**
	 * This method specifies that for interaction will be used the specified
	 * User proxy.
	 * 
	 * @param value
	 *            path to a user proxy certificate
	 */
	protected void setUserProxy(String value) {
		userProxy = value;

		if ((!usingRobotProxy) && (!value.equals(""))) {
			Session session_temp;
			Context context;
			try {
				logger.info("Creating session....");

				session_temp = SessionFactory.createSession(false);
				logger.info(ContextImpl.URL_PREFIX);

				context = ContextFactory.createContext("VOMS");
				context.setAttribute(Context.USERPROXY, userProxy);
				session_temp.addContext(context);

				DN = context.getAttribute(Context.USERID);
				logger.info("DN=" + DN);

				// if (!usingRobotProxy) {
				VO = context.getAttribute(Context.USERVO);
				logger.info("VO=" + VO);
				// System.out.println("VO="+VO);
				FQAN = context.getAttribute("UserFQAN");
				logger.info("FQAN" + FQAN);
				// System.out.println("FQAN"+FQAN);
				// }

				session_temp.close();
			} catch (Exception e) {
				logger.error("Error in reading user proxy...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());

			}
		}

	}

	/**
	 * Returns the user proxy path used for this interaction.
	 * 
	 * @return user proxy path used for this interaction.
	 */
	protected String getUserProxy() {
		return userProxy;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager.
	 * 
	 * @param resourceManager
	 *            resource manager host
	 * @return {@link JobService} for the specified resource manager.
	 */
	protected JobService getJobService(URL resourceManager) {
		if ((!usingRobotProxy) && (userProxy.equals("")))
			return null;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			if (!usingRobotProxy) {
				jobservice = jobServicesDispatcher.getJobService(userProxy, DN,
						VO, FQAN, resourceManager.toString());
				// session = jobservice.getSession();
			} else {
				jobservice = jobServicesDispatcher.getJobService(etokenServer,
						etokenServerPort, ProxyId, VO, FQAN, ProxyRenewal, RFC,
						resourceManager.toString());
				DN = jobServicesDispatcher.getDN(jobservice);
				// session = jobservice.getSession();
				//
				// Context[] contexts = session.listContexts();
				// String proxyPath =
				// contexts[0].getAttribute(Context.USERPROXY);
				// setUserProxy(proxyPath);
				// System.out.println("proxyPath="+proxyPath);
			}

		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());

		}
		return jobservice;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager using
	 * UNICORE middleware.
	 * 
	 * @param jksPath
	 *            keystore path
	 * @param jskPassword
	 *            keystore password
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object that allows job creation, submission and
	 *         status monitoring using UNICORE middleware.
	 */
	protected JobService getJobService(String jksPath, String jskPassword,
			URL resourceManager) {

		usingJKS = true;
		JKSPath = jksPath;
		JKSPassword = jskPassword;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			jobservice = jobServicesDispatcher.getJKSJobService(jksPath,
					jskPassword, resourceManager.toString());
			DN = jobServicesDispatcher.getDN(jobservice);
		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());

		}
		return jobservice;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager using
	 * OURGRID middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param password
	 *            password parameter
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for OURGRID middleware
	 * 
	 */
	protected JobService getOurGridJobService(String username, String password,
			URL resourceManager) {

		usingOurGrid = true;
		OurGridUserName = username;
		OurGridPassword = password;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			jobservice = jobServicesDispatcher.getOurGridJobService(username,
					password, resourceManager.toString());
			// DN = jobServicesDispatcher.getDN(jobservice);
		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());

		}
		return jobservice;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager using
	 * BESGENESIS middleware.
	 * 
	 * @param jksPath
	 *            keystore path
	 * @param jskPassword
	 *            keystore password
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for BESGENESIS middleware
	 */
	protected JobService getGenesisJobService(String jksPath, String jskPassword,
			URL resourceManager) {

		usingGenesisJKS = true;
		genesisJKSPath = jksPath;
		genesisJKSPassword = jskPassword;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			jobservice = jobServicesDispatcher.getGenesisJobService(jksPath,
					jskPassword, resourceManager.toString());
			DN = jobServicesDispatcher.getDN(jobservice);
		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());

		}
		return jobservice;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager using GOS
	 * middleware.
	 * 
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for GOS middleware
	 */
	protected JobService getGOSJobService(URL resourceManager) {

		usingGOS = true;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			jobservice = jobServicesDispatcher.getGOSJobService(resourceManager
					.toString());
			// DN = jobServicesDispatcher.getDN(jobservice);
		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());

		}
		return jobservice;
	}

	/**
	 * Returns a {@link JobService} for the specified resource manager using SSH
	 * middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param password
	 *            password parameter
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for SSH middleware
	 */
	protected JobService getSSHJobService(String username, String password,
			URL resourceManager) {

		usingSSH = true;
		SSHUserName = username;
		SSHPassword = password;

		JobService jobservice = null;
		ResourceManager = resourceManager;

		try {
			jobservice = jobServicesDispatcher.getSSHJobService(username,
					password, resourceManager.toString());
		} catch (Exception e) {
			logger.error("Error in getting job service:" + e.toString());
		}
		return jobservice;
	}

	/**
	 * This method closes 
	 */
	protected void closeSession() {
		if (usingJKS) {
			jobServicesDispatcher.closeJKSJobService(JKSPath,
					ResourceManager.toString());
			return;
		} else if (usingOurGrid) {
			jobServicesDispatcher.closeOurGridJobService(OurGridUserName,
					ResourceManager.toString());
			return;
		} else if (usingGenesisJKS) {
			jobServicesDispatcher.closeGenesisJobService(genesisJKSPath,
					ResourceManager.toString());
			return;
		} else if (usingGOS) {
			jobServicesDispatcher
					.closeGOSJobService(ResourceManager.toString());
			return;
		} else if (usingSSH) {
			jobServicesDispatcher.closeSSHJobService(SSHUserName,
					ResourceManager.toString());
			return;
		}

		if (usingRobotProxy)
			jobServicesDispatcher.closeJobService(usingRobotProxy, ProxyId, VO,
					FQAN, ResourceManager.toString());
		else
			jobServicesDispatcher.closeJobService(usingRobotProxy, DN, VO,
					FQAN, ResourceManager.toString());
	}

	protected String getUserDN() {
		// if (userProxy.equals("")) return "";

		return DN;
	}

	protected String getUserVO() {
		return VO;
	}

	protected String getUserFQAN() {
		return FQAN;
	}

	protected String getProxyId() {
		// if (session==null) return ""; //TODO manage error

		if (usingRobotProxy) {
			return ProxyId;
		}

		return "";
	}

	protected String getJKSPath() {
		return JKSPath;
	}

	protected String getJKSPassword() {
		return JKSPassword;
	}

	protected String getOurGridUserName() {
		return OurGridUserName;
	}

	protected String getOurGridPassword() {
		return OurGridPassword;
	}

	protected String getGenesisJKSPath() {
		return genesisJKSPath;
	}

	protected String getGenesisJKSPassword() {
		return genesisJKSPassword;
	}

	protected String getSSHUserName() {
		return SSHUserName;
	}

	protected String getSSHPassword() {
		return SSHPassword;
	}

}
