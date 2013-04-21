package de.haw_hamburg.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import de.haw_hamburg.requests.*;

import de.haw_hamburg.replies.Reply;

public abstract class Pop3Component extends Thread {

	protected PrintWriter out;
	protected BufferedReader in;
	protected Pop3State state = Pop3State.IDLE;
	protected static final String CRLF="\r\n";
	protected static final String TERMINATION=".";
	protected abstract Logger getLog();

	public Pop3State getPop3State() {
		return state;
	}

	protected void ensureCorrectState(Pop3State... expectedState) {
		if (!new HashSet<Pop3State>(Arrays.asList(expectedState))
				.contains(state))
			throw new IllegalStateException("Expected "
					+ Arrays.asList(expectedState) + ". was" + state.toString());
	}

	protected String readLine() throws IOException {
		String result=in.readLine();
		getLog().info("Received: "+result);
		return result;
	}

	protected void println(String line) throws IOException {
		getLog().info("Sending reply: "+line);
		out.println(line);
	}
	
	protected void println(Request request) throws IOException {
		getLog().info("Sending reply: "+request);
		out.println(request.toString());
	}

	protected void println(Reply reply) throws IOException {
		getLog().info("Sending reply: "+reply);
		out.println(reply.toString());
	}
	
}
