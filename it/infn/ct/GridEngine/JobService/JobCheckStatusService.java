package it.infn.ct.GridEngine.JobService;

import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

import java.util.Vector;

import org.apache.log4j.Logger;

public class JobCheckStatusService {

	private static final Vector<String> jobsCheckStatusThread = new Vector<String>();
	private static JobCheckStatusService instance;
	private static int jobsUpdatingInterval = 60;
	private String URL = "";
	private String userName = "";
	private String password = "";
	boolean inAppServer;

	private static final Logger logger = Logger
			.getLogger(JobCheckStatusService.class);

	private JobCheckStatusService() {
		inAppServer = true;
	}

	private JobCheckStatusService(String url, String username, String pwd) {
		inAppServer = false;
		URL = url;
		userName = username;
		password = pwd;
	}

	/**
	 * Returns the unique instance of this class if it isn't null otherwise
	 * creates a new instance.
	 * 
	 * @return the unique instance of this class.
	 */
	public synchronized static JobCheckStatusService getInstance() {
		if (instance == null) {
			instance = new JobCheckStatusService();
		}

		return instance;
	}

	/**
	 * Returns the unique instance of this class if it isn't null otherwise
	 * creates a new instance with the specified database connection parameters.
	 * 
	 * @param url
	 *            database url
	 * @param username
	 *            database username
	 * @param pwd
	 *            database password
	 * @return the unique instance of this class.
	 */
	public synchronized static JobCheckStatusService getInstance(String url,
			String username, String pwd) {
		if (instance == null) {
			instance = new JobCheckStatusService(url, username, pwd);
		}

		return instance;
	}

	/**
	 * Sets the time interval between two consecutively job updating.
	 * 
	 * @param value
	 *            time interval between two consecutively job updating.
	 */
	public synchronized void setJobsUpdatingInterval(int value) {
		jobsUpdatingInterval = value;
	}

	/**
	 * Starts a thread responsible for check of the status of the jobs for a
	 * specified user, if it not exists.
	 * 
	 * @param commonName
	 *            name of the user for who to start the check job status thread
	 * @param outputPathPrefix
	 *            path file prefix where output files are located.
	 * 
	 */
	public synchronized void startJobCheckStatusThread(String commonName,
			String outputPathPrefix) {
//		logger.info("########## jobsCheckStatusThread.size(): "+ jobsCheckStatusThread.size() +" ##########");
//		for (String s : jobsCheckStatusThread) {
//			logger.info("########## COMMON NAME: "+ s +" ##########");
//		}
		
		if (jobsCheckStatusThread.contains(commonName)){
			logger.info("Check Status Thread for " + commonName + " already started");
			return;
		}
		
		logger.info("Starting new check jobs status thread for user "
				+ commonName);
		// System.out.println("Starting new check jobs status thread for user "
		// + commonName);

		jobsCheckStatusThread.add(commonName);

		UsersTrackingDBInterface dbInterface;
		if (inAppServer)
			dbInterface = new UsersTrackingDBInterface();
		else
			dbInterface = new UsersTrackingDBInterface(URL, userName, password);

		dbInterface.updateJobsStatusAsync(commonName, outputPathPrefix,
				jobsUpdatingInterval);
	}

	/**
	 * Stops the thread responsible for check of the status of the jobs for a
	 * specified user,
	 * 
	 * @param commonName
	 *            name of the user for who to stop the check job status thread
	 */
	public synchronized void stopJobCheckStatusThread(String commonName) {
		if (jobsCheckStatusThread.contains(commonName)) {
			logger.info("Deleting " + commonName
					+ " from the list of active users");
			// System.out.println("Deleting " + commonName +
			// " from the list of active users");
			jobsCheckStatusThread.removeElement(commonName);
		}
	}
}
