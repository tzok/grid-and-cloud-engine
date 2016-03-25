package it.infn.ct.GridEngine.JobResubmission;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import it.infn.ct.GridEngine.Config.GridEngineConfig;


public class GESessionFactoryUtil {
	
	private static SessionFactory sessionFactory;
	private static final Logger logger = Logger.getLogger(GESessionFactoryUtil.class);
	
	static{
		try{			
			GridEngineConfig gec = GridEngineConfig.getInstance();
			sessionFactory = new Configuration().setProperty("hibernate.connection.datasource", gec.getUserstrackingDatasource()).configure().buildSessionFactory();
			logger.info("Using hibernate datasource: " + gec.toString());
		}
		catch(Throwable ex){
			logger.info("Could not find hibernate datasource, using local connection parameters ...");
			sessionFactory = new Configuration().configure("hibernateStandAlone.cfg.xml").buildSessionFactory();
			logger.info("Configured Hibernate local datasource");
		}
	}

	public static SessionFactory getSessionfactory() {
		return sessionFactory;
	}
	
}
