package ru.infotecs.cicd;

public class KettleInternalException extends RuntimeException {

	public KettleInternalException() {
	}

	public KettleInternalException(String message) {
		super(message);
	}

	public KettleInternalException(String message, Throwable cause) {
		super(message, cause);
	}

	public KettleInternalException(Throwable cause) {
		super(cause);
	}
}
