package de.haw_hamburg.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.xml.bind.JAXBException;

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
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.db.MessageType;
import de.haw_hamburg.db.MessagesType;

public class Pop3Server extends Pop3Component {
	private Set<Integer> markedAsDeleted;
	private AccountType account;

	private Pop3Server(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
		this.state = Pop3State.CONNECTED;
		this.markedAsDeleted = new HashSet<Integer>();
	}

	public static Pop3Server create(Socket socket) throws IOException {
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
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
	}

	public void handleRequest(String rawRequest) throws IOException,
			JAXBException {
		Request request = Requests.fromRawRequest(rawRequest);

		if (request.isUser()) {
			ensureCorrectState(Pop3State.AUTHORIZATION);
			// get user
			AccountType account = DBUtils.getAccountForName(request.param());
			if (account != null) {
				this.account = account;
				sendOk();
			} else {
				sendError("Unknown user");
			}
		} else if (request.isPass()) {
			ensureCorrectState(Pop3State.AUTHORIZATION);
			// get pass
			if (account != null && account.getPassword() == request.param()) {
				sendOk();
			} else {
				sendError("Not a valid user/password combination");
			}
		} else if (request.isDelete()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			// mark message as deleted unless it has been marked as such already
			if (markMessageForDeletion(Integer.parseInt(request.param()))) {
				sendOk(OkReply.okReply("message deleted"));
			} else {
				sendError("no such message");
			}
		} else if (request.isUidl()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			List<MessageType> messages = DBUtils.getAllMessages();
			if (request instanceof SimpleUidlRequest) {
				sendOk();
				//unique-id listing follows
				for (MessageType message : messages) {
					println("" + message.getId() + "" + message.getUid());
				}
				// TODO send termination crlf.crlf
			} else {
				int indexOfMessage = messages.indexOf(Integer.parseInt(request.param()));
				MessageType message = messages.get(indexOfMessage);
				sendOk(OkReply.okReply("" + message.getId() + "" + message.getUid()));
			}
			// differentiate between simple and complex somehow
		} else if (request.isQuit()) {
			ensureCorrectState(Pop3State.AUTHORIZATION, Pop3State.TRANSACTION);
			if (state == Pop3State.TRANSACTION) {
				state = Pop3State.UPDATE;
				if (removeMessagesMarkedForDeletion()) {
					DBUtils.saveAccount(account); // save all account changes
					sendOk();
				} else {
					sendError("some deleted messages not removed");
				}
			} else {
				// Remove nothing in the AUTHORIZATION State
				sendOk();
			}
			disconnect();
		} else if (request.isList()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			if (request instanceof SimpleListRequest) {
				sendOk();
				for (MessageType message : account.getMessages().getMessage()) {
					if (!isMessageMarkedForDeletion(safeLongToInt(message
							.getId()))) {
						println(listMessageLine(message));
					}
				}
			} else {
				MessageType requestedMessage = null;
				for (MessageType message : account.getMessages().getMessage()) {
					if (message.getId() == (long) Integer.parseInt(request
							.param())) {
						requestedMessage = message;
					}
				}

				if (requestedMessage == null
						|| isMessageMarkedForDeletion(safeLongToInt(requestedMessage
								.getId()))) {
					sendError("no such message");
				} else {
					OkReply reply = OkReply
							.okReply(listMessageLine(requestedMessage));
					sendOk(reply);
				}
			}
			// TODO terminate with CRLF pair
		} else if (request.isReset()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			resetMessagesMarkedForDeletion();
			sendOk();
		} else if (request.isNoop()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			sendOk();
		} else if (request.isRetrieve()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			List<MessageType> messages = DBUtils.getAllMessages();
			int indexOfMessageInMessagesList = messages.indexOf(Integer.parseInt(request.param()));
			if (messages.size() >= indexOfMessageInMessagesList + 1) {
				MessageType message = messages
						.get(indexOfMessageInMessagesList);
				sendOk(OkReply.okReply("" + message.getId() + ""
						+ message.getContentLengthInBytes() + "octets"));
				println(message.getContent());
				// TODO send termination crlf.crlf
			} else {
				sendError("no such message");
			}
		} else if (request.isStat()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			List<MessageType> messages = DBUtils.getAllMessages();
			int sizeOfMaildrop = 0;
			for (MessageType message : messages) {
				sizeOfMaildrop += message.getContentLengthInBytes();
			}
			// send ok with number of messages in maildrop and number of bytes
			sendOk(OkReply.okReply("" + messages.size() + " " + sizeOfMaildrop));
		} else {
			// FIXME Log error
		}
	}

	private boolean removeMessagesMarkedForDeletion()
			throws FileNotFoundException, JAXBException {
		return DBUtils
				.removeMessagesMarkedForDeletion(account, markedAsDeleted);
	}

	private void sendError() throws IOException {
		println(ErrorReply.errorReply());
	}

	private void sendError(String errorMessage) throws IOException {
		println(ErrorReply.errorReply(errorMessage));
	}

	private void sendOk() throws IOException {
		println(OkReply.okReply());
	}

	private void sendOk(OkReply reply) throws IOException {
		println(reply);
	}

	private boolean isMessageMarkedForDeletion(Integer messageNumber) {
		return markedAsDeleted.contains(messageNumber);
	}

	private boolean markMessageForDeletion(Integer messageNumber) {
		if (isMessageMarkedForDeletion(messageNumber)) {
			return false;
		} else {
			return markedAsDeleted.add(messageNumber);
		}
	}

	private void resetMessagesMarkedForDeletion() {
		markedAsDeleted.clear();
	}

	private void disconnect() {
		interrupt();
	}

	private String listMessageLine(MessageType message) {
		return "" + message.getId() + "" + message.getContentLengthInBytes();
	}

	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l
					+ " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

}
