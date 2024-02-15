package ru.infotecs.cicd;

import java.util.Collection;

public interface KettleConnector extends AutoCloseable {

	void startListening() throws KettleInternalException;

	void stopListening() throws KettleInternalException;

	Collection<KettleState> getAvailable();

	void turnOn(String id) throws KettleInternalException;

	void turnOff(String id) throws KettleInternalException;

	void heat(String id, int temperature) throws KettleInternalException;

	void close() throws ConnectionNotClosedException;

	static KettleConnector create(String url) {
		return new KettleConnectorImpl(url);
	}
}
