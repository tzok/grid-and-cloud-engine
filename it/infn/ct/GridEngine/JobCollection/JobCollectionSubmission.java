package it.infn.ct.GridEngine.JobCollection;

import it.infn.ct.GridEngine.Job.InfrastructureInfo;
import it.infn.ct.GridEngine.Job.JSagaJobSubmission;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.UsersTracking.ActiveInteractions;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class allow to submit a collection of job to the Grid Infrastructure A
 * collection can be:
 * <ul>
 * <li>an instance of {@link JobCollection}: that represent a set of jobs
 * running parallel;
 * <li>an instance of {@link WorkflowN1}: that represent a simple work flow
 * where the first level output file are input file for a final job.
 * </ul>
 * 
 * @author mario
 * 
 */
public class JobCollectionSubmission {

	// oggetto che rappresenta la collezione da sottomettere
	private JobCollection jobCollection;

	private boolean inAppServer = true;
	private String DB = "";
	private String DBUser = "";
	private String DBUserPwd = "";

	private static final Logger logger = Logger
			.getLogger(JobCollectionSubmission.class);

	/**
	 * Constructs a JobCollectionSubmission object that can be used for
	 * submission, accepts a {@link JobCollection} object that is the submitting
	 * collection.
	 * 
	 * @param jobCollection
	 *            the submitting collection.
	 */
	public JobCollectionSubmission(JobCollection jobCollection) {
		// Costruttore richiamato se ci troviamo su application server.
		setJobCollection(jobCollection);
	}

	/**
	 * Constructs a JobCollectionSubmission object that can be used for
	 * submission, accepts the local database connection parameters and a
	 * {@link JobCollection} object that is the submitting collection.
	 * 
	 * @param dB
	 *            database name
	 * @param dBUser
	 *            database username
	 * @param dBUserPwd
	 *            database password
	 * @param jobCollection
	 *            the submitting collection.
	 */
	public JobCollectionSubmission(String dB, String dBUser, String dBUserPwd,
			JobCollection jobCollection) {

		this(jobCollection); // Chiamo il costruttore di default
		// Setto i parametri di connessione locali
		this.DB = dB;
		this.DBUser = dBUser;
		this.DBUserPwd = dBUserPwd;

		this.inAppServer = false;

	}

	/**
	 * Returns the submitting {@link JobCollection} object.
	 * 
	 * @return the submitting collection.
	 */
	public JobCollection getJobCollection() {
		return jobCollection;
	}

	/**
	 * Sets the submitting {@link JobCollection} object.
	 * 
	 * @param jobCollection
	 *            the submitting collection.
	 */
	public void setJobCollection(JobCollection jobCollection) {
		this.jobCollection = jobCollection;
	}

	/**
	 * Returns <b>true</b> if the application is running on a application
	 * server.
	 * 
	 * @return <b>true</b> if the application is running on a application
	 *         server, <b>false</b> otherwise.
	 */
	public boolean isInAppServer() {
		return inAppServer;
	}

	// public void setInAppServer(boolean inAppServer) {
	// this.inAppServer = inAppServer;
	// }

	public void submitJobCollection(InfrastructureInfo[] infrastructures,
			String tcpAddress, int gridInteractionId) {// array di infra e
														// scegiere random a
														// aqual submit

		for (int i = 0; i < jobCollection.getSubJobDescriptions().size(); i++) {
			GEJobDescription descr = jobCollection.getSubJobDescriptions().get(
					i);
			JSagaJobSubmission jobSubmission = createJobSubmissionObject(
					getRandomInfrastructure(infrastructures), descr);

			jobSubmission.submitJobAsync(jobCollection.getCommonName(),
					tcpAddress, gridInteractionId,
					jobCollection.getDescription() + "_" + i,
					jobCollection.getId());
		}

	}

	private InfrastructureInfo getRandomInfrastructure(
			InfrastructureInfo[] infrastructures) {
		if (infrastructures == null)
			return null;

		int index = (int) ((Math.random()) * (new Integer(
				infrastructures.length).doubleValue()));
		if (index == infrastructures.length)
			index--;
		if (logger.isDebugEnabled())
			logger.debug("Infrastructure selected = " + infrastructures[index]);

		return infrastructures[index];
	}

