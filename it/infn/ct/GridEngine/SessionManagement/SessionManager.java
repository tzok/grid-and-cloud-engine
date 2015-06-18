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


import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import fr.in2p3.jsaga.impl.context.ContextImpl;

public class SessionManager {

	Session session = null;
	private boolean usingRobotProxy = false;
	private RobotProxy robotProxy = null;
	private String userProxy = ""; //for example /tmp/x509up_u500
	private String retryCount = "3";
	
	public void useRobotProxy(String etokenserver, String etokenserverport, String proxyId, String vo, String fqan, boolean proxyrenewal) {
		robotProxy = new RobotProxy(etokenserver, etokenserverport, proxyId, vo, fqan, proxyrenewal);
		usingRobotProxy = true;
	}
	
	public void useRobotProxy(String proxyId, String vo, String fqan, boolean proxyrenewal) {
		robotProxy = new RobotProxy(proxyId, vo, fqan, proxyrenewal);
		usingRobotProxy = true;
	}
	
	public void setUserProxy(String value) {
		userProxy = value;
	}

	public String getUserProxy() {
		return userProxy;
	}
	
	public void setRetryCount(String value) {
		retryCount = value;
	}

	public String getRetryCount() {
		return retryCount;
	}
	
	public Session getSession() {
		
		if (usingRobotProxy)
			setUserProxy(robotProxy.getRobotProxy());
		
		if (userProxy.equals("")) return null;

		session = null;
		
		try {
			System.out.println("Creating session....");
			session = SessionFactory.createSession(false);
			System.out.println(ContextImpl.URL_PREFIX);

			Context context = ContextFactory.createContext("VOMS");
			context.setAttribute(Context.USERPROXY,userProxy);
			context.setVectorAttribute("JobServiceAttributes", new String[]{"wms.RetryCount="+retryCount});
			session.addContext(context);
			context.getAttribute(Context.USERID);

			System.out.println("VO="+context.getAttribute(Context.USERVO));
			System.out.println("DN="+context.getAttribute(Context.USERID));


		} catch (Exception e) {
			System.out.println("Error in Session recovery...");
			System.out.println(e.toString());
			System.out.println("Cause :"+e.getCause());
			return null;
		}

		System.out.println("Session recovered...");

		return session;
	}
	
	public void closeSession() {
		if (usingRobotProxy) {
			robotProxy.deleteRobotProxy();
			setUserProxy("");
		}
		session.close();
		session = null;
	}
	
	public String getUserDN() {
		if (session==null)
			getSession();
		if (session==null) return ""; //TODO manage error
		
		try {
			Context[] contexts = session.listContexts();
			return contexts[0].getAttribute(Context.USERID);
		}
		catch (Exception e) {System.out.println("Error in getting User DN");}
		
		return "";
	}
	
	public String getUserVO() {
		if (session==null)
			getSession();
		if (session==null) return ""; //TODO manage error
	
		try {
			Context[] contexts = session.listContexts();
			return contexts[0].getAttribute(Context.USERVO);
		}
		catch (Exception e) {System.out.println("Error in getting User DN");}
		
		return "";
	}
	
	public String getUserFQAN() {
		if (session==null)
			getSession();
		if (session==null) return null; //TODO manage error
		
		if (usingRobotProxy) {
			return robotProxy.getFQAN();
		}
		
		return getUserVO(); //TODO using VOMS Java API to get FQAN from a proxy
	}
	
	public String getProxyId() {
		if (session==null)
			getSession();
		if (session==null) return null; //TODO manage error
		
		if (usingRobotProxy) {
			return robotProxy.getproxyId();
		}
		
		return "";
	}

}
