package it.infn.ct.GridEngine.JobCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.infn.ct.GridEngine.Job.JSagaJobSubmission;
import it.infn.ct.GridEngine.JobResubmission.GEActiveGridInteraction;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.JobResubmission.GESessionFactoryUtil;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

//@Entity
//@Table(name="ActiveJobCollections")
//@DiscriminatorValue("WORKFLOW_N1")
/**
 * This class represent a simple work flow which performs a number of parallel
 * jobs, the outputs of this jobs are inputs for the final job. When final job
 * has been successfully completed the work flow goes in DONE state.
 * 
 * @author mario
 * 
 */
public class WorkflowN1 extends JobCollection {

//	@Column (name="id_final_job")
	private Long idFinalJob;

//	@Transient
	private GEJobDescription finalJobDescription;
//	@Transient
	private String[] inputFilePrefixes;

	/**
	 * Constructs a WorkFlowN1 object without a specified user email and in
	 * which all first level jobs outputs are inputs for final job.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a work flow description
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            work flow are stored
	 * @param subJobDescriptions
	 *            an ArrayList of GEJobDescription that represents the entire
	 *            sub-job descriptions
	 * @param finalJobDescription
	 *            description of the work flow final job.
	 */
	public WorkflowN1(String commonName, String description,
			String outputPath, ArrayList<GEJobDescription> subJobDescriptions,
			GEJobDescription finalJobDescription) {
		// super(commonName, description, outputPath, subJobDescriptions);
		this(commonName, description, null, outputPath, subJobDescriptions,
				finalJobDescription, null);
		// this.finalJobDescription = finalJobDescription;
	}

	/**
	 * Constructs a WorkFlowN1 object without a specified user email and in
	 * which all first level jobs outputs are inputs for final job.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a work flow description
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            work flow are stored
	 * @param subJobDescriptions
	 *            an ArrayList of GEJobDescription that represents the entire
	 *            sub-job descriptions
	 * @param finalJobDescription
	 *            description of the work flow final job.
	 * @param finalJobInputFilePrefixes
	 *            a String[] with the first level output file names must have to
	 *            become input file for the final job.
	 */
	public WorkflowN1(String commonName, String description,
			String outputPath, ArrayList<GEJobDescription> subJobDescriptions,
			GEJobDescription finalJobDescription,String[] finalJobInputFilePrefixes) {
		// super(commonName, description, outputPath, subJobDescriptions);
		this(commonName, description, null, outputPath, subJobDescriptions,
				finalJobDescription, finalJobInputFilePrefixes);
		// this.finalJobDescription = finalJobDescription;
	}
	
	/**
	 * Constructs a WorkFlowN1 object with with the specified user email to
	 * notify that this work flow is completed. Using this constructor all first
	 * level jobs outputs are inputs for final job.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a work flow description
	 * @param userEmail
	 *            email address to notify completed work flow
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            work flow are stored
	 * @param subJobDescriptions
	 *            an ArrayList of GEJobDescription that represents the entire
	 *            sub-job descriptions
	 * @param finalJobDescription
	 *            description of the work flow final job.
	 */
	public WorkflowN1(String commonName, String description,
			String userEmail, String outputPath,
			ArrayList<GEJobDescription> subJobDescriptions,
			GEJobDescription finalJobDescription) {

		this(commonName, description, userEmail, outputPath,
				subJobDescriptions, finalJobDescription, null);
	}

