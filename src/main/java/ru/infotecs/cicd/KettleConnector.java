package ru.infotecs.cicd;

import java.util.Collection;

public interface KettleConnector extends AutoCloseable {

	void startListening() throws KettleInternalException;

	void stopListening() throws KettleInternalException;

	Collection<String> getAvailableIds();

	void turnOn(String id) throws KettleInternalException;

	void turnOff(String id) throws KettleInternalException;

	static KettleConnector create(String url) {
		return null; // TODO: 13.02.2024 tbd
	}
}
