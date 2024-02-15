package ru.infotecs.cicd;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.UUID;

public interface MqttConnector extends AutoCloseable {
	void publish(String topic, String payload) throws RuntimeException;

	void subscribe(String topic, MqttCallback callback) throws RuntimeException;

	static MqttConnector create(String url) throws RuntimeException {
		try {
			return new MqttConnectorImpl(url, () -> UUID.randomUUID().toString());
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	void close() throws ConnectionNotClosedException;
}
