package de.haw_hamburg.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.haw_hamburg.common.Pop3Component;
import de.haw_hamburg.common.Pop3State;
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.replies.OkReply;
import de.haw_hamburg.replies.Replies;
import de.haw_hamburg.replies.Reply;
import de.haw_hamburg.requests.Request;
import de.haw_hamburg.requests.Requests;

public class Pop3Client extends Pop3Component {

	private AccountType account;
	private Socket socket;
	private int number;

	private Integer numberOfMessagesInMaildrop;
	private Integer sizeOfMaildropInOctets;

	private Map<Integer, Integer> messageInfo = new HashMap<Integer, Integer>();
	private Map<Integer, String> uidl = new HashMap<Integer, String>();
	private Set<Integer> markedAsDeleted = new HashSet<Integer>();
	private Logger LOG = Logger.getLogger(Pop3Client.class.getName() + " " + number);

	private Pop3Client(AccountType account) {
		this.account = account;
	}

	public static Pop3Client create(AccountType account) {
		return new Pop3Client(account);
	}

	public Map<Integer, Integer> getMessageInfo() {
		return messageInfo;
	}

	public void connect() {
		try {
			socket = new Socket(account.getPop3Server(), account.getPop3Port());
			state = Pop3State.CONNECTED;
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
		state = Pop3State.IDLE;
		out.close();
	}

	public void login() {
		ensureCorrectState(Pop3State.CONNECTED);
		state = Pop3State.AUTHORIZATION;
		user();
		pass();
		state = Pop3State.TRANSACTION;
	}

	private void user() {
		ensureCorrectState(Pop3State.AUTHORIZATION);
		try {
			isOk();
			sendAndWaitForOk(Requests.user(account.getName()));
		} catch (IOException e) {
			logError("Failed to send username", e);
			e.printStackTrace();
		}
	}

	private void pass() {
		ensureCorrectState(Pop3State.AUTHORIZATION);
		try {
			sendAndWaitForOk(Requests.pass(account.getPassword()));
		} catch (IOException e) {
			logError("Failed to send pass", e);
			e.printStackTrace();
		}
	}

	public void quit() {
		ensureCorrectState(Pop3State.AUTHORIZATION, Pop3State.TRANSACTION);
		try {
			if (state == Pop3State.TRANSACTION) {
				state = Pop3State.UPDATE;
			}
			sendAndWaitForOk(Requests.quit());
		} catch (IOException e) {
			logError("Failed to quit", e);
			e.printStackTrace();
		}
		disconnect();
	}

	protected void stat() {
		ensureCorrectState(Pop3State.TRANSACTION);
		try {
			OkReply reply = (OkReply) sendAndWaitForOkAndParams(Requests.stat());
			numberOfMessagesInMaildrop = Integer.parseInt(reply.getParams()
					.split("\\s")[0]);
			sizeOfMaildropInOctets = Integer.parseInt(reply.getParams().split(
					"\\s")[1]);
		} catch (IOException e) {
			logError("Failed to get stat", e);
			e.printStackTrace();
		}
	}

	public void list() {
		ensureCorrectState(Pop3State.TRANSACTION);
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

	protected void list(Integer messageNumber) {
		ensureCorrectState(Pop3State.TRANSACTION);
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

	public void retr(Integer messageNumber) {
		ensureCorrectState(Pop3State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			sendAndWaitForOk(Requests.retrieve(messageNumber));
			saveMessage(messageNumber, uidl.get(messageNumber));
		} catch (Exception e) {
			logError("Failed to retrieve message " + messageNumber, e);
			e.printStackTrace();
		}
	}

	protected void dele(Integer messageNumber) {
		ensureCorrectState(Pop3State.TRANSACTION);
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
		ensureCorrectState(Pop3State.TRANSACTION);
		try {
			sendAndWaitForOk(Requests.noop());
		} catch (IOException e) {
			logError("Failed to noop", e);
			e.printStackTrace();
		}
	}

	protected void rset() {
		ensureCorrectState(Pop3State.TRANSACTION);
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
		ensureCorrectState(Pop3State.TRANSACTION);
		try {
			if (sendAndWaitForOk(Requests.uniqueIdListing())) {
				String response = readLine();
				while (!response.trim().equals(".")) {
					updateUidl(response);
					response = readLine();
				}
			} else {
				LOG.warning("Failed to get uid listing");
			}
		} catch (IOException e) {
			logError("Failed to get uid listing", e);
			e.printStackTrace();
		}
	}

	protected void uidl(Integer messageNumber) {
		ensureCorrectState(Pop3State.TRANSACTION);
		ensureMessageNotMarkedAsDeleted(messageNumber);
		try {
			Reply reply = sendAndWaitForOkAndParams(Requests
					.uniqueIdListing(messageNumber));
			if (reply.isOk()) {
				updateUidl(((OkReply) reply).getParams());
			} else {
				LOG.warning("Failed to get uid listing\n" + reply);
			}
		} catch (IOException e) {
			LOG.warning("Failed to get uid listing for message "
					+ messageNumber);
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
				uidl.put(Integer.parseInt(responseArray[0]), responseArray[1]);
			} catch (NumberFormatException e) {
				LOG.warning(e.getMessage() + "\n" + e.getCause());
			}
		} else {
			LOG.warning("Omitting response because I cannot understand it: "
					+ response);
		}
	}

	private void saveMessage(Integer messageNumber, String uid)
			throws IOException, JAXBException {
		StringBuilder message = new StringBuilder("");
		String response = readLine();
		while (!response.trim().equals(".")) {
			message.append(response);
			message.append("\n");
			response = readLine();
		}
		DBUtils.saveMessage(account, message.toString(), messageNumber, uid);
	}

	private void ensureMessageNotMarkedAsDeleted(Integer messageNumber) {
		if (markedAsDeleted.contains(messageNumber))
			throw new IllegalArgumentException(
					"Tried to access message marked for deletion.");
	}

	private boolean sendAndWaitForOk(Request command) throws IOException {
		println(command);
		return isOk();
	}

	private Reply sendAndWaitForOkAndParams(Request command) throws IOException {
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
		return reply.isOk();
	}

	protected Integer getNumberOfMessagesInMaildrop() {
		return this.numberOfMessagesInMaildrop;
	}

	protected Integer getSizeOfMaildropInOctets() {
		return this.sizeOfMaildropInOctets;
	}
	public void setMarkedAsDeleted(Set<Integer> markedAsDeleted) {
		this.markedAsDeleted = markedAsDeleted;
	}

	private void logError(String message, Exception e) {
		LOG.warning(message + "\n" + e.getMessage() + "\n" + e.getCause());
	}

	public Map<Integer, String> getUidl() {
		return uidl;
	}
	
	@Override
	protected Logger getLog(){
		return LOG;
	}
}
