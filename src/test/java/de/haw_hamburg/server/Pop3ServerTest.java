package de.haw_hamburg.server;

import static org.junit.Assert.assertEquals;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import de.haw_hamburg.Pop3TestCommon;
import de.haw_hamburg.Starter;
import de.haw_hamburg.client.Pop3Client;
import de.haw_hamburg.common.Pop3State;
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;

@Ignore
public class Pop3ServerTest {

	GreenMail greenMail;

	@Before
	public void setUp() throws Exception {

		// Files.copy(Paths.get("src/main/resources/messageDbTemplate.xml"),
		// Paths.get("data/messageDb.xml"),
		// StandardCopyOption.REPLACE_EXISTING);
		Runtime.getRuntime()
				.exec("cp -f src/main/resources/messageDbTemplate.xml data/messageDb.xml");
		greenMail = new GreenMail(ServerSetupTest.ALL);
		greenMail.start();

		// use random content to avoid potential residual lingering problems

		MimeMessage message = Pop3TestCommon.construct("wael@localhost.com",
				"from@example.com");
		MimeMessage message2 = Pop3TestCommon.construct("wael@localhost.com",
				"from@example.com");
		GreenMailUser user = greenMail.setUser("wael@localhost.com", "waelc",
				"soooosecret");
		user.deliver(message);
		user.deliver(message2);
	}

	@Test
	public void testHandleRequest() throws Exception {

		// Greenmail running and our own proxy running.
		Starter.main(null);

		// Allow for the Proxy to fetch the mails before the client tries to get
		// mails from the Proxy
		Thread.sleep(100);

		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 11000);
		Pop3Client client = Pop3Client.create(account,0);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		client.retr(2);
		client.quit();
		assertEquals(1, DBUtils.getAccountForNameAndServer("waelc","localhost").getMessages()
				.getMessage().size());
		client = Pop3Client.create(account,1);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		client.retr(1);
		client.quit();
		assertEquals(2, DBUtils.getAccountForNameAndServer("waelc","localhost").getMessages()
				.getMessage().size());
	}

}
