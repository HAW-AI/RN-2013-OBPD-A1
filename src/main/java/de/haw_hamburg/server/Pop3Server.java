package de.haw_hamburg.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.haw_hamburg.common.Pop3Component;
import de.haw_hamburg.common.Pop3State;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.db.MessageType;
import de.haw_hamburg.replies.ErrorReply;
import de.haw_hamburg.replies.OkReply;
import de.haw_hamburg.requests.Request;
import de.haw_hamburg.requests.Requests;

public class Pop3Server extends Pop3Component {
	
	private Map<Integer, MessageType> markedAsDeleted;
	private List<MessageType> messages;
	private boolean correctUserName = false;
	public static final String USER_NAME = "waelc";
	public static final String PASSWORD = "soooosecret";
	private Logger LOG;
	private Socket socket;

	private Pop3Server(Socket socket,BufferedReader in, PrintWriter out,
			List<MessageType> messages,int number) {
		this.socket=socket;
		this.in = in;
		this.out = out;
		this.state = Pop3State.CONNECTED;
		this.messages = messages;
		this.markedAsDeleted = new HashMap<Integer, MessageType>();
		LOG=Logger.getLogger(Pop3Server.class.getName() + " " + number);
	}

	public static Pop3Server create(Socket socket,int number) throws IOException,
			JAXBException {
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		return new Pop3Server(socket,in, out, DBUtils.getAllMessages(),number);

	}

	public void run() {
		try {
			println(OkReply.okReply("POP server ready"));
			state = Pop3State.AUTHORIZATION;
			String rawRequest = in.readLine();
			while (!this.isInterrupted() && rawRequest != null) {
				try {
					LOG.info("Received request: " + rawRequest);
					handleRequest(rawRequest);
					rawRequest = in.readLine();
				} catch (JAXBException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			LOG.info("Connection terminated");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			LOG.warning("Failed to close socket:\n"+e.getMessage());
		}
	}

	public void handleRequest(String rawRequest) throws IOException,
			JAXBException {
		Request request = Requests.fromRawRequest(rawRequest);

		if (request.isUser()) {
			ensureCorrectState(Pop3State.AUTHORIZATION);
			// get user
			if (USER_NAME.equals(request.param())) {
				correctUserName = true;
				sendOk(OkReply.okReply("please enter password"));
			} else {
				sendError("Unknown user");
			}
		} else if (request.isPass()) {
			ensureCorrectState(Pop3State.AUTHORIZATION);
			// get pass
			if (correctUserName && PASSWORD.equals(request.param())) {
				sendOk();
				state = Pop3State.TRANSACTION;
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
			// List<MessageType> messages = DBUtils.getAllMessages();
			if (!request.hasParam()) {
				sendOk();
				// unique-id listing follows
				for (int i = 0; i < messages.size(); i++) {
					if (!isMessageMarkedForDeletion(i + 1))
						println((i + 1) + " " + messages.get(i).getProxyuid());
				}
				println(TERMINATION);
			} else {
				int indexOfMessage = Integer.parseInt(request.param()) - 1;
				MessageType message = messages.get(indexOfMessage);
				if (isMessageMarkedForDeletion(indexOfMessage + 1))
					sendError("message marked for deletion");
				else {
					sendOk(OkReply.okReply("" + indexOfMessage + " "
							+ message.getProxyuid()));
				}
			}
			// differentiate between simple and complex somehow
		} else if (request.isQuit()) {
			ensureCorrectState(Pop3State.AUTHORIZATION, Pop3State.TRANSACTION);
			if (state == Pop3State.TRANSACTION) {
				state = Pop3State.UPDATE;
				if (removeMessagesMarkedForDeletion()) {
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
			if (!request.hasParam()) {
				sendOk();
				for (int i = 0; i < messages.size(); i++) {
					if (!isMessageMarkedForDeletion(i + 1))
						println(i + 1 + " "
								+ messages.get(i).getContentLengthInBytes());
				}
				println(TERMINATION);
			} else {
				MessageType requestedMessage = null;
				int indexOfMessage = Integer.parseInt(request.param()) - 1;
				requestedMessage = messages.get(indexOfMessage);

				if (requestedMessage == null
						|| isMessageMarkedForDeletion(safeLongToInt(requestedMessage
								.getId()))) {
					sendError("no such message");
				} else {
					OkReply reply = OkReply.okReply((indexOfMessage + 1) + ""
							+ requestedMessage.getContentLengthInBytes());
					sendOk(reply);
				}
			}
		} else if (request.isReset()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			resetMessagesMarkedForDeletion();
			sendOk();
		} else if (request.isNoop()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			sendOk();
		} else if (request.isRetrieve()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			// List<MessageType> messages = DBUtils.getAllMessages();
			int indexOfMessageInMessagesList = Integer
					.parseInt(request.param()) - 1;
			if (messages.size() >= indexOfMessageInMessagesList + 1) {
				MessageType message = messages
						.get(indexOfMessageInMessagesList);
				sendOk(OkReply.okReply("" + message.getId() + " "
						+ message.getContentLengthInBytes() + "octets"));
				println(message.getContent().replaceAll("\n\\.", "\n.."));
				println(TERMINATION);
			} else {
				sendError("no such message");
			}
		} else if (request.isStat()) {
			ensureCorrectState(Pop3State.TRANSACTION);
			// List<MessageType> messages = DBUtils.getAllMessages();
			int sizeOfMaildrop = 0;
			for (MessageType message : messages) {
				sizeOfMaildrop += message.getContentLengthInBytes();
			}
			// send ok with number of messages in maildrop and number of bytes
			sendOk(OkReply.okReply("" + messages.size() + " " + sizeOfMaildrop));
		} else if (request.isUnknown()) {
			sendError("unknown request");
		} else {
			// this should not happen!!!
		}
	}

	private boolean removeMessagesMarkedForDeletion()
			throws FileNotFoundException, JAXBException {
		return DBUtils
				.removeMessagesMarkedForDeletion(new ArrayList<MessageType>(
						markedAsDeleted.values()));
	}

	
	@SuppressWarnings("unused")
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
		return markedAsDeleted.containsKey(messageNumber);
	}

	private boolean markMessageForDeletion(Integer messageNumber) {
		if (isMessageMarkedForDeletion(messageNumber)) {
			return false;
		} else if (messageNumber > messages.size() || messageNumber < 1) {
			return false;
		} else {
			markedAsDeleted.put(messageNumber, messages.get(messageNumber - 1));
			return true;
		}
	}

	private void resetMessagesMarkedForDeletion() {
		markedAsDeleted.clear();
	}

	private void disconnect() {
		interrupt();
	}

	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l
					+ " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	@Override
	protected Logger getLog(){
		return LOG;
	}

}
