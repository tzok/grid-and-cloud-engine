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

Author: XXXXXXXXXXXXXXXXXX
****************************************************************************/

package it.infn.ct.GridEngine.Data;

import it.infn.ct.GridEngine.InformationSystem.BDII;
import it.infn.ct.GridEngine.SessionManagement.SessionManager;
import it.infn.ct.GridEngine.UsersTracking.UsersTrackingDBInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public class JSagaDataManagement {
    
    private Log log =LogFactory.getLog(JSagaDataManagement.class);
    
    private SessionManager sessionM;
    private BDII bdii;
    private UsersTrackingDBInterface userTrackingDB;

    public JSagaDataManagement() {
        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        userTrackingDB = new UsersTrackingDBInterface();
        sessionM = new SessionManager();
        log.info("A new JSagaDataManagement object created");
    }
    
    public void useRobotProxy(String proxyId, String vo, String fqan, boolean proxyrenewal) {
            sessionM.useRobotProxy(proxyId, vo, fqan, proxyrenewal);
            log.info("Enabled the proxy robot with id='"+proxyId+"',"
                    + " vo='"+vo+"', "
                    + " fqan='"+fqan+"',"
                    + " proxyRenewal='"+proxyrenewal+"'");
    }
    
    public void setUserProxy(String value) {
        sessionM.setUserProxy(value);
        log.info("Using the proxy '"+value+"'");
    }

    public String getUserProxy() {
        return sessionM.getUserProxy();
    }

    public String getBDII() {
        return bdii.getLocation().toString();
    }

    public void setBDII(String value) {
        try {
            bdii = new BDII(new URI(value));
        }
        catch (Exception exc) {
            log.error(exc);
        }
    }
    
    public void upload(String sourceURL, String relativeLocation, String commonName, String tcpAddress, int GridInteractionId, String userDescription) throws DataError{
        try {
            java.net.URL source = new java.net.URL(sourceURL);
            upload(source, relativeLocation, commonName, tcpAddress, GridInteractionId, userDescription);
        } catch (Exception ex) {
            log.error(ex);
            throw new DataError(ex.getMessage());
        }
    }
    
    public URI upload(java.net.URL source, String relativeLocation, String commonName, String tcpAddress, int GridInteractionId, String userDescription) throws DataError {
    	ArrayList<URI> srms = null;
    	URI uriSrm = null;

    	try {
			srms = new java.util.ArrayList<URI>(bdii.querySRMURIs(sessionM.getUserVO()));
		} catch (Exception e) {
			log.error(e);
			throw new DataError(e.getMessage());
		}
		Random rdm = new Random();
		while(srms.size()>0){
	    	int idxSrm = rdm.nextInt(srms.size());
			try {
		    	uriSrm = new URI(srms.get(idxSrm).toString().concat(relativeLocation));
				copyFile(URLFactory.createURL(source.toString()), URLFactory.createURL(uriSrm.toString()), commonName, tcpAddress, GridInteractionId, userDescription);
				break;
			} catch (BadParameterException e) {
				log.error(e);
				srms.remove(idxSrm);
			} catch (NoSuccessException e) {
				log.error(e);
				srms.remove(idxSrm);
			} catch (URISyntaxException e) {
				log.error(e);
				srms.remove(idxSrm);
			}
			
		}
		if(srms.size()==0)
			throw new DataError("All available storage fails");
		
		return uriSrm;
		
	}

	public void copyFile(String sourceURL, String destinationURL, String commonName, String tcpAddress, int GridInteractionId, String userDescription) throws DataError {
        try {
            URL source = URLFactory.createURL(sourceURL);
            URL destination = URLFactory.createURL(destinationURL);
            copyFile(source, destination, commonName, tcpAddress, GridInteractionId, userDescription);
        } catch (Exception ex) {
            log.error(ex);
            throw new DataError(ex.getMessage());
        }
    }

    public void copyFile(java.net.URL sourceURL, java.net.URL destinationURL, String commonName, String tcpAddress, int GridInteractionId, String userDescription) throws DataError {
        try {
            URL source = URLFactory.createURL(sourceURL.toString());
            URL destination = URLFactory.createURL(destinationURL.toString());
            copyFile(source, destination, commonName, tcpAddress, GridInteractionId, userDescription);
        } catch (Exception ex) {
            log.error(ex);
            throw new DataError(ex.getMessage());
        }
    	
    }
	
    private void copyFile(URL source, URL destination, String commonName, String tcpAddress, int GridInteractionId, String userDescription) throws DataError {
    	int gIntAct = userTrackingDB.InsertActiveGridInteraction(
    			commonName, 
    			tcpAddress, 
    			GridInteractionId, 
    			"["+source+"]"+"-["+destination+"]", 
    			sessionM.getUserDN(), 
    			sessionM.getProxyId(), 
    			sessionM.getUserVO(), 
    			sessionM.getUserFQAN(), 
    			userDescription);
        Session session = sessionM.getSession();

        try {
            NSEntry entrySource = NSFactory.createNSEntry(session, source);
            
            entrySource.copy(destination);
            entrySource.close();
            userTrackingDB.CloseGridInteraction(gIntAct);
            sessionM.closeSession();
        } catch (Exception ex) {
            log.error(ex);
            userTrackingDB.CloseGridInteraction(gIntAct);
            throw new DataError(ex.getMessage());
        }
        
    }

}
