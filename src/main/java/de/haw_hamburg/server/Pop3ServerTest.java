package de.haw_hamburg.server;

import static org.junit.Assert.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.junit.Test;

public class Pop3ServerTest {

	@Test
	public void testHandleRequest() throws Exception {
		Properties props = new Properties();

	    String host = "localhost";
	    String username = "waelc";
	    String password = "soooosecret";
	    String provider = "pop3";
	    
	    Pop3Server pop3Server = Pop3Server.create();
	    pop3Server.run();

	    Session session = Session.getDefaultInstance(props, null);
	    Store store = session.getStore(provider);
	    store.connect(host, username, password);

	    
	    Folder inbox = store.getFolder("INBOX");
	    if (inbox == null) {
	      System.out.println("No INBOX");
	      System.exit(1);
	    }
	    inbox.open(Folder.READ_ONLY);

	    Message[] messages = inbox.getMessages();
	    for (int i = 0; i < messages.length; i++) {
	      System.out.println("Message " + (i + 1));
	      messages[i].writeTo(System.out);
	    }
	    inbox.close(false);
	    store.close();
	}

}
