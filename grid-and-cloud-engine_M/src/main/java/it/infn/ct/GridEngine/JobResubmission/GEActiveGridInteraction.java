package it.infn.ct.GridEngine.JobResubmission;

import it.infn.ct.GridEngine.JobCollection.JobCollection;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * This class is a persistence class used to mapping an active grid interaction
 * in database, with an object.
 * 
 * @author mario
 * 
 */
public class GEActiveGridInteraction {

	private static final Logger logger = Logger.getLogger(GEActiveGridInteraction.class);
	// @Id
	// @GeneratedValue
	// @Column (name="id")
	private Long id; // primary key

	// @Column (name="commonName")
	private String commonName;

	// @Column (name="tcpAddress")
	private String tcpAddress;

	// @Column (name="timestamp")
	private Date timestamp;

	// @Column (name="grid_interaction")
	private int gridInteraction;

	// @Column (name="grid_id")
	private String jobId;

	// @Column (name="robot_certficate")
	private String robotCertificate;

	// @Column (name="proxy_id")
	private String proxyId;

	// @Column (name="virtual_organization")
	private String VO;

	// @Column (name="fqan")
	private String fqan;

	// @Column (name="user_description")
	private String userDescription;

	// @Column (name="status")
	private String status;

	// @Column (name="grid_ce")
	private String CE;

	// @Column (name="latitude")
	private float latitude;

	// @Column (name="longitude")
	private float longitude;

	// @Column (name="timestamp_endjob")
	private Date timestampEndJob;

	// @Column (name="email")
	private String email;

	// @Column (name="e_token_eserver")
	private String eTokenServer;

	// @Column (name="id_job_collection")
	private Integer idJobCollection;

	public GEActiveGridInteraction() {
	}

	/**
	 * Returns the database id for this interaction.
	 * 
	 * @return the database id for this interaction.
	 */
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the common name of the user who did this interaction.
	 * 
	 * @return the common name of the user who did this interaction.
	 */
	public String getCommonName() {
		return commonName;
	}

	protected void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * Returns the IP address where this interaction originated.
	 * 
	 * @return the IP address where this interaction originated.
	 */
	public String getTcpAddress() {
		return tcpAddress;
	}

	protected void setTcpAddress(String tcpAddress) {
		this.tcpAddress = tcpAddress;
	}

	/**
	 * Returns the start time stamp for this interaction.
	 * 
	 * @return the start time stamp for this interaction.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	protected void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns an identifier of application in a specified portal.
	 * 
	 * @return an identifier of application in a specified portal.
	 */
	public int getGridInteraction() {
		return gridInteraction;
	}

	protected void setGridInteraction(int gridInteraction) {
		this.gridInteraction = gridInteraction;
	}

	/**
	 * Returns the grid job identifier.
	 * 
	 * @return the grid job identifier.
	 */
	public String getJobId() {
		return jobId;
	}

	protected void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Returns the Robot Certficate.
	 * 
	 * @return the Robot Certficate.
	 */
	public String getRobotCertificate() {
		return robotCertificate;
	}

	protected void setRobotCertificate(String robotCertificate) {
		this.robotCertificate = robotCertificate;
	}

	/**
	 * Returns the Proxy identifier.
	 * 
	 * @return the Proxy identifier.
	 */
	public String getProxyId() {
		return proxyId;
	}

