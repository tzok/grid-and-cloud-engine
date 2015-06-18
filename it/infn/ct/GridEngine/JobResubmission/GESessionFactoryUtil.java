package it.infn.ct.GridEngine.JobResubmission;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class GESessionFactoryUtil {
	
	//private static ServiceRegistry serviceRegistry;
//	private static final SessionFactory sessionFactory;
	private static SessionFactory sessionFactory;
	
	static{
		try{			
			sessionFactory = new Configuration().configure().buildSessionFactory();
			System.out.println("Using jdbc/gehibernatepool");
		}
		catch(Throwable ex){
			//System.err.println("Initial SessionFactory creation failed." + ex);
			sessionFactory = new Configuration().configure("hibernateStandAlone.cfg.xml").buildSessionFactory();
			System.out.println("Using local DB connection");
		}
	}

	public static SessionFactory getSessionfactory() {
		return sessionFactory;
	}
	
}