	/**
	 * Constructs a WorkFlowN1 object with the specified user email to notify
	 * that this work flow has been completed and a list of prefixes that output
	 * file names of the first level job must have to become input file of the
	 * final job.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a work flow description
	 * @param userEmail
	 *            email address to notify completed work flow
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            work flow are stored
	 * @param subJobDescriptions
	 *            an ArrayList of GEJobDescription that represents the entire
	 *            sub-job descriptions
	 * @param finalJobDescription
	 *            description of the work flow final job.
	 * @param finalJobInputFilePrefixes
	 *            a String[] with the first level output file names must have to
	 *            become input file for the final job.
	 */
	public WorkflowN1(String commonName, String description,
			String userEmail, String outputPath,
			ArrayList<GEJobDescription> subJobDescriptions,
			GEJobDescription finalJobDescription,
			String[] finalJobInputFilePrefixes) {

		super.setCommonName(commonName);
		super.setDescription(description);
		super.setOutputPath(outputPath);
		super.setSubJobDescriptions(subJobDescriptions);
		super.setTaskCounter(subJobDescriptions.size());
		super.setUserEmail(userEmail);
		this.inputFilePrefixes = finalJobInputFilePrefixes;
		this.finalJobDescription = finalJobDescription;

		saveJobCollection();
	}

	protected WorkflowN1() {
		super();
	}

	/**
	 * Returns final Job description.
	 * 
	 * @return final Job description.
	 */
	public GEJobDescription getFinalJobDescription() {
		return finalJobDescription;
	}

	protected void setFinalJobDescription(GEJobDescription finalJobDescription) {
		this.finalJobDescription = finalJobDescription;
	}

	/**
	 * Returns final job database id.
	 * 
	 * @return final job database id.
	 */
	public Long getIdFinalJob() {
		return idFinalJob;
	}

	private void setIdFinalJob(Long idFinalJob) {
		this.idFinalJob = idFinalJob;
	}

	/**
	 * Returns a list of input file prefixes for final job of this work flow.
	 * 
	 * @return a list of input file prefixes for final job of this work flow.
	 */
	public String[] getInputFilePrefixes() {
		return inputFilePrefixes;
	}

	protected void setInputFilePrefixes(String[] inputFilePrefixes) {
		this.inputFilePrefixes = inputFilePrefixes;
	}
	
	@Override
	protected void saveJobCollection() {

		if (this.inputFilePrefixes != null) {
			StringTokenizer tmp = new StringTokenizer(
					this.finalJobDescription.getInputFiles(), ",");
			String[] oldFinalJobInputFiles = new String[tmp.countTokens()];
			String[] newFinalJobInputFiles = new String[tmp.countTokens()];
			for (int i = 0; tmp.hasMoreTokens(); i++) {
				oldFinalJobInputFiles[i] = tmp.nextToken();
				newFinalJobInputFiles[i] = " ";
			}

			for (String inputFilePrefix : this.inputFilePrefixes) {
				for (int i = 0; i < this.getSubJobDescriptions().size(); i++)

					if (this.getSubJobDescriptions().get(i).getOutput()
							.contains(inputFilePrefix)) {
						newFinalJobInputFiles[i] = oldFinalJobInputFiles[i];
					}
			}
			for (int i = 0; i < oldFinalJobInputFiles.length; i++) {
				if (oldFinalJobInputFiles[i].contains("/"))
					newFinalJobInputFiles[i] = oldFinalJobInputFiles[i];
			}

			this.finalJobDescription.setInputFiles("");
			for (String s : newFinalJobInputFiles) {

				if (this.finalJobDescription.getInputFiles().equals(""))
					this.finalJobDescription.setInputFiles(s);
				else
					this.finalJobDescription
							.setInputFiles(this.finalJobDescription
									.getInputFiles() + "," + s);
			}

		}

		this.finalJobDescription.saveJobDescription();
		this.setIdFinalJob(this.finalJobDescription.getId());

		super.saveJobCollection();
	}

	// @Override
	// public void saveJobCollection() {
	//
	// this.finalJobDescription.saveJobDescription();
	// this.setIdFinalJob(this.finalJobDescription.getId());
	//
	// super.saveJobCollection();
	// }
	
