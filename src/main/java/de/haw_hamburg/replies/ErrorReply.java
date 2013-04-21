package de.haw_hamburg.replies;

public class ErrorReply implements Reply {
	String errorMessage = "";

	private ErrorReply() {
	}

	private ErrorReply(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public static ErrorReply errorReply() {
		return new ErrorReply();
	}

	public static ErrorReply errorReply(String errorMessage) {
		return new ErrorReply(errorMessage.trim());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((errorMessage == null) ? 0 : errorMessage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorReply other = (ErrorReply) obj;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		return true;
	}

	public static String errorString() {
		return "-ERR";
	}

	public String toString() {
		return (errorString() + " " + errorMessage).trim();
	}

	public boolean isOk() {
		return false;
	}

	public boolean isError() {
		return true;
	}
}
