package de.haw_hamburg.client;

import static de.haw_hamburg.client.Requests.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.server.Replies;

public class Pop3Client extends Thread {

	// Begin in IDLE state
	private State state = State.IDLE;
	private PrintWriter out;
	private BufferedReader in;
	private AccountType account;
	private Socket socket;

	private Pop3Client(AccountType account) {
		this.account = account;
	}

	public Pop3Client create(AccountType account) {
		return new Pop3Client(account);
	}

	private void connect() {
		try {
			socket = new Socket(account.getPop3Server(), account.getPop3Port());
			state = state.CONNECTED;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void login() {
		if (state != State.CONNECTED)
			throw new IllegalStateException("Expected CONNECTED. was"
					+ state.toString());
		try {
			if (isOk() && sendAndWait(user(account.getName()))
					&& sendAndWait(pass(account.getPassword()))) {
				state = State.AUTHORIZATION;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean sendAndWait(String command) throws IOException {
		out.println(command);
		return isOk();
	}

	private boolean isOk() throws IOException {
		String response = in.readLine();
		return response != null && response.startsWith(Replies.ok());
	}

	public void run() {
		connect();
		login();
	}

	private enum State {
		AUTHORIZATION, CONNECTED, TRANSACTION, UPDATE, IDLE;

		public String toString() {
			switch (this) {
			case AUTHORIZATION:
				return "AUTHORIZATION";
			case TRANSACTION:
				return "TRANSACTION";
			case UPDATE:
				return "UPDATE";
			case IDLE:
				return "IDLE";
			case CONNECTED:
				return "CONNECTED";
			default:
				throw new IllegalArgumentException("Unknown state");
			}
		}
	}

}
