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

package it.infn.ct.GridEngine.InformationSystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.naming.directory.Attributes;

/**
 * This class executes LDAP query to a BDII to get information about a gLite
 * based infrastructure
 * 
 * @author mario
 */
public class BDII {

	final static String[] srmServiceType = { "srm", "SRM" };
	final static String[] wmsServiceType = { "org.glite.wms.WMProxy" };
	final static String[] cesServiceType = { "org.glite.ce.CREAM" };

	URI location;

	/**
	 * Constructs a new {@link BDII} object given a LDAP URI
	 * 
	 * @param location
	 *            the given URI (e.g. ldap://gridit-bdii-01.cnaf.infn.it:2170).
	 */
	public BDII(URI location) {
		super();
		this.location = location;
	}

	/**
	 * Returns the LDAP URI.
	 * 
	 * @return LDAP URI.
	 */
	public URI getLocation() {
		return location;
	}

	private Set<URI> querySRMURIs(String vo) throws Exception {

		Set<URI> srmURIs = new HashSet<URI>();

		BDIIQuery q = new BDIIQuery(location.toString());

		ArrayList<SearchResult> res = q
				.query("(&(objectClass=GlueSA)(GlueSAAccessControlBaseRule=VO:"
						+ vo + "))");

		Map<String, String> ids = new HashMap<String, String>();

		for (SearchResult r : res) {

			try {
				String id = r.getAttributes()
						.get("GlueChunkKey").get().toString(); //$NON-NLS-1$;
				id = id.substring(id.indexOf('=') + 1);
				ArrayList<SearchResult> resForPath = q
						.query("(&(GlueChunkKey=GlueSEUniqueID=" + id
								+ ")(GlueVOInfoAccessControlBaseRule=VO:" + vo
								+ "))");
				if (!resForPath.isEmpty()) {
					String path = resForPath.get(0).getAttributes()
							.get("GlueVOInfoPath").get().toString();
					ids.put(id, path);
				}
			} catch (NamingException ex) {
				// Logger.getLogger(BDII.class.getName()).log(Level.FINE,
				// "Error when quering BDII", ex);
			}
		}

		String searchPhrase = "(&(objectClass=GlueService)(GlueServiceUniqueID=*)(GlueServiceAccessControlRule=" + vo + ")"; //$NON-NLS-1$

		searchPhrase += "(|"; //$NON-NLS-1$
		for (String type : srmServiceType) {
			searchPhrase += "(GlueServiceType=" + type + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		searchPhrase += "))";

		// System.out.println(searchPhrase);

		res = q.query(searchPhrase);

		Set<String> srmIds = new HashSet<String>();

		for (SearchResult r : res) {

			try {
				String serviceEndPoint = GlueUtility.getStringAttribute(
						"GlueServiceEndpoint", r.getAttributes());

				URI httpgURI = new URI(serviceEndPoint);

				if (ids.containsKey(httpgURI.getHost())) {

					StringBuilder srmURI = new StringBuilder();

					srmURI.append("srm");
					srmURI.append("://");
					srmURI.append(httpgURI.getHost());
					if (httpgURI.getPort() != -1) {
						srmURI.append(':');
						srmURI.append(httpgURI.getPort());
					}

					// System.out.println();
					srmURI.append(ids.get(httpgURI.getHost()));

					String srmURIString = srmURI.toString();
					srmURIs.add(URI.create(srmURIString));

					srmIds.add(httpgURI.getHost());
				}
				// else
				// Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING,"No path found in BDII for host "
				// + httpgURI.getHost());

			} catch (URISyntaxException e) {
				// Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING,"Error creating URI for a storge element.",
				// e);
			}
		}

		return srmURIs;
	}

	/**
	 * Returns a WMS list for a specified virtual organization.
	 * 
	 * @param vo
	 *            the virtual organization name
	 * @return WMS list for a specified virtual organization.
	 * @throws NamingException
	 */
	public List<URI> queryWMSURIs(String vo) throws NamingException {
		BDIIQuery q = new BDIIQuery(location.toString());

		String searchPhrase = "(&(objectClass=GlueService)(GlueServiceUniqueID=*)(GlueServiceAccessControlRule="
				+ vo + ")";
		searchPhrase += "(|"; //$NON-NLS-1$
		for (String type : wmsServiceType) {
			searchPhrase += "(GlueServiceType=" + type + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		searchPhrase += "))";

		ArrayList<SearchResult> res = q.query(searchPhrase);

		List<URI> wmsURIs = new LinkedList<URI>();

		for (SearchResult r : res) {

			try {
				URI wmsURI = new URI(GlueUtility.getStringAttribute(
						"GlueServiceEndpoint", r.getAttributes()));
				wmsURIs.add(wmsURI);
			} catch (URISyntaxException e) {
				// Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING,
				// "Error creating URI for WMS.", e);
			}
		}

		return wmsURIs;
	}

	/**
	 * Returns a randomly chosen WMS for a specified virtual organization.
	 * 
	 * @param VO
	 *            he virtual organization name
	 * @return randomly chosen WMS for a specified virtual organization.
	 */
	public String getRandomWMS(String VO) {
		try {
			List<URI> wmsList = queryWMSURIs(VO);
			int index = (int) ((Math.random()) * (new Integer(wmsList.size())
					.doubleValue()));

			System.out.println("Index=" + index);
			if (index == wmsList.size())
				index--;

			String wmsSelected = wmsList.get(index).toString()
					.replaceFirst("https", "wms");
			return wmsSelected;
		} catch (NamingException exc) {
			System.out.println(exc.toString());
		}
		return null;
	}

	// ldapsearch -x -LLL -H ldap://bdii.eela.ufrj.br:2170 -b o=grid
	// '(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:prod.vo.eu-eela.eu))'
	// GlueCEUniqueID
	/**
	 * Returns a computing element list for a given virtual organization.
	 * 
	 * @param vo
	 *            virtual organization
	 * @return a computing element list for a given virtual organization.
	 * @throws NamingException
	 */
	public List<String> queryCEQueues(String vo) throws NamingException {
		BDIIQuery q = new BDIIQuery(location.toString());

		String searchPhrase = "(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
				+ vo + "))";
		q.setAttribute("GlueCEUniqueID");

		ArrayList<SearchResult> res = q.query(searchPhrase);

		List<String> ceQueues = new LinkedList<String>();

		for (SearchResult r : res) {
			String ce = GlueUtility.getStringAttribute("GlueCEUniqueID",
					r.getAttributes());
			ceQueues.add(ce);
		}

		return ceQueues;
	}

	/**
	 * Returns a randomly chosen CE for a specified virtual organization.
	 * 
	 * @param VO
	 *            he virtual organization name
	 * @return randomly chosen CE for a specified virtual organization.
	 */
	public String getRandomCE(String VO) {
		try {
			List<String> cesList = queryCEQueues(VO);
			int index = (int) ((Math.random()) * (new Integer(cesList.size())
					.doubleValue()));

			System.out.println("Index=" + index);
			if (index == cesList.size())
				index--;

			String ceSelected = cesList.get(index).toString();
			return ceSelected;
		} catch (NamingException exc) {
			System.out.println(exc.toString());
		}
		return null;
	}

	/**
	 * Returns the geographical coordinates for a given computing element.
	 * 
	 * @param ce
	 *            computing element name
	 * @return a String[] containing latitude and longitude for a given
	 *         computing element.
	 * @throws NamingException
	 */
	public String[] queryCECoordinate(String ce) throws NamingException {
		String coordinates[] = new String[2];
		BDIIQuery q = new BDIIQuery(ce.toString());

		String searchPhrase = "(&(objectClass=GlueSite))";
		q.setAttribute("GlueSiteLatitude");
		q.setAttribute("GlueSiteLongitude");
		ArrayList<SearchResult> res = q.query(searchPhrase);

		for (SearchResult r : res) {
			coordinates[0] = GlueUtility.getStringAttribute("GlueSiteLatitude",
					r.getAttributes());
			System.out.println("latitude" + coordinates[0]);
			coordinates[1] = GlueUtility.getStringAttribute(
					"GlueSiteLongitude", r.getAttributes());
			System.out.println("longitude=" + coordinates[1]);

			break;
		}
		ldap: // gridit-bdii-01.cnaf.infn.it:2170

		if (coordinates[0] == null)
			coordinates[0] = "0";
		if (coordinates[1] == null)
			coordinates[1] = "0";
		return coordinates;
	}

	// ldapsearch -x -H ldap://bdii.eumedgrid.eu:2170 -b o=grid
	// "(&(objectclass=GlueSubCluster)(GlueHostApplicationSoftwareRunTimeEnvironment=VO-eumed-ASTRA))"
	// GlueSubClusterName
	/**
	 * Returns a computing element list for a given software tag.
	 * 
	 * @param tag
	 *            tag identifying software
	 * @return a computing element list for a given software tag.
	 * @throws NamingException
	 */
	public List<String> queryCEForSWTag(String tag) throws NamingException {
		BDIIQuery q = new BDIIQuery(location.toString());

		String searchPhrase = "(&(objectClass=GlueSubCluster)(GlueHostApplicationSoftwareRunTimeEnvironment="
				+ tag + "))";
		q.setAttribute("GlueSubClusterName");

		ArrayList<SearchResult> res = q.query(searchPhrase);

		List<String> ceForSWTag = new LinkedList<String>();

		for (SearchResult r : res) {
			String ce = GlueUtility.getStringAttribute("GlueSubClusterName",
					r.getAttributes());
			ceForSWTag.add(ce);
		}
		return ceForSWTag;
	}

	/**
	 * Returns a randomly chosen computing element for a given software tag.
	 * 
	 * @param tag
	 *            tag identifying software
	 * 
	 * @return a randomly chosen computing element for a given software tag.
	 * @throws NamingException
	 */
	public String getRandomCEForSWTag(String tag) throws NamingException {
		List<String> ceForSWTag = queryCEForSWTag(tag);

		if (ceForSWTag.size() == 0)
			return "";

		int index = (int) ((Math.random()) * (new Integer(ceForSWTag.size())
				.doubleValue()));

		System.out.println("Index=" + index);
		if (index == ceForSWTag.size())
			index--;

		return ceForSWTag.get(index).toString();
	}

	/**
	 * Returns a computing element list for a given SWTag, virtual organization
	 * and maximum CPU time.
	 * 
	 * @param TAG
	 *            tag identifying software
	 * @param VO
	 *            virtual organization
	 * @param MaxCPUTime
	 *            required maximum CPU time
	 * @return a computing element list for a given SWTag, virtual organization
	 *         and maximum CPU time.
	 * @throws NamingException
	 */
	public List<String> queryCEFromSWTag_MaxCPUTime(String TAG, String VO,
			Integer MaxCPUTime) throws NamingException {
		BDIIQuery q = new BDIIQuery(location.toString());

		String searchPhrase = "(&(objectClass=GlueSubCluster)(GlueHostApplicationSoftwareRunTimeEnvironment="
				+ TAG + "))";

		q.setAttribute("GlueSubClusterName");

		ArrayList<SearchResult> res = q.query(searchPhrase);
		List<String> ceFromSWTag_MaxCPUTime = new LinkedList<String>();

		String ce = null;
		for (SearchResult r : res) {
			ce = GlueUtility.getStringAttribute("GlueSubClusterName",
					r.getAttributes());
			searchPhrase = "(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
					+ VO + ")(GlueCEPolicyMaxCPUTime>=" + MaxCPUTime + "))";
			q.setAttribute("GlueCEUniqueID");

			ArrayList<SearchResult> res2 = q.query(searchPhrase);
			for (SearchResult response : res2) {
				String queue = GlueUtility.getStringAttribute("GlueCEUniqueID",
						response.getAttributes());
				// Check if the queue matches the ce...
				if (queue.matches(ce + "(.*)"))
					ceFromSWTag_MaxCPUTime.add(ce);
			}
		}
		return ceFromSWTag_MaxCPUTime;
	}

	/**
	 * Returns a randomly chosen computing element for a given SWTag, virtual
	 * organization. and maximum CPU time.
	 * 
	 * @param TAG
	 *            tag identifying software
	 * @param VO
	 *            virtual organization
	 * @param MaxCPUTime
	 *            required maximum CPU time
	 * @return a randomly chosen computing element for a given SWTag, virtual
	 *         organization
	 * @throws NamingException
	 */
	public String getRandomCEFromSWTag_MaxCPUTime(String TAG, String VO,
			Integer MaxCPUTime) throws NamingException {
		List<String> ceFromSWTag_MaxCPUTime = queryCEFromSWTag_MaxCPUTime(TAG,
				VO, MaxCPUTime);

		if (ceFromSWTag_MaxCPUTime.size() == 0)
			return "";

		int index = (int) ((Math.random()) * (new Integer(
				ceFromSWTag_MaxCPUTime.size()).doubleValue()));
		if (index == ceFromSWTag_MaxCPUTime.size())
			index--;
		return ceFromSWTag_MaxCPUTime.get(index).toString();
	}

	// Retrieve the GlueSEImplementationName for a given storage resource
	// String SE_HOSTNAME = "ldap://iceage-se-01.ct.infn.it:2170";
	/**
	 * Retrieve the GlueSEImplementationName for a given storage resource
	 * 
	 * @param SE_HOSTNAME
	 *            storage element. (e.g. ldap://iceage-se-01.ct.infn.it:2170)
	 * @return the GlueSEImplementationName for a given storage resource
	 * @throws NamingException
	 */
	public String getGlueSEImplementationName(String SE_HOSTNAME)
			throws NamingException {
		String GlueSEImplementationName = "";

		BDIIQuery q = new BDIIQuery(SE_HOSTNAME);
		String searchPhrase = ("(&(objectClass=GlueSE))");
		q.setAttribute("GlueSEImplementationName");

		ArrayList<SearchResult> res = q.query(searchPhrase);

		for (SearchResult r : res)
			GlueSEImplementationName = GlueUtility.getStringAttribute(
					"GlueSEImplementationName", r.getAttributes());

		return GlueSEImplementationName;
	}

	// Retrieve the GlueVOInfoPath for a given storage resource and a VO
	// String SE_HOST = "iceage-se-01.ct.infn.it";
	// String VO = "gridit";
	/**
	 * Retrieve the GlueVOInfoPath for a given storage resource and a VO.
	 * 
	 * @param SE_HOST
	 *            storage element host. (e.g. iceage-se-01.ct.infn.it)
	 * @param VO_NAME
	 *            virtual organization name.
	 * @return the GlueVOInfoPath for a given storage resource and a VO.
	 * @throws NamingException
	 */
	public String getGlueVOInfoPath(String SE_HOST, String VO_NAME)
			throws NamingException {

		String GlueVOInfoPath = "";

		BDIIQuery q = new BDIIQuery(location.toString());
		String searchPhrase = ("(&(GlueVOInfoLocalID=" + VO_NAME
				+ "*)(GlueChunkKey=GlueSEUniqueID=" + SE_HOST + "))");
		q.setAttribute("GlueVOInfoPath");

		ArrayList<SearchResult> res = q.query(searchPhrase);

		for (SearchResult r : res)
			GlueVOInfoPath = GlueUtility.getStringAttribute("GlueVOInfoPath",
					r.getAttributes());

		return GlueVOInfoPath;
	}

	// Retrieve the list of GlueCEUniqueID for a given VO and a
	// GlueCEPolicyMaxCPUTime
	// String VO = "gridit";
	// Integer MAX_CPUTime = 30240;
	/**
	 * Retrieve the list of GlueCEUniqueID for a given VO and a
	 * GlueCEPolicyMaxCPUTime
	 * 
	 * @param VO_NAME
	 *            virtual organization name
	 * @param MaxCPUTime
	 *            required maximum CPU time
	 * @return the list of GlueCEUniqueID for a given VO and a
	 *         GlueCEPolicyMaxCPUTime
	 * @throws NamingException
	 */
	public List<String> getGlueCEUniqueIDs(String VO_NAME, Integer MaxCPUTime)
			throws NamingException {

		BDIIQuery q = new BDIIQuery(location.toString());
		String searchPhrase = ("(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
				+ VO_NAME + "(GlueCEPolicyMaxCPUTime>=" + MaxCPUTime + "))");
		q.setAttribute("GlueCEUniqueID");

		ArrayList<SearchResult> res = q.query(searchPhrase);
		List<String> GlueCEUniqueIDs = new LinkedList<String>();

		for (SearchResult r : res) {
			String CE = GlueUtility.getStringAttribute("GlueCEUniqueID",
					r.getAttributes());
			GlueCEUniqueIDs.add(CE);
		}

		return GlueCEUniqueIDs;
	}

	// Retrieve a full list of GlueCEInfoHostName for a given VO
	// String VO = "gridit";
	/**
	 * Retrieve a full list of GlueCEInfoHostName for a given VO.
	 * 
	 * @param VO_NAME
	 *            virtual organization name.
	 * @return a full list of GlueCEInfoHostName for a given VO.
	 * @throws NamingException
	 */
	public List<String> getGlueCEInfoHostNames(String VO_NAME)
			throws NamingException {

		BDIIQuery q = new BDIIQuery(location.toString());
		String searchPhrase = ("(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
				+ VO_NAME + "))");
		q.setAttribute("GlueCEInfoHostName");

		ArrayList<SearchResult> res = q.query(searchPhrase);
		List<String> GlueCEInfoHostNames = new LinkedList<String>();

		for (SearchResult r : res) {
			String CE = GlueUtility.getStringAttribute("GlueCEInfoHostName",
					r.getAttributes());
			GlueCEInfoHostNames.add(CE);
		}

		return GlueCEInfoHostNames;
	}

	// Retrieve the defaultSE for a given CE
	// String CE_HOST = "grid012.ct.infn.it";
	/**
	 * Retrieve the defaultSE for a given computing element.
	 * 
	 * @param CE_HOST
	 *            computing element host.(e.g. grid012.ct.infn.it).
	 * @return the defaultSE for a given computing element.
	 * @throws NamingException
	 */
	public String getGlueCEInfoDefaultSE(String CE_HOST) throws NamingException {
		String GlueCEInfoDefaultSE = "";

		BDIIQuery q = new BDIIQuery(location.toString());
		String searchPhrase = ("(&(objectClass=GlueCE)(GlueCEUniqueID="
				+ CE_HOST + "*))");
		q.setAttribute("GlueCEInfoDefaultSE");

		ArrayList<SearchResult> res = q.query(searchPhrase);

		for (SearchResult r : res)
			GlueCEInfoDefaultSE = GlueUtility.getStringAttribute(
					"GlueCEInfoDefaultSE", r.getAttributes());

		return GlueCEInfoDefaultSE;
	}

	public String getRandomCEFromSWTag_MaxWallClockTime(String TAG, String VO,
			Integer MaxWallClockTime) throws NamingException {

		List<String> ceFromSWTag_MaxWallClockTime = queryCEFromSWTag_MaxWallClockTime(
				TAG, VO, MaxWallClockTime);
		if (ceFromSWTag_MaxWallClockTime.size() == 0)
			return "";
		int index = (int) ((Math.random()) * (new Integer(
				ceFromSWTag_MaxWallClockTime.size()).doubleValue()));
		if (index == ceFromSWTag_MaxWallClockTime.size())
			index--;
		return ceFromSWTag_MaxWallClockTime.get(index).toString();
	}

	public List<String> queryCEFromSWTag_MaxWallClockTime(String TAG,
			String VO, Integer MaxWallClockTime) throws NamingException {
		BDIIQuery q = new BDIIQuery(location.toString());

		String searchPhrase = "(&(objectClass=GlueSubCluster)(GlueHostApplicationSoftwareRunTimeEnvironment="
				+ TAG + "))";

		q.setAttribute("GlueSubClusterName");

		List<String> CE_LIST = new LinkedList<String>();
		List<String> QUEUE_LIST = new LinkedList<String>();

		ArrayList<SearchResult> res = q.query(searchPhrase);
		List<String> ceFromSWTag_MaxWallClockTime = new LinkedList<String>();

		String ce = null;
		for (SearchResult r : res) {
			ce = GlueUtility.getStringAttribute("GlueSubClusterName",
					r.getAttributes());
			CE_LIST.add(ce);
		}

		searchPhrase = "(&(objectClass=GlueCE)(GlueCEAccessControlBaseRule=VO:"
				+ VO + ")(GlueCEPolicyMaxWallClockTime>=" + MaxWallClockTime
				+ "))";

		q.setAttribute("GlueCEUniqueID");

		ArrayList<SearchResult> res2 = q.query(searchPhrase);
		for (SearchResult response : res2) {
			String queue = GlueUtility.getStringAttribute("GlueCEUniqueID",
					response.getAttributes());
			QUEUE_LIST.add(queue);
		}

		// Check if the queue matches the ce
		for (int i = 0; i < QUEUE_LIST.size(); i++)
			for (int j = 0; j < CE_LIST.size(); j++)
				if ((QUEUE_LIST.get(i)).contains(CE_LIST.get(j)))
					ceFromSWTag_MaxWallClockTime.add(CE_LIST.get(j));

		return ceFromSWTag_MaxWallClockTime;
	}

	public static void main(String[] args) {
		BDII bdii = null;
		String bdiiCometa = "ldap://gridit-bdii-01.cnaf.infn.it:2170";
		String VO = "gridit";
		String tag = "VO-gridit-GEANT4-09-05-patch-01";
		Integer MAX_CPUTime = 30240;
		String cequeue = "";

		try {
			bdii = new BDII(new URI(bdiiCometa));
		} catch (Exception exc) {
			System.out.println(exc.toString());
		}

		try {
			cequeue = bdii
					.getRandomCEFromSWTag_MaxCPUTime(tag, VO, MAX_CPUTime);
		} catch (NamingException e) {
			e.printStackTrace();
		}

		System.out.println("cequeue=" + cequeue);
		/*
		 * String bdiiCometa = "ldap://gridit-bdii-01.cnaf.infn.it:2170";
		 * //String bdiiGisela = "ldap://bdii.eela.ufrj.br:2170"; //String
		 * bdiiEumed = "ldap://topbdii.junet.edu.jo:2170"; //String VO =
		 * "eumed"; String VO = "prod.vo.eu-eela.eu"; String tag =
		 * "VO-gridit-POVRAY-3.6"; //String CE = "ldap://ce01.unlp.edu.ar:2170";
		 * BDII bdii = null;
		 * 
		 * try { bdii = new BDII(new URI(bdiiCometa)); } catch (Exception exc) {
		 * System.out.println(exc.toString()); }
		 * 
		 * List<String> cequeues = null; String cequeue = null;
		 * 
		 * // try { // cequeues = bdii.queryCEQueues(VO); // } catch
		 * (NamingException e) { // // TODO Auto-generated catch block //
		 * e.printStackTrace(); // }
		 * 
		 * // try { // cequeues = bdii.queryCEForSWTag(tag); // } catch
		 * (NamingException e) { // // TODO Auto-generated catch block //
		 * e.printStackTrace(); // }
		 * 
		 * try { cequeue = bdii.getRandomCEForSWTag(tag); } catch
		 * (NamingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * System.out.println("cequeue="+cequeue);
		 * 
		 * // String CECoordinate[] = null; // Iterator<String> iterator =
		 * cequeues.iterator(); // try { // while (iterator.hasNext()) { //
		 * String ce = iterator.next(); // System.out.println(ce); //
		 * CECoordinate = bdii.queryCECoordinate("ldap://"+ce+":2170"); // if
		 * (CECoordinate!=null) { //
		 * System.out.println("latitude="+CECoordinate[0]); //
		 * System.out.println("longitude="+CECoordinate[1]); // // } // } // }
		 * catch (NamingException e) { // // TODO Auto-generated catch block //
		 * e.printStackTrace(); // }
		 * 
		 * // String CECoordinate[] = null; // try { // CECoordinate =
		 * bdii.queryCECoordinate(CE); // } catch (NamingException e) { // //
		 * TODO Auto-generated catch block // e.printStackTrace(); // } // // if
		 * (CECoordinate!=null) { //
		 * System.out.println("latitude"+CECoordinate[0]); //
		 * System.out.println("longitude="+CECoordinate[1]); // // }
		 */
	}

}
