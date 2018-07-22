package com.sgchanges.engine.feed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.engine.ChangesEngine;
import com.sgchanges.engine.sequence.SequenceProcessor;
import com.sgchanges.utils.ChangesWorkerUtils;
import com.sgchanges.utils.RetryManager;

public class ContinuousFeedConsumer implements Runnable, FeedConsumer {
	
	private static final Logger log = Logger.getLogger( ChangesEngine.class.getName() ); 
	private BufferedReader input;
	public boolean isRunning;
	
	private SequenceProcessor seqProcessor;
	private ChangesEngine engine;

	public ContinuousFeedConsumer(SequenceProcessor seqProcessor, ChangesEngine engine) {
		this.seqProcessor = seqProcessor; 
		this.engine = engine;
	}

	@Override
	public void run() {
		RetryManager rm = new RetryManager(ChangesWorkerConfig.getRetrySeconds(), ChangesWorkerConfig.getRetryTimes());
		boolean isConnected = false;
		isRunning = true;
		while(isRunning) {
			try {
				while(!isConnected && rm.hasNext()) {
					isConnected = openConnection();
					if(!isConnected) {
						log.info("Connection failed. Reconnecting in " + rm.getInterval() + " seconds");
						Thread.sleep(rm.getNextWaitTime());
					}
				}
				if(!isConnected) {
					stop();
				}
				rm.reset();
				String message = null; 
				while(isRunning && (message = input.readLine()) != null) {
					engine.consume(message, this);
				}
			} catch (Exception e) {
				isConnected = false;
				if(rm.hasNext()) {
					try {
						log.info("Connection not established (" + e.getMessage() + "). Reconnecting in " + rm.getInterval() + " seconds");
						Thread.sleep(rm.getNextWaitTime());
					} catch (InterruptedException e1) {
						log.info(e1.getMessage());
					}
				}
				else {
					log.info("Connection not established (" + e.getMessage() + "). Maximum connect attempts done (" + rm.getTimes() + "). Exiting ... ");
					stop();
				}
			}
		}
		
	}

	public boolean openConnection() {
		try {
			URL url = new URL(ChangesWorkerUtils.getBaseUrl(false) + ChangesWorkerUtils.getExtraParamsUrl(seqProcessor, false));
			log.info("Connecting to: " + url.toString());
	
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			ChangesWorkerUtils.authenticateHttp(connection);

			input = new BufferedReader (new InputStreamReader (connection.getInputStream()));
			log.info("Connection established. Waiting for changes");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void stop() {
		isRunning = false;
	}


}
