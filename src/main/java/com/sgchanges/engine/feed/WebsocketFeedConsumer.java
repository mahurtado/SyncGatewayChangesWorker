package com.sgchanges.engine.feed;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;
import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.engine.ChangesEngine;
import com.sgchanges.engine.sequence.SequenceProcessor;
import com.sgchanges.utils.RetryManager;

public class WebsocketFeedConsumer implements FeedConsumer {

	private static final Logger log = Logger.getLogger( WebsocketFeedConsumer.class.getName() ); 

	private SequenceProcessor seqProcessor;
	private ChangesEngine engine;
	private WebsocketFeedConnection conn;
	private URI serverUri;
	private Map<String, String> httpHeaders;
	private boolean isRunning;

	public WebsocketFeedConsumer(URI serverUri, Map<String, String> httpHeaders, SequenceProcessor seqProcessor, ChangesEngine engine) {
		this.serverUri = serverUri;
		this.httpHeaders = httpHeaders;
		this.seqProcessor = seqProcessor;
		this.engine = engine;
		isRunning = true;
	}

	public void startConection() {
		RetryManager rm = new RetryManager(ChangesWorkerConfig.getRetrySeconds(), ChangesWorkerConfig.getRetryTimes());
		boolean isConnected = false;
		while(!isConnected && rm.hasNext()) {
			try {
				conn = new WebsocketFeedConnection(serverUri, httpHeaders, this); 
				conn.setConnectionLostTimeout(0);
				isConnected = conn.connectBlocking();
				if(!isConnected) {
					log.info("Connection failed. Reconnecting in " + rm.getInterval() + " seconds");
					Thread.sleep(rm.getNextWaitTime());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!isConnected && !rm.hasNext())
			log.info("Maximum connect attempts done (" + rm.getTimes() + "). Exiting ... ");
	}

	public SequenceProcessor getSeqProcessor() {
		return seqProcessor;
	}

	public ChangesEngine getEngine() {
		return engine;
	}
	
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void stop() {
		isRunning = false;
		if(conn != null && !conn.isClosed())
			conn.close();
	}
}
