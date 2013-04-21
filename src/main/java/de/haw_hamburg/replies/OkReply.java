package de.haw_hamburg.replies;

import java.util.regex.Pattern;

public class OkReply implements Reply {
	String params = "";

	private OkReply() {
	}

	private OkReply(String params) {
		this.params = params;
	}

	public static OkReply okReply() {
		return new OkReply();
	}

	public static OkReply okReply(String params) {
		return new OkReply(params.trim());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		OkReply other = (OkReply) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

	public static String okRegex() {
		return Pattern.quote(okString());
	}

	public static String okString() {
		return "+OK";
	}

	public String getParams() {
		return this.params;
	}

	public String toString() {
		return (okString() + " " + params).trim();
	}

	public boolean isOk() {
		return true;
	}

	public boolean isError() {
		return false;
	}
}
