package com.sgchanges.engine.feed;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sgchanges.utils.ChangesWorkerUtils;

public class WebsocketFeedConnection extends WebSocketClient {

	private static final Logger log = Logger.getLogger( WebsocketFeedConnection.class.getName() ); 
	private WebsocketFeedConsumer wfc;
	JsonParser parser = new JsonParser();

	public WebsocketFeedConnection(URI serverUri, Map<String, String> httpHeaders, WebsocketFeedConsumer wfc) {
		super(serverUri, httpHeaders);
		this.wfc = wfc;
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		log.info("Websocket closed with exit code " + code + "; additional info: " + reason + "; remote: " + remote);
		if(wfc.isRunning() && code != -1)
			wfc.startConection();
	}

	@Override
	public void onError(Exception e) {
		log.severe("Error from websocket feed:" + e.getMessage());
	}

	@Override
	public void onMessage(String message) {		
		if(message.startsWith("[")) {			
			JsonArray jArr = (JsonArray) parser.parse(message);
			for(int i = 0; i < jArr.size(); i++) {
				String mItem = jArr.get(i).toString();
				wfc.getEngine().consume(mItem, wfc);
			}
		}
		else {
			wfc.getEngine().consume(message, wfc);
		}
	}

	@Override
	public void onOpen(ServerHandshake arg0) {
		String json = null;
		try {
			json = ChangesWorkerUtils.getExtraParamsUrl(wfc.getSeqProcessor(), true);
			send(json);
		} catch (Exception e) {
			log.info("Error sending websocket params (" + e.getMessage() + "). json:  " + json);
			e.printStackTrace();
		}
	}

}
