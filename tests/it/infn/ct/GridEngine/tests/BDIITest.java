package it.infn.ct.GridEngine.tests;

import static org.junit.Assert.*;

import it.infn.ct.GridEngine.InformationSystem.BDII;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.junit.Test;

public class BDIITest {

	@Test
	public void testQuerySRMURIs() {
		Set<URI> srms = null;
		try {
			BDII bdii = new BDII(new URI("ldap://gilda-bdii.ct.infn.it:2170"));
			srms = bdii.querySRMURIs("gilda");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("Andata", srms!=null);
		for(URI srm:srms){
			System.out.println("SRM: "+srm.toString());
		}
	}

}
