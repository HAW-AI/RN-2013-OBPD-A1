package de.haw_hamburg.server;

public class OkReply implements Reply {
	String params = "";

	private OkReply() {}
	private OkReply(String params) {
		this.params = params;
	}

	public static OkReply okReply() {
		return new OkReply();
	}
	
	public static OkReply okReply(String params) {
		return new OkReply(params.trim());
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
}
