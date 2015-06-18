package it.infn.ct.GridEngine.JobCollection;

import it.infn.ct.GridEngine.JobResubmission.GESessionFactoryUtil;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @author mario
 * 
 */
public class ClosedJobCollection {

	// @Id
	// @GeneratedValue
	// @Column (name="id")
	private int id; // primary key

	// @Column (name="common_name")
	private String commonName;

	// @Column (name="description")
	private String description;

	// @Column (name="task_counter")
	private int taskCounter;

	// @Column (name="collection_status")
	private String collectionStatus;

	// @Column (name="start_timestamp")
	private Timestamp startTimestamp;

	// @Column (name="end_timestamp")
	private Timestamp endTimestamp;

	// @Column (name="collection_type")
	private String collectionType;

	protected ClosedJobCollection() {

	}

	protected ClosedJobCollection(String commonName, String description,
			int taskCounter, String collectionStatus, Timestamp startTimestamp,
			Timestamp endTimestamp, String collectionType) {
		super();

		this.commonName = commonName;
		this.description = description;
		this.taskCounter = taskCounter;
		this.collectionStatus = collectionStatus;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.collectionType = collectionType;
	}

	/**
	 * Returns database id of this closed collection.
	 * 
	 * @return database id of this closed collection.
	 */
	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns description for this closed collection.
	 * 
	 * @return description for this closed collection.
	 */
	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
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
	 * Returns closed collection status.
	 * 
	 * @return closed collection status.
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
	 * Returns the timestamp when this collection has been successfully
	 * completed.
	 * 
	 * @return timestamp when this collection has been successfully completed.
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	protected void setEndTimestamp(Timestamp endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the user common-name who submitted this collection.
	 * 
	 * @return the user common-name who submitted this collection.
	 */
	public String getCommonName() {
		return commonName;
	}

	protected void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * Returns a String that represent the collection type.
	 * 
	 * @return a String that represent the collection type.
	 */
	public String getCollectionType() {
		return collectionType;
	}

	protected void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	protected void saveClosedJobCollection() {

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
			throw he;
		} finally {
			session.close();
		}
	}

	@Override
	public String toString() {
		return "ClosedJobCollection [id=" + id + ", commonName=" + commonName
				+ ", description=" + description + ", collectionStatus="
				+ collectionStatus + "]";
	}

	/**
	 * Returns ClosedCollection object for the specified key.
	 * 
	 * @param key
	 *            that uniquely identifies the closed collection in the
	 *            database.
	 * @return a {@link ClosedJobCollection} for the specified key.
	 */
	public static ClosedJobCollection getClosedCollections(Integer key) {
		ClosedJobCollection result = new ClosedJobCollection();
		Session session = GESessionFactoryUtil.getSessionfactory()
				.openSession();

		Query q = session
				.createQuery("from ClosedJobCollection where id = :id ");
		q.setParameter("id", key.intValue());
		List<ClosedJobCollection> closedCollections = q.list();

		try {
			if (closedCollections.size() == 1) {
				result = closedCollections.get(0);
			} else
				// Gestire
				System.out.println("Multiple row in ClosedCollection");
		} catch (HibernateException he) {
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Returns a String[] with some ClosedCollection information.
	 * 
	 * @return a String[] with some ClosedCollection information.
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
