package de.haw_hamburg.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.db.MessageType;

public class Pop3UpdateTask extends TimerTask {

	private static Logger LOG=Logger.getLogger(Pop3UpdateTask.class.getName());
	private AccountType account;
	private boolean withUidl;
	
	private Pop3UpdateTask(AccountType account, boolean withUidl){
		this.account=account;
		this.withUidl=withUidl;
	}
	
	public static Pop3UpdateTask create(AccountType account, boolean withUidl){
		return new Pop3UpdateTask(account, withUidl);
	}
	
	@Override
	public void run() {
			Pop3Client client=Pop3Client.create(account);
			client.connect();
			client.login();
			client.list();
			Map<Integer,Integer> messageInfo=client.getMessageInfo();
			if(!messageInfo.isEmpty()){
				Collection<Integer> messagesToDownload=null;
				if(withUidl){
					client.uidl();
					Map<Integer,String> uidl=client.getUidl();
					messagesToDownload=calcMessagesToDownloadFromUidl(uidl);
				}
				else{
					messagesToDownload=messageInfo.keySet();
				}
				for(int messageNumber:messagesToDownload){
					client.retr(messageNumber);
					client.dele(messageNumber);
				}
			}
			client.quit();
	}

	private List<Integer> calcMessagesToDownloadFromUidl(Map<Integer,String> uidl) {
		List<Integer> result=new ArrayList<Integer>();
		try {
		AccountType currentAccount=DBUtils.getAccountForName(account.getName());
		Set<String> uidsOfDownloadedMessages=getUidlsFromAccount(currentAccount);
		for(Map.Entry<Integer, String> entry:uidl.entrySet()){
			if(!uidsOfDownloadedMessages.contains(entry.getValue())){
				result.add(entry.getKey());
			}
		}
		} catch (JAXBException e) {
			LOG.warning("Could not clearify if messages needed to be downloaded \n"+e.getMessage());
		}
		return result;
	}
	
	private Set<String> getUidlsFromAccount(AccountType account){
		Set<String> result=new HashSet<String>();
		for(MessageType message:account.getMessages().getMessage()){
			result.add(message.getUid());
		}
		return result;
	}
	

}
