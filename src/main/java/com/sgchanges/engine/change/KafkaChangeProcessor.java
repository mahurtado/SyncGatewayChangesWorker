package com.sgchanges.engine.change;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sgchanges.config.ChangesWorkerConfig;

public class KafkaChangeProcessor implements ChangeProcessor {

	JsonParser parser = new JsonParser();
	private Producer<String, String> producer;
	private String topic;

	public KafkaChangeProcessor() {
		topic = ChangesWorkerConfig.getProperty("KafkaChangeProcessor.topic");
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ChangesWorkerConfig.getProperty("KafkaChangeProcessor.bootstrap.servers"));
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		producer = new KafkaProducer<String, String>(props);
	}

	@Override
	public String process(String msg) {
		JsonObject json = parser.parse(msg).getAsJsonObject();;
		String key = json.get("seq").toString();

		ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, key, msg);
		producer.send(data);

		return key;
	}

}
