package de.haw_hamburg.db;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class DBUtils {
	
	private static final String JAXBCONTEXT = "de.haw_hamburg.db";
	private static final String DATABASE_PATH = "messageDb.xml";
	
	private static Database getDatabase() throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Database) unmarshaller.unmarshal(new File(DATABASE_PATH));
	}
	
	public static List<AccountType> getAccounts() throws JAXBException{
		Database db=getDatabase();
		if(db!=null){
			return db.getAccount();
		}
		else {
			return new ArrayList<AccountType>();
		}
	}
	
	public static AccountType getAccountForName(String name) throws JAXBException {
		AccountType result=null;
		for (AccountType account : getAccounts()) {
			if(account.getName().equals(name)){
				result=account;
				break;
			}
		}
		return result;
	}
	
    public static void saveAccount(AccountType account) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(JAXBCONTEXT);
        Database db = getDatabase();
        List<AccountType> list = db.getAccount();
        for(AccountType elem:list){
        	if(elem.getName().equals(account.getName())){
        		elem.setMessages(account.getMessages());
        	}
        }
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                new Boolean(true));
        marshaller.marshal(db, new FileOutputStream(DATABASE_PATH));
    }

	public static void saveMessage(AccountType account, String content,int id) throws FileNotFoundException, JAXBException {
		MessageType newMessage=new MessageType();
		newMessage.setId(id);
		newMessage.setContent(content);
		account.getMessages().getMessage().add(newMessage);
		saveAccount(account);
	}
    
    


}
