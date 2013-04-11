package de.haw_hamburg.server;

public class Replies {
	
	private Replies(){}
	
	public static String ok(){
		return "+OK";
	}
	
	public static String ok(String message){
		return "+OK " + message;
	}
	
	public static String error(){
		return "-ERR";
	}
	
	public static String error(String message){
		return "-ERR " + message;
	}

}
