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

package it.infn.ct.GridEngine.JobService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class RobotProxy {

	String folderPath = "/tmp/";
	String proxyPath = "";
	String proxyId = "";
	String VO = "";
	boolean proxyRenewal = false;
	String FQAN = "";
	boolean RFC = false;
	String cnLabel = "eToken:Empty";
	
	String eTokenServer = "myproxy.ct.infn.it";
	String eTokenServerPort = "8082";

	private static final Logger logger = Logger.getLogger(RobotProxy.class);

	/**
	 * Constructs a {@link RobotProxy} object with given parameters.
	 * 
	 * @param proxyid
	 *            robot proxy identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 */
	public RobotProxy(String proxyid, String vo, String fqan,
			boolean proxyrenewal) {
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
	}
	
	/**
	 * Constructs a {@link RobotProxy} object with given parameters.
	 * 
	 * @param proxyid
	 *            robot proxy identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	public RobotProxy(String proxyid, String vo, String fqan,
			boolean proxyrenewal, boolean rfc) {
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
		RFC = rfc;
	}

	
	/**
	 * Constructs a {@link RobotProxy} object with given parameters.
	 * 
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 */
	public RobotProxy(String etokenserver, String etokenserverport,
			String proxyid, String vo, String fqan, boolean proxyrenewal) {
		eTokenServer = etokenserver;
		eTokenServerPort = etokenserverport;
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
	}
	
	/**
	 * Constructs a {@link RobotProxy} object with given parameters.
	 * 
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles
	 * @param proxyrenewal
	 *            a boolean value that says if the robot proxy is renewable or
	 *            not
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	public RobotProxy(String etokenserver, String etokenserverport,
			String proxyid, String vo, String fqan, boolean proxyrenewal, boolean rfc, String cnLabel) {
		eTokenServer = etokenserver;
		eTokenServerPort = etokenserverport;
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
		RFC = rfc;
		this.cnLabel = cnLabel;
	}

	/**
	 * Returns the folder path where user proxy is stored.
	 * 
	 * @return folder path where user proxy is stored.
	 */
	public String getFolderPath() {
		return folderPath;
	}

	/**
	 * Sets folder path where user proxy is stored.
	 * 
	 * @param path
	 *            folder path where user proxy is stored.
	 */
	public void setFolderPath(String path) {
		folderPath = path;
	}

	/**
	 * Returns the path where user proxy is stored.
	 * 
	 * @return path where user proxy is stored.
	 */
	public String getProxyPath() {
		return proxyPath;
	}

	/**
	 * Sets the path where user proxy is stored.
	 * 
	 * @param path
	 *            path where user proxy is stored.
	 */
	public void setProxyPath(String path) {
		proxyPath = path;
	}

	/**
	 * Returns the robot proxy identifier.
	 * 
	 * @return robot proxy identifier.
	 */
	public String getproxyId() {
		return proxyId;
	}

	/**
	 * Sets the robot proxy identifier.
	 * 
	 * @param proxyid
	 *            the robot proxy identifier.
	 */
	public void setproxyId(String proxyid) {
		proxyId = proxyid;
	}

	/**
	 * Returns the robot proxy virtual organization.
	 * 
	 * @return robot proxy virtual organization.
	 */
	public String getVO() {
		return VO;
	}

	/**
	 * Sets the robot proxy virtual organization.
	 * 
	 * @param vo
	 *            robot proxy virtual organization.
	 */
	public void setVO(String vo) {
		VO = vo;
	}

	/**
	 * Returns the robot proxy roles.
	 * 
	 * @return robot proxy roles.
	 */
	public String getFQAN() {
		return FQAN;
	}

	/**
	 * Sets the robot proxy roles.
	 * 
	 * @param fqan
	 *            robot proxy roles.
	 */
	public void setFQAN(String fqan) {
		FQAN = fqan;
	}

	/**
	 * Returns a string representing robot proxy information.
	 * 
	 * @return string representing robot proxy information.
	 */
	public String getRobotProxy() {
		File proxyFile;

		if (!proxyPath.equals(""))
			deleteRobotProxy();

		logger.info("----->New GET HTTP<--------");

		proxyPath = folderPath + UUID.randomUUID();
		proxyFile = new File(proxyPath);
		logger.info("proxyPath=" + proxyPath);

		String proxyContent = "";
		URL proxyURL = null;
		try {
			if (RFC) {
				proxyURL = new URL("http://" + eTokenServer + ":"
					+ eTokenServerPort + "/eTokenServer/eToken/" + proxyId
					+ "?voms=" + VO + ":" + FQAN + "&disable-voms-proxy=false&proxy-renewal="
					+ proxyRenewal + "&rfc-proxy=" + RFC + "&cn-label=" + cnLabel);
			}
			else {
				proxyURL = new URL("http://" + eTokenServer + ":"
					+ eTokenServerPort + "/eTokenServer/eToken/" + proxyId
					+ "?voms=" + VO + ":" + FQAN + "&proxy-renewal="
					+ proxyRenewal + "&cn-label=" + cnLabel);
			}
			logger.info("get proxy: " + proxyURL.toString());
			URLConnection proxyConnection = proxyURL.openConnection();
			proxyConnection.setDoInput(true);
			InputStream proxyStream = proxyConnection.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(
					proxyStream));
			String line = "";
			while ((line = input.readLine()) != null) {
				proxyContent += line + "\n";
			}
			FileUtils.writeStringToFile(proxyFile, proxyContent);
		} catch (Exception e) {
			if (proxyURL != null)
				logger.error("Error in getting proxy: " + proxyURL.toString());
			return "";
		}

		return proxyPath;
	}

	/**
	 * This method tries to remove the user proxy file.
	 */
	public void deleteRobotProxy() {
		File f = new File(proxyPath);
		if (!f.exists())
			return;

		// Attempt to delete it
		boolean success = f.delete();

		if (!success)
			logger.warn("Error in deleting proxy = " + proxyPath);
		else
			proxyPath = "";
	}
}
