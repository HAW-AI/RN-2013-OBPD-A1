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
import javax.xml.bind.JAXBElement;
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

	public static boolean removeMessagesMarkedForDeletion(AccountType account,
			Set<Integer> messagesMarkedForDeletion)
			throws FileNotFoundException, JAXBException {
		boolean result = true;
		MessagesType messages = new MessagesType();
		for (MessageType message : account.getMessages().getMessage()) {
			if (!messagesMarkedForDeletion.contains(Pop3Server.safeLongToInt(message.getId()))) {
				messages.message.add(message);
				// TODO if something goes wrong we should return false. right now
				// 		this method always returns true.
			}
		}
		account.messages = messages;
		saveAccount(account);
		return result;
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
    
    public static List<MessageType> getAllMessages(){
        return new ArrayList<MessageType>();
    }
    
    public static Map<String,MessageType> getAllMessagesUidl(){
        return new HashMap<String,MessageType>();
    }

}
