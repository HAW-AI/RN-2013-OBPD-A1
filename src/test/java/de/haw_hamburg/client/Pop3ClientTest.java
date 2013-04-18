package de.haw_hamburg.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.db.ObjectFactory;


public class Pop3ClientTest {

    GreenMail greenMail;
    
    @Before
    public void setUp() throws Exception {
    	
    	Files.copy(Paths.get("src/main/resources/messageDbTemplate.xml"), Paths.get("src/main/resources/messageDb.xml"), StandardCopyOption.REPLACE_EXISTING);
        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.start();

        //use random content to avoid potential residual lingering problems

        MimeMessage message=construct("wael@localhost.com", "from@example.com");
        MimeMessage message2=construct("wael@localhost.com", "from@example.com");
        GreenMailUser user = greenMail.setUser("wael@localhost.com", "waelc", "soooosecret");
        user.deliver(message);
        user.deliver(message2);
    }

    @Test
    public void testLogin() {
        AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED,client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION,client.getClientState());
    }
    
    @Test
    public void testQuit() {
    	AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED, client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION, client.getClientState());
        client.quit();
        assertEquals(Pop3Client.State.IDLE, client.getClientState());
    }
    
    @Test
    public void testStat() {
    	AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED, client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION, client.getClientState());
        client.stat();
        assertTrue("Message count must be greater than zero", client.getNumberOfMessagesInMaildrop() > 0);
        //assertTrue("Size of Maildrop must be larger than zero", client.getSizeOfMaildropInOctets() > 0);
    }
    
    @Test
    public void testList(){
    	AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED, client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION, client.getClientState());
        client.list();
        assertEquals(2,client.getMessageInfo().size());
        client.quit();
    }
    
    @Test
    public void testListWithMessageNumber(){
    	AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED, client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION, client.getClientState());
        client.list(1);
        assertEquals(1,client.getMessageInfo().size());
        client.list(2);
        assertEquals(2,client.getMessageInfo().size());
        client.quit();
    }
    
    @Test
    public void testRetrieve() throws JAXBException{
    	AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED, client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.TRANSACTION, client.getClientState());
        client.retr(1);
        client.quit();
        assertEquals(1,DBUtils.getAccountForName("waelc").getMessages().getMessage().size());
    }
        
    
    @After
    public void tearDown() throws Exception {
        if (null!=greenMail) {
            greenMail.stop();
        }
    }
    
    private MimeMessage construct(String to,String from){
        // Assuming you are sending email from localhost
        //String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        //properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        try{
           message.setFrom(new InternetAddress(from));
           message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(to));
           message.setSubject("This is the Subject Line!");
           message.setContent("This is actual message","text/plain");
        }catch (MessagingException mex) {
           mex.printStackTrace();
		}
        
        return message;
    }

}
