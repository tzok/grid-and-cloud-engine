package it.infn.ct.GridEngine.JobResubmission;

import it.infn.ct.GridEngine.JobCollection.WorkflowN1;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Table;

//@Entity
//@Table (name="JobDescription")
/**
 * This class is a persistence class used to mapping a job description in
 * database, with an object.
 * 
 * @author mario
 * 
 */
public class GEJobDescription {

	// private static final Session session =
	// GESessionFactoryUtil.getSessionfactory().getCurrentSession();//.openSession();
	// @Id
	// @GeneratedValue
	// @Column (name="id")
	private Long id; // primary key

	// @Column
	private String jobId;

	// @Column (name="executable")
	private String executable; // JobDescription executable attribute

	// @Column (name="arguments")
	private String arguments;

	// @Column (name="output")
	private String output;

	// @Column (name="error")
	private String error;

	// @Column (name="queue")
	private String queue;

	// @Column (name="file_transfer")
	private String fileTransfer;

	// @Column (name="total_cpu")
	private String totalCPUCount;

	// @Column (name="SPDM_variation")
	private String SPDMVariation;

	// @Column (name="number_of_processes")
	private String numberOfProcesses;

	// @Column (name="JDL_requirements")
	private String JDLRequirements;

	// @Column (name="output_path")
	private String outputPath;

	// @Column (name="input_files")
	private String inputFiles;
	
	// @Column (name="output_files")
	private String outputFiles; 

	// @Column (name="proxy_renewal")
	private boolean proxyRenewal;

	// @Column (name="maxResubmitCount")
	private int resubmitCount;

	public GEJobDescription() {
	}

	/**
	 * Returns the database id for this description.
	 * 
	 * @return the database id for this description.
	 */
	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the specified executable for this description.
	 * 
	 * @return the specified executable for this description.
	 */
	public String getExecutable() {
		if (executable != null)
			return executable;
		else
			return "";
	}

	/**
	 * Sets the executable in this description.
	 * 
	 * @param executable
	 *            a string that represents the executable file name.
	 */
	public void setExecutable(String executable) {
		this.executable = executable;
	}

	/**
	 * Returns a comma separated string represents the list of the arguments for
	 * this description.
	 * 
	 * @return comma separated string represents the list of the arguments for
	 *         this description.
	 */
	public String getArguments() {
		if (arguments != null)
			return arguments;
		else
			return "";
	}

	/**
	 * Sets the arguments for this description.
	 * 
	 * @param arguments
	 *            a comma separated string containing the list of arguments.
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * Returns the grid job identifier.
	 * 
	 * @return the grid job identifier.
	 */
	public String getjobId() {
		return jobId;
	}

	/**
	 * Sets the grid job identifier for the job specified by this description.
	 * 
	 * @param activeJobId
	 *            string containing grid job identifier for the job specified by
	 *            this description.
	 */
	public void setjobId(String activeJobId) {
		this.jobId = activeJobId;
	}

	/**
	 * Returns the output file name specified in this description.
	 * 
	 * @return the output file name specified in this description.
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * Sets the output file name.
	 * 
	 * @param output
	 *            the output file name.
	 */
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * Returns the error file name specified in this description.
	 * 
	 * @return the error file name specified in this description.
	 */
	public String getError() {
		return error;
	}

	/**
	 * Sets the error file name.
	 * 
	 * @param error
	 *            the error file name.
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Returns queue name for this description.
	 * 
	 * @return queue Queue name.
	 */
	public String getQueue() {
		return queue;
	}

	/**
	 * Sets the queue name for the job specified by this description.
	 * 
	 * @param queue
	 *            Queue name.
	 */
	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getFileTransfer() {
		return fileTransfer;
	}

	public void setFileTransfer(String fileTransfer) {
		this.fileTransfer = fileTransfer;
	}

	/**
	 * Returns the total number of CPUs required to execute the job specified by
	 * this description.
	 * 
	 * @return total number of CPUs required to execute the job specified by
	 *         this description.
	 */
	public String getTotalCPUCount() {
		return totalCPUCount;
	}

	/**
	 * Sets the total number of CPUs required to execute the job specified by
	 * this description.
	 * 
	 * @param totalCPUCount
	 *            total number of CPUs required to execute the job.
	 */
	public void setTotalCPUCount(String totalCPUCount) {
		this.totalCPUCount = totalCPUCount;
	}

	public String getSPDMVariation() {
		return SPDMVariation;
	}

	public void setSPDMVariation(String sPDMVariation) {
		SPDMVariation = sPDMVariation;
	}

	/**
	 * Returns the total number of processes required by the job specified by
	 * this description.
	 * 
	 * @return the total number of processes required by the job specified by
	 *         this description.
	 */
	public String getNumberOfProcesses() {
		return numberOfProcesses;
	}

	/**
	 * Sets the total number of processes required by the job specified by this
	 * description.
	 * 
	 * @param numberOfProcess
	 *            total number of processes required by the job.
	 */
	public void setNumberOfProcesses(String numberOfProcess) {
		this.numberOfProcesses = numberOfProcess;
	}

