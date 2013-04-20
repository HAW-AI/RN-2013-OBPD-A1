package de.haw_hamburg.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.haw_hamburg.server.Pop3Server;

public class DBUtils {

	private static final String JAXBCONTEXT = "de.haw_hamburg.db";
	private static final String DATABASE_PATH = "data/messageDb.xml";

	private static Database getDatabase() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (Database) unmarshaller.unmarshal(new File(DATABASE_PATH));
	}

	public static List<AccountType> getAccounts() throws JAXBException {
		Database db = getDatabase();
		if (db != null) {
			return db.getAccount();
		} else {
			return new ArrayList<AccountType>();
		}
	}

	public static AccountType getAccountForName(String name)
			throws JAXBException {
		AccountType result = null;
		for (AccountType account : getAccounts()) {
			if (account.getName().equals(name)) {
				result = account;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Add a new account to database. No integrity checks are performed. 
	 * @param account
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public static void addAccount(AccountType account) throws JAXBException, FileNotFoundException{
		JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
		Database db = getDatabase();
		List<AccountType> accounts=db.getAccount();
		accounts.add(account);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(
				true));
		marshaller.marshal(db, new FileOutputStream(DATABASE_PATH));
	}

	public static void saveAccount(AccountType account) throws JAXBException,
			FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
		Database db = getDatabase();
		List<AccountType> list = db.getAccount();
		for (AccountType elem : list) {
			if (elem.getName().equals(account.getName())) {
				elem.setMessages(account.getMessages());
			}
		}
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(
				true));
		marshaller.marshal(db, new FileOutputStream(DATABASE_PATH));
	}

	public static void saveMessage(AccountType account, String content, int id,
			String uid) throws FileNotFoundException, JAXBException {
		MessageType newMessage = new MessageType();
		newMessage.setId(id);
		newMessage.setContent(content);
		if (uid != null) {
			newMessage.setUid(uid);
		}
		newMessage.setProxyuid(UUID.randomUUID().toString());
		account.getMessages().getMessage().add(newMessage);
		saveAccount(account);
	}

	public static boolean removeMessagesMarkedForDeletion(
			List<MessageType> messagesMarkedForDeletion)
			throws FileNotFoundException, JAXBException {
		boolean result = true;
		for(MessageType message:messagesMarkedForDeletion){
			removeMessageByProxyUid(message.getProxyuid());
		}
//		for (MessageType message : account.getMessages().getMessage()) {
//			if (!messagesMarkedForDeletion.contains(Pop3Server
//					.safeLongToInt(message.getId()))) {
//				messages.message.add(message);
//				// TODO if something goes wrong we should return false. right
//				// now
//				// this method always returns true.
//			}
//		}
//		account.messages = messages;
//		saveAccount(account);
		return result;
	}
	
	private static void removeMessageByProxyUid(String proxyUid) throws JAXBException, FileNotFoundException{
		JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Database db= (Database) unmarshaller.unmarshal(new File(DATABASE_PATH));
		List<AccountType> accounts=db.getAccount();
		for(AccountType account:accounts){
			MessageType message=getMessageByProxyUid(account,proxyUid);
			if(message!=null){
				account.getMessages().getMessage().remove(message);
			}
		}
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(
				true));
		marshaller.marshal(db, new FileOutputStream(DATABASE_PATH));
	}
	
	private static MessageType getMessageByProxyUid(AccountType account,String proxyUid){
		for(MessageType message:account.getMessages().getMessage()){
			if(proxyUid.equals(message.getProxyuid()))
				return message;
		}
		return null;
	}

	/**
	 * Create an instance of {@link AccountType }
	 * 
	 */
	public static AccountType createAccountType(String name, String password,
			String pop3server, int pop3port) {
		AccountType result = new AccountType();
		result.setName(name);
		result.setPassword(password);
		result.setPop3Server(pop3server);
		result.setPop3Port(pop3port);
		MessagesType messages = new MessagesType();
		messages.message = new ArrayList<MessageType>();
		result.setMessages(messages);
		return result;
	}

	/**
	 * List of all messages in all accounts. The order is not guaranteed.
	 * @return list of messages
	 * @throws JAXBException
	 */
	public static List<MessageType> getAllMessages() throws JAXBException {
		List<MessageType> result=new ArrayList<MessageType>();
		for(AccountType account:getAccounts()){
			result.addAll(account.getMessages().getMessage());
		}
		return result;
	}

	/**
	 * Get mapping of proxy uid to message
	 * @return map of proxy uid to message
	 * @throws JAXBException
	 * 
	 * TODO: Rename to getAllMessagesProxyUidl
	 */
	public static Map<String, MessageType> getAllMessagesUidl() throws JAXBException {
		Map<String,MessageType> result=new HashMap<String, MessageType>();
		for(MessageType message:getAllMessages()){
			result.put(message.getProxyuid(), message);
		}
		return result;
	}

}
