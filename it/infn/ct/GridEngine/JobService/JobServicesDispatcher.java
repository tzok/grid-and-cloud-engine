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

package it.infn.ct.GridEngine.JobService;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;

public class JobServicesDispatcher {

	static final Hashtable<String, JobService> jobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> jobServicesCounter = new Hashtable<JobService, Integer>();
	static final Hashtable<Session, Long> sessionsTime = new Hashtable<Session, Long>();

	static final Hashtable<String, JobService> jksJobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> jksJobServicesCounter = new Hashtable<JobService, Integer>();

	static final Hashtable<String, JobService> ourgridJobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> ourgridJobServicesCounter = new Hashtable<JobService, Integer>();

	static final Hashtable<String, JobService> genesisJobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> genesisJobServicesCounter = new Hashtable<JobService, Integer>();

	static final Hashtable<String, JobService> gosJobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> gosJobServicesCounter = new Hashtable<JobService, Integer>();

	static final Hashtable<String, JobService> sshJobServices = new Hashtable<String, JobService>();
	static final Hashtable<JobService, Integer> sshJobServicesCounter = new Hashtable<JobService, Integer>();

	private static JobServicesDispatcher instance;
	private static int retryCount = 3;
	private static int resubNumber = 10;
	private static String myproxyServers = "gridit=myproxy.ct.infn.it";
	private static String trustStorePath = "/tmp/EMI-truststore.jks";
	private static String trustStorePassword = "goodpass";

	private static String genesisTrustStorePath = "/opt/glassfish3/glassfish/domains/liferay/config/trust-genesis.jks";
	private static String genesisTrustStorePassword = "chaindemo";

	private static String smtpServer = "mbox.ct.infn.it";
	private static String senderAddress = "sg-licence@ct.infn.it";
	
	private static final Logger logger = Logger
			.getLogger(JobServicesDispatcher.class);

	private JobServicesDispatcher() {

	}

	/**
	 * Returns the unique instance of this class if it isn't null otherwise
	 * creates a new instance.
	 * 
	 * @return the unique instance of this class.
	 */
	public synchronized static JobServicesDispatcher getInstance() {
		if (instance == null) {
			instance = new JobServicesDispatcher();
		}

		return instance;
	}

