package ru.infotecs.cicd;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class KettleConnectorImpl implements KettleConnector {

	private final ConcurrentHashMap<String, KettleState> knownKettles = new ConcurrentHashMap<>();
	private final AtomicReference<MqttConnector> mqttStorage = new AtomicReference<>();

	private final String connectionUrl;

	public KettleConnectorImpl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	@Override
	public void startListening() throws KettleInternalException {
		try {
			MqttConnector mqttConnector = MqttConnector.create(connectionUrl);
			try (MqttConnector previousConnector = mqttStorage.getAndSet(mqttConnector)) {
				// TODO: 14.02.2024 log closing
			} catch (ConnectionNotClosedException e) {
				// TODO: 14.02.2024 log failure to close and handle dangling
			}
			mqttConnector.subscribe("polaris/+/+/state/last_wifi", ((topic, message) -> {
				String[] topicParts = topic.split("/");
				String kettleId = topicParts[1] + topicParts[2];
				// TODO: 14.02.2024 add sub to state
				knownKettles.putIfAbsent(kettleId,
						createKettleState(kettleId, ConnectedState.ACTIVE, 0, SwitchMode.OFF));
			}));
			mqttConnector.subscribe("polaris/+/+/state/mode", ((topic, message) -> {
				String[] topicParts = topic.split("/");
				String kettleId = topicParts[1] + topicParts[2];
				Optional<SwitchMode> mode = SwitchMode.valueOfKey(
						new String(message.getPayload(), StandardCharsets.UTF_8));

				knownKettles.computeIfPresent(kettleId,
						(key, state) -> createKettleState(kettleId, state.connectedState(), state.temperature(),
								mode.orElse(state.switchMode())));
			}));
			mqttConnector.subscribe("polaris/+/+/state/sensor/temperature", ((topic, message) -> {
				String[] topicParts = topic.split("/");
				String kettleId = topicParts[1] + topicParts[2];

				knownKettles.computeIfPresent(kettleId,
						(key, state) -> {
							int temperature = 0;
							String temperatureRaw = new String(message.getPayload(), StandardCharsets.UTF_8);
							String[] temperatureTokens = temperatureRaw.split("\\.");
							if (temperatureTokens.length >= 1) {
								temperature = Integer.parseInt(temperatureTokens[0]);
							}
							return createKettleState(kettleId, state.connectedState(), temperature,
									state.switchMode());
						});
			}));
		} catch (RuntimeException e) {
			throw new KettleInternalException(e);
		}
	}

	private KettleState createKettleState(String id, ConnectedState connectedState, int temperature,
			SwitchMode switchMode) {
		return new KettleState(id, connectedState, temperature, switchMode);
	}

	@Override
	public void stopListening() throws KettleInternalException {
		try {
			try (MqttConnector previousConnector = mqttStorage.getAndSet(null)) {
				// TODO: 14.02.2024 log closing
			} catch (ConnectionNotClosedException e) {
				// TODO: 14.02.2024 log failure to close and handle dangling
			}
			knownKettles.clear();
		} catch (RuntimeException e) {
			throw new KettleInternalException(e);
		}
	}

	@Override
	public Collection<KettleState> getAvailable() {
		return knownKettles.searchValues(10,
				kettleState -> kettleState.connectedState() == ConnectedState.ACTIVE ? List.of(kettleState) : null);
	}

	@Override
	public void turnOn(String id) throws KettleInternalException {
		MqttConnector mqttConnector = mqttStorage.get();
		if (mqttConnector == null) {
			throw new KettleInternalException("Broker connection not established");
		}
		try {
			mqttConnector.publish("polaris/%s/%s/control/mode".formatted(id.substring(0, 2), id.substring(2)), "1");
		} catch (RuntimeException e) {
			throw new KettleInternalException(e);
		}
	}

	@Override
	public void turnOff(String id) throws KettleInternalException {
		MqttConnector mqttConnector = mqttStorage.get();
		if (mqttConnector == null) {
			throw new KettleInternalException("Broker connection not established");
		}
		try {
			mqttConnector.publish("polaris/%s/%s/control/mode".formatted(id.substring(0, 2), id.substring(2)), "0");
		} catch (RuntimeException e) {
			throw new KettleInternalException(e);
		}
	}

	@Override
	public void heat(String id, int temperature) throws KettleInternalException {
		MqttConnector mqttConnector = mqttStorage.get();
		if (mqttConnector == null) {
			throw new KettleInternalException("Broker connection not established");
		}
		try {
			mqttConnector.publish("polaris/%s/%s/control/mode".formatted(id.substring(0, 2), id.substring(2)), "3");
			mqttConnector.publish("polaris/%s/%s/control/temperature".formatted(id.substring(0, 2), id.substring(2)), String.valueOf(temperature));
		} catch (RuntimeException e) {
			throw new KettleInternalException(e);
		}
	}

	@Override
	public void close() throws ConnectionNotClosedException {
		try (MqttConnector mqttConnector = mqttStorage.get()) {
			// TODO: 13.02.2024 logger info
		}
	}
}
