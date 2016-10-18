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

import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.UsersTracking.ActiveInteractions;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

public class MultiInfrastructureJobSubmission {

	private List<InfrastructureInfo> infrastructureInfo;
	
//	private String executable = "";
//	private String arguments = "";
//	private String jobQueue = "";
//	private String outputPath = "";
//	private String jobOutput = "";
//	private String jobError = "";
//	private String inputFiles = "";
	private String outputFiles = "";
//	private String totalCPUCount = "";
//	private String SPMDVariation = "";
//	private String NumberOfProcesses = "";
//	private String JDLRequirements[] = null;
	private boolean checkJobsStatus = true;
	private boolean randomCE = false;
	
	
	private boolean inAppServer = true;
	private String DB = "";
	private String DBUser = "";
	private String DBUserPwd = "";
	
	//********MARIO***************
	private String userEmail = "";
	private static final Logger logger = Logger.getLogger(MultiInfrastructureJobSubmission.class);
	private GEJobDescription description;
	//****************************
	
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} object without specify 
	 * connection parameters to users tracking database.
	 */
	public MultiInfrastructureJobSubmission() {
		infrastructureInfo = new LinkedList<InfrastructureInfo>();
		description = new GEJobDescription();
	}
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} object specifying the description for 
	 * the submitting job.
	 * 
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public MultiInfrastructureJobSubmission(GEJobDescription description) {
		this();
		
		this.description = description;
	}
	
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} object specifying 
	 * connection parameters to users tracking database.
	 *  
	 * @param db database name
	 * @param dbUser database username
	 * @param dbUserPwd database password.
	 */
	public MultiInfrastructureJobSubmission(String db, String dbUser, String dbUserPwd) {
		infrastructureInfo = new LinkedList<InfrastructureInfo>();
		
		DB = db;
		DBUser = dbUser;
		DBUserPwd = dbUserPwd;
		inAppServer = false;
		
		description = new GEJobDescription();
	}
	
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} object specifying 
	 * connection parameters to users tracking database and and the description 
	 * of submitting job.
	 *  
	 * @param db database name
	 * @param dbUser database username
	 * @param dbUserPwd database password
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public MultiInfrastructureJobSubmission(String db, String dbUser, String dbUserPwd, GEJobDescription description) {
		this(db, dbUser, dbUserPwd);
		
		this.description = description;
	}
		
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} object specifying 
	 * connection parameters to users tracking database, the list of enabled infrastructures and and the description 
	 * of submitting job.
	 *  
	 * @param db database name
	 * @param dbUser database username
	 * @param dbUserPwd database password
	 * @param infrastructures list of enabled infrastructures
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public MultiInfrastructureJobSubmission(String db, String dbUser, String dbUserPwd, ArrayList<InfrastructureInfo> infrastructures, GEJobDescription description){
		this(db, dbUser, dbUserPwd);
		this.infrastructureInfo = infrastructures;
		this.description = description;
	}
	
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} specifying a list of possible infrastructures
	 * to which submit job.
	 * 
	 * @param infrastructures list of possible infrastructures to which submit job.
	 */
	public MultiInfrastructureJobSubmission(InfrastructureInfo infrastructures[]) {
		infrastructureInfo = new LinkedList<InfrastructureInfo>();
		
		for (int i=0;i<infrastructures.length;i++) {
			infrastructureInfo.add(infrastructures[i]);
			logger.debug("Added infrastructure " + infrastructures[i].getName());
		}
		description = new GEJobDescription();
	}
	
	/**
	 * Constructs a {@link MultiInfrastructureJobSubmission} specifying a list of possible infrastructures
	 * to which submit job and the description for this job.
	 * 
	 * @param infrastructures list of possible infrastructures to which submit job.
	 * @param description a {@link GEJobDescription} object contains description 
	 * 			of the submitting job.
	 */
	public MultiInfrastructureJobSubmission(InfrastructureInfo infrastructures[], GEJobDescription description){
		this(infrastructures);
		
		this.description = description;
	}
	
	/**
	 * This method adds a the specified infrastructure to the list of the infrastructures
	 * to which submit job. 
	 * 
	 * @param infrastructure infrastructure will be add.
	 */
	public void addInfrastructure(InfrastructureInfo infrastructure) {
		infrastructureInfo.add(infrastructure);
		logger.debug("Added infrastructure " + infrastructure.getName());
	}
	
	/**
	 * Returns a random infrastructure from the the list of the possible infrastructures. 
	 * 
	 * @return a random infrastructure from the the list of the possible infrastructures.
	 */
	public InfrastructureInfo getInfrastructure() {
		int index = (int)((Math.random())*(new Integer(infrastructureInfo.size()).doubleValue()));
		if (index==infrastructureInfo.size()) index--;
		
		logger.debug("Selected infrastructure:" + infrastructureInfo.get(index).getName());
		
		return infrastructureInfo.get(index);
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
//		return jobQueue;
		return description.getQueue();
	}

	/**
	 * Sets path where store output of this job.
	 * 
	 * @param value a String that represents path where store output of this job.
	 */
	public void setOutputPath(String value) {
//		outputPath = value;
		description.setOutputPath(value);
	}

	/**
	 * Returns path where output files of this job are stored.
	 * 
	 * @return path where output files of this job are stored.
	 */
	public String getOutputPath() {
//		return outputPath;
		return description.getOutputPath();
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
//		jobError = value;
		description.setError(value);
	}
	
	/**
	 * Returns error file name of this Job.
	 * 
	 * @return error file name of this Job.
	 */
	public String getJobError() {
//		return jobError;
		return description.getError();	
	}

	public void setOutputFiles(String value) {
		description.setOutputFiles(value);
		//		outputFiles = value;
	}

	public String getOutputFiles() {
		return outputFiles;
	}

	/**
	 * Sets a list of input file of this job.
	 * 
	 * @param value a comma separated string that represents list of input file of this job.
	 */
	public void setInputFiles(String value) {
//		inputFiles = value;
		description.setInputFiles(value);
	}

	/**
	 * Returns a string array that represents a list of input files for this job.
	 * 
	 * @return list of input files for this job.
	 */
	public String getInputFiles() {
//		return inputFiles;
		return description.getInputFiles();
	}

	/**
	 * Sets total number of cpus requested for this job.
	 * 
	 * @param value total number of cpus requested for this job.
	 */
	public void setTotalCPUCount(String value) {
//		totalCPUCount = value;
		description.setTotalCPUCount(value);
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

	public void setJDLRequirements(String[] value) {
//		JDLRequirements = value;
		String s = "";
		for (int i = 0; i < value.length; i++) {
			if(i != 0)
				s += ";";
			s += value[i];
		}
		description.setJDLRequirements(s);
	}

	public String[] getJDLRequirements() {
//		return JDLRequirements;
		return description.getJDLRequirements().split(";");
	}
	
	public void setSPMDVariation(String value) {
//		SPMDVariation = value;
		description.setSPDMVariation(value);
	}

	public String getSPMDVariation() {
//		return SPMDVariation;
		return description.getSPDMVariation();
	}
	
	/**
	 * Sets total number of processes to be started.
	 * 
	 * @param value total number of processes to be started.
	 */
	public void setNumberOfProcesses(String value) {
//		NumberOfProcesses = value;
		description.setNumberOfProcesses(value);
	}

	/**
	 * Returns total number of processes to be started.
	 * 
	 * @return total number of processes to be started.
	 */
	public String getNumberOfProcesses() {
//		return NumberOfProcesses;
		return description.getNumberOfProcesses();
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
		return (this.userEmail.contains("|")) ? this.userEmail.substring(0, this.userEmail.indexOf('|')) : this.userEmail;
	}
	
	private JSagaJobSubmission createJobSubmissionObject(String commonName) {
		InfrastructureInfo infrastructure = getInfrastructure();
		return createJobSubmissionObject(commonName, infrastructure);
	}
	
	private JSagaJobSubmission createJobSubmissionObject(String commonName, InfrastructureInfo infrastructure) {
		//InfrastructureInfo infrastructure = getInfrastructure();
		JSagaJobSubmission jobSubmission = null;
		
		if (inAppServer)
			jobSubmission = new JSagaJobSubmission(description);
		else
			jobSubmission = new JSagaJobSubmission(DB, DBUser, DBUserPwd, description);
			
		if (infrastructure.getMiddleware().equals("glite")) {
			if (!(infrastructure.getBDII().equals("")))
				jobSubmission.setBDII(infrastructure.getBDII());
			//******MARIO*****
			else
				jobSubmission.setCEList(infrastructure.getCEList());
			//****************
		}
		
		if (infrastructure.getWmsList()!=null)
			jobSubmission.setWMSList(infrastructure.getWmsList());
		
		if ( (infrastructure.getMiddleware().equals("glite")) || (infrastructure.getMiddleware().equals("wsgram")) || (infrastructure.getMiddleware().equals("rocci")) ) {
			if (infrastructure.getUserProxy().equals(""))
				jobSubmission.useRobotProxy(infrastructure.getETokenServer(), infrastructure.getETokenServerPort(), infrastructure.getProxyId(), infrastructure.getVO(), infrastructure.getFQAN(), true, infrastructure.getRFC(), commonName);
			else
				jobSubmission.setUserProxy(infrastructure.getUserProxy());
		}
		else if (infrastructure.getMiddleware().equals("unicore")) {
			jobSubmission.setJKS(infrastructure.getKeystore(), infrastructure.getPassword());
		}
		else if (infrastructure.getMiddleware().equals("ourgrid")) {
			jobSubmission.setOurGridCredential(infrastructure.getUserName(), infrastructure.getPassword());
		}
		else if (infrastructure.getMiddleware().equals("bes-genesis2")) {
			jobSubmission.setGenesisJKS(infrastructure.getKeystore(), infrastructure.getPassword());
		}
		else if (infrastructure.getMiddleware().equals("ssh")) {
			jobSubmission.setSSHCredential(infrastructure.getUserName(), infrastructure.getPassword());
		}
		
		if ( (infrastructure.getMiddleware().equals("ourgrid")) && (!(description.getExecutable().equals("/bin/sh"))) ) {
			jobSubmission.setExecutable("/bin/sh");

			if (!(description.getArguments().equals("")))
				description.setExecutable(description.getExecutable() + " " + description.getArguments());

			jobSubmission.setArguments(description.getExecutable());
		}
		if (infrastructure.getMiddleware().equals("glite")) {
			String tmp = description.getJDLRequirements();
			String[] JDLRequirements = null;
			if(tmp!=null){
				JDLRequirements = description.getJDLRequirements().split(";");
			}
			
			if (infrastructure.getSWTag().equals("")) {
				if (JDLRequirements!=null){
					logger.info("No SWTag specified, setting JDL Requirements.");
					jobSubmission.setJDLRequirements(JDLRequirements);
				}
			}
			else {
				//Member("VO-prod.vo.eu-eela.eu-OCTAVE-3.2.4",other.GlueHostApplicationSoftwareRunTimeEnvironment);
				String requirements[] = null;
				String swTags[] = null;
				if (JDLRequirements!=null) {
					requirements = new String[JDLRequirements.length+1];
					logger.info("Adding SWTag: " + infrastructure.getSWTag() + " to JDL Requirements.");
					swTags=infrastructure.getSWTag().split(",");
					if(swTags!=null){
						requirements[0] = "JDLRequirements=(";
						for (int i = 0; i < swTags.length; i++){
							if(i==0)
								requirements[0] += "Member(\""+swTags[i].replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment)";
							else
								requirements[0] += " || Member(\""+swTags[i].replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment)";
						}
						requirements[0] += ")";
					} else {
						requirements[0] = "JDLRequirements=(Member(\""+infrastructure.getSWTag().replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
					}
									
					for (int i=0;i<JDLRequirements.length;i++){
						requirements[i+1] = JDLRequirements[i];
					}
				}
				else {
					logger.info("Adding SWTag: " + infrastructure.getSWTag());
					requirements = new String[1];
					swTags=infrastructure.getSWTag().split(",");
					if(swTags!=null){
						requirements[0] = "JDLRequirements=(";
						for (int i = 0; i < swTags.length; i++){
							if(i==0)
								requirements[0] += "Member(\""+swTags[i].replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment)";
							else
								requirements[0] += " || Member(\""+swTags[i].replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment)";
						}
						requirements[0] += ")";
					} else {
						requirements[0] = "JDLRequirements=(Member(\""+infrastructure.getSWTag().replace(" ", "")+"\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
					}
					
				}
				String req = "";
				for (int i = 0; i < requirements.length; i++) {
					logger.info("JDL Requirements[" + i +"]: " + requirements[i]);
					if(i!=0){
						req += ";" + requirements[i];
					}
					else
						req = requirements[i];
				}
				description.setJDLRequirements(req);
				description.saveJobDescription();
				jobSubmission.setJDLRequirements(requirements);
			}
		}

		if(!userEmail.equals(""))
			jobSubmission.setUserEmail(userEmail);
		
		return jobSubmission;
	}
	
	public JobId submitJob(String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		JSagaJobSubmission jobSubmission= createJobSubmissionObject(commonName);
		if (!checkJobsStatus) jobSubmission.setCheckJobsStatus(false);
		if (randomCE) jobSubmission.setRandomCE(true);

		return jobSubmission.submitJob(commonName, tcpAddress, GridInteractionId, userDescription);
	}
	
	public void submitJobAsync(String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		JSagaJobSubmission jobSubmission= createJobSubmissionObject(commonName);
		if (!checkJobsStatus) jobSubmission.setCheckJobsStatus(false);
		if (randomCE) jobSubmission.setRandomCE(true);

		jobSubmission.submitJobAsync(commonName, tcpAddress, GridInteractionId, userDescription);
	}
	
	public JobId submitJob(InfrastructureInfo infrastructure, String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		JSagaJobSubmission jobSubmission= createJobSubmissionObject(commonName, infrastructure);
		if (!checkJobsStatus) jobSubmission.setCheckJobsStatus(false);
		if (randomCE) jobSubmission.setRandomCE(true);

		return jobSubmission.submitJob(commonName, tcpAddress, GridInteractionId, userDescription);
	}
	
	/**
	 * This method allows to submit a job in an asynchronous way to a specified infrastructure 
	 * It starts a separated thread responsible for the submission of this job.
	 * 
	 * @param infrastructure infrastructure to which the job will be submitted
	 * @param commonName a String representing user name
	 * @param tcpAddress user's IP address
	 * @param GridInteractionId an identifier of application in a specified portal
	 * @param userDescription a description for this job.
	 */
	public void submitJobAsync(InfrastructureInfo infrastructure, String commonName, String tcpAddress, int GridInteractionId, String userDescription) {
		JSagaJobSubmission jobSubmission= createJobSubmissionObject(commonName, infrastructure);
		if (!checkJobsStatus) jobSubmission.setCheckJobsStatus(false);
		if (randomCE) jobSubmission.setRandomCE(true);
				
		jobSubmission.submitJobAsync(commonName, tcpAddress, GridInteractionId, userDescription);
	}

	public static void main(String [] args)
	{
		
		Properties p = new Properties(System.getProperties());
		p.setProperty("GLOBUS_TCP_PORT_RANGE", "20000,25000");
		System.setProperties(p); 
		System.out.println("GLOBUS_TCP_PORT_RANGE: " + p.getProperty("GLOBUS_TCP_PORT_RANGE"));
		p.setProperty("GridEngineLogConfig.path", "/home/mario/Documents/GridEngineLogConfig.xml");
		p.setProperty("GridEngineLog.path", "/home/mario/Documents/logs/");
		InfrastructureInfo infrastructures[] = new InfrastructureInfo[6];
		
		//gridit
//		String wmsList[] = {"wms://prod-wms-01.pd.infn.it:7443/glite_wms_wmproxy_server"};//,
//		String wmsList[] = {"wms://marwms.in2p3.fr:7443/glite_wms_wmproxy_server"};
//				"wms://gridit-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server",
//				"wms://egee-rb-09.cnaf.infn.it:7443/glite_wms_wmproxy_server",
//				"wms://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server",
////			"wms://wms013.cnaf.infn.it:7443/glite_wms_wmproxy_server",
//				"wms://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server"};
		
		String CEs[] = {"ce-01.roma3.infn.it:8443/cream-pbs-grid"
				//"ce-02.roma3.infn.it:8443/cream-pbs-grid"
//						"grisuce.scope.unina.it:8443/cream-pbs-grisu_short" 
//						//"cccreamceli09.in2p3.fr:8443/cream-sge-long"
						};
		String wmsList[] = {"wms://prod-wms-01.ct.infn.it:7443/glite_wms_wmproxy_server"};
//		String wmsList[] = {"wms://wms-4.dir.garr.it:7443/glite_wms_wmproxy_server"};
//		String wmsList[] = {"wms://prod-wms-01.pd.infn.it:7443/glite_wms_wmproxy_server"};
//		String wmsList[] = {"wms://wms.magrid.ma:7443/glite_wms_wmproxy_server"};
		String wmsListSEEGRID[] = {"wms://wms01.afroditi.hellasgrid.gr:7443/glite_wms_wmproxy_server"};
//		String CEsSEE[] ={"cream-ce01.ariagni.hellasgrid.gr:8443/cream-pbs-see"};
		//		infrastructures[0] = new InfrastructureInfo("SEEGRID","ldap://bdii.hellasgrid.gr:2170", wmsListSEEGRID, "/tmp/proxy","");
//		infrastructures[0] = new InfrastructureInfo("SEEGRID",wmsListSEEGRID, "212.189.145.106","8082","bc681e2bd4c3ace2a4c54907ea0c379b","see","see",CEsSEE);
//		infrastructures[0] = new InfrastructureInfo("SEEGRID","ldap://bdii.hellasgrid.gr:2170", wmsListSEEGRID, "etokenserver.ct.infn.it","8082","bc681e2bd4c3ace2a4c54907ea0c379b","see","see", true, "VO-see-GROMACS-4.6.5-sl6-x86_64-gccany, VO-see-GROMACS-4.6.5-sl5-x86_64-gccany");
//		String wmsList[] = {"wms://srvslngrd010.uct.ac.za:7443"};
//		infrastructures[0] = new InfrastructureInfo("gridit","ldap://egee-bdii.cnaf.infn.it:2170", wmsList, "etokenserver.ct.infn.it","8082","bc779e33367eaad7882b9dfaa83a432c","gridit","gridit", "PROVA",false);
		

		infrastructures[0] = new InfrastructureInfo("gridit 0", "ldap://egee-bdii.cnaf.infn.it:2170", wmsList, "etokenserver.ct.infn.it","8082","bc779e33367eaad7882b9dfaa83a432c","gridit","gridit");
//		infrastructures[4] = new InfrastructureInfo("gridit 1","ldap://gridit-bdii-01.cnaf.infn.it:2170", wmsList, "/tmp/proxy","");
//		infrastructures[0] = new InfrastructureInfo("gridit", wmsList, "/tmp/proxy","");
//		infrastructures[2] = new InfrastructureInfo("gridit 2", wmsList, "/tmp/proxy","",CEs);
		//String wmsListGARUDA[] = {"wsgram://xn03.ctsf.cdacb.in:8443/GW"};
		String wmsListGARUDA[] = {"gatekeeper://xn03.ctsf.cdacb.in:2119/jobmanager-gw"};
		infrastructures[1] = new InfrastructureInfo("GARUDA","", wmsListGARUDA, "/home/mario/x509up_u500", "");
		//infrastructures[1] = new InfrastructureInfo("GARUDA","wsgram","", wmsListGARUDA, "etokenserver.ct.infn.it","8082","332576f78a4fe70a52048043e90cd11f","gridit","gridit");
//		String sshList[] = {"ssh://api.ct.infn.it"};
//		String sshList[] = {"ssh://90.147.74.95"}; //jobtest
//		String sshList[] = {"ssh://90.147.74.77"}; //futuregateway
		String sshList[] = {"ssh://151.97.41.60"}; //jsaga
//		String sshList[] = {"ssh://151.97.41.51"}; //repast
//		String sshList[] = {"ssh://cresco1-f1.portici.enea.it"};
//		infrastructures[2] = new InfrastructureInfo("SSH Infrastructure", "ssh", "saga_igi", "sagaXigi", sshList);
//		infrastructures[2] = new InfrastructureInfo("SSH Infrastructure", "ssh", "jobtest", "Xvf56jZ751f", sshList);
		infrastructures[2] = new InfrastructureInfo("SSH Infrastructure", "ssh", "jsaga", sshList);
		String wmsListUnicore[] = {"unicore://zam052v01.zam.kfa-juelich.de:8080/?Target=EMI-UNICOREX"};
		//String wmsListUnicore[] = {"unicore://zam052v02.zam.kfa-juelich.de:8080/?Target=EMI-UNICOREX-RC"};
		infrastructures[4] = new InfrastructureInfo("Unicore","unicore","/tmp/robot2012.jks", "robot2012", wmsListUnicore);
//		
//		String wmsListOurGrid[] = {"ourgrid://api.ourgrid.org"};
//		infrastructures[3] = new InfrastructureInfo("OurGrid","ourgrid","diego", "scardaci", wmsListOurGrid);
//		
//		String wmsListGenesis[] = {"bes-genesis2://xcg-server1.uvacse.virginia.edu:20443/axis/services/GeniiBESPortType?genii-container-id=93B641B7-9422-EA4C-A90B-CA6A9D98E344"};
//		infrastructures[4] = new InfrastructureInfo("GenesisII","bes-genesis2","/home/diego/genesisII/genesis-keys.jks", "chaindemo", wmsListGenesis);
//		
//		String wmsListGOS[] = {"gos://124.205.18.242"};
//		infrastructures[0] = new InfrastructureInfo("GOS","gos", wmsListGOS);
		
		//cometa
		//infrastructures[1] = new InfrastructureInfo("cometa","ldap://infn-bdii-01.ct.pi2s2.it:2170", wmsList2,"/home/diego/proxy_cometa.txt");
		//infrastructures[1] = new InfrastructureInfo("cometa","ldap://infn-bdii-01.ct.pi2s2.it:2170", wmsList2, "myproxy.ct.infn.it","8082","21174","cometa","cometa");
//		String EUMEDwmsList[] = {"wms://wms.ulakbim.gov.tr:7443/glite_wms_wmproxy_server"};
//		infrastructures[0] = new InfrastructureInfo("EUMEDGRID",
//				"ldap://bdii.eumedgrid.eu:2170", EUMEDwmsList,
//				"etokenserver.ct.infn.it", "8082",
//				"bc681e2bd4c3ace2a4c54907ea0c379b", "eumed", "eumed", true);
//		
		
		//https://egi-cloud.zam.kfa-juelich.de:8787
		// https://okeanos-occi2.hellasgrid.gr:9000
//		String OCCI_ENDPOINT_HOST = "rocci://carach5.ics.muni.cz";
		String OCCI_ENDPOINT_HOST = "rocci://nebula-server-01.ct.infn.it";
		
		//String OCCI_ENDPOINT_HOST = "rocci://okeanos-occi2.hellasgrid.gr";
		//String OCCI_ENDPOINT_PORT = "11443";        
		String OCCI_ENDPOINT_PORT = "9000";        
		String OCCI_AUTH = "x509";
        
        // Possible RESOURCE values: 'os_tpl', 'resource_tpl', 'compute'
		String OCCI_RESOURCE = "compute";
		//String OCCI_RESOURCE_ID = "https://carach5.ics.muni.cz:11443/compute/a0ad539e-ad17-4309-bc9c-4f9f91aecbaa";
		String OCCI_VM_TITLE = "MyDebianROCCITest";
        
        // Possible OCCI_OS values: 'debianvm', 'octave', 'r' and 'generic_www'
		String OCCI_OS = "debianvm";        
		String OCCI_FLAVOUR = "small";                

        // Possible ACTION values: 'list', 'describe', 'create' and 'delete'
		String OCCI_ACTION = "create";    
        String OCCI_PUBLIC_KEY = "/home/larocca/.ssh/id_rsa.pub";
        String OCCI_PRIVATE_KEY = "/home/larocca/.ssh/id_rsa";
        
		String rOCCIURL = OCCI_ENDPOINT_HOST + ":" + 
                 OCCI_ENDPOINT_PORT + 
                 System.getProperty("file.separator") + "?" +
                 "action=" + OCCI_ACTION + 
                 "&resource=" + OCCI_RESOURCE +
                 "&attributes_title=" + OCCI_VM_TITLE +
                 "&mixin_os_tpl=" + OCCI_OS +
                 "&mixin_resource_tpl=" + OCCI_FLAVOUR +
                 "&auth=" + OCCI_AUTH +
                 "&publickey_file=" + OCCI_PUBLIC_KEY +                                     
                 "&privatekey_file=" + OCCI_PRIVATE_KEY;
		
		String rOCCIResourcesList[] = {rOCCIURL};
		
		System.out.println("defining cloud infrastructure...");
		
//		infrastructures[0] = new InfrastructureInfo("EGI-FEDCLOUD","rocci", "", rOCCIResourcesList, "etokenserver.ct.infn.it","8082","bc779e33367eaad7882b9dfaa83a432c","fedcloud.egi.eu","fedcloud.egi.eu",true);
						
		
		//gisela
//        String wmsListGisela[] = {"wms://wms.eela.ufrj.br:7443/glite_wms_wmproxy_server"};
//        infrastructures[2] = new InfrastructureInfo("prod.vo.eu-eela.eu","ldap://bdii.eela.ufrj.br:2170", wmsListGisela, "myproxy.ct.infn.it","8082","21873","prod.vo.eu-eela.eu","prod.vo.eu-eela.eu","VO-prod.vo.eu-eela.eu-OCTAVE-3.2.4");
//
//        String wmsListEumed[] = {"wms://infn-wms-01.ct.pi2s2.it:7443/glite_wms_wmproxy_server"};
//        infrastructures[3] = new InfrastructureInfo("eumed","ldap://topbdii.junet.edu.jo:2170", wmsListEumed, "myproxy.ct.infn.it","8082","21057","eumed","eumed");
//   
//        String wmsListDecide[] = {"wms://infn-wms-01.ct.pi2s2.it:7443/glite_wms_wmproxy_server"};
//        infrastructures[4] = new InfrastructureInfo("decide","ldap://bdii.eu-decide.eu:2170", wmsListDecide, "myproxy.ct.infn.it","8082","21143","vo.eu-decide.eu","/vo.eu-decide.eu/GridANN4ND/Role=Scientist");

	
		int num_job = 1;
		
		for (int i=0;i<num_job;i++) {
			MultiInfrastructureJobSubmission multiInfrastructureJobSubmission = new MultiInfrastructureJobSubmission("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
//			MultiInfrastructureJobSubmission multiInfrastructureJobSubmission = new MultiInfrastructureJobSubmission();
//			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[0]);
//			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[1]);
			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[2]);
//			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[3]);
//			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[4]);
//			multiInfrastructureJobSubmission.addInfrastructure(infrastructures[5]);
//			multiInfrastructureJobSubmission.setRandomCE(true);

			GEJobDescription description = new GEJobDescription();
			description.setExecutable("/bin/sh");
			description.setArguments("hostname.sh");
			description.setInputFiles("/home/mario/Documents/hostname.sh");
//			description.setOutputFiles("output.README");
			description.setOutputPath("/tmp");
			description.setOutput("myOutput-" + i + ".txt");
//			description.setOutput("PippomyOutput-" + i + ".txt");
			description.setError("myError-" + i + ".txt");
//			String jdlRequirements = "JDLRequirements=(Member(\"MPI-START\",other.GlueHostApplicationSoftwareRunTimeEnvironment));JDLRequirements=(Member(\"MPICH\", other.GlueHostApplicationSoftwareRunTimeEnvironment))";
//			description.setJDLRequirements(jdlRequirements);
			multiInfrastructureJobSubmission.setDescription(description);
			InfrastructureInfo infrastructure = multiInfrastructureJobSubmission.getInfrastructure();
			
			if ( (infrastructure.getMiddleware().equals("rocci")) || (infrastructure.getMiddleware().equals("glite")) || (infrastructure.getMiddleware().equals("wsgram")) || (infrastructure.getMiddleware().equals("unicore")) || (infrastructure.getMiddleware().equals("ourgrid")) || (infrastructure.getMiddleware().equals("gos")) || (infrastructure.getMiddleware().equals("ssh")) ) {
//				multiInfrastructureJobSubmission.setExecutable("/bin/sh");
//				multiInfrastructureJobSubmission.setArguments("hostname.sh");
//				multiInfrastructureJobSubmission.setOutputPath("/tmp");
//				multiInfrastructureJobSubmission.setJobOutput("myOutput-" + i + ".txt");
//				multiInfrastructureJobSubmission.setJobError("myError-" + i + ".txt");
//				multiInfrastructureJobSubmission.setInputFiles("/home/mario/Documents/hostname.sh");
//				multiInfrastructureJobSubmission.setRandomCE(true);
				
				//***************MARIO*****************
				multiInfrastructureJobSubmission.setUserEmail("mario.torrisi@ct.infn.it");
				multiInfrastructureJobSubmission.setSenderEmail("mario.torrisi@ct.infn.it");
				//multiInfrastructureJobSubmission.setSPMDVariation("");
				//multiInfrastructureJobSubmission.setNumberOfProcesses("");
				//*************************************
				//multiInfrastructureJobSubmission.setInputFiles("/home/diego/hostname.sh");
				//multiInfrastructureJobSubmission.setJobQueue("grid-ce.bio.dist.unige.it:8443/cream-pbs-infinite");
			}
			else if ( (infrastructure.getMiddleware().equals("bes-genesis2")) ) {
				multiInfrastructureJobSubmission.setExecutable("/bin/hostname");
				multiInfrastructureJobSubmission.setArguments("-f");
				multiInfrastructureJobSubmission.setOutputPath("/tmp");
				multiInfrastructureJobSubmission.setJobOutput("stdout");
			}
			//multiInfrastructureJobSubmission.submitJobAsync(infrastructure, "scardaci", "193.206.208.183:8162", 1, "J  o  b  -"+i);
			
//			if (infrastructure.getMiddleware().equals("wsgram")) {
//				//WSGRAM MPI
//				multiInfrastructureJobSubmission.setExecutable("diego_mpi_xn03");
//				multiInfrastructureJobSubmission.setSPMDVariation("MPI");
//				multiInfrastructureJobSubmission.setInputFiles("/home/diego/mpitest/diego_mpi_xn03");
//			}
//			else if (infrastructure.getMiddleware().equals("unicore")) {
//				//UNICORE MPI
//				multiInfrastructureJobSubmission.setExecutable("./hello.mpi");
//				multiInfrastructureJobSubmission.setSPMDVariation("OpenMPI");
//				multiInfrastructureJobSubmission.setTotalCPUCount("4");
//				multiInfrastructureJobSubmission.setInputFiles("/home/diego/UnicoreOutput/hello.mpi");
//			}
//
//			multiInfrastructureJobSubmission.setNumberOfProcesses("4");
//			multiInfrastructureJobSubmission.setJobOutput("myOutput-" + i + ".txt");
//			multiInfrastructureJobSubmission.setJobError("myError-" + i + ".txt");
//			multiInfrastructureJobSubmission.setOutputPath("/tmp");
			
			//multiInfrastructureJobSubmission.setJDLRequirements(new String[] {"JDLRequirements=(Member(\"MPI-START\",other.GlueHostApplicationSoftwareRunTimeEnvironment))","JDLRequirements=(Member(\"MPICH\", other.GlueHostApplicationSoftwareRunTimeEnvironment))" });
			multiInfrastructureJobSubmission.submitJobAsync(infrastructure, "test", "193.206.208.183:8162", 1, "J  o  b  -"+i);

//			if (i%num_job==0)
//				multiInfrastructureJobSubmission.submitJobAsync("scardaci", "193.206.208.183:8162", 1, "J  o  b  -"+i);
//			else if (i%num_job==1)
//				multiInfrastructureJobSubmission.submitJobAsync("test1", "193.206.208.183:8162", 1, "J  o  b  -"+i);
//			else
//				multiInfrastructureJobSubmission.submitJobAsync("test2", "193.206.208.183:8162", 1, "J  o  b  -"+i);
		}
		
//		UsersTrackingDBInterface DBInterface = new UsersTrackingDBInterface("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
//		Vector<String[]> cesListwithCoord = null;
//		
//		System.out.println("Vado in sleep...");
//		while(true) {
//			try {
//				Thread.sleep(120000);
//
//				cesListwithCoord = DBInterface.getCEsGeographicDistribution("ViralGrid","scardaci");
//
//				if (cesListwithCoord!=null) {
//					for (int i=0;i<cesListwithCoord.size();i++) {
//						System.out.println("CE = " + cesListwithCoord.elementAt(i)[0] + " Num Jobs = " + cesListwithCoord.elementAt(i)[1] + " - Lat = " + cesListwithCoord.elementAt(i)[2] + " - Long = " + cesListwithCoord.elementAt(i)[3]);
//					}
//				}
//			}
//			catch (Exception e) {}
//		}
		
		System.out.println("Vado in sleep...");
		try {
			Thread.sleep(300000);
		}
		catch (Exception e) {}
		
		System.out.println("Checking jobs...");
		
		UsersTrackingDBInterface DBInterface = new UsersTrackingDBInterface("jdbc:mysql://localhost/userstracking","tracking_user","usertracking");
		String status = "RUNNING";
		Vector<ActiveInteractions> jobList = null;
		
		while(status.equals("RUNNING")) {
			try {
				Thread.sleep(15000);
			}
			catch (Exception e) {}
		
			System.out.println("Users running jobs = " + DBInterface.getTotalNumberOfUsersWithRunningJobs());
			status = "DONE";
//			jobList = DBInterface.getActiveJobsListByName("mtorrisi");
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
			
			if (status.equals("RUNNING")) {
				try {
					Thread.sleep(15000);
				}
				catch (Exception e) {}
			}
			
		}
		
//		String allTarPath = DBInterface.createAllJobsFromDescriptionArchive("mtorrisi", "J  o  b  -", "/tmp");
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

	private void setDescription(GEJobDescription description) {
		this.description = description;
		
	}
		
}
