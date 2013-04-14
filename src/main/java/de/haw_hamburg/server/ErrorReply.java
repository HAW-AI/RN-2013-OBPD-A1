package de.haw_hamburg.server;

public class ErrorReply implements Reply {
	String errorMessage;

	private ErrorReply() {}
	private ErrorReply(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public static ErrorReply errorReply() {
		return new ErrorReply();
	}

	public static ErrorReply errorReply(String errorMessage) {
		return new ErrorReply(errorMessage.trim());
	}
	public static String errorString() {
		return "-ERR";
	}	
}