	/**
	 * Returns a boolean that represent if this work flow is in final status or
	 * not. A WorkFlowN1 is in final status if all its sub-jobs (first level sub-jobs and final job) aren't in
	 * RUNING or in SUBMITTED state.
	 * 
	 * @return true if WorkFlowN1 is in final status, false otherwise.
	 */
	@Override
	public boolean isInFinalStatus() {
		if (!this.getCollectionStatus().equals("SUBMITTING_FINAL_JOB")
				&& !this.getCollectionStatus().equals("RUNNING_FINAL_JOB"))
			return super.isInFinalStatus();
		else {
			boolean result = false;
			Session session = GESessionFactoryUtil.getSessionfactory()
					.openSession();

			Transaction tx = null;
			try {
				tx = session.beginTransaction();

				Query q = session
						.createSQLQuery("SELECT count(*) FROM ActiveGridInteractions WHERE id_job_collection = :jobCollectionId AND (status <> 'RUNNING' AND status <> 'SUBMITTED')");
				q.setParameter("jobCollectionId", this.getId());
				java.math.BigInteger tmp = (java.math.BigInteger) q
						.uniqueResult();
				int activeJobCont = tmp.intValue();

				if (activeJobCont == this.getTaskCounter() + 1)
					result = true;
				else
					result = false;
				tx.commit();
			} catch (HibernateException he) {
				if (tx != null)
					tx.rollback();
				he.printStackTrace();

			} finally {
				session.close();
			}
			return result;
		}
	}

