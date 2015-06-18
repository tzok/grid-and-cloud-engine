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

package it.infn.ct.GridEngine.SessionManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class RobotProxy {

	String folderPath = "/tmp/";
	String proxyPath = "";
	String proxyId = "";
	String VO = "";
	boolean proxyRenewal = false;
	String FQAN = "";
	String eTokenServer = "myproxy.ct.infn.it";
	String eTokenServerPort = "8082";

	public RobotProxy(String proxyid, String vo, String fqan, boolean proxyrenewal) {
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
	}
	
	public RobotProxy(String etokenserver, String etokenserverport, String proxyid, String vo, String fqan, boolean proxyrenewal) {
		eTokenServer = etokenserver;
		eTokenServerPort = etokenserverport;
		proxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		proxyRenewal = proxyrenewal;
	}
	
	public String getFolderPath() {
		return folderPath;
	}
	
	public void setFolderPath(String path) {
		folderPath = path;
	}
	
	public String getProxyPath() {
		return proxyPath;
	}
	
	public void setProxyPath(String path) {
		proxyPath = path;
	}
	
	public String getproxyId() {
		return proxyId;
	}
	
	public void setproxyId(String proxyid) {
		proxyId = proxyid;
	}
	
	public String getVO() {
		return VO;
	}
	
	public void setVO(String vo) {
		VO = vo;
	}
	
	public String getFQAN() {
		return FQAN;
	}
	
	public void setFQAN(String fqan) {
		FQAN = fqan;
	}
	
	public String getRobotProxy() {
		File proxyFile;

		if (!proxyPath.equals(""))
			deleteRobotProxy();

		System.out.println("----->New GET HTTP<--------");

		proxyPath = folderPath + UUID.randomUUID();
		proxyFile = new File(proxyPath);                
		System.out.println("proxyPath="+proxyPath);
		String proxyContent="";
		try {
			URL proxyURL=new URL("http://"+eTokenServer+":"+eTokenServerPort+"/eTokenServer/eToken/" + proxyId + "?voms=" + VO + ":" + FQAN + "&proxy-renewal=" + proxyRenewal);
			URLConnection proxyConnection = proxyURL.openConnection();
			proxyConnection.setDoInput(true);
			InputStream proxyStream = proxyConnection.getInputStream();
			BufferedReader input = new BufferedReader(new  InputStreamReader(proxyStream));
			String line = "";
			while ((line = input.readLine()) != null) {
				//System.out.println(line);
				proxyContent+=line+"\n";
			}
			FileUtils.writeStringToFile(proxyFile, proxyContent);
		}
		catch (Exception e) {
			return "";
		}
		
		return proxyPath;
	}


	public void deleteRobotProxy() {
		File f = new File(proxyPath);
		if (!f.exists())
			return;
		
		// Attempt to delete it
	    boolean success = f.delete();

	    if (!success)
	    	System.out.println("Error in deleting proxy = " + proxyPath);
	    else
	    	proxyPath = "";
	}
}
