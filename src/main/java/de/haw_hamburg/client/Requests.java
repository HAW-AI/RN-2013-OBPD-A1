package de.haw_hamburg.client;

public class Requests {
	
	private Requests(){}
	
	public static String user(String user){
		return "USER " + user;
	}

	public static String pass(String pass){
		return "PASS " + pass; 
	}
	
	public static String quit(){
		return "QUIT";
	}
	
	public static String stat(){
		return "STAT";
	}
	
	public static String list(){
		return "LIST";
	}
	
	public static String list(int messageNumber){
		return "LIST " + messageNumber;
	}
	
	public static String message(String message){
		//TODO: Single point has to be dealt with
		return message;
	}
	
	public static String retrieve(int messageNumber){
		return "RETR " + messageNumber;
	}
	
	public static String delete(int messageNumber){
		return "DELE " + messageNumber;
	}
	
	public static String noop(){
		return "NOOP";
	}
	
	public static String reset(){
		return "RSET";
	}
	
	public static String uniqueIdListing(){
		return "UIDL";
	}
	
	public static String uniqueIdListing(int messageNumber){
		return "UIDL " + messageNumber;
	} 
	
}
