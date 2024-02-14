package ru.infotecs.cicd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@Slf4j
public class MqttConnectorImpl implements MqttConnector {

	private final IMqttClient client;

	public MqttConnectorImpl(String url, Supplier<String> idGenerator) throws MqttException {
		this.client = new MqttClient(url, idGenerator.get(), new MemoryPersistence());
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		client.connect(options);
	}

	@Override
	public void publish(String topic, String payload) throws RuntimeException {
		if (payload == null || topic == null) {
			throw new RuntimeException("Cannot use with null arguments");
		}
		if (!client.isConnected()) {
			throw new RuntimeException("Client not connected, cannot publish message");
		}
		MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
		message.setQos(0);
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void subscribe(String topic, MqttCallback callback) throws RuntimeException {
		if (topic == null || callback == null) {
			throw new RuntimeException("Cannot use with null arguments");
		}
		if (!client.isConnected()) {
			throw new RuntimeException("Client not connected, cannot subscribe");
		}
		try {
			client.subscribe(topic, callback::call);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws ConnectionNotClosedException {
		try (client) {
			// TODO: 13.02.2024 logger info
		} catch (MqttException e) {
			throw new ConnectionNotClosedException(e); // TODO: 13.02.2024 dangling connections cleanup
		}
	}
}
