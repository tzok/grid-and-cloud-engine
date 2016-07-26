package it.infn.ct.GridEngine.UsersTracking;

import java.util.Vector;

/**
 * This class represent an active grid interaction. An active grid interaction can
 * be a simple job or a JobCollection still active on the infrastructure.
 * 
 * @author mario
 *
 */
public class ActiveInteractions {
	
	private String[] interactionInfos;
	private Vector<String[]> subJobs;
	
	public ActiveInteractions() {
		super();
	}

	public ActiveInteractions(String[] interactionInfos,
			Vector<String[]> subJobs) {
		super();
		this.interactionInfos = interactionInfos;
		this.subJobs = subJobs;
	}

	/**
	 * Returns a string array that contains information of this active grid interaction.
	 * If this active interactions not belongs to a collection getSubJobs() method return null.
	 * 
	 * @return a string array that contains information of this active grid interaction.
	 * @see #getSubJobs()
	 */
	public String[] getInteractionInfos() {
		return interactionInfos;
	}

	protected void setInteractionInfos(String[] interactionInfos) {
		this.interactionInfos = interactionInfos;
	}

	/**
	 * Returns a {@link Vector} of string array that contains informations
	 * of the sub-jobs belonging to a collection.
	 * 
	 * @return a {@link Vector} of string array that contains informations
	 * of the sub-jobs belonging to a collection.
	 */
	public Vector<String[]> getSubJobs() {
		return subJobs;
	}

	protected void setSubJobs(Vector<String[]> subJobs) {
		this.subJobs = subJobs;
	}
	
}