	/**
	 * This method handles state updating for this WorkFlowN1.
	 * 
	 * @param inFinalStatus
	 */
	@Override
	public void updateJobCollectionStatus(boolean inFinalStatus) {

		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			String newStatus = "";

			tx = session.beginTransaction();

			if (inFinalStatus) {

				Query q = session
						.createSQLQuery("SELECT count(*) FROM ActiveGridInteractions WHERE id_job_collection = :jobCollectionId AND status = 'DONE'");
				q.setParameter("jobCollectionId", this.getId());
				java.math.BigInteger tmp = (java.math.BigInteger) q
						.uniqueResult();
				int result = tmp.intValue();

				if (this.getCollectionStatus().equals("SUBMITTING_FINAL_JOB")
						|| this.getCollectionStatus().equals(
								"RUNNING_FINAL_JOB")) {
					if (result == this.getTaskCounter() + 1) {
						newStatus = "DONE";
						this.setEndTimestamp(getCurrentUTCTimestamp());
					} else {
						newStatus = "Aborted";
						this.setEndTimestamp(getCurrentUTCTimestamp());
					}
				} else if (result == this.getTaskCounter()) {
					if (!this.getCollectionStatus().equals(
							"SUBMITTING_FINAL_JOB")
							&& !this.getCollectionStatus().equals(
									"RUNNING_FINAL_JOB")) {
						// this.setCollectionStatus("SUBMITTING_FINAL_JOB");
						newStatus = "SUBMITTING_FINAL_JOB";
						// this.setTaskCounter(this.getTaskCounter()+1);
					} else {
						// this.setCollectionStatus("DONE");
						newStatus = "DONE";
						this.setEndTimestamp(getCurrentUTCTimestamp());
					}
				} else {
					// this.setCollectionStatus("Aborted");
					newStatus = "Aborted";
					this.setEndTimestamp(getCurrentUTCTimestamp());
				}
			} else if (this.getCollectionStatus().equalsIgnoreCase(
					"SUBMITTING_FINAL_JOB")) {
				// this.setCollectionStatus("RUNNING_FINAL_JOB");
				newStatus = "RUNNING_FINAL_JOB";
			} else if (!this.getCollectionStatus().equalsIgnoreCase(
					"RUNNING_FINAL_JOB")) {
				// this.setCollectionStatus("RUNNING");
				newStatus = "RUNNING";
			} else
				newStatus = this.getCollectionStatus();

			if (!newStatus.equals(this.getCollectionStatus())) {
				this.setCollectionStatus(newStatus);
				session.update(this);
			}

			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			he.printStackTrace();

		} finally {
			session.close();
		}

	}

	/**
	 * This method closes a WorkFlowN1 with "Aborted" state because at least
	 * one of its sub-job or its fina job has been unsucessfully completed for a number of times
	 * greater than maximum re-submission attempts allowed.
	 * 
	 * @param dbInt
	 *            an interface to the database where make persistent this
	 *            information.
	 */
	@Override
	public void abortCollection(UsersTrackingDBInterface dbInt) {
		super.abortCollection(dbInt);

		GEJobDescription jobDescr = GEJobDescription
				.findJobDescriptionById(this.getIdFinalJob());
		if (jobDescr != null)
			jobDescr.delete();
	}

	/**
	 * This method is responsible for the final job submission for this work flow. 
	 *
	 * @param dbInt an interface to user tracking database.
	 */
	public void submitFinalJob(UsersTrackingDBInterface dbInt) {
		// logger.info("Closing Active sub-jobs...");

		List<GEActiveGridInteraction> doneSubJobs = GEActiveGridInteraction.findActiveJobForJobCollection(this.getId());
		List<GEActiveGridInteraction> specialDoneSubJobs = new ArrayList<GEActiveGridInteraction>();

		logger.info("Submitting Final Job collection: " + this.toString());
		JSagaJobSubmission tmpJSaga = new JSagaJobSubmission(dbInt);

		GEJobDescription finalJobDescription = GEJobDescription
				.findJobDescriptionById(this.getIdFinalJob());
		StringTokenizer tmp = new StringTokenizer(
				finalJobDescription.getInputFiles(), ",");
		String[] outpuFilesSuffix = new String[tmp.countTokens()];
		for (int i = 0; tmp.hasMoreTokens(); i++) {
			outpuFilesSuffix[i] = tmp.nextToken();
		}

		// for (int i = 0; i<outpuFilesSuffix.length; i++) {
		for (GEActiveGridInteraction activeGridInteraction : doneSubJobs) {
			StringTokenizer st = new StringTokenizer(
					activeGridInteraction.getUserDescription(), "_");
			st.nextToken();
			if (!outpuFilesSuffix[Integer.parseInt(st.nextToken())].equals(" "))
				specialDoneSubJobs.add(activeGridInteraction);
		}
		// }

		String inputFilesPathPrefix = this.getOutputPath()
				+ JSagaJobSubmission.removeNotAllowedCharacter(this
						.getDescription()) + "_" + this.getId();
		String inputFilesPath = "";
		// for (GEActiveGridInteraction activeGridInteraction : doneSubJobs) {
		for (GEActiveGridInteraction activeGridInteraction : specialDoneSubJobs) {
			if (!inputFilesPath.equals(""))
				inputFilesPath += ",";
			StringTokenizer st = new StringTokenizer(
					activeGridInteraction.getUserDescription(), "_");
			st.nextToken();
			inputFilesPath += inputFilesPathPrefix
					+ "/"
					+ JSagaJobSubmission
							.removeNotAllowedCharacter(activeGridInteraction
									.getUserDescription()) + "_"
					+ activeGridInteraction.getId() + "/"
					+ outpuFilesSuffix[Integer.parseInt(st.nextToken())];
		}
		int additionalInputFilesCount = outpuFilesSuffix.length
				- doneSubJobs.size();
		if (additionalInputFilesCount > 0) {
			for (int i = doneSubJobs.size(); i < outpuFilesSuffix.length; i++)
				inputFilesPath += "," + outpuFilesSuffix[i];
		}
		logger.debug("Final Job inputFilesPath: " + inputFilesPath);
		finalJobDescription.setInputFiles(inputFilesPath);

		finalJobDescription.saveJobDescription();

		tmpJSaga.submitFinalJob(doneSubJobs.get(0), finalJobDescription);
	}
	
}
