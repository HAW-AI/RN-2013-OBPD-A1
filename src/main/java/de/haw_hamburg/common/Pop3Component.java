package de.haw_hamburg.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

import org.omg.CORBA.Request;

import de.haw_hamburg.server.Reply;

public abstract class Pop3Component extends Thread {

	protected PrintWriter out;
	protected BufferedReader in;
	protected Pop3State state = Pop3State.IDLE;
	protected static final String CRLF="\r\n";
	protected static final String TERMINATION=".";

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
		return in.readLine();
	}

	protected void println(String line) throws IOException {
		System.out.println("Sending: "+line);
		out.println(line+"\r\n");
	}

	protected void println(Request request) throws IOException {
		System.out.println("C: "+request);
		out.println(request.toString()+"\r\n");
	}

	protected void println(Reply reply) throws IOException {
		System.out.println("S: "+reply);
		out.println(reply.toString()+"\r\n");
	}
}