	public String getJDLRequirements() {
		return JDLRequirements;
	}

	public void setJDLRequirements(String jDLRequirements) {
		JDLRequirements = jDLRequirements;
	}

	/**
	 * Returns output path where output files of the job specified by this
	 * description are stored.
	 * 
	 * @return output path where output files of the job specified by this
	 *         description are stored.
	 */
	public String getOutputPath() {
		return outputPath;
	}

	/**
	 * Sets output path where output files of the job specified by this
	 * description are stored. This method appends jobOutput folder to the
	 * specified path where files are really stored.
	 * 
	 * @param outputPath
	 *            prefix of the path of the folder followed by <b>/jobOuput/</b>
	 *            folder where output files are located..
	 */
	public void setOutputPath(String outputPath) {
		// this.outputPath = outputPath;
		if (!outputPath.contains("/jobOutput/"))
			this.outputPath = outputPath + "/jobOutput/";
		else
			this.outputPath = outputPath;
	}

	/**
	 * Returns a comma separated string representing the input files list for
	 * the the job specified by this description.
	 * 
	 * @return a comma separated string representing the input files list.
	 */
	public String getInputFiles() {
		if (inputFiles != null)
			return inputFiles;
		else
			return "";
	}

	/**
	 * Sets the input files list for the job specified by this description.
	 * 
	 * @param inputFiles
	 *            a comma separated string representing the input files list.
	 */
	public void setInputFiles(String inputFiles) {
		this.inputFiles = inputFiles;
	}
	
	public void setOutputFiles(String value) {
		this.outputFiles = value;
		
	}

	public String getOutputFiles() {
		if (outputFiles != null)
			return outputFiles;
		else
			return "";
	}
	
	/**
	 * Returns true if the robot proxy is renewable false otherwise.
	 * 
	 * @return true if the robot proxy is renewable false otherwise
	 */
	public boolean isProxyRenewal() {
		return proxyRenewal;
	}

	/**
	 * Sets true if the robot proxy is renewable false otherwise.
	 * 
	 * @param proxyRenewal
	 *            true if the robot proxy is renewable false otherwise
	 */
	public void setProxyRenewal(boolean proxyRenewal) {
		this.proxyRenewal = proxyRenewal;
	}

	/**
	 * Returns the remaining re-submission attempts for the job represented by
	 * this description.
	 * 
	 * @return the remaining re-submission attempts for the job represented by
	 *         this description.
	 */
	public int getResubmitCount() {
		return resubmitCount;
	}

	/**
	 * Sets the remaining re-submission attempts for the job represented by this
	 * description
	 * 
	 * @param resubmitCount
	 *            the remaining re-submission attempts.
	 */
	public void setResubmitCount(int resubmitCount) {
		this.resubmitCount = resubmitCount;
	}

	/**
	 * Makes this description persistent in the database.
	 */
	public void saveJobDescription() {
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			// session.save(this);
			session.saveOrUpdate(this);
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
	 * Retrieves the previously saved final job description for a
	 * {@link WorkflowN1} by its own database identifier.
	 * 
	 * @param idFinalJob
	 *            final job description database identifier.
	 * 
	 * @return a {@link GEJobDescription} representing the final final job
	 *         description for a {@link WorkflowN1}
	 */
	public static GEJobDescription findJobDescriptionById(Long idFinalJob) {
		GEJobDescription result = new GEJobDescription();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Query q = session.createQuery("from GEJobDescription where id = :id ");
		q.setParameter("id", idFinalJob);
		List<GEJobDescription> jobDescriptions = q.list();

		try {
			if (jobDescriptions.size() == 1) {
				result = jobDescriptions.get(0);
			} else if (jobDescriptions.size() == 0) {

			} else
				// Gestire
				System.out
						.println("Multiple row in JobDescription for idFinalJob:"
								+ idFinalJob);
		} catch (HibernateException he) {
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Retrieves the description for a specific job by its own grid job
	 * identifier.
	 * 
	 * @param jobId
	 *            grid job identifier of the job for which you want retrieve the
	 *            description.
	 * @return a {@link GEJobDescription} object containing the specified job
	 *         description.
	 */
	public static GEJobDescription findJobDescriptionByJobId(String jobId) {
		GEJobDescription result = new GEJobDescription();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Query q = session
				.createQuery("from GEJobDescription where jobId = :jobId ");
		q.setParameter("jobId", jobId);
		List<GEJobDescription> jobDescriptions = q.list();

		try {
			if (jobDescriptions.size() == 1) {
				result = jobDescriptions.get(0);
			} else if (jobDescriptions.size() == 0) {

			} else
				// Gestire
				System.out
						.println("Multiple row in JobDescription for idFinalJob:"
								+ jobId);
		} catch (HibernateException he) {
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Removes the corresponding record from the database for this description.
	 */
	public void delete() {
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(this);

			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			he.printStackTrace();
		} finally {
			session.close();
		}
	}

	@Override
	public String toString() {
		return "GEJobDescription [id=" + id + ", executable=" + executable
				+ ", arguments=" + arguments + ", output=" + output
				+ ", error=" + error + "]";
	}

}
