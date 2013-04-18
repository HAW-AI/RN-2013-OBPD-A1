package de.haw_hamburg.common;

public enum Pop3State {
	AUTHORIZATION, CONNECTED, TRANSACTION, UPDATE, IDLE;

	public String toString() {
		switch (this) {
		case AUTHORIZATION:
			return "AUTHORIZATION";
		case TRANSACTION:
			return "TRANSACTION";
		case UPDATE:
			return "UPDATE";
		case IDLE:
			return "IDLE";
		case CONNECTED:
			return "CONNECTED";
		default:
			throw new IllegalArgumentException("Unknown state");
		}
	}
}