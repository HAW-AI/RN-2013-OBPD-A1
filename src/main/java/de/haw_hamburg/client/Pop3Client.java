package de.haw_hamburg.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.server.OkReply;
import de.haw_hamburg.server.Replies;
import de.haw_hamburg.server.Reply;

public class Pop3Client extends Thread {

	// Begin in IDLE state
	private State state = State.IDLE;
	private PrintWriter out;
	private BufferedReader in;
	private AccountType account;
	private Socket socket;

	private Integer numberOfMessagesInMaildrop;
	private Integer sizeOfMaildropInOctets;

	private Pop3Client(AccountType account) {
		this.account = account;
	}

	public static Pop3Client create(AccountType account) {
		return new Pop3Client(account);
	}

	protected void connect() {
		try {
			socket = new Socket(account.getPop3Server(), account.getPop3Port());
			state = State.CONNECTED;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void disconnect() {
		try {
			socket.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		state = State.IDLE;
		out.close();
	}

	// FIXME
	protected void login() {
		ensureCorrectState(State.CONNECTED);
		try {
			if (isOk() && sendAndWaitForOk(Requests.user(account.getName()))
					&& sendAndWaitForOk(Requests.pass(account.getPassword()))) {
				state = State.AUTHORIZATION;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This seems more like what is happening according to the spec
	protected void login2() {
		ensureCorrectState(State.CONNECTED);
		state = State.AUTHORIZATION;
		user();
		pass();
		state = State.TRANSACTION;
	}

	private void user() {
		ensureCorrectState(State.AUTHORIZATION);
		try {
			isOk();
			sendAndWaitForOk(Requests.user(account.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void pass() {
		ensureCorrectState(State.AUTHORIZATION);
		try {
			sendAndWaitForOk(Requests.pass(account.getPassword()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void quit() {
		ensureCorrectState(State.AUTHORIZATION);
		try {
			sendAndWaitForOk(Requests.quit());
		} catch (IOException e) {
			e.printStackTrace();
		}
		disconnect();
	}

	protected void stat() {
		ensureCorrectState(State.TRANSACTION);
		try {
			OkReply reply = (OkReply) sendAndWaitForOkAndParams(Requests.stat());
			numberOfMessagesInMaildrop = Integer.parseInt(reply.getParams()
					.split(" ")[0]);
			sizeOfMaildropInOctets = Integer.parseInt(reply.getParams().split(
					" ")[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void list() {
		ensureCorrectState(State.TRANSACTION);
		// TODO
	}

	protected void list(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		// TODO
	}

	protected void retr(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		// TODO
	}

	protected void dele(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		// TODO
	}

	protected void noop() {
		ensureCorrectState(State.TRANSACTION);
		try {
			sendAndWaitForOk(Requests.noop());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO
	}

	protected void rset() {
		ensureCorrectState(State.TRANSACTION);
		try {
			sendAndWaitForOk(Requests.reset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO
	}

	protected void uidl() {
		ensureCorrectState(State.TRANSACTION);
		// TODO
	}

	protected void uidl(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		// TODO
	}

	private void ensureMessageNotMarkedAsDeleted(Integer messageNumber) {
		// TODO
	}

	private void ensureCorrectState(State expectedState) {
		if (state != expectedState)
			throw new IllegalStateException("Expected "
					+ expectedState.toString() + ". was" + state.toString());
	}

	private boolean sendAndWaitForOk(String command) throws IOException {
		out.println(command);
		return isOk();
	}

	private Reply sendAndWaitForOkAndParams(String command) throws IOException {
		out.println(command);
		return OkWithParams();
	}

	private Reply OkWithParams() {
		String response = null;
		try {
			response = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Replies.replyFromString(response);
	}

	private boolean isOk() throws IOException {
		String response = in.readLine();
		Reply reply = Replies.replyFromString(response);
		return OkReply.class.isInstance(reply);
	}

	protected Pop3Client.State getClientState() {
		return state;
	}

	protected Integer getNumberOfMessagesInMaildrop() {
		return this.numberOfMessagesInMaildrop;
	}

	protected Integer getSizeOfMaildropInOctets() {
		return this.sizeOfMaildropInOctets;
	}

	public void run() {
		connect();
		login();
	}

	public enum State {
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
