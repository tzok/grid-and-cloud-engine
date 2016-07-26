package it.infn.ct.GridEngine.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class GridEngineConfig {

	private static GridEngineConfig instance = null;
	private static final Logger _log = Logger.getLogger(GridEngineConfig.class.getName());
	private final String gePropertiesFile = "GridEngine.properties";

	private String userstrackingDatasource = "jdbc/UserTrackingPool"; 
	private String hibernateDatasource = "jdbc/gehibernatepool";

	public static synchronized GridEngineConfig getInstance() {
		if (instance == null)
			instance = new GridEngineConfig();
		return instance;
	}

	/**
	 * Load the given configuration file which overrides static settings
	 * 
	 * @param configFile
	 */
	private GridEngineConfig() {
		/*
		 * Load a configuration file containing GridEngineDaemon settings wich
		 * override the static settings defined in the class
		 */
		loadProperties();
		_log.info("GridEngine config:" + this.toString());
	}

	private void loadProperties() {
		//
		InputStream inputStream = null;
		Properties prop = new Properties();
		try {
			inputStream = this.getClass().getClassLoader().getResourceAsStream(gePropertiesFile);

			prop.load(inputStream);

			/*
			 * Retrieving configuration values
			 */

			// APIServer DB settings
			String prop_usertrackingDatasource = prop.getProperty("usertracking_datasource");
			String prop_hibernateDatasource = prop.getProperty("hibernate_datasource");

			if (prop_usertrackingDatasource != null) {
				this.userstrackingDatasource = prop_usertrackingDatasource;
			}
			if (prop_hibernateDatasource != null) {
				this.hibernateDatasource = prop_hibernateDatasource;
			}
		} catch (NullPointerException e) {
			_log.warn("Unable to load property file; using default settings");
		} catch (IOException e) {
			_log.warn("Error reading file: " + e);
		} catch (NumberFormatException e) {
			_log.warn("Error while reading property file: " + e);
		} finally {
			try {
				if (null != inputStream)
					inputStream.close();
			} catch (IOException e) {
				_log.error("Error closing configuration file input stream");
			}
		}
	}

	public String getUserstrackingDatasource() {
		return userstrackingDatasource;
	}

	public String getHibernateDatasource() {
		return hibernateDatasource;
	}

	@Override
	public String toString() {
		return "GridEngineConfig [gePropetiesFile=" + gePropertiesFile + ", userstrackingDatasource="
				+ userstrackingDatasource + ", hibernateDatasource=" + hibernateDatasource + "]";
	}

}
