package com.sgchanges.engine.change;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConsoleChangeProcessor implements ChangeProcessor {

	JsonParser parser = new JsonParser();

	public ConsoleChangeProcessor() {

	}

	@Override
	public String process(String msg) {
		JsonObject json = parser.parse(msg).getAsJsonObject();
		Object seq = json.get("seq");
		System.out.println(msg);
		return seq.toString();
	}

}
