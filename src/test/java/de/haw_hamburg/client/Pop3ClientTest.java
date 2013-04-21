package de.haw_hamburg.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import de.haw_hamburg.Pop3TestCommon;
import de.haw_hamburg.common.Pop3State;
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;

public class Pop3ClientTest {

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
	public void testLogin() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
	}

	@Test
	public void testQuit() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.quit();
		assertEquals(Pop3State.IDLE, client.getPop3State());
	}

	@Test
	public void testStat() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.stat();
		assertTrue("Message count must be greater than zero",
				client.getNumberOfMessagesInMaildrop() > 0);
		// assertTrue("Size of Maildrop must be larger than zero",
		// client.getSizeOfMaildropInOctets() > 0);
	}

	@Test
	public void testList() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		assertEquals(2, client.getMessageInfo().size());
		client.quit();
	}

	@Test
	public void testListWithMessageNumber() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list(1);
		assertEquals(1, client.getMessageInfo().size());
		client.list(2);
		assertEquals(2, client.getMessageInfo().size());
		client.quit();
	}

	@Test
	public void testRetrieve() throws JAXBException {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.retr(1);
		client.quit();
		assertEquals(1, DBUtils.getAccountForName("waelc").getMessages()
				.getMessage().size());
		client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.retr(2);
		client.quit();
		assertEquals(2, DBUtils.getAccountForName("waelc").getMessages()
				.getMessage().size());
	}

	@Test
	public void testDelete() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		assertEquals(2, client.getMessageInfo().size());
		client.dele(1);
		client.quit();
		client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		assertEquals(1, client.getMessageInfo().size());
		client.quit();
	}

	@Test
	public void testReset() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		assertEquals(2, client.getMessageInfo().size());
		client.dele(1);
		client.rset();
		client.quit();
		client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();
		assertEquals(2, client.getMessageInfo().size());
		client.quit();
	}

	@Test
	public void testNoop() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.noop(); // this is a silly test but at least it shows that it
						// doesnt crash something
		client.quit();
	}

	@Test
	public void testUidl() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();

		assertFalse(client.getUidl().containsKey(1));
		client.uidl();
		assertTrue(client.getUidl().containsKey(1));

		client.quit();
	}

	@Test
	public void testUidlWithMessageNumber() {
		AccountType account = DBUtils.createAccountType("waelc", "soooosecret",
				"localhost", 3110);
		Pop3Client client = Pop3Client.create(account);
		client.connect();
		assertEquals(Pop3State.CONNECTED, client.getPop3State());
		client.login();
		assertEquals(Pop3State.TRANSACTION, client.getPop3State());
		client.list();

		assertFalse(client.getUidl().containsKey(1));
		client.uidl(1);
		assertTrue(client.getUidl().containsKey(1));

		client.quit();
	}

	@After
	public void tearDown() throws Exception {
		if (null != greenMail) {
			greenMail.stop();
		}
	}


}
