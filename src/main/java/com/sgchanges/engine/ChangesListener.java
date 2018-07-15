package com.sgchanges.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Logger;

import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.engine.change.ChangeProcessor;
import com.sgchanges.engine.sequence.SequenceProcessor;
import com.sgchanges.utils.ChangesWorkerUtils;
import com.sgchanges.utils.RetryManager;

public class ChangesListener implements Runnable {
	
	private static final Logger log = Logger.getLogger( ChangesListener.class.getName() );
	
	public static final String FILTER_BY_CHANNEL = "sync_gateway/bychannel";
	public static final String FILTER_BY_DOC_ID = "_doc_ids";

	private boolean isRunning = false;
	private ChangeProcessor msgProcessor;
	private SequenceProcessor seqProcessor;
	private BufferedReader input;

	public ChangesListener() {
		try {
			isRunning = true;
			msgProcessor = (ChangeProcessor) Class.forName(ChangesWorkerConfig.getMessageProcessor()).newInstance();
			if(ChangesWorkerConfig.getSequenceProcessor() != null)
				seqProcessor = (SequenceProcessor) Class.forName(ChangesWorkerConfig.getSequenceProcessor()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		RetryManager rm = new RetryManager(ChangesWorkerConfig.getRetrySeconds(), ChangesWorkerConfig.getRetryTimes());
		while(isRunning) {
			try {
				openConnection();
				rm.reset();
				String msg = null; 
				while(isRunning && (msg = input.readLine()) != null) {
					// Skip heartbeats CRLF
					if(!"".equals(msg)) { 
						String seq = msgProcessor.process(msg);
						if(seqProcessor != null)
							seqProcessor.saveProcessedSeq(seq);
					}
				}
			} catch (Exception e) {
				if(rm.hasNext()) {
					try {
						log.info("Connetion not established (" + e.getMessage() + "). Reconnecting in " + rm.getInterval() + " seconds");
						Thread.sleep(rm.getNextWaitTime());
					} catch (InterruptedException e1) {
						log.info(e1.getMessage());
					}
				}
				else {
					log.info("Connetion not established (" + e.getMessage() + "). Maximum connect attempts done (" + rm.getTimes() + "). Exiting ... ");
					isRunning = false;
				}
				log.info(e.getMessage());
			}
		}
	}

	private void openConnection() throws Exception {
			URL url = new URL(getUrlConnectionString());
			log.info("Connecting to: " + url.toString());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			ChangesWorkerUtils.authenticateHttp(connection);

			input = new BufferedReader (new InputStreamReader (connection.getInputStream()));
			log.info("Connection established. Waiting for changes");
	}

	private String getUrlConnectionString() throws Exception {
		StringBuffer sbUrl = new StringBuffer("http://")
				.append(ChangesWorkerConfig.getSgAddress())
				.append("/")
				.append(ChangesWorkerConfig.getDb())
				.append("/_changes?")
				.append("feed=" + ChangesWorkerConfig.getFeed());
		
		if(seqProcessor == null) {
			appendNotNull(sbUrl, "&since=", ChangesWorkerConfig.getSince());
		}
		else {
			appendNotNull(sbUrl, "&since=", seqProcessor.getLastProcessedSeq());	
		}
		
		appendNotNull(sbUrl, "&limit=", ChangesWorkerConfig.getLimit());
		appendNotNull(sbUrl, "&style=", ChangesWorkerConfig.getStyle());
		appendNotNull(sbUrl, "&active_only=", ChangesWorkerConfig.getActive_only());
		appendNotNull(sbUrl, "&include_docs=", ChangesWorkerConfig.getInclude_docs());
		if(ChangesWorkerConfig.getFilter() != null) {
			appendNotNull(sbUrl, "&filter=", ChangesWorkerConfig.getFilter());
			if(FILTER_BY_CHANNEL.equals(ChangesWorkerConfig.getFilter()))
				appendNotNull(sbUrl, "&channels=", ChangesWorkerConfig.getChannels());
			else if(FILTER_BY_DOC_ID.equals(ChangesWorkerConfig.getFilter()))
				appendNotNull(sbUrl, "&doc_ids=", ChangesWorkerConfig.getDoc_ids());
		}
		appendNotNull(sbUrl, "&heartbeat=", ChangesWorkerConfig.getHeartbeat());
		appendNotNull(sbUrl, "&timeout=", ChangesWorkerConfig.getTimeout());
		
		return sbUrl.toString();

	}

	private void appendNotNull(StringBuffer sbUrl, String pre, Object obj) {
		if(obj != null)
			sbUrl.append(pre + obj.toString());
	}
	
	public void stop() {
		isRunning = false;
	}
	
}
