package com.sgchanges.engine.sequence;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sgchanges.config.ChangesWorkerConfig;
import com.sgchanges.utils.ChangesWorkerUtils;

public class SyncGatewaySequenceProcessor implements SequenceProcessor {
	
	private static final Logger log = Logger.getLogger( SyncGatewaySequenceProcessor.class.getName() );
	private String currRev;
	private String clientId;
	private JsonParser parser = new JsonParser();
	
	public SyncGatewaySequenceProcessor() {
		
		String clientIdFileName = ChangesWorkerConfig.getPropertyDef("SyncGatewaySequenceProcessor.clientIdFile", "clientId.txt");
		BufferedReader clientIdFile = null;
		try {
			clientIdFile = new BufferedReader(new FileReader(clientIdFileName));
			String firstLine = clientIdFile.readLine();
			if(firstLine != null) {
				clientId = firstLine;
			}
		} catch (Exception e) {
			// If file not exist, create a random client Id and save to file
			clientId = UUID.randomUUID().toString();
			saveClientId(clientIdFileName);
		}
		finally {
			if(clientIdFile != null)
				try {
					clientIdFile.close();
				} catch (Exception e) {}
		}
		log.info("Using clientId: " + clientId + ", saved in file: " + clientIdFileName);
	}
	
	private void saveClientId(String clientIdFileName) {
		PrintWriter output = null;
		try {
			output = new PrintWriter(new FileWriter(clientIdFileName));
			output.println(clientId);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(output != null)
				try {
					output.close();
				} catch (Exception e) {}
		}
	}

	@Override
	public String getLastProcessedSeq() throws Exception {
		String result = "0";
		try {
			URL url = new URL(getSyncSeqUri());
			
			log.info("getLastProcessedSeq. Connecting to: " + url.toString());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			ChangesWorkerUtils.authenticateHttp(connection);
			
			BufferedReader input = new BufferedReader (new InputStreamReader (connection.getInputStream()));
			String lRes;
			StringBuffer sbRes = new StringBuffer();
			while((lRes = input.readLine()) != null) {
				sbRes.append(lRes);
			}
						
			JsonObject json = parser.parse(sbRes.toString()).getAsJsonObject();;
			result = json.get("seq").toString();
			currRev = json.get("_rev").toString();
		} catch (FileNotFoundException e) {
		}
		return result;
	}

	@Override
	public void saveProcessedSeq(String seq) throws Exception {
		
		URL url = new URL(getSyncSeqUri());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("PUT");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json" );
		
		ChangesWorkerUtils.authenticateHttp(connection);
		
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(getJsonSeq(seq));
		out.close();
		
		BufferedReader input = new BufferedReader (new InputStreamReader (connection.getInputStream()));
		String lRes;
		StringBuffer sbRes = new StringBuffer();
		while((lRes = input.readLine()) != null) {
			sbRes.append(lRes);
		}
		JsonObject json = parser.parse(sbRes.toString()).getAsJsonObject();;
		currRev = json.get("rev").toString();
		
		if(connection.getResponseCode() == HttpURLConnection.HTTP_CREATED)
			return;
		
		else
			log.severe("Error saving sequence '" + seq + "': [HTTP Resp code: " + connection.getResponseCode() + "]");

	}

	private String getJsonSeq(String seq) {
		StringBuffer sbSeq = new StringBuffer("{");
		sbSeq.append("\"_id\":\"" + getId()  + "\",\n");
		if(currRev != null) {
			sbSeq.append("\"_rev\":" + currRev + ",\n");
		}
		sbSeq.append("\"date\":\"" + new Date().toString() + "\",\n");
		sbSeq.append("\"seq\":" + seq);
		sbSeq.append("}");
				
		return sbSeq.toString();
	}

	private String getSyncSeqUri() throws Exception {	
		StringBuffer sbUrl = new StringBuffer("http://")
				.append(ChangesWorkerConfig.getSgAddress())
				.append("/")
				.append(ChangesWorkerConfig.getDb())
				.append("/_local/")
				.append(getId());
		
		return sbUrl.toString();
	}
	
	
	private String getId() {
		return "sgjChangesClient:" + clientId;
	}
}