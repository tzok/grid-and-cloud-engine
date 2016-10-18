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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;


public class JobServicesDispatcherFactory implements javax.naming.spi.ObjectFactory, java.io.Serializable {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {
		JobServicesDispatcher jobServiceDispatcher = JobServicesDispatcher.getInstance();

		try {
			Reference reference = (Reference) obj;
			Enumeration<?> enumeration = reference.getAll();
			while (enumeration.hasMoreElements()) {
				RefAddr refAddr = (RefAddr) enumeration.nextElement();
				String pname = refAddr.getType();
				String pvalue = (String) refAddr.getContent();
				if ("retrycount".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setRetryCount(Integer.parseInt(pvalue));
				}
				else if ("myproxyservers".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setMyproxyServers(pvalue);
				}
				else if ("truststorepath".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setTrustStorePath(pvalue);
				}
				else if ("truststorepassword".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setTrustStorePassword(pvalue);
				}
				else if ("genesistruststorepath".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setGenesisTrustStorePath(pvalue);
				}
				else if ("genesistruststorepassword".equalsIgnoreCase(pname)) {
					jobServiceDispatcher.setGenesisTrustStorePassword(pvalue);
				}
				else if("resubnumber".equalsIgnoreCase(pname)){
					jobServiceDispatcher.setResubmissionNumber(Integer.parseInt(pvalue));
				}
				else if("smtpserver".equalsIgnoreCase(pname)){
					jobServiceDispatcher.setSmtpServer(pvalue);
				}
				else if("senderaddress".equalsIgnoreCase(pname)){
					jobServiceDispatcher.setSenderAddress(pvalue);
				}
				else {
					throw new IllegalArgumentException("Unrecognized property name: " + pname);
				}
			}
		} catch (Exception e) {
			throw (NamingException) (new NamingException()).initCause(e);
		}
		return jobServiceDispatcher;
	}
};