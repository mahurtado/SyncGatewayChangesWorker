package com.sgchanges.utils;

import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.Map;

import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.engine.sequence.SequenceProcessor;

public class ChangesWorkerUtils {

	public static final String FILTER_BY_CHANNEL = "sync_gateway/bychannel";
	public static final String FILTER_BY_DOC_ID = "_doc_ids";
	
	public static void authenticateHttp(HttpURLConnection connection) throws Exception {
		String user = ChangesWorkerConfig.getUser();
		String password = ChangesWorkerConfig.getPassword();
		if(user != null && password != null) {
			String authToken = user + ":" + password;
			String authEncoded = Base64.getEncoder().encodeToString(authToken.getBytes("UTF-8"));
			connection.setRequestProperty  ("Authorization", "Basic " + authEncoded);
		}
	}

	public static void authenticateHttpWS(Map<String, String> headers) throws Exception {
		String user = ChangesWorkerConfig.getUser();
		String password = ChangesWorkerConfig.getPassword();
		if(user != null && password != null) {
			String authToken = user + ":" + password;
			String authEncoded = Base64.getEncoder().encodeToString(authToken.getBytes("UTF-8"));
			headers.put("Authorization", "Basic " + authEncoded);
		}
	}
	
	public static String getBaseUrl(boolean isWebsocket) throws Exception {
		StringBuffer sbUrl = new StringBuffer().append(isWebsocket ? "ws://" : "http://")
.append(ChangesWorkerConfig.getSgAddress())
			.append("/")
			.append(ChangesWorkerConfig.getDb())
			.append("/_changes?")
			.append("feed=" + ChangesWorkerConfig.getFeed());
		return sbUrl.toString();
	}
	
	public static String getExtraParamsUrl(SequenceProcessor seqProcessor, boolean isJson) throws Exception {
		StringBuffer sbUrl = new StringBuffer();
		
		if(seqProcessor == null) {
			appendNotNull(sbUrl, "since", ChangesWorkerConfig.getSince(), isJson);
		}
		else {
			appendNotNull(sbUrl, "since", seqProcessor.getLastProcessedSeq(), isJson);	
		}
		
		appendNotNull(sbUrl, "limit", ChangesWorkerConfig.getLimit(), isJson);
		appendNotNull(sbUrl, "style", ChangesWorkerConfig.getStyle(), isJson);
		appendNotNull(sbUrl, "active_only", ChangesWorkerConfig.getActive_only(), isJson);
		appendNotNull(sbUrl, "include_docs", ChangesWorkerConfig.getInclude_docs(), isJson);
		if(ChangesWorkerConfig.getFilter() != null) {
			appendNotNull(sbUrl, "filter", ChangesWorkerConfig.getFilter(), isJson);
			if(FILTER_BY_CHANNEL.equals(ChangesWorkerConfig.getFilter()))
				appendNotNull(sbUrl, "channels", ChangesWorkerConfig.getChannels(), isJson);
			else if(FILTER_BY_DOC_ID.equals(ChangesWorkerConfig.getFilter()))
				appendNotNull(sbUrl, "doc_ids", ChangesWorkerConfig.getDoc_ids(), isJson);
		}
		appendNotNull(sbUrl, "heartbeat", ChangesWorkerConfig.getHeartbeat(), isJson);
		appendNotNull(sbUrl, "timeout", ChangesWorkerConfig.getTimeout(), isJson);
		
		if(isJson) {
			String sub = sbUrl.toString();
			return "{" + sub.substring(0, sub.length()-1) + "}" ;
		}
		else
			return sbUrl.toString();

	}	
	
	private static void appendNotNull(StringBuffer sbUrl, String pre, Object obj, boolean isJson) {
		if(obj != null) {
			if(isJson)
				sbUrl.append("\"" + pre + "\":" + obj.toString() + ",");
			else
				sbUrl.append("&" + pre + "=" + obj.toString());
		}
	}
	
}
