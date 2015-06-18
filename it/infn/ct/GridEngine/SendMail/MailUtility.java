package it.infn.ct.GridEngine.SendMail;

import it.infn.ct.GridEngine.JobService.JobServicesDispatcher;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * An utility class used to send an email to notify an user for a successfully
 * completed interaction.
 * 
 * @author mario
 * 
 */
public class MailUtility {

	public enum ContentMessage {
		SIMPLE_JOB, JOB_COLLECTION, WORKFLOW_N1, SUBMISSION_ERROR;
	}

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(MailUtility.class);
	//
	private String from = JobServicesDispatcher.getInstance().getSenderAddress();// = "sg-licence@ct.infn.it";
	private String portal;
	private String application;
	private String description;
	private String messageContent;
	private String to;// = "mario.torrisi@ct.infn.it";
	// private String smtp = "mbox.ct.infn.it";
	private String smtp = JobServicesDispatcher.getInstance().getSmtpServer();

	/**
	 * Prepares an {@link MailUtility} object to send to a specified user email
	 * address.
	 * 
	 * @param to
	 *            user email address
	 * @param portal
	 *            the portal where the interaction was generated
	 * @param application
	 *            the application used by the user
	 * @param description
	 *            the description for this interaction
	 * @param cm
	 *            interaction type.
	 * @see ContentMessage
	 */
	public MailUtility(String to, String portal, String application,
			String description, ContentMessage cm) {
		super();
		if(to.contains("|")){
			this.from = to.substring(0,to.indexOf('|'));
			this.to = to.substring(to.indexOf('|')+1);
		} else 
			this.to = to;
		
		this.application = application;
		this.portal = portal;
		this.description = description;
		setMessageContent(cm);
	}

	/**
	 * Prepares an {@link MailUtility} object to send to a specified user email
	 * address.
	 * 
	 * @param to
	 *            to user email address
	 * @param description
	 *            the description for this interaction
	 * @param cm
	 *            interaction type. see {@link ContentMessage}.
	 */
	public MailUtility(String to, String description, ContentMessage cm) {
		super();
		if(to.contains("|")){
			this.from = to.substring(0,to.indexOf('|'));
			this.to = to.substring(to.indexOf('|')+1);
		} else 
			this.to = to;
		
		this.description = description;
		setMessageContent(cm);
	}

	// public String getApplication() {
	// return application;
	// }
	//
	// public void setApplication(String application) {
	// this.application = application;
	// }
	//
	// public String getPortal() {
	// return portal;
	// }
	//
	// public void setPortal(String portal) {
	// this.portal = portal;
	// }
	//
	// public String getDescription() {
	// return description;
	// }
	//
	// public void setDescription(String description) {
	// this.description = description;
	// }
	//
	// public String getTo() {
	// return to;
	// }
	//
	// public void setTo(String to) {
	// this.to = to;
	// }

