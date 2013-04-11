package de.haw_hamburg.client;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.ObjectFactory;


public class Pop3ClientTest {

    GreenMail greenMail;
    
    @Before
    public void setUp() throws Exception {
        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.start();

        //use random content to avoid potential residual lingering problems
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        GreenMailUser user = greenMail.setUser("wael@localhost.com", "waelc", "soooosecret");
    }

    @Test
    public void testLogin() {
        AccountType account=ObjectFactory.createAccountType("waelc", "soooosecret", "localhost", 3110);
        Pop3Client client=Pop3Client.create(account);
        client.connect();
        assertEquals(Pop3Client.State.CONNECTED,client.getClientState());
        client.login();
        assertEquals(Pop3Client.State.AUTHORIZATION,client.getClientState());
    }
    
    @After
    public void tearDown() throws Exception {
        if (null!=greenMail) {
            greenMail.stop();
        }
    }

}
