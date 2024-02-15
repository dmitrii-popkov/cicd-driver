package ru.infotecs.cicd;

public class ConnectionNotClosedException extends RuntimeException {
	public ConnectionNotClosedException() {
	}

	public ConnectionNotClosedException(String message) {
		super(message);
	}

	public ConnectionNotClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionNotClosedException(Throwable cause) {
		super(cause);
	}
}
