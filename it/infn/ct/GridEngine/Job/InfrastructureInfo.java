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

/**
 * This class contains the information needed to access an e-Infrastructure:
 * middleware, resource managers, credential, etc. 
 * @author mario
 *
 */
public class InfrastructureInfo {

	private String Name = "";
	private String Middleware = "glite";
	private String BDII = "";
	private String ResourcemanagerList[] = null;
	private String ETokenServer = "";
	private String ETokenServerPort = "";
	private String ProxyId = "";
	private String VO = "";
	private String FQAN = "";
	private String SWTag = "";
	// boolean proxyRenewal = true;
	private String UserProxy = ""; // for example /tmp/x509up_u500
	private String Keystore = "";
	private String UserName = "";
	private String Password = "";
	private String CEList[] = null;
	private boolean RFC = false;

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param proxy
	 *            path to a user proxy certificate
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String proxy) {
		Name = name;
		BDII = bdii;
		ResourcemanagerList = resourcemanagerlist;
		UserProxy = proxy;
	}

	public InfrastructureInfo(String name, String middleware,
			String authparam1, String resourcemanagerlist[]) {
		Name = name;
		Middleware = middleware;
		UserName = authparam1;
		ResourcemanagerList = resourcemanagerlist;
	}
		
	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param middleware
	 *            middleware name
	 * @param authparam1
	 *            first authentication parameter. This parameter can have more
	 *            meanings depending on middleware type, if middleware is:
	 *            <ul>
	 *            <li>glite, it is the BDII host;</li>
	 *            <li>wsgram, is ignored;</li>
	 *            <li>unicore or bes-genesis2, it is the keystore name;</li>
	 *            <li>ourgrid or ssh, it is the user name;</li>
	 *            </ul>
	 * @param authparam2
	 *            second authentication parameter. This parameter can have more
	 *            meanings depending on middleware type, if middleware is:
	 *            <ul>
	 *            <li>glite or wsgram, it is the path to a user proxy
	 *            certificate;</li>
	 *            <li>unicore or bes-genesis2, it is the keystore password;</li>
	 *            <li>ourgrid or ssh, it is the user password;</li>
	 *            </ul>
	 * @param resourcemanagerlist
	 *            resourcemanagerlist String[] that contains list of enabled
	 *            WMSes
	 */
	public InfrastructureInfo(String name, String middleware,
			String authparam1, String authparam2, String resourcemanagerlist[]) {
		Name = name;
		Middleware = middleware;
		if (Middleware.equals("glite")) {
			BDII = authparam1;
			UserProxy = authparam2;
		} else if (Middleware.equals("wsgram") || Middleware.equals("gatekeeper")) {
			UserProxy = authparam2;
		} else if ((Middleware.equals("unicore"))
				|| (Middleware.equals("bes-genesis2"))) {
			Keystore = authparam1;
			Password = authparam2;
		} else if (Middleware.equals("ourgrid") || Middleware.equals("ssh")) {
			UserName = authparam1;
			Password = authparam2;
		}

		ResourcemanagerList = resourcemanagerlist;
	}

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param proxy
	 *            path to a user proxy certificate
	 * @param swtag
	 *            infrastructure software tag
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String proxy, String swtag) {
		Name = name;
		BDII = bdii;
		ResourcemanagerList = resourcemanagerlist;
		SWTag = swtag;
		UserProxy = proxy;
	}

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param proxy
	 *            path to a user proxy certificate
	 * @param swtag
	 *            infrastructure software tag
	 * @param CEs
	 *            a String[] of possible CEs
	 */
	public InfrastructureInfo(String name, String resourcemanagerlist[],
			String proxy, String swtag, String[] CEs) {
		Name = name;
		ResourcemanagerList = resourcemanagerlist;
		SWTag = swtag;
		UserProxy = proxy;
		CEList = CEs;
	}

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
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
	 * @param CEs
	 *            a String[] of possible CEs
	 */
	public InfrastructureInfo(String name, String resourcemanagerlist[],
			String etokenserver, String etokenserverport, String proxyid,
			String vo, String fqan, String[] CEs) {
//		Name = name;
//		CEList = CEs;
//		ResourcemanagerList = resourcemanagerlist;
//		ETokenServer = etokenserver;
//		ETokenServerPort = etokenserverport;
//		ProxyId = proxyid;
//		VO = vo;
//		FQAN = fqan;
		
		this(name,resourcemanagerlist,etokenserver,etokenserverport,proxyid,vo,fqan,false,CEs);
	}
	
	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
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
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 * @param CEs
	 *            a String[] of possible CEs
	 */
	public InfrastructureInfo(String name, String resourcemanagerlist[],
			String etokenserver, String etokenserverport, String proxyid,
			String vo, String fqan, boolean rfc, String[] CEs) {
		Name = name;
		CEList = CEs;
		ResourcemanagerList = resourcemanagerlist;
		ETokenServer = etokenserver;
		ETokenServerPort = etokenserverport;
		ProxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		RFC = rfc;
	}

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles.
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan) {
//		Name = name;
//		BDII = bdii;
//		ResourcemanagerList = resourcemanagerlist;
//		ETokenServer = etokenserver;
//		ETokenServerPort = etokenserverport;
//		ProxyId = proxyid;
//		VO = vo;
//		FQAN = fqan;
		
		this(name,bdii,resourcemanagerlist,etokenserver,etokenserverport,proxyid,vo,fqan,false);
	}
	
	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
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
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan, boolean rfc) {
		Name = name;
		BDII = bdii;
		ResourcemanagerList = resourcemanagerlist;
		ETokenServer = etokenserver;
		ETokenServerPort = etokenserverport;
		ProxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		RFC = rfc;
	}


	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param middleware
	 *            middleware name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles.
	 */
	public InfrastructureInfo(String name, String middleware, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan) {
//		Name = name;
//		Middleware = middleware;
//		BDII = bdii;
//		ResourcemanagerList = resourcemanagerlist;
//		ETokenServer = etokenserver;
//		ETokenServerPort = etokenserverport;
//		ProxyId = proxyid;
//		VO = vo;
//		FQAN = fqan;
		
		this(name,middleware,bdii,resourcemanagerlist,etokenserver,etokenserverport,proxyid,vo,fqan,false);
	}
	
	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param middleware
	 *            middleware name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
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
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 */
	public InfrastructureInfo(String name, String middleware, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan, boolean rfc) {
		Name = name;
		Middleware = middleware;
		BDII = bdii;
		ResourcemanagerList = resourcemanagerlist;
		ETokenServer = etokenserver;
		ETokenServerPort = etokenserverport;
		ProxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		RFC = rfc;
	}

	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles.
	 * @param swtag
	 *            infrastructure software tag
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan,
			String swtag) {
//		Name = name;
//		BDII = bdii;
//		ResourcemanagerList = resourcemanagerlist;
//		ETokenServer = etokenserver;
//		ETokenServerPort = etokenserverport;
//		ProxyId = proxyid;
//		VO = vo;
//		FQAN = fqan;
//		SWTag = swtag;
		
		this(name,bdii,resourcemanagerlist,etokenserver,etokenserverport,proxyid,vo,fqan,false,swtag);
	}
	
	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param bdii
	 *            BDII host
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 * @param etokenserver
	 *            robot proxy server host
	 * @param etokenserverport
	 *            robot proxy server port
	 * @param proxyid
	 *            robot proxy server identifier
	 * @param vo
	 *            robot proxy virtual organization
	 * @param fqan
	 *            robot proxy roles.
	 * @param rfc
	 * 			  a boolean value that says if the robot proxy is in RFC format or
	 *            not
	 * @param swtag
	 *            infrastructure software tag
	 */
	public InfrastructureInfo(String name, String bdii,
			String resourcemanagerlist[], String etokenserver,
			String etokenserverport, String proxyid, String vo, String fqan,
			boolean rfc, String swtag) {
		Name = name;
		BDII = bdii;
		ResourcemanagerList = resourcemanagerlist;
		ETokenServer = etokenserver;
		ETokenServerPort = etokenserverport;
		ProxyId = proxyid;
		VO = vo;
		FQAN = fqan;
		SWTag = swtag;
		RFC = rfc;
	}


	/**
	 * Constructs a {@link InfrastructureInfo} object initializing its attribute
	 * with the given data.
	 * 
	 * @param name
	 *            infrastructure name
	 * @param middleware
	 *            middlewarename
	 * @param resourcemanagerlist
	 *            String[] that contains list of enabled WMSes
	 */
	public InfrastructureInfo(String name, String middleware,
			String resourcemanagerlist[]) {
		Name = name;
		Middleware = middleware;
		ResourcemanagerList = resourcemanagerlist;
	}

	/**
	 * Returns the infrastructure full name
	 * 
	 * @return infrastructure full name
	 */
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}

	/**
	 * Returns the BDII host.
	 * 
	 * @return BDII host.
	 */
	public String getBDII() {
		return BDII;
	}
	
	public void setBDII(String bDII) {
		BDII = bDII;
	}

	/**
	 * Returns the list of enabled WMSes.
	 * @deprecated
	 * @return list of enabled WMSes.
	 */
	public String[] getWmsList() {
		return ResourcemanagerList;
	}
	public String[] getResourcemanagerList() {
		return ResourcemanagerList;
	}

	public void setResourcemanagerList(String[] resourcemanagerList) {
		ResourcemanagerList = resourcemanagerList;
	}

	/**
	 * Returns the path to a user proxy certificate.
	 * 
	 * @return path to a user proxy certificate.
	 */
	public String getUserProxy() {
		return UserProxy;
	}
	
	public void setUserProxy(String userProxy) {
		UserProxy = userProxy;
	}

	/**
	 * Returns the robot proxy host.
	 * 
	 * @return robot proxy host.
	 */
	public String getETokenServer() {
		return ETokenServer;
	}
	
	public void setETokenServer(String eTokenServer) {
		ETokenServer = eTokenServer;
	}

	/**
	 * Returns the robot proxy port.
	 * 
	 * @return robot proxy port.
	 */
	public String getETokenServerPort() {
		return ETokenServerPort;
	}
	
	public void setETokenServerPort(String eTokenServerPort) {
		ETokenServerPort = eTokenServerPort;
	}

	/**
	 * Returns the robot proxy identifier.
	 * 
	 * @return robot proxy identifier.
	 */
	public String getProxyId() {
		return ProxyId;
	}
	
	public void setProxyId(String proxyId) {
		ProxyId = proxyId;
	}

	/**
	 * Returns the robot proxy robot proxy virtual organization.
	 * 
	 * @return robot proxy virtual organization.
	 */
	public String getVO() {
		return VO;
	}
	
	public void setVO(String vO) {
		VO = vO;
	}

	/**
	 * Returns the robot proxy roles.
	 * 
	 * @return robot proxy roles.
	 */
	public String getFQAN() {
		return FQAN;
	}
	
	public void setFQAN(String fQAN) {
		FQAN = fQAN;
	}

	/**
	 * Returns the infrastructure software tag.
	 * 
	 * @return infrastructure software tag.
	 */
	public String getSWTag() {
		return SWTag;
	}
	
	public void setSWTag(String sWTag) {
		SWTag = sWTag;
	}

	/**
	 * Returns the infrastructure middleware name.
	 * 
	 * @return infrastructure middleware name.
	 */
	public String getMiddleware() {
		return Middleware;
	}
	
	public void setMiddleware(String middleware) {
		Middleware = middleware;
	}

	/**
	 * Returns the keystore.
	 * 
	 * @return keystore.
	 */
	public String getKeystore() {
		return Keystore;
	}
	
	public void setKeystore(String keystore) {
		Keystore = keystore;
	}

	/**
	 * Returns the username.
	 * 
	 * @return username.
	 */
	public String getUserName() {
		return UserName;
	}
	
	public void setUserName(String userName) {
		UserName = userName;
	}

	/**
	 * Returns the password.
	 * 
	 * @return password.
	 */
	public String getPassword() {
		return Password;
	}
	
	public void setPassword(String password) {
		Password = password;
	}

	/**
	 * Returns the list possible CEs.
	 * 
	 * @return list possible CEs.
	 */
	public String[] getCEList() {
		return CEList;
	}
	
	public void setCEList(String[] cEList) {
		CEList = cEList;
	}
	
	/**
	 * Returns the RFC flag.
	 * 
	 * @return RFC flag.
	 */
	public boolean getRFC() {
		return RFC;
	}
	
	public void setRFC(boolean rFC) {
		RFC = rFC;
	}

}