	private JSagaJobSubmission createJobSubmissionObject(
			InfrastructureInfo infrastructure, GEJobDescription descr) {

		JSagaJobSubmission jobSubmission = null;

		if (inAppServer)
			jobSubmission = new JSagaJobSubmission(descr);
		else
			jobSubmission = new JSagaJobSubmission(DB, DBUser, DBUserPwd, descr);

		if (infrastructure.getMiddleware().equals("glite")) {
			if (!(infrastructure.getBDII().equals("")))
				jobSubmission.setBDII(infrastructure.getBDII());
			// ******MARIO*****
			else
				jobSubmission.setCEList(infrastructure.getCEList());
			// ****************
		}

		if (infrastructure.getWmsList() != null)
			jobSubmission.setWMSList(infrastructure.getWmsList());

		if ((infrastructure.getMiddleware().equals("glite"))
				|| (infrastructure.getMiddleware().equals("wsgram"))) {
			if (infrastructure.getUserProxy().equals(""))
				jobSubmission.useRobotProxy(infrastructure.getETokenServer(),
						infrastructure.getETokenServerPort(),
						infrastructure.getProxyId(), infrastructure.getVO(),
						infrastructure.getFQAN(), true, true, jobCollection.getCommonName());
			else
				jobSubmission.setUserProxy(infrastructure.getUserProxy());
		} else if (infrastructure.getMiddleware().equals("unicore")) {
			jobSubmission.setJKS(infrastructure.getKeystore(),
					infrastructure.getPassword());
		} else if (infrastructure.getMiddleware().equals("ourgrid")) {
			jobSubmission.setOurGridCredential(infrastructure.getUserName(),
					infrastructure.getPassword());
		} else if (infrastructure.getMiddleware().equals("bes-genesis2")) {
			jobSubmission.setGenesisJKS(infrastructure.getKeystore(),
					infrastructure.getPassword());
		} else if (infrastructure.getMiddleware().equals("ssh")) {
			jobSubmission.setSSHCredential(infrastructure.getUserName(),
					infrastructure.getPassword());
		}

		if ((infrastructure.getMiddleware().equals("ourgrid"))
				&& (!(descr.getExecutable().equals("/bin/sh")))) {
			jobSubmission.setExecutable("/bin/sh");

			if (!(descr.getArguments().equals("")))
				descr.setExecutable(descr.getExecutable() + " "
						+ descr.getArguments());

			jobSubmission.setArguments(descr.getArguments());
		} else {
			if (!(descr.getExecutable().equals("")))
				jobSubmission.setExecutable(descr.getExecutable());

			if (!(descr.getArguments().equals("")))
				jobSubmission.setArguments(descr.getArguments());
		}

		if (descr.getQueue() != null && !(descr.getQueue().equals("")))
			jobSubmission.setJobQueue(descr.getQueue());

		if (!(descr.getOutputPath().equals("")))
			jobSubmission.setOutputPath(descr.getOutputPath());

		if (!(descr.getOutput().equals("")))
			jobSubmission.setJobOutput(descr.getOutput());

		if (!(descr.getError().equals("")))
			jobSubmission.setJobError(descr.getError());

		if (!descr.getInputFiles().equals(""))
			jobSubmission.setInputFiles(descr.getInputFiles());
		// TODO DA FARE
		// if (!(outputFiles.equals("")))
		// jobSubmission.setOutputFiles(descr.get);

		if (descr.getTotalCPUCount() != null
				&& !(descr.getTotalCPUCount().equals("")))
			jobSubmission.setTotalCPUCount(descr.getTotalCPUCount());

		if (descr.getSPDMVariation() != null
				&& !(descr.getSPDMVariation().equals("")))
			jobSubmission.setSPMDVariation(descr.getSPDMVariation());

		if (descr.getNumberOfProcesses() != null
				&& !(descr.getNumberOfProcesses().equals("")))
			jobSubmission.setNumberOfProcesses(descr.getNumberOfProcesses());

		// if (infrastructure.getMiddleware().equals("glite")) {
		// if (infrastructure.getSWTag().equals("")) {
		// if (descr.getJDLRequirements() != null)
		// jobSubmission.setJDLRequirements(descr.getJDLRequirements());
		// } else {
		// //
		// Member("VO-prod.vo.eu-eela.eu-OCTAVE-3.2.4",other.GlueHostApplicationSoftwareRunTimeEnvironment);
		// String requirements[] = null;
		// if (JDLRequirements != null) {
		// requirements = new String[JDLRequirements.length + 1];
		// requirements[0] = "JDLRequirements=(Member(\""
		// + infrastructure.getSWTag()
		// + "\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
		// for (int i = 0; i < JDLRequirements.length; i++)
		// requirements[i + 1] = JDLRequirements[i];
		// } else {
		// requirements = new String[1];
		// requirements[0] = "JDLRequirements=(Member(\""
		// + infrastructure.getSWTag()
		// + "\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
		// }
		// jobSubmission.setJDLRequirements(requirements);
		// }
		// }

		return jobSubmission;
	}

