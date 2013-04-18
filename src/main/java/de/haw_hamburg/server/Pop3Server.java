package de.haw_hamburg.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.haw_hamburg.client.DeleteRequest;
import de.haw_hamburg.client.NoopRequest;
import de.haw_hamburg.client.PasswordRequest;
import de.haw_hamburg.client.QuitRequest;
import de.haw_hamburg.client.Request;
import de.haw_hamburg.client.Requests;
import de.haw_hamburg.client.ResetRequest;
import de.haw_hamburg.client.RetrieveRequest;
import de.haw_hamburg.client.SimpleListRequest;
import de.haw_hamburg.client.SimpleUidlRequest;
import de.haw_hamburg.client.StatRequest;
import de.haw_hamburg.client.UserRequest;
import de.haw_hamburg.common.Pop3Component;
import de.haw_hamburg.common.Pop3State;

public class Pop3Server extends Pop3Component {

	private Pop3Server(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
		this.state = Pop3State.CONNECTED;
	}

	public Pop3Server create(Socket socket) throws IOException {
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		return new Pop3Server(in, out);
	}

	public void run() {
		while (!this.isInterrupted()) {
			try {
				String rawRequest = in.readLine();
				handleRequest(rawRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void handleRequest(String rawRequest) {
		Request request = Requests.fromRawRequest(rawRequest);
		
		if (request.isUser()) {
			// get user
			// send ok if exists
			// send err if not
		} else if (request.isPass()) {
			// get pass
			// send ok if pass matches user
			// send err if not
		} else if (request.isDelete()) {
			// mark message as deleted unless it has been marked as such already
			// send ok if marked as deleted
			// send err if message not found or already marked
		} else if (request.isUidl()) {
			// differentiate between simple and complex somehow
		} else if (request.isQuit()) {
			// save everything
			// send ok
			// close socket
		} else if (request.isList()) {
			// differentiate between simple and complex
		} else if (request.isReset()) {
			// reset list of messages marked for deletion
			// send ok
		} else if (request.isNoop()) {
			// send ok
		} else if (request.isRetrieve()) {
			// send ok
			// send message
			// send termination crlf.crlf
		} else if (request.isStat()) {
			// send ok with number of messages in maildrop and number of bytes
		} else {
			// FIXME Log error
		}
	}

}
