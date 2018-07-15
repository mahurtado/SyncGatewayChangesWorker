package com.sgchanges.utils;

import java.net.HttpURLConnection;
import java.util.Base64;

import com.sgchanges.config.ChangesWorkerConfig;

public class ChangesWorkerUtils {

	public static void authenticateHttp(HttpURLConnection connection) throws Exception {
		String user = ChangesWorkerConfig.getUser();
		String password = ChangesWorkerConfig.getPassword();
		if(user != null && password != null) {
			String authToken = user + ":" + password;
			String authEncoded = Base64.getEncoder().encodeToString(authToken.getBytes("UTF-8"));
			connection.setRequestProperty  ("Authorization", "Basic " + authEncoded);
		}
	}

}