	public static void main(String[] args) {
		logger.info("Submitting Collection");

		InfrastructureInfo infrastructures[] = new InfrastructureInfo[1];

		String collectionDescr = "Collection";

		String sshList[] = { "ssh://gilda-liferay-vm-06.ct.infn.it:5000" };
		String sshList1[] = { "ssh://gilda-liferay-vm-15.ct.infn.it:5000" };
		String sshList2[] = { "ssh://api.ct.infn.it" };
		 String sshList3[] = { "ssh://lrt01.ct.infn.it" };
		String wmsList[] = {"wms://wmsdecide.dir.garr.it:7443/glite_wms_wmproxy_server"};
		String wmsListSEEGRID[] = {"wms://wms01.afroditi.hellasgrid.gr:7443/glite_wms_wmproxy_server"};
//				"wms://wms.magrid.ma:7443/glite_wms_wmproxy_server"}; 
//				"wms://wmsdecide.dir.garr.it:7443/glite_wms_wmproxy_server" };
		//  wms://wms-4.dir.garr.it:7443/glite_wms_wmproxy_server
		// infrastructures[0] = new InfrastructureInfo("SSH Infrastructure",
		// "ssh", "liferayadmin", "liferayadmin", sshList);
		// infrastructures[1] = new InfrastructureInfo("SSH Infrastructure",
		// "ssh", "liferayadmin", "CiematANDInfn2013", sshList1);
//		infrastructures[0] = new InfrastructureInfo("SSH Infrastructure",
//				"ssh", "root", "Passw0rd!", sshList2);
////
//		infrastructures[1] = new InfrastructureInfo("SSH Infrastructure",
//		"ssh", "liferayadmin", "liferay", sshList3);


		infrastructures[0] = new InfrastructureInfo("EUMEDGRID",
		"ldap://bdii.eumedgrid.eu:2170", wmsListSEEGRID,
		"etokenserver.ct.infn.it", "8082",
		"bc681e2bd4c3ace2a4c54907ea0c379b", "see", "see");
//		infrastructures[0] = new InfrastructureInfo("gridit",
//				"ldap://gridit-bdii-01.cnaf.infn.it:2170", wmsList,
//				"etokenserver.ct.infn.it", "8082",
//				"332576f78a4fe70a52048043e90cd11f", "gridit", "gridit");

		// String wmsListUnicore[] = {
		// "unicore://zam052v01.zam.kfa-juelich.de:8080/?Target=EMI-UNICOREX" };
		// InfrastructureInfo infrastructure = new InfrastructureInfo("Unicore",
		// "unicore", "/tmp/robot2012.jks", "robot2012", wmsListUnicore);

		ArrayList<GEJobDescription> descriptions = new ArrayList<GEJobDescription>();

		for (int i = 0; i < 2; i++) {
			GEJobDescription description = new GEJobDescription();
			description.setExecutable("/bin/sh");
			switch (i) {
			case 0:
//				description.setExecutable("/bin/hostname");
				description.setArguments("hostname.sh");
				description.setInputFiles("./hostname.sh");
				description.setOutputFiles("output.README");
				break;
			case 1:
//				description.setExecutable("/bin/ls");
//				description.setArguments("-l");
				description.setArguments("ls.sh");
				description.setInputFiles("./ls.sh");
				break;
			case 2:
//				description.setExecutable("/bin/pwd");
				description.setArguments("pwd.sh");
				description.setInputFiles("./pwd.sh");
				break;
			default:
				if ((i % 2) != 0) {
//					description.setExecutable("ifconfig");
					description.setArguments("ifconfig.sh");
					description
							.setInputFiles("./ifconfig.sh");
				} else {
//					description.setExecutable("/bin/ps");
					description.setArguments("ps.sh");
					description.setInputFiles("./ps.sh");
				}
				break;
			}

			description.setOutputPath("/tmp");
			if ((i % 2) != 0)
				description.setOutput("myOutput-" + i + ".txt");
			else
				description.setOutput("PippomyOutput-" + i + ".txt");
			description.setError("myError-" + i + ".txt");

			descriptions.add(description);
		}

		GEJobDescription finalJobDescription = new GEJobDescription();
//		finalJobDescription.setExecutable("/bin/ls");
		finalJobDescription.setExecutable("ls.sh");
		finalJobDescription.setArguments("./ls.sh");

		String tmp = "";
		for (int i = 0; i < descriptions.size(); i++) {
			if (tmp.equals(""))
				tmp = descriptions.get(i).getOutput();
			else
				tmp += "," + descriptions.get(i).getOutput();
		}

		finalJobDescription
				.setInputFiles(tmp
						+ ",./ls.sh");

		finalJobDescription.setOutputPath("/tmp");
		finalJobDescription.setOutput("myOutput-FinalJob.txt");
		finalJobDescription.setError("myError-FinalJob.txt");

//		 JobCollection wf1 = new WorkflowN1("test", "Workflow N-1 ALL",
//		 "mario.torrisi@ct.infn.it", "/tmp", descriptions,
//		 finalJobDescription);
//		 JobCollectionSubmission tmpJobCollectionSubmission1 = new
//		 JobCollectionSubmission(
//		 "jdbc:mysql://localhost/userstracking", "tracking_user",
//		 "usertracking", wf1);
//		 tmpJobCollectionSubmission1.submitJobCollection(infrastructures,
//		 "193.206.208.183:8162", 81);

//		 JobCollection wf = new WorkflowN1("mtorrisi", "Workflow N-1","mario.torrisi@ct.infn.it|mario.torrisi@ct.infn.it", "/tmp",
//		 descriptions,
//		 finalJobDescription, new String[] { "Pippo","3" });
//		 //
//		 JobCollectionSubmission tmpJobCollectionSubmission = new
//		 JobCollectionSubmission(
//		 "jdbc:mysql://localhost/userstracking", "tracking_user",
//		 "usertracking", wf);
//		 tmpJobCollectionSubmission.submitJobCollection(infrastructures,
//		 "193.206.208.183:8162", 1);

		 /*SOTTOMETTO UNA NUOVA COLLEZIONE
		JobCollection collection = new JobCollection("test",
				"Collection - TEST - ", "/tmp", descriptions);
		JobCollectionSubmission tmpJobCollectionSubmission = new JobCollectionSubmission(
				"jdbc:mysql://localhost/userstracking", "tracking_user",
				"usertracking", collection);

		tmpJobCollectionSubmission.submitJobCollection(infrastructures,
				"193.206.208.183:8162", 1);
		// Fine sottomissione nuova collezione*/
		 JobParametric p = new JobParametric("test",
		 "Parametric Job - mtorrisi", "mario.torrisi@ct.infn.it",
		 "/tmp", descriptions, "hostname.sh");
		
		 JobCollectionSubmission tmpJobCollectionSubmission = new
		 JobCollectionSubmission(
		 "jdbc:mysql://localhost/userstracking", "tracking_user",
		 "usertracking", p);
		
		 tmpJobCollectionSubmission.submitJobCollection(infrastructures,
		 "193.206.208.183:8162", 81);
		System.out.println("Vado in sleep...");
		try {
			Thread.sleep(60000);
		} catch (Exception e) {
		}

		System.out.println("Checking jobs...");

		UsersTrackingDBInterface DBInterface = new UsersTrackingDBInterface(
				"jdbc:mysql://localhost/userstracking", "tracking_user",
				"usertracking");
		String status = "RUNNING";
		Vector<ActiveInteractions> jobList = null;

		while (status.equals("RUNNING")) {
			try {
				Thread.sleep(15000);
			} catch (Exception e) {
			}

			System.out.println("Users running jobs = "
					+ DBInterface.getTotalNumberOfUsersWithRunningJobs());
			status = "DONE";
			// jobList = DBInterface.getActiveJobsListByName("mtorrisi");
			jobList = DBInterface.getActiveInteractionsByName("mtorrisi");

			if (jobList != null) {
				for (int i = 0; i < jobList.size(); i++) {

					if (jobList.get(i).getSubJobs() == null) {

						System.out
								.println("DBID = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[0]
										+ " Portal = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[1]
										+ " - Application = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[2]
										+ " - Description = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[3]
										+ " - Timestamp = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[4]
										+ " - Status = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[5]);

					} else {
						System.out
								.println("***COLLECTION INFOS*** DBID = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[0]
										+ " Portal = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[1]
										+ " - Application = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[2]
										+ " - Description = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[3]
										+ " - Timestamp = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[4]
										+ " - Status = "
										+ jobList.elementAt(i)
												.getInteractionInfos()[5]);
						Vector<String[]> subJobs = jobList.get(i).getSubJobs();
						for (String[] subJobInfos : subJobs)
							System.out.println("\t|_***SUBJOB INFOS*** DBID = "
									+ subJobInfos[0] + " Portal = "
									+ subJobInfos[1] + " - Application = "
									+ subJobInfos[2] + " - Description = "
									+ subJobInfos[3] + " - Timestamp = "
									+ subJobInfos[4] + " - Status = "
									+ subJobInfos[5]);
					}
					if (!jobList.elementAt(i).getInteractionInfos()[5]
							.equals("DONE"))
						status = "RUNNING";
				}
			}

			if (status.equals("RUNNING")) {
				try {
					Thread.sleep(15000);
				} catch (Exception e) {
				}
			}

		}

		String allTarPath = DBInterface
				.createAllJobsArchive("test", "/tmp");
		System.out.println("allTarPath=" + allTarPath);
		//
		Vector<ActiveInteractions> jobList1 = null;
		jobList1 = DBInterface.getDoneInteractionsByName("test");

		if (jobList1.size() == 0)
			System.out.println("No jobs for user test");
		for (int i = 0; i < jobList1.size(); i++) {
			if (jobList1.get(i).getSubJobs() == null) {

				System.out.println("DBID = "
						+ jobList1.elementAt(i).getInteractionInfos()[0]
						+ " Portal = "
						+ jobList1.elementAt(i).getInteractionInfos()[1]
						+ " - Application = "
						+ jobList1.elementAt(i).getInteractionInfos()[2]
						+ " - Description = "
						+ jobList1.elementAt(i).getInteractionInfos()[3]
						+ " - Timestamp = "
						+ jobList1.elementAt(i).getInteractionInfos()[4]
						+ " - Status = "
						+ jobList1.elementAt(i).getInteractionInfos()[5]);

			} else {
				System.out.println("***COLLECTION INFOS*** DBID = "
						+ jobList1.elementAt(i).getInteractionInfos()[0]
						+ " Portal = "
						+ jobList1.elementAt(i).getInteractionInfos()[1]
						+ " - Application = "
						+ jobList1.elementAt(i).getInteractionInfos()[2]
						+ " - Description = "
						+ jobList1.elementAt(i).getInteractionInfos()[3]
						+ " - Timestamp = "
						+ jobList1.elementAt(i).getInteractionInfos()[4]
						+ " - Status = "
						+ jobList1.elementAt(i).getInteractionInfos()[5]);
				Vector<String[]> subJobs = jobList1.get(i).getSubJobs();
				for (String[] subJobInfos : subJobs)
					System.out.println("\t|_***SUBJOB INFOS*** DBID = "
							+ subJobInfos[0] + " Portal = " + subJobInfos[1]
							+ " - Application = " + subJobInfos[2]
							+ " - Description = " + subJobInfos[3]
							+ " - Timestamp = " + subJobInfos[4]
							+ " - Status = " + subJobInfos[5]);
			}
		}

	}

}