	public synchronized void setSmtpServer(String smtpserver) {
		smtpServer = smtpserver;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public synchronized void setSenderAddress(String senderaddress) {
		senderAddress = senderaddress;
	}

	protected synchronized void setRetryCount(int retrycount) {
		retryCount = retrycount;
	}

	protected synchronized void setMyproxyServers(String myproxyservers) {
		myproxyServers = myproxyservers;
	}

	protected synchronized void setTrustStorePath(String truststorepath) {
		trustStorePath = truststorepath;
	}

	protected synchronized void setTrustStorePassword(String truststorepassword) {
		trustStorePassword = truststorepassword;
	}

	protected synchronized void setGenesisTrustStorePath(String truststorepath) {
		genesisTrustStorePath = truststorepath;
	}

	protected synchronized void setGenesisTrustStorePassword(
			String truststorepassword) {
		genesisTrustStorePassword = truststorepassword;
	}

	protected void setResubmissionNumber(int pvalue) {
		resubNumber = pvalue;
	}

	/**
	 * Returns the maximum attempts re-submission numbers.
	 * 
	 * @return the maximum attempts re-submission numbers.
	 */
	public int getResubNumber() {
		return resubNumber;
	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using an user proxy.
	 * 
	 * @param proxyPath
	 *            path to a user proxy certificate
	 * @param DN
	 *            User ID or user name to use
	 * @param vo
	 *            proxy virtual organization
	 * @param fqan
	 *            proxy roles
	 * @param resourceManager
	 *            resource manager host
	 * @return JobService that allows job creation, submission and status
	 *         monitoring using a user proxy.
	 */
	public synchronized JobService getJobService(String proxyPath, String DN,
			String vo, String fqan, String resourceManager) {
		logger.info("GetJobService Local Proxy...");

		String proxyInfo = DN + "-" + vo + "-" + fqan + "-" + resourceManager;
		JobService service = jobServices.get(proxyInfo);

		String adaptor = (resourceManager.toString()).substring(0,
				(resourceManager.toString()).indexOf(":"));
		logger.info("JobServiceDispatcher: Using adaptor:" + adaptor);

		if (service == null) {
			logger.info("Creating a new Session...");

			Session session = null;
			Context context = null;
			try {
				logger.info(("Initialize the security context for the adaptor " + adaptor));
				session = SessionFactory.createSession(false);
				logger.info(ContextImpl.URL_PREFIX);

				context = ContextFactory.createContext("VOMS");
				context.setAttribute(Context.USERPROXY, proxyPath);
				//if (adaptor.equals("rocci")) context.setVectorAttribute("BaseUrlIncludes", new String[]{"rocci://"});
				
				if (adaptor.equals("wms")) {
					// context.setVectorAttribute("JobServiceAttributes", new
					// String[]{"wms.RetryCount="+retryCount});
					// context.setVectorAttribute("JobServiceAttributes", new
					// String[]{"wms.RetryCount="+retryCount,"wms.rank=other.GlueCEStateFreeCPUs"});
					context.setVectorAttribute("JobServiceAttributes",
							new String[] {
									"wms.RetryCount=" + retryCount,
									"wms.rank=other.GlueCEStateFreeCPUs",
									"wms.myproxyserver="
											+ getMyproxyServerForVO(vo) });
				} else if(adaptor.equals("rocci")) {
					context = ContextFactory.createContext("SSH");
					context.setAttribute(Context.USERID,"root");
					//context.setAttribute(Context.USERPASS,"");
					context.setAttribute(Context.USERCERT,
							System.getProperty("user.home") + 
							System.getProperty("file.separator") + 
							".ssh/id_rsa.pub");
					//Put here your private key file path
					context.setAttribute(Context.USERKEY,
							System.getProperty("user.home") + 
							System.getProperty("file.separator") + 
							".ssh/id_rsa");
					//context.setAttribute("UrlPrefix","sftp");
					context.setVectorAttribute("BaseUrlIncludes", new String[]{"sftp://"});
					context.setVectorAttribute("DataServiceAttributes", new String[]{"sftp.KnownHosts="});
					//session.addContext(context);
				} else if(adaptor.equals("gatekeeper")){
					
					//Modifiy this section according to the A&A schema of your middleware
		            //In this example the VOMS A&A schema is used
		            context = ContextFactory.createContext("VOMS");
		            context.setAttribute(Context.USERPROXY, proxyPath);
		            logger.info(("PROXY PATH: " + proxyPath));
		            logger.info(("PROXY PATH CONTEXT: " + context.getAttribute(Context.USERPROXY)));
		            // example code to get a GSSCredential object
		            File proxyFile = new File(proxyPath);
		            byte [] proxyBytes = new byte[(int) proxyFile.length()];
		            FileInputStream in = new FileInputStream(proxyFile);
		            in.read(proxyBytes);
		            in.close();

		            ExtendedGSSManager manager =
		                    (ExtendedGSSManager) ExtendedGSSManager.getInstance();

		            GSSCredential cred =
		                    manager.createCredential(
		                        proxyBytes,
		                        ExtendedGSSCredential.IMPEXP_OPAQUE,
		                        GSSCredential.DEFAULT_LIFETIME,
		                        null, // use default mechanism: GSI
		                        GSSCredential.INITIATE_AND_ACCEPT);

		            // code to pass the GSSCredential object to JSAGA
		            context.setAttribute("UserProxyObject",	fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential.toBase64(cred));
					
				}
				
				session.addContext(context);

			} catch (Exception e) {
				logger.error("Error in reading user proxy...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = null;
				if (adaptor.equals("rocci"))
					serviceURL = URLFactory.createURL(resourceManager+"&proxy_path="+proxyPath);
				else
					serviceURL = URLFactory.createURL(resourceManager);
				
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			jobServices.put(proxyInfo, service);
			jobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");
			Integer counter = jobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! JobServicesCounter disaligned!");

			jobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + jobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ jobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Returns user ID or user name used in a specified service.
	 * 
	 * @param service
	 *            for which you want to retrieve user ID or user name.
	 * @return user ID or user name used in a specified service.
	 */
	public synchronized String getDN(JobService service) {
		Session session_temp = null;
		String DN = "";
		try {
			session_temp = service.getSession();
			Context[] contexts = session_temp.listContexts();
			DN = contexts[0].getAttribute(Context.USERID);
			logger.info("DN=" + DN);

		} catch (Exception e) {
			logger.error("Error in reading user proxy...");
			logger.error(e.toString());
			logger.error("Cause :" + e.getCause());
		}
		return DN;
	}
	
	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using a robot proxy.
	 * 
	 * @param etokenserver
	 *            robot proxy host
	 * @param etokenserverport
	 *            robot proxy port
	 * @param proxyId
	 *            robot proxy identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param resourceManager
	 *            resource manager host
	 * @return JobService that allows job creation, submission and status
	 *         monitoring using a user proxy.
	 */
//	public synchronized JobService getJobService(String etokenserver,
//			String etokenserverport, String proxyId, String vo, String fqan,
//			boolean proxyrenewal, String resourceManager) {
//		return getJobService(etokenserver,etokenserverport,proxyId,vo,fqan,proxyrenewal,false,resourceManager);
//	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using a robot proxy.
	 * 
	 * @param etokenserver
	 *            robot proxy host
	 * @param etokenserverport
	 *            robot proxy port
	 * @param proxyId
	 *            robot proxy identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param resourceManager
	 *            resource manager host
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 * @return JobService that allows job creation, submission and status
	 *         monitoring using a user proxy.
	 */
	public synchronized JobService getJobService(String etokenserver,
			String etokenserverport, String proxyId, String vo, String fqan,
			boolean proxyrenewal, boolean rfc, String resourceManager) {
		logger.info("GetJobService RobotProxy...");
		int oldCounter = 0;
		String proxyInfo = proxyId + "-" + vo + "-" + fqan + "-"
				+ resourceManager;
		JobService service = jobServices.get(proxyInfo);

		String adaptor = (resourceManager.toString()).substring(0,
				(resourceManager.toString()).indexOf(":"));
		logger.info("JobServiceDispatcher: Using adaptor:" + adaptor);

		if (service != null) {
			boolean proxyIsExpired = false;

			try {
				proxyIsExpired = checkProxyLifetime(service.getSession());
			} catch (Exception e) {
				proxyIsExpired = true;
			}

			if (proxyIsExpired) {
				logger.warn("Proxy is expired! Getting a new proxy...");
				logger.info("Deleting old job service...");

				oldCounter = jobServicesCounter.get(service);
				logger.info("Old counter = " + oldCounter);

				jobServices.remove(proxyInfo);
				jobServicesCounter.remove(service);

				Context[] contexts;
				String proxyToBeDeleted = "";
				Session session = null;
				try {
					session = service.getSession();
					contexts = session.listContexts();
					proxyToBeDeleted = contexts[0]
							.getAttribute(Context.USERPROXY);
					session.close();
				} catch (Exception e) {
					logger.error(e.toString());
					logger.error("Cause :" + e.getCause());
					return null;
				}
				deleteProxy(proxyToBeDeleted);

				sessionsTime.remove(session);

				service = null;

				//
				//
				// RobotProxy robotProxy = null;
				// if ( (!etokenserver.equals("")) ||
				// (!etokenserverport.equals("")) )
				// robotProxy = new RobotProxy(etokenserver, etokenserverport,
				// proxyId, vo, fqan, proxyrenewal);
				// else
				// robotProxy = new RobotProxy(proxyId, vo, fqan, proxyrenewal);
				//
				// String proxyPath = robotProxy.getRobotProxy();
				// System.out.println("proxyPath2="+proxyPath);
				//
				// Context context = null;
				// try {
				// //contexts[0].setAttribute(Context.USERPROXY,proxyPath);
				// session = SessionFactory.createSession(false);
				// context = ContextFactory.createContext("VOMS");
				// context.setAttribute(Context.USERPROXY,proxyPath);
				//
				// context.setVectorAttribute("JobServiceAttributes", new
				// String[]{"wms.RetryCount="+retryCount});
				// session.addContext(context);
				// System.out.println("Added new security context in session.");
				// } catch (Exception e) {
				// System.out.println(e.toString());
				// System.out.println("Cause :"+e.getCause());
				// return null;
				// }
				//
				// try {
				// sessionsTime.put(session,
				// Calendar.getInstance().getTimeInMillis());
				// }
				// catch (Exception e) {
				// System.out.println(e.toString());
				// System.out.println("Cause :"+e.getCause());
				// return null;
				// }

				// deleteProxy(proxyToBeDeleted);

			}
		}

		if (service == null) {
			logger.info("Getting a new proxy...");

			RobotProxy robotProxy = null;
			if ((!etokenserver.equals("")) || (!etokenserverport.equals("")))
				robotProxy = new RobotProxy(etokenserver, etokenserverport,
						proxyId, vo, fqan, proxyrenewal, rfc);
			else
				robotProxy = new RobotProxy(proxyId, vo, fqan, proxyrenewal, rfc);

			String proxyPath = robotProxy.getRobotProxy();
			logger.info("Creating a new Session...");
			Session session = null;
			Context context = null;
//			Context context_rocci = null;
			try {
				session = SessionFactory.createSession(false);
				logger.info(ContextImpl.URL_PREFIX);

				context = ContextFactory.createContext("VOMS");
				context.setAttribute(Context.USERPROXY, proxyPath);
				//if (adaptor.equals("rocci")) context.setVectorAttribute("BaseUrlIncludes", new String[]{"rocci://"});
				
				if (adaptor.equals("wms")) {
					context = ContextFactory.createContext("VOMS");
					context.setAttribute(Context.USERPROXY, proxyPath);
					// context.setVectorAttribute("JobServiceAttributes", new
					// String[]{"wms.RetryCount="+retryCount});
					// context.setVectorAttribute("JobServiceAttributes", new
					// String[]{"wms.RetryCount="+retryCount,"wms.rank=other.GlueCEStateFreeCPUs"});
					context.setVectorAttribute("JobServiceAttributes",
							new String[] {
									"wms.RetryCount=" + retryCount,
									"wms.rank=other.GlueCEStateFreeCPUs",
									"wms.MyProxyServer="
											+ getMyproxyServerForVO(vo) });
				} else if(adaptor.equals("rocci")) {
					context = ContextFactory.createContext("rocci");
					context.setAttribute(Context.USERPROXY, proxyPath);
					context.setAttribute(Context.USERID,"root");
					//context.setAttribute(Context.USERPASS,"");
					context.setAttribute(Context.USERCERT,
							System.getProperty("user.home") + 
							System.getProperty("file.separator") + 
							".ssh/id_rsa.pub");
					//Put here your private key file path
					context.setAttribute(Context.USERKEY,
							System.getProperty("user.home") + 
							System.getProperty("file.separator") + 
							".ssh/id_rsa");
					//context.setAttribute("UrlPrefix","sftp");
//					context.setVectorAttribute("BaseUrlIncludes", new String[]{"sftp://"});
//					context.setVectorAttribute("DataServiceAttributes", new String[]{"sftp.KnownHosts="});
				}

				session.addContext(context);
				
				logger.info("DN=" + context.getAttribute(Context.USERID));
				logger.info("VO=" + context.getAttribute(Context.USERVO));
				logger.info("FQAN=" + context.getAttribute("UserFQAN"));
		
				session.addContext(context);
//				if (adaptor.equals("rocci")) {
//					context = ContextFactory.createContext("SSH");
//					context.setAttribute(Context.USERID,"root");
//					//context.setAttribute(Context.USERPASS,"");
//					context.setAttribute(Context.USERCERT,
//							System.getProperty("user.home") + 
//							System.getProperty("file.separator") + 
//							".ssh/id_rsa.pub");
//					//Put here your private key file path
//					context.setAttribute(Context.USERKEY,
//							System.getProperty("user.home") + 
//							System.getProperty("file.separator") + 
//							".ssh/id_rsa");
//					//context.setAttribute("UrlPrefix","sftp");
//					context.setVectorAttribute("BaseUrlIncludes", new String[]{"sftp://"});
//					context.setVectorAttribute("DataServiceAttributes", new String[]{"sftp.KnownHosts="});
//					session.addContext(context);
//				}

			} catch (Exception e) {
				logger.error("Error in reading user proxy...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");

				URL serviceURL = null;
				if (adaptor.equals("rocci"))
					serviceURL = URLFactory.createURL(resourceManager+"&proxy_path="+proxyPath);
				else
					serviceURL = URLFactory.createURL(resourceManager);
				
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			sessionsTime.put(session, Calendar.getInstance().getTimeInMillis());
			jobServices.put(proxyInfo, service);
			jobServicesCounter.put(service, new Integer(oldCounter + 1));
		} else {
			logger.info("JobService already exists!");

			Integer counter = jobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! JobServicesCounter disaligned!");

			jobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + jobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ jobServicesCounter.size());
		}

		return service;
	}

	private synchronized boolean checkProxyLifetime(Session session) {
		try {
			Context[] contexts = session.listContexts();
			String lifetime = contexts[0].getAttribute(Context.LIFETIME);
			logger.info("LIFETIME=" + lifetime);

			// if (lifetime.charAt(0)!='P') return false;

			// lifetime = lifetime.substring(lifetime.indexOf('T')+1);

			long time = sessionsTime.get(session);

			long interval = Calendar.getInstance().getTimeInMillis() - time;

			logger.info("Interval=" + interval);

			// se sono passate + di 6 ore
			// if (interval>1000)
			if (interval > (6 * 60 * 60 * 1000))
				return true;

			// int hour = 0;
			// if (lifetime.indexOf('H') != -1)
			// hour = new
			// Integer(lifetime.substring(0,lifetime.indexOf('H'))).intValue();
			//
			// System.out.println("hour="+hour);
			//
			// if (hour<2)
			// return true;

		} catch (Exception e) {
			logger.error(e.toString());
			logger.error("Cause :" + e.getCause());
			return false;
		}

		return false;
	}

	/**
	 * Closes the JobService with the specified parameters.
	 * 
	 * @param usingRobotProxy
	 *            true if JobService uses a robot proxy, false otherwise
	 * @param proxyId
	 *            robot proxy user identifier
	 * @param vo
	 *            proxyId robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeJobService(boolean usingRobotProxy,
			String proxyId, String vo, String fqan, String resourceManager) {
		String proxyInfo = proxyId + "-" + vo + "-" + fqan + "-"
				+ resourceManager;
		JobService service = jobServices.get(proxyInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = jobServicesCounter.get(service);
		if (counter == null) {
			logger.warn("ERROR! JobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			jobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + jobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ jobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			jobServices.remove(proxyInfo);
			jobServicesCounter.remove(service);

			try {
				Session session = service.getSession();
				String proxyToBeDeleted = "";
				if (usingRobotProxy) {
					Context[] contexts = session.listContexts();
					proxyToBeDeleted = contexts[0]
							.getAttribute(Context.USERPROXY);
				}

				session.close();

				if (usingRobotProxy) {
					sessionsTime.remove(session);
					deleteProxy(proxyToBeDeleted);
					// File f = new File(proxyToBeDeleted);
					// if (!f.exists())
					// return;
					//
					// // Attempt to delete it
					// boolean success = f.delete();
					//
					// if (!success)
					// System.out.println("Error in deleting proxy = " +
					// proxyToBeDeleted);
				}
			} catch (Exception e) {
				logger.error("Error in closing session!");
				// System.out.println("Error in closing session!");
			}
			logger.info("Job service removed...");
			// System.out.println("Job service removed...");

		}
	}

	private synchronized void deleteProxy(String proxyToBeDeleted) {
		logger.info("Deleting proxy = " + proxyToBeDeleted);
		File f = new File(proxyToBeDeleted);
		if (!f.exists())
			return;

		// Attempt to delete it
		boolean success = f.delete();

		if (!success)
			logger.warn("Error in deleting proxy = " + proxyToBeDeleted);
		else
			logger.info("Deleted proxy = " + proxyToBeDeleted);

	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using UNICORE middleware.
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
	public synchronized JobService getJKSJobService(String jksPath,
			String jskPassword, String resourceManager) {
		logger.info("getJKSJobService...");
		String jksInfo = jksPath + "-" + resourceManager;
		JobService service = jksJobServices.get(jksInfo);

		if (service == null) {
			logger.info("Creating a new Session...");

			System.setProperty("javax.net.ssl.trustStore", trustStorePath);
			System.setProperty("javax.net.ssl.trustStorePassword",
					trustStorePassword);

			Session session = null;
			Context context = null;
			try {
				session = SessionFactory.createSession(false);
				context = ContextFactory.createContext("JKS");
				context.setAttribute("Keystore", jksPath);
				context.setAttribute("KeystorePass", jskPassword);

				session.addContext(context);
				logger.info("DN=" + context.getAttribute(Context.USERID));
			} catch (Exception e) {
				logger.error("Error in reading user proxy...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = URLFactory.createURL(resourceManager);
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			jksJobServices.put(jksInfo, service);
			jksJobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");
			Integer counter = jksJobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! jksJobServicesCounter disaligned!");

			jksJobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + jksJobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ jksJobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Closes the JobService with the specified parameters.
	 * 
	 * @param jksPath
	 *            keystore path
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeJKSJobService(String jksPath,
			String resourceManager) {
		logger.info("getJKSJobService...");
		String jksInfo = jksPath + "-" + resourceManager;
		JobService service = jksJobServices.get(jksInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = jksJobServicesCounter.get(service);
		if (counter == null) {
			logger.error("ERROR! JobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			jksJobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + jksJobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ jksJobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			jksJobServices.remove(jksInfo);
			jksJobServicesCounter.remove(service);

			try {
				Session session = service.getSession();
				session.close();
			} catch (Exception e) {
				logger.error("Error in closing session!");
			}
			logger.info("Job service removed...");

		}
	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using OURGRID middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param password
	 *            password parameter
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for OURGRID middleware
	 */
	public synchronized JobService getOurGridJobService(String username,
			String password, String resourceManager) {
		logger.info("getPWDJobService...");
		String ourgridInfo = username + "-" + resourceManager;
		JobService service = ourgridJobServices.get(ourgridInfo);

		if (service == null) {
			logger.info("Creating a new Session...");

			Session session = null;
			Context context = null;
			try {
				session = SessionFactory.createSession(false);
				context = ContextFactory.createContext("ourgrid");
				context.setAttribute("UserID", username);
				context.setAttribute("UserPass", password);

				session.addContext(context);

			} catch (Exception e) {
				logger.error("Error in creating session");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = URLFactory.createURL(resourceManager);
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			ourgridJobServices.put(ourgridInfo, service);
			ourgridJobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");
			Integer counter = ourgridJobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! ourgridJobServicesCounter disaligned!");

			ourgridJobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + ourgridJobServicesCounter.get(service));
			logger.info("Number of elements in ourgridJobServicesCounter="
					+ ourgridJobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Closes a JobService used for OURGRID middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeOurGridJobService(String username,
			String resourceManager) {
		logger.info("closeOurGridJobService...");
		String ourgridInfo = username + "-" + resourceManager;
		JobService service = ourgridJobServices.get(ourgridInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = ourgridJobServicesCounter.get(service);
		if (counter == null) {
			logger.warn("ERROR! ourgridJobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			ourgridJobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + ourgridJobServicesCounter.get(service));
			logger.info("NUmber of elements in ourgridJobServicesCounter="
					+ ourgridJobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			ourgridJobServices.remove(ourgridInfo);
			ourgridJobServicesCounter.remove(service);

			try {
				Session session = service.getSession();
				session.close();
			} catch (Exception e) {
				logger.error("Error in closing session!");
			}
			logger.info("Job service removed...");

		}
	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using BESGENESIS middleware.
	 * 
	 * @param jksPath
	 *            keystore path
	 * @param jskPassword
	 *            keystore password
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for BESGENESIS middleware
	 */
	public synchronized JobService getGenesisJobService(String jksPath,
			String jskPassword, String resourceManager) {
		logger.info("getGenesisJobService...");
		String jksInfo = jksPath + "-" + resourceManager;
		JobService service = genesisJobServices.get(jksInfo);

		if (service == null) {
			logger.info("Creating a new Session...");

			System.setProperty("javax.net.ssl.trustStore",
					genesisTrustStorePath);
			System.setProperty("javax.net.ssl.trustStorePassword",
					genesisTrustStorePassword);
			System.setProperty("javax.net.ssl.keyStore", jksPath);
			System.setProperty("javax.net.ssl.keyStorePassword", jskPassword);
			System.setProperty("genesis.ssh.username", "chaindemo");
			System.setProperty("genesis.ssh.password", "chaindemo");

			Session session = null;
			Context context = null;
			try {
				session = SessionFactory.createSession(false);
				context = ContextFactory.createContext("AxisJKS");
				// context.setAttribute("Keystore",jksPath);
				// context.setAttribute("KeystorePass",jskPassword);

				String[] attributes = new String[] {
						"ReferenceParameterNS=http://edu.virginia.vcgr.genii/ref-params",
						"ReferenceParameterName=resource-key",
						"bes-genesis2.ReferenceParameterValue=10543AFE-56A7-6A27-E017-ADA51BFFAD47" };
				context.setVectorAttribute("JobServiceAttributes", attributes);

				session.addContext(context);
				logger.info("DN=" + context.getAttribute(Context.USERID));
			} catch (Exception e) {
				logger.error("Error in reading user proxy...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = URLFactory.createURL(resourceManager);
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			genesisJobServices.put(jksInfo, service);
			genesisJobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");
			Integer counter = genesisJobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! genesisJobServicesCounter disaligned!");

			genesisJobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + genesisJobServicesCounter.get(service));
			logger.info("Number of elements in jobServicesCounter="
					+ genesisJobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Closes the JobService with the specified parameters.
	 * 
	 * @param jksPath
	 *            keystore path
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeGenesisJobService(String jksPath,
			String resourceManager) {
		logger.info("closeGenesisJobService...");
		String jksInfo = jksPath + "-" + resourceManager;
		JobService service = genesisJobServices.get(jksInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = genesisJobServicesCounter.get(service);
		if (counter == null) {
			logger.warn("ERROR! genesisJobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			genesisJobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + genesisJobServicesCounter.get(service));
			logger.info("NUmber of elements in jobServicesCounter="
					+ genesisJobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			genesisJobServices.remove(jksInfo);
			genesisJobServicesCounter.remove(service);

			try {
				Session session = service.getSession();
				session.close();
			} catch (Exception e) {
				logger.error("Error in closing session!");
			}
			logger.info("Job service removed...");

		}
	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using GOS middleware.
	 * 
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for GOS middleware
	 */
	public synchronized JobService getGOSJobService(String resourceManager) {
		logger.info("getGOSJobService...");
		String gosInfo = resourceManager;
		JobService service = gosJobServices.get(gosInfo);

		if (service == null) {
			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = URLFactory.createURL(resourceManager);
				service = JobFactory.createJobService(serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			gosJobServices.put(gosInfo, service);
			gosJobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");
			Integer counter = gosJobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! gosJobServicesCounter disaligned!");

			gosJobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + gosJobServicesCounter.get(service));
			logger.info("NUmber of elements in gosJobServicesCounter="
					+ gosJobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Closes the JobService with the specified parameters.
	 * 
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeGOSJobService(String resourceManager) {
		logger.info("closeGOSJobService...");
		String gosInfo = resourceManager;
		JobService service = gosJobServices.get(gosInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = gosJobServicesCounter.get(service);
		if (counter == null) {
			logger.warn("ERROR! gosJobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			gosJobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + gosJobServicesCounter.get(service));
			logger.info("NUmber of elements in gosJobServicesCounter="
					+ gosJobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			gosJobServices.remove(gosInfo);
			gosJobServicesCounter.remove(service);
			logger.info("Job service removed...");

		}
	}

	/**
	 * Returns a JobService object that allows job creation, submission and
	 * status monitoring using SSH middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param password
	 *            password parameter
	 * @param resourceManager
	 *            resource manager host
	 * @return a JobService object for SSH middleware
	 */
	public synchronized JobService getSSHJobService(String username,
			String password, String resourceManager) {
		logger.info("getSSHJobService...");
		String sshInfo = username + "-" + resourceManager;
		JobService service = sshJobServices.get(sshInfo);

		if (service == null) {
			logger.info("Creating a new Session...");

			Session session = null;
			Context context = null;
			try {
				session = SessionFactory.createSession(false);
				context = ContextFactory.createContext("UserPass");
				context.setAttribute("UserID", username);
				context.setAttribute("UserPass", password);
				context.setVectorAttribute("DataServiceAttributes", new String[]{"sftp.KnownHosts="});
				
				session.addContext(context);

			} catch (Exception e) {
				logger.error("Error in creating session");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			try {
				logger.info("Creating a new JobService...");
				URL serviceURL = URLFactory.createURL(resourceManager);
				service = JobFactory.createJobService(session, serviceURL);
			} catch (Exception e) {
				logger.error("Error in creating jobService...");
				logger.error(e.toString());
				logger.error("Cause :" + e.getCause());
				return null;
			}

			sshJobServices.put(sshInfo, service);
			sshJobServicesCounter.put(service, new Integer(1));
		} else {
			logger.info("JobService already exists!");

			Integer counter = sshJobServicesCounter.get(service);
			if (counter == null)
				logger.warn("ERROR! sshJobServicesCounter disaligned!");

			sshJobServicesCounter.put(service, counter + 1);
			logger.info("New counter=" + sshJobServicesCounter.get(service));
			logger.info("Number of elements in sshJobServicesCounter="
					+ sshJobServicesCounter.size());
		}

		return service;
	}

	/**
	 * Closes a JobService used for SSH middleware.
	 * 
	 * @param username
	 *            username parameter
	 * @param resourceManager
	 *            resource manager host
	 */
	public synchronized void closeSSHJobService(String username,
			String resourceManager) {
		logger.info("closeSSHJobService...");
		String sshInfo = username + "-" + resourceManager;
		JobService service = sshJobServices.get(sshInfo);

		if (service == null) {
			logger.warn("Error: Job Service not available..");
			return;
		}

		Integer counter = sshJobServicesCounter.get(service);
		if (counter == null) {
			logger.warn("ERROR! sshJobServicesCounter disaligned!");
			return;
		}

		if (counter.intValue() > 1) {
			sshJobServicesCounter.put(service, counter - 1);
			logger.info("New counter=" + sshJobServicesCounter.get(service));
			logger.info("NUmber of elements in sshJobServicesCounter="
					+ sshJobServicesCounter.size());
		} else if (counter.intValue() == 1) {
			sshJobServices.remove(sshInfo);
			sshJobServicesCounter.remove(service);

			try {
				Session session = service.getSession();

				try {
					((JobServiceImpl) service).disconnect();
				} catch (NoSuccessException e2) {
					logger.error("ERROR! SSH service not disconnected");
					e2.printStackTrace();
				}

				session.close();
			} catch (Exception e) {
				logger.error("Error in closing session!");
			}
			logger.info("Job service removed...");

		}
	}

	private synchronized String getMyproxyServerForVO(String vo) {
		logger.info("getMyproxyServerForVO=" + vo);

		String myproxyserver = "";
		int startIndex = myproxyServers.indexOf(vo);

		if (startIndex != -1) {
			int endIndex = myproxyServers.indexOf(";", startIndex);

			if (endIndex != -1)
				myproxyserver = myproxyServers.substring(
						startIndex + vo.length() + 1, endIndex);
			else
				myproxyserver = myproxyServers.substring(startIndex
						+ vo.length() + 1);
		}
		logger.info("myproxyserver=" + myproxyserver);

		return myproxyserver;
	}
}
