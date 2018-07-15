package com.sgchanges.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ChangesWorkerConfig {
	
	private static final Logger log = Logger.getLogger( ChangesWorkerConfig.class.getName() );
	
	public static final String CONFIG_FILE_PROP = "ChangesWorkerConfig.file";
	public static final String CONFIG_FILE_DEFAULT = "ChangesWorkerConfig.properties";
	
	// Singleton pattern
	private static ChangesWorkerConfig instance;
	
	private Properties props;
	
	static {
		instance = new ChangesWorkerConfig();
	}

	private ChangesWorkerConfig() {
		loadProps();
	}
	
	private void loadProps() {
		try {
			File fConfig = new File(System.getProperty(CONFIG_FILE_PROP, CONFIG_FILE_DEFAULT));
			log.info("Loading configuration from file: " + fConfig.getAbsolutePath());
			props = new Properties();
			props.load(new FileInputStream(fConfig));
			log.info("Configuration loaded: " + this.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String propName) {
		return instance.props.getProperty(propName);
	}

	public static String getSgAddress() {
		return instance.props.getProperty("sgAddress");
	}

	public static String getDb() {
		return instance.props.getProperty("db");
	}

	public static String getUser() {
		return instance.props.getProperty("user");
	}

	public static String getPassword() {
		return instance.props.getProperty("password");
	}

	public static Integer getLimit() {
		return instance.props.getProperty("limit") == null ? null : Integer.parseInt(instance.props.getProperty("limit"));
	}

	public static String getStyle() {
		return instance.props.getProperty("style");
	}

	public static Boolean getActive_only() {
		return instance.props.getProperty("active_only") == null ? null : Boolean.valueOf(instance.props.getProperty("active_only"));
	}

	public static Boolean getInclude_docs() {
		return instance.props.getProperty("include_docs") == null ? null : Boolean.valueOf(instance.props.getProperty("include_docs"));
	}

	public static String getFilter() {
		return instance.props.getProperty("filter");
	}

	public static String getChannels() {
		return instance.props.getProperty("channels");
	}

	public static String getDoc_ids() {
		return instance.props.getProperty("doc_ids");
	}

	public static String getFeed() {
		return instance.props.getProperty("feed");
	}

	public static Integer getSince() {
		return instance.props.getProperty("since") == null ? null : Integer.parseInt(instance.props.getProperty("since"));
	}

	public static Integer getHeartbeat() {
		return instance.props.getProperty("heartbeat") == null ? null : Integer.parseInt(instance.props.getProperty("heartbeat"));
	}
	
	public static Integer getTimeout() {
		return instance.props.getProperty("timeout") == null ? null : Integer.parseInt(instance.props.getProperty("timeout"));
	}
	
	public static String getMessageProcessor() {
		return instance.props.getProperty("messageProcessor");
	}
	
	public static String getSequenceProcessor() {
		return instance.props.getProperty("sequenceProcessor");
	}
	
	public static Integer getRetrySeconds() {
		return instance.props.getProperty("retrySeconds") == null ? new Integer(60) : Integer.parseInt(instance.props.getProperty("retrySeconds"));
	}
	
	public static Integer getRetryTimes() {
		return instance.props.getProperty("retryTimes") == null ? new Integer(0) : Integer.parseInt(instance.props.getProperty("retryTimes"));
	}

	@Override
	public String toString() {
		return "ChangesWorkerConfig [props=" + props.toString() + "]";
	}
	
}