	/**
	 * Sends the email.
	 */
	public void sendMail() {
		// Creazione di una mail session
		java.util.Properties props = new java.util.Properties();
		props.put("mail.smtp.host", smtp);
		Session session = Session.getDefaultInstance(props);

		// Creazione del messaggio da inviare
		MimeMessage message = new MimeMessage(session);
		try {
			message.setSubject(" [" + this.from + "] - [ " + this.application
					+ " ] ");

			java.util.Date currentDate = new java.util.Date();
			currentDate.setTime(currentDate.getTime());

			message.setContent(this.messageContent, "text/html");
			// Aggiunta degli indirizzi del mittente e del destinatario
			InternetAddress fromAddress = new InternetAddress(from);
			InternetAddress toAddress = new InternetAddress(this.to);
			message.setFrom(fromAddress);
			message.setRecipient(Message.RecipientType.TO, toAddress);

			// Invio del messaggio
			Transport.send(message);

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			logger.fatal(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.fatal(e.getLocalizedMessage());
		}
	}

	private void setMessageContent(ContentMessage cm) {
		switch (cm) {
		case SIMPLE_JOB:
			this.messageContent = "<br/><H4>"
					+ "<img src=\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\" height=\"100\">Science Gateway "
					+ this.portal
					+ "</H4><hr><br/>"
					+ "<b>Notification for completed job, Description:</b> [ "
					+ this.description
					+ " ]</br>"
					+ "<i>The job identified by: <b>"
					+ this.description
					+ " </b> has been successfully <b>completed</b></i>.<br/><br/>"
					+ "<b>TimeStamp:</b> "
					+ new Date(System.currentTimeMillis())
					+ "<br/><br/>"
					+ "<b>Disclaimer:</b><br/>"
					+ "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.</br>"
					+ "If you did not submit any jobs through the Science Gateway, please "
					+ "<a href=\"mailto:sg-license@ct.infn.it\">contact us</a></i>";
			break;

		case JOB_COLLECTION:
			this.messageContent = "<br/><H4>"
					+ "<img src=\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\" height=\"100\">Science Gateway "
					+ this.portal
					+ "</H4><hr><br/>"
					+ "<b>Notification for completed job collection, Description:</b> [ "
					+ this.description
					+ " ]</br>"
					+ "<i>The job collection identified by: <b>"
					+ this.description
					+ " </b> has been successfully <b>completed</b></i>.<br/><br/>"
					+ "<b>TimeStamp:</b> "
					+ new Date(System.currentTimeMillis())
					+ "<br/><br/>"
					+ "<b>Disclaimer:</b><br/>"
					+ "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.</br>"
					+ "If you did not submit any jobs through the Science Gateway, please "
					+ "<a href=\"mailto:sg-license@ct.infn.it\">contact us</a></i>";
			break;

		case WORKFLOW_N1:
			this.messageContent = "<br/><H4>"
					+ "<img src=\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\" height=\"100\">Science Gateway "
					+ this.portal
					+ "</H4><hr><br/>"
					+ "<b>Notification for completed workflow, Description:</b> [ "
					+ this.description
					+ " ]</br>"
					+ "<i>The workflow identified by: <b>"
					+ this.description
					+ " </b> has been successfully <b>completed</b></i>.<br/><br/>"
					+ "<b>TimeStamp:</b> "
					+ new Date(System.currentTimeMillis())
					+ "<br/><br/>"
					+ "<b>Disclaimer:</b><br/>"
					+ "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.</br>"
					+ "If you did not submit any jobs through the Science Gateway, please "
					+ "<a href=\"mailto:sg-license@ct.infn.it\">contact us</a></i>";
			break;
		case SUBMISSION_ERROR:
			this.messageContent = "<br/><H4>"
					+ "<img src=\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\" height=\"100\">Science Gateway "
					+ this.portal
					+ "</H4><hr><br/>"
					+ "<b>Notification for submission error</b></br>"
					+ "<i>The interaction identified by: <b>"
					+ this.description
					+ " </b> hasn't been submitted for uncorrect portlet configuration parameters.  Please, contact the Science Gateway <a href=\"mailto:sg-license@ct.infn.it\">support team</a> to fix the problem</i>.<br/><br/>"
					+ "<b>TimeStamp:</b> "
					+ new Date(System.currentTimeMillis())
					+ "<br/><br/>"
					+ "<b>Disclaimer:</b><br/>"
					+ "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.</br>"
					+ "If you did not submit any jobs through the Science Gateway, please "
					+ "<a href=\"mailto:sg-license@ct.infn.it\">contact us</a></i>";
			break;
		}
	}
}

// public void sendMail (String[] jobData){
// if (!jobData[7].equals("")){
// to = jobData[7];
// if (logger.isDebugEnabled())
// logger.debug("Send email, FROM: ["+ from +"] TO: ["+ to +"]");
// // Creazione di una mail session
// java.util.Properties props = new java.util.Properties();
// props.put("mail.smtp.host", smtp);
// Session session = Session.getDefaultInstance(props);
//
// // Creazione del messaggio da inviare
// MimeMessage message = new MimeMessage(session);
// try {
// message.setSubject(" [liferay-sg-gateway] - [ " + jobData[10] + " ] ");
//
// java.util.Date currentDate = new java.util.Date();
// currentDate.setTime (currentDate.getTime());
//
// message.setContent("<br/><H4>"
// +
// "<img src=\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/195775_220075701389624_155250493_n.jpg\" width=\"100\" height=\"100\">Science Gateway "
// + jobData[9]
// + "</H4><hr><br/>"
// + "<b>Notification for completed job, Description:</b> [ " + jobData[6] +
// " ]</br>"
// + "<i>The job identified by: <b>"+ jobData[6]
// +" </b> has been successfully <b>completed</b></i>.<br/><br/>"
// + "<b>TimeStamp:</b> " + currentDate + "<br/><br/>"
// + "<b>Disclaimer:</b><br/>"
// +
// "<i>This is an automatic message sent by the Science Gateway based on Liferay technology.</br>"
// + "If you did not submit any jobs through the Science Gateway, please "
// + "<a href=\"mailto:sg-license@ct.infn.it\">contact us</a></i>",
// "text/html");
// // Aggiunta degli indirizzi del mittente e del destinatario
// InternetAddress fromAddress = new InternetAddress(from);
// InternetAddress toAddress = new InternetAddress(jobData[7]);
// message.setFrom(fromAddress);
// message.setRecipient(Message.RecipientType.TO, toAddress);
//
// // Invio del messaggio
// Transport.send(message);
//
// } catch (MessagingException e) {
// // TODO Auto-generated catch block
// logger.fatal(e.getLocalizedMessage());
// //e.printStackTrace();
// } catch (Exception e){
// logger.fatal(e.getLocalizedMessage());
// }
// }
// else
// logger.info("No 'TO' mail address is set");
// return;
// }

