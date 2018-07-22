package com.sgchanges.engine;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.engine.change.ChangeProcessor;
import com.sgchanges.engine.feed.ContinuousFeedConsumer;
import com.sgchanges.engine.feed.FeedConsumer;
import com.sgchanges.engine.feed.WebsocketFeedConsumer;
import com.sgchanges.engine.sequence.SequenceProcessor;
import com.sgchanges.utils.ChangesWorkerUtils;

public class ChangesEngine {
	
	private static final Logger log = Logger.getLogger( ChangesEngine.class.getName() ); 
	
	private ChangeProcessor msgProcessor;
	private SequenceProcessor seqProcessor;
	private int limit;
	private int messageCount;
	
	public static void main(String[] args) {
		ChangesEngine engine = new ChangesEngine();
		engine.start();
        System.out.println("ChangesEngine started");
	}
	
	public ChangesEngine() {
		try {
			limit = ChangesWorkerConfig.getLimit() != null ? ChangesWorkerConfig.getLimit() : 0;
			messageCount = 0;
			msgProcessor = (ChangeProcessor) Class.forName(ChangesWorkerConfig.getMessageProcessor()).newInstance();
			if(ChangesWorkerConfig.getSequenceProcessor() != null)
				seqProcessor = (SequenceProcessor) Class.forName(ChangesWorkerConfig.getSequenceProcessor()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		try {
			String feedOption = ChangesWorkerConfig.getFeed();
			switch(feedOption) {
			case "continuous":
				new Thread(new ContinuousFeedConsumer(seqProcessor, this)).start();
				break;
			case "websocket":
				Map<String, String> headers = new HashMap<String,String>();
				ChangesWorkerUtils.authenticateHttpWS(headers);
				WebsocketFeedConsumer client = new WebsocketFeedConsumer(new URI(ChangesWorkerUtils.getBaseUrl(true)), headers, seqProcessor, this);
				client.startConection();
				break;
			default: 
				throw new UnsupportedOperationException("Option ' + feedOption + ' not supported. Only 'continuous' or 'websocket' supported");
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void consume(String message, FeedConsumer feedConsumer) {
		try {
			if(!"".equals(message) && !"[]".equals(message)) { 
				String seq = msgProcessor.process(message);
				messageCount++;
				if(seqProcessor != null)
					seqProcessor.saveProcessedSeq(seq);
				if(limit > 0 && messageCount >= limit) {
					log.info("Message limit reached. Stopping feed service ...");
					feedConsumer.stop();	
				}
			}
		} 
		catch (Exception e) {
			log.severe("Error processing message [" + message + "]: " + e.getMessage());
			e.printStackTrace();
			log.severe("Stopping feed service ...");
			feedConsumer.stop();
		}
	}

}
