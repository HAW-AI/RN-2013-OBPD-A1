package de.haw_hamburg.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.haw_hamburg.Starter;
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
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

	private Map<Integer, Integer> messageInfo = new HashMap<Integer, Integer>();
	private Map<Integer, String> uidl = new HashMap<Integer, String>();
	private Set<Integer> markedAsDeleted = new HashSet<Integer>();
	private Logger LOG = Logger.getLogger(Pop3Client.class.getName());

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
			logError("Failed to send username", e);
			e.printStackTrace();
		}
	}

	private void pass() {
		ensureCorrectState(State.AUTHORIZATION);
		try {
			sendAndWaitForOk(Requests.pass(account.getPassword()));
		} catch (IOException e) {
			logError("Failed to send pass", e);
			e.printStackTrace();
		}
	}

	protected void quit() {
		ensureCorrectState(State.AUTHORIZATION,State.TRANSACTION);
		try {
			sendAndWaitForOk(Requests.quit());
		} catch (IOException e) {
			logError("Failed to quit", e);
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
			logError("Failed to get stat", e);
			e.printStackTrace();
		}
	}

	protected void list() {
		ensureCorrectState(State.TRANSACTION);
		try {
			if (sendAndWaitForOk(Requests.list())) {
				String response = readLine();
				while (!response.trim().equals(".")) {
					updateMessageInfo(response);
					response = readLine();
				}
			} else {
				LOG.warning("Received error");
			}
		} catch (IOException e) {
			logError("Failed to list messages", e);
			e.printStackTrace();
		}
	}

	private void updateMessageInfo(String response) {
		String[] responseArray = response.split("\\s");
		if (responseArray.length == 2) {
			try {
				messageInfo.put(Integer.parseInt(responseArray[0]),
						Integer.parseInt(responseArray[1]));
			} catch (NumberFormatException e) {
				LOG.warning(e.getMessage() + "\n" + e.getCause());
			}
		} else {
			LOG.warning("Omitting response because I cannot understand it: "
					+ response);
		}
	}
	
	private void updateUidl(String response) {
		String[] responseArray = response.split("\\s");
		if (responseArray.length == 2) {
			try {
				uidl.put(Integer.parseInt(responseArray[0]),
						responseArray[1]);
			} catch (NumberFormatException e) {
				LOG.warning(e.getMessage() + "\n" + e.getCause());
			}
		} else {
			LOG.warning("Omitting response because I cannot understand it: "
					+ response);
		}
	}

	protected void list(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			Reply reply = sendAndWaitForOkAndParams(Requests
					.list(messageNumber));
			if (reply.isOk()) {
				String params = ((OkReply) reply).getParams();
				updateMessageInfo(params);
			} else {
				LOG.warning("Received error");
			}

		} catch (IOException e) {

		}
	}

	protected void retr(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			sendAndWaitForOk(Requests.retrieve(messageNumber));
			saveMessage(messageNumber);
		} catch (Exception e) {
			logError("Failed to retrieve message " + messageNumber, e);
			e.printStackTrace();
		}
	}

	private void saveMessage(Integer messageNumber) throws IOException, JAXBException {
		StringBuilder message = new StringBuilder("");
		String response = readLine();
		while (!response.trim().equals(".")) {
			message.append(response);
			response = readLine();
		}
		DBUtils.saveMessage(account, message.toString(), messageNumber);
	}

	protected void dele(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			if (sendAndWaitForOk(Requests.delete(messageNumber))) {
				markedAsDeleted.add(messageNumber);
			} else {
				LOG.warning("Failed to mark message " + messageNumber
						+ " as deleted");
			}
		} catch (IOException e) {
			logError("Failed to mark message as deleted", e);
		}
	}

	protected void noop() {
		ensureCorrectState(State.TRANSACTION);
		try {
			sendAndWaitForOk(Requests.noop());
		} catch (IOException e) {
			logError("Failed to noop", e);
			e.printStackTrace();
		}
	}

	protected void rset() {
		ensureCorrectState(State.TRANSACTION);
		try {
			if (sendAndWaitForOk(Requests.reset())) {
				markedAsDeleted.clear();
			} else {
				// Should not happen
				LOG.warning("Failed to reset");
			}

		} catch (IOException e) {
			logError("Failed to reset", e);
			e.printStackTrace();
		}
	}

	protected void uidl() {
		ensureCorrectState(State.TRANSACTION);
		try {
			if(sendAndWaitForOk(Requests.uniqueIdListing())){
				String response=readLine();
				while(response.trim().equals(".")){
					updateUidl(response);
					response=readLine();
				}
			}
			else{
				LOG.warning("Failed to get uid listing");
			}
		} catch (IOException e) {
			logError("Failed to get uid listing", e);
			e.printStackTrace();
		}
	}

	protected void uidl(Integer messageNumber) {
		ensureCorrectState(State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			Reply reply=sendAndWaitForOkAndParams(Requests.uniqueIdListing(messageNumber));
			if(reply.isOk()){
				updateUidl(((OkReply)reply).getParams());
			}
			else{
				LOG.warning("Failed to get uid listing\n"+reply);
			}
		} catch (IOException e) {
			LOG.warning("Failed to get uid listing for message "+messageNumber);
			e.printStackTrace();
		}
	}

	private void ensureMessageNotMarkedAsDeleted(Integer messageNumber) {
		if (markedAsDeleted.contains(messageNumber))
			throw new IllegalArgumentException(
					"Tried to access message marked for deletion.");
	}

	private void ensureCorrectState(State... expectedState) {
		if(!new HashSet<State>(Arrays.asList(expectedState)).contains(state))
			throw new IllegalStateException("Expected "
					+ expectedState.toString() + ". was" + state.toString());
	}

	private boolean sendAndWaitForOk(String command) throws IOException {
		println(command);
		return isOk();
	}

	private Reply sendAndWaitForOkAndParams(String command) throws IOException {
		println(command);
		return OkWithParams();
	}

	private Reply OkWithParams() {
		String response = null;
		try {
			response = readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Replies.replyFromString(response);
	}

	private boolean isOk() throws IOException {
		String response = readLine();
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

	private String readLine() throws IOException {
		return in.readLine();
	}

	private void println(String line) throws IOException {
		out.println(line);
	}

	private void logError(String message, Exception e) {
		LOG.warning(message + "\n" + e.getMessage() + "\n" + e.getCause());
	}
}
