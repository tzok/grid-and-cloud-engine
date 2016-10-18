package it.infn.ct.GridEngine.JobCollection;

import it.infn.ct.GridEngine.Job.JSagaJobSubmission;
import it.infn.ct.GridEngine.JobResubmission.GEActiveGridInteraction;
import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;
import it.infn.ct.GridEngine.JobResubmission.GESessionFactoryUtil;
import it.infn.ct.GridEngine.SendMail.MailUtility;
import it.infn.ct.GridEngine.SendMail.MailUtility.ContentMessage;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


//@Entity
//@Table(name = "ActiveJobCollections")
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(
//    name="collection_type",
//    discriminatorType=DiscriminatorType.STRING
//)
//@DiscriminatorValue(value="JOB_COLLECTION")
/**
 * This class is a simple collection of parallel running jobs. When you create a
 * new JobCollection objects, this handles all behavior. It is responsible for
 * the creation, updating collection state and complete collection when all jobs
 * have gone DONE.
 * 
 * @author mario
 * 
 */
public class JobCollection {

	private static final String OUTPUT_DIR = "/jobOutput/";
	protected static final Logger logger = Logger
			.getLogger(JobCollection.class);

//	@Id
//	@GeneratedValue
//	@Column (name="id")
	private int id; // primary key

//	@Column (name="common_name")
	private String commonName;

//	@Column (name="description")
	private String description;

//	@Column (name="task_counter")
	private int taskCounter;

//	@Column (name="user_email")
	private String userEmail;

//	@Column (name="status")
	private String collectionStatus;

//	@Column (name="start_timestamp")
	private Timestamp startTimestamp;

//	@Column (name="end_timestamp")
	private Timestamp endTimestamp;

//	@Column (name="output_path")
	private String outputPath;

//	@Transient
	private ArrayList<GEJobDescription> subJobDescriptions;

	/**
	 * Constructs a JobCollection object without a user email.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a collection description
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            collection are stored
	 * @param subJobDescriptions
	 *            an {@link ArrayList} of {@link GEJobDescription} that
	 *            represents the entire sub-job descriptions.
	 */
	public JobCollection(String commonName, String description,
			String outputPath, ArrayList<GEJobDescription> subJobDescriptions) {
		this(commonName, description, null, outputPath, subJobDescriptions);
	}

	/**
	 * Constructs a JobCollection object with the specified user email to notify
	 * that this job collection is completed.
	 * 
	 * @param commonName
	 *            a string that identifies the user
	 * @param description
	 *            a string that gives a collection description
	 * @param userEmail
	 *            email address to notify completed collection
	 * @param outputPath
	 *            a string that specifies the path where the output file for the
	 *            collection are stored
	 * @param subJobDescriptions
	 *            an {@link ArrayList} of {@link GEJobDescription} that
	 *            represents the entire sub-job descriptions.
	 */
	public JobCollection(String commonName, String description,
			String userEmail, String outputPath,
			ArrayList<GEJobDescription> subJobDescriptions) {
		// this(commonName, description, outputPath, subJobDescriptions);
		this.commonName = commonName;
		this.description = description;
		this.outputPath = outputPath + OUTPUT_DIR;
		this.setSubJobDescriptions(subJobDescriptions);
		this.taskCounter = subJobDescriptions.size();
		this.userEmail = userEmail;

		saveJobCollection();
	}

	protected JobCollection() {

	}

	/**
	 * Returns the JobCollection id.
	 * 
	 * @return an integer that is the JobCollection id.
	 */
	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the JobCollection user description.
	 * 
	 * @return JobCollection user description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets JobCollection user description
	 * 
	 * @param description
	 *            user description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the parallel tasks number.
	 * 
	 * @return parallel tasks number.
	 */
	public int getTaskCounter() {
		return taskCounter;
	}

	protected void setTaskCounter(int taskCounter) {
		// this.taskCounter = getSubJobDescriptions().size();
		this.taskCounter = taskCounter;
	}

	/**
	 * Returns the user email, if it was specified in JobCollection constructor.
	 * 
	 * @return the user email.
	 */
	public String getUserEmail() {
		return userEmail;
	}

	protected void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	/**
	 * Returns the user email, if it was specified in JobCollection constructor.
	 * 
	 * @return the user email.
	 */
	public String getCollectionStatus() {
		return collectionStatus;
	}

	protected void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}

	/**
	 * Returns the JobCollection submission Timestamp.
	 * 
	 * @return submission Timestamp
	 */
	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	protected void setStartTimestamp(Timestamp startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * Returns the timestamp when this JobCollection has been successfully
	 * completed.
	 * 
	 * @return timestamp when this JobCollection has been successfully
	 *         completed.
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	protected void setEndTimestamp(Timestamp endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the user common-name who submitted this JobCollection.
	 * 
	 * @return the user common-name who submitted this JobCollection.
	 */
	public String getCommonName() {
		return commonName;
	}

	protected void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * Returns the path where output files of this JobCollection are stored.
	 * 
	 * @return the path where output files of this JobCollection are stored.
	 */
	public String getOutputPath() {
		return outputPath;
	}

	protected void setOutputPath(String outputPath) {
		if (!outputPath.contains(OUTPUT_DIR))
			this.outputPath = outputPath + OUTPUT_DIR;
		else
			this.outputPath = outputPath;
	}

	/**
	 * Returns a list of GEJobDescription objects that represent descriptions
	 * for all sub-jobs of this job collection.
	 * 
	 * @return a list of {@link GEJobDescription} objects that represent
	 *         descriptions for all sub-jobs of this job collection.
	 */
	public ArrayList<GEJobDescription> getSubJobDescriptions() {
		return subJobDescriptions;
	}

	protected void setSubJobDescriptions(
			ArrayList<GEJobDescription> subJobDescriptions) {
		this.subJobDescriptions = subJobDescriptions;
	}

	protected void saveJobCollection() {

		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			this.setCollectionStatus("CREATED");
			this.setStartTimestamp(getCurrentUTCTimestamp());
			session.save(this);
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			he.printStackTrace();
			throw he;
		} finally {
			session.close();
		}
	}

	protected void delete() {
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

	/**
	 * This method retrieves all running JobCollections for the specified user.
	 * A running JobCollection is a collection whose state can be in one of the
	 * following possibilities:
	 * <ul>
	 * <li>CREATED: when the collection was just created and none of its
	 * sub-jobs was still submitted;</li>
	 * <li>RUNNING: when at least one of its sub-jobs aren't in a final status;</li>
	 * <li>SUBMITTING_FINAL_JOB: when the collection is an instance of
	 * {@link WorkflowN1} and its final job was just submitted;</li>
	 * <li>RUNNING_FINAL_JOB: when the final job of a {@link WorkflowN1} are
	 * still running.</li>
	 * </ul>
	 * 
	 * @param commonName
	 *            for which you want to retrieve running JobCollections.
	 * @return list of all running JobCollections for the specified common name.
	 */
	public static Vector<JobCollection> getRunningJobCollections(
			String commonName) {

		Vector<JobCollection> result = new Vector<JobCollection>();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session
					.createQuery("from JobCollection where commonName = :commonName and collectionStatus <> 'DONE' and collectionStatus <> 'Aborted' ");
			q.setParameter("commonName", commonName);
			List<JobCollection> activeJobCollections = q.list();
			for (JobCollection jobCollection : activeJobCollections)
				result.add(jobCollection);
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

	/**
	 * This method retrieves all successfully completed JobCollections for the
	 * specified user.
	 * 
	 * @param commonName
	 *            for which you want to retrieve all successfully completed
	 *            JobCollections.
	 * @return list of all successfully completed JobCollections for the
	 *         specified common name.
	 */
	public static Vector<JobCollection> getDoneJobCollections(String commonName) {
		Vector<JobCollection> result = new Vector<JobCollection>();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session
					.createQuery("from JobCollection where commonName = :commonName and collectionStatus = 'DONE'");
			q.setParameter("commonName", commonName);
			List<JobCollection> activeJobCollections = q.list();
			for (JobCollection jobCollection : activeJobCollections)
				result.add(jobCollection);
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

	/**
	 * This method retrieves all JobCollections for a specified user.
	 * 
	 * @param commonName
	 *            of the user for which you want retrieve active JobCollectios.
	 * @return a {@link Vector} of active JobCollections for the spicified user.
	 */
	public static Vector<JobCollection> getActiveJobCollectionsByName(
			String commonName) {

		Vector<JobCollection> result = new Vector<JobCollection>();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session
					.createQuery("from JobCollection where commonName = :commonName ");
			q.setParameter("commonName", commonName);
			List<JobCollection> activeJobCollections = q.list();
			for (JobCollection jobCollection : activeJobCollections)
				result.add(jobCollection);
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

	/**
	 * Returns the JobCollection with the specified id.
	 * 
	 * @param jobCollectionId
	 *            JobCollection id.
	 * @return the {@link JobCollection} with the specified id
	 */
	public static JobCollection getJobCollection(int jobCollectionId) {

		JobCollection result = new JobCollection();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Query q = session.createQuery("from JobCollection where id = :id ");
		q.setParameter("id", jobCollectionId);
		List<JobCollection> activeJobCollections = q.list();

		try {
			if (activeJobCollections.size() == 1) {
				result = activeJobCollections.get(0);
			} else
				// Gestire
				System.out.println("Multiple row in JobCollections");
		} catch (HibernateException he) {
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Returns a boolean that represent if this collection is in final status or
	 * not. A JobCollection is in final status if all its sub-jobs aren't in
	 * RUNING or in SUBMITTED state.
	 * 
	 * @return true if JobCollection is in final status, false otherwise.
	 */
	public boolean isInFinalStatus() {
		boolean result = false;
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			Query q = session
					.createSQLQuery("SELECT count(*) FROM ActiveGridInteractions WHERE id_job_collection = :jobCollectionId AND (status <> 'RUNNING' AND status <> 'SUBMITTED')");
			q.setParameter("jobCollectionId", this.getId());
			java.math.BigInteger tmp = (java.math.BigInteger) q.uniqueResult();
			int activeJobCont = tmp.intValue();
//			q = session
//					.createSQLQuery("SELECT count(*) FROM ActiveGridInteractions WHERE id_job_collection = :jobCollectionId");
//			q.setParameter("jobCollectionId", this.getId());
//			java.math.BigInteger tmp1 = (java.math.BigInteger) q.uniqueResult();
//			int totalJobCont = tmp1.intValue();
//			if (activeJobCont == this.getTaskCounter() || totalJobCont < this.getTaskCounter())
			if (activeJobCont == this.getTaskCounter())
				result = true;
			else
				result = false;

			// Query q = session
			// .createQuery("from GEActiveGridInteraction where id_job_collection = :jobCollectionId AND (status = 'RUNNING' OR status = 'SUBMITTED')");
			// q.setParameter("jobCollectionId", this.getId());

			// List<GEActiveGridInteraction> activeJobInCollections = q.list();
			// if (activeJobInCollections.size() == 0)
			// // La collection è in uno stato finale
			// result = true;
			// else
			// result = false;

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

	/**
	 * This method handles state updating for this JobCollection.
	 * 
	 * @param inFinalStatus
	 */
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

				if (result == this.getTaskCounter())
					// this.setCollectionStatus("DONE");
					newStatus = "DONE";
				else
					// this.setCollectionStatus("Aborted");
					newStatus = "Aborted";
				this.setEndTimestamp(getCurrentUTCTimestamp());

				// session.update(this);
			} else if (!this.getCollectionStatus().equals("RUNNING")) {
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

	@Override
	public String toString() {
		return "JobCollection [id=" + id + ", description=" + description
				+ ", commonName=" + commonName + ", taskCounter=" + taskCounter
				+ "]";
	}

	protected Timestamp getCurrentUTCTimestamp() {
		Calendar calendar = Calendar.getInstance();
		java.util.Date date = calendar.getTime();
		TimeZone tz = calendar.getTimeZone();

		long msFromEpochGMT = date.getTime();

		int offsetFromUTC = tz.getOffset(msFromEpochGMT);

		Calendar UTCCalendar = Calendar
				.getInstance(TimeZone.getTimeZone("GMT"));
		UTCCalendar.setTime(date);
		UTCCalendar.add(Calendar.MILLISECOND, -offsetFromUTC);

		Timestamp currentTimestamp = new Timestamp(
				UTCCalendar.getTimeInMillis());
		return currentTimestamp;
	}

	/**
	 * This method closes a JobCollection with "Aborted" state because at least
	 * one of its sub-job has been unsucessfully completed for a number of times
	 * greater than maximum re-submission attempts allowed.
	 * 
	 * @param dbInt
	 *            an interface to the database where make persistent this
	 *            information.
	 */
	public void abortCollection(UsersTrackingDBInterface dbInt) {
		ClosedJobCollection abortedCollection = new ClosedJobCollection(
				this.commonName, this.description, this.taskCounter,
				this.collectionStatus, this.startTimestamp, this.endTimestamp,
				this.getClass().getSimpleName());
		abortedCollection.saveClosedJobCollection();
		logger.info("Aborted Collection inserted: "
				+ abortedCollection.toString());

		this.delete();

		List<GEActiveGridInteraction> listOfActiveGridInteractions = GEActiveGridInteraction.findActiveJobForJobCollection(this.getId()); 
				
		for (GEActiveGridInteraction activeGridInteraction : listOfActiveGridInteractions){
//			if(!activeGridInteraction.getStatus().equals("DONE")){
//				GEJobDescription descr = GEJobDescription.findJobDescriptionByJobId(activeGridInteraction.getJobId());
//				if(descr.getId()!=0)
//					descr.delete();
//			}
			dbInt.CloseGridInteraction(activeGridInteraction.getId().intValue(), abortedCollection.getId() );
		}

		// TODO rimovere eventuali directory di job DONE
		
//		if(!this.getUserEmail().equals("")){
//			MailUtility m = new MailUtility(this.getUserEmail(), this.getDescription(), MailUtility.ContentMessage.SUBMISSION_ERROR);
//			m.sendMail();
//		}

	}

	/**
	 * This method moves a successfully completed JobCollection from the
	 * ActiveJobJollections database table to the completed one.
	 * 
	 * @return the auto-generated id for the completed JobCollection, just added
	 *         in JobCollections table.
	 */
	public int close() {
		ClosedJobCollection doneCollection = new ClosedJobCollection(
				this.commonName, this.description, this.taskCounter,
				this.collectionStatus, this.startTimestamp, this.endTimestamp,
				this.getClass().getSimpleName());
		doneCollection.saveClosedJobCollection();
		logger.info("Done Collection inserted: " + doneCollection.toString());
		this.delete();
		return doneCollection.getId();
	}

	/**
	 * This method completes a JobCollection by making an archive of the output
	 * directory of this collection. Furthermore, it sends an email to the user
	 * to notify the JobCollection completed.
	 * 
	 * @param jobData
	 *            a String[] with job information used in the notification email
	 *            sent to the user.
	 */
	public void completeCollection(String[] jobData) {

		logger.info("creating a tgz archive containing output files...");

		String collectionOutputDirectory = JSagaJobSubmission
				.removeNotAllowedCharacter(this.getDescription());

		// logger.info("tar czvf "+this.getOutputPath()+
		// collectionOutputDirectory + "_" + this.getId()+".tgz "+
		// this.getOutputPath() + collectionOutputDirectory + "_" +
		// this.getId());
		logger.info("tar czvf " + this.getOutputPath()
				+ collectionOutputDirectory + "_" + this.getId()
				+ ".tgz --directory=" + this.getOutputPath() + " "
				+ collectionOutputDirectory + "_" + this.getId());

		Process process;
		try {
			// process = Runtime.getRuntime().exec("tar czvf "
			// +this.getOutputPath()+ collectionOutputDirectory + "_"
			// +this.getId()+".tgz "+this.getOutputPath() +
			// collectionOutputDirectory + "_" +this.getId());
			process = Runtime.getRuntime().exec(
					"tar czvf " + this.getOutputPath()
							+ collectionOutputDirectory + "_" + this.getId()
							+ ".tgz --directory=" + this.getOutputPath() + " "
							+ collectionOutputDirectory + "_" + this.getId());
			process.waitFor();
			// deleting folder...
			String command = "rm -Rf " + this.getOutputPath()
					+ collectionOutputDirectory + "_" + this.getId();
			logger.info(command);
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (this.getUserEmail() != null && !this.getUserEmail().equals("")) {

			if (!jobData[11].equals("")) {
				logger.debug("Sending email for completed job collection: "
						+ this.toString());
				ContentMessage cm;
				if (this instanceof WorkflowN1)
					cm = ContentMessage.WORKFLOW_N1;
				else
					cm = ContentMessage.JOB_COLLECTION;
				MailUtility m = new MailUtility(this.getUserEmail(),
						jobData[9], jobData[10], this.getDescription(), cm);
				m.sendMail();
			}

		}

	}

	/**
	 * Returns a String[] with some JobCollection information.
	 * 
	 * @return a String[] with some JobCollection information
	 */
	public String[] getCollectionInfos() {

		String[] collectionInfos = new String[6];

		collectionInfos[0] = "" + this.getId();
		collectionInfos[3] = this.description;
		collectionInfos[4] = "" + this.startTimestamp;
		collectionInfos[5] = this.collectionStatus;

		return collectionInfos;
	}
}
