package de.haw_hamburg.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replies {
	
	private Replies(){}
	
	public static Reply ok(){
		return OkReply.okReply();
	}
	
	public static Reply ok(String parameters){
		return OkReply.okReply(parameters);
	}
	
	public static Reply error(){
		return ErrorReply.errorReply();
	}
	
	public static Reply error(String message){
		return ErrorReply.errorReply(message);
	}
	
	public static Reply replyFromString(String replyString) {
		String cleanedReplyString = replyString.trim();

		if (cleanedReplyString.startsWith(OkReply.okString())) {
			if (cleanedReplyString.endsWith(OkReply.okString())) {
				return ok();
			} else {
				return ok(cleanedReplyString.substring(OkReply.okString().length(),cleanedReplyString.length()));
			}
		} else {
			if (cleanedReplyString.endsWith(ErrorReply.errorString())) {
				return error();
			} else {
				Matcher matcher = Pattern.compile(ErrorReply.errorString()).matcher(cleanedReplyString);
				matcher.find();
				return ok(matcher.group());
			}
		}
	}

}