	protected void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}

	/**
	 * Returns the Virtual Organization.
	 * 
	 * @return the Virtual Organization.
	 */
	public String getVO() {
		return VO;
	}

	protected void setVO(String vO) {
		VO = vO;
	}

	/**
	 * Returns the FQAN.
	 * 
	 * @return the FQAN
	 */
	public String getFqan() {
		return fqan;
	}

	protected void setFqan(String fqan) {
		this.fqan = fqan;
	}

	/**
	 * Returns a description for this job.
	 * 
	 * @return a description for this job.
	 */
	public String getUserDescription() {
		return userDescription;
	}

	protected void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}

	/**
	 * Returns status for this job.
	 * 
	 * @return status for this job.
	 */
	public String getStatus() {
		return status;
	}

	protected void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the Compute Element.
	 * 
	 * @return the Compute Element.
	 */
	public String getCE() {
		return CE;
	}

	protected void setCE(String cE) {
		CE = cE;
	}

	/**
	 * Returns latitude of the Compute element.
	 * 
	 * @return latitude of the Compute element.
	 */
	public float getLatitude() {
		return latitude;
	}

	protected void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	/**
	 * Returns longitude of the Compute element.
	 * 
	 * @return longitude of the Compute element.
	 */
	public float getLongitude() {
		return longitude;
	}

	protected void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	/**
	 * Returns the end time stamp for this interaction.
	 * 
	 * @return the end time stamp for this interaction.
	 */
	public Date getTimestampEndJob() {
		return timestampEndJob;
	}

	protected void setTimestampEndJob(Date timestampEndJob) {
		this.timestampEndJob = timestampEndJob;
	}

	/**
	 * Returns the user email address.
	 * 
	 * @return the user email address.
	 */
	public String getEmail() {
		return email;
	}

	protected void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns the eTokenServer address.
	 * 
	 * @return the eTokenServer address.
	 */
	public String geteTokenServer() {
		return eTokenServer;
	}

	protected void seteTokenServer(String eTokenServer) {
		this.eTokenServer = eTokenServer;
	}

	/**
	 * Returns the job collection identifier which this job belongs to, null
	 * otherwise.
	 * 
	 * @return the job collection identifier which this job belongs to, null
	 *         otherwise.
	 */
	public Integer getIdJobCollection() {
		return idJobCollection;
	}

	protected void setIdJobCollection(Integer idJobCollection) {
		this.idJobCollection = idJobCollection;
	}

	/**
	 * This method persists an active grid interaction into the
	 * ActiveGridInterctions table.
	 */
	public void save() {
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(this);
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
	 * This method updates the active grid interaction specified by the
	 * identifier.
	 * 
	 * @param jobId
	 *            ActiveGridInteractions identifier of the record that will be
	 *            updated
	 */
	public void updateJobId(String jobId) {
		this.jobId = jobId;
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(this);
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
	 * Retrieves the active grid interactions with a specified database identifier.
	 * 
	 * @param dbId
	 *            identifier for which you want to retrieve active grid
	 *            interactions information.
	 * @return a {@link GEActiveGridInteraction} object with the grid
	 *         interactions information.
	 */
	public static GEActiveGridInteraction findActiveGridInteractionByJobId(
			Long dbId) {
		GEActiveGridInteraction result = new GEActiveGridInteraction();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		
		try {
			tx=session.beginTransaction();
			Query q = session
					.createQuery("from GEActiveGridInteraction where id = :dbId ");
			q.setParameter("dbId", dbId);
			List<GEActiveGridInteraction> activeGridInteractions = q.list();
			
			if (activeGridInteractions.size() == 1) {
				result = activeGridInteractions.get(0);
			} else if (activeGridInteractions.size() == 0) {
				logger.debug("None row in ActiveGridInteractions for jobId:"
						+ dbId);
//				System.out
//						.println("None row in ActiveGridInteractions for jobId:"
//								+ jobId);
			} else
				// Gestire
				logger.debug("Multiple rows in ActiveGridInteractions for jobId:"
								+ dbId);
//				System.out
//						.println("Multiple row in ActiveGridInteractions for jobId:"
//								+ jobId);
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
//	public static GEActiveGridInteraction findActiveGridInteractionByJobId(
//			String jobId) {
//		GEActiveGridInteraction result = new GEActiveGridInteraction();
//		Session session = GESessionFactoryUtil.getSessionfactory()
//				.openSession();
//
//		Transaction tx = null;
//		
//		try {
//			tx=session.beginTransaction();
//			Query q = session
//					.createQuery("from GEActiveGridInteraction where jobId = :jobId ");
//			q.setParameter("jobId", jobId);
//			List<GEActiveGridInteraction> activeGridInteractions = q.list();
//			
//			if (activeGridInteractions.size() == 1) {
//				result = activeGridInteractions.get(0);
//			} else if (activeGridInteractions.size() == 0) {
//				logger.debug("None row in ActiveGridInteractions for jobId:"
//						+ jobId);
////				System.out
////						.println("None row in ActiveGridInteractions for jobId:"
////								+ jobId);
//			} else
//				// Gestire
//				logger.debug("Multiple rows in ActiveGridInteractions for jobId:"
//								+ jobId);
////				System.out
////						.println("Multiple row in ActiveGridInteractions for jobId:"
////								+ jobId);
//			tx.commit();
//		} catch (HibernateException he) {
//			if (tx != null)
//				tx.rollback();
//			he.printStackTrace();
//		} finally {
//			session.close();
//		}
//		return result;
//	}

	/**
	 * Removes the corresponding record from the database for this active
	 * interactions.
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

	/**
	 * Retrieves all active interactions belong to a specified
	 * {@link JobCollection}.
	 * 
	 * @param idJobCollection
	 *            {@link JobCollection} identifier for which you want to
	 *            retrieve all active sub-jobs.
	 * 
	 * @return a {@link List} of {@link GEActiveGridInteraction} representing
	 *         all active grid interactions for the specified
	 *         {@link JobCollection}
	 */
	public static List<GEActiveGridInteraction> findActiveJobForJobCollection(
			int idJobCollection) {
		List<GEActiveGridInteraction> result = null;
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session
					.createQuery("from GEActiveGridInteraction where idJobCollection = :idJobCollection ");
			q.setParameter("idJobCollection", idJobCollection);
			result = q.list();

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	private Timestamp getCurrentUTCTimestamp() {
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

}
