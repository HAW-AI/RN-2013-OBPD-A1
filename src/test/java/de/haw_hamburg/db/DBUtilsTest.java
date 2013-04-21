package de.haw_hamburg.db;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMailUtil;

public class DBUtilsTest {

	@Before
	public void setUp() throws Exception {
		Runtime.getRuntime()
				.exec("cp -f src/main/resources/messageDbTemplate.xml data/messageDb.xml");
		AccountType account1 = DBUtils.createAccountType("Ash", "clatu...",
				"cabinInTheWoods", 666);
		AccountType account2 = DBUtils.createAccountType("OneHandedAsh",
				"clatu...", "fancyCastle", 666);
		DBUtils.addAccount(account1);
		DBUtils.addAccount(account2);
		DBUtils.saveMessage(account1, GreenMailUtil.random(), 1, null);
		DBUtils.saveMessage(account1, GreenMailUtil.random(), 2, null);
		DBUtils.saveMessage(account2, GreenMailUtil.random(), 1, null);
	}

	@Test
	public void testGetAllMessages() throws FileNotFoundException,
			JAXBException {
		assertEquals(3, DBUtils.getAllMessages().size());
	}

	@Test
	public void testGetAllMessagesUidl() throws JAXBException {
		Map<String, MessageType> messages = DBUtils.getAllMessagesProxyUidl();
		for (MessageType message : DBUtils.getAllMessages()) {
			assertTrue(messages.containsKey(message.getProxyuid()));
			assertNotEquals(null, messages.get(message.getProxyuid()));
		}
	}

	@Test
	public void testRemoveMarkedAsDeleted() throws JAXBException,
			FileNotFoundException {
		assertEquals(3, DBUtils.getAllMessages().size());
		MessageType message1 = DBUtils.getAllMessages().get(0);
		MessageType message2 = DBUtils.getAllMessages().get(2);
		DBUtils.removeMessagesMarkedForDeletion(Arrays.asList(message1,
				message2));
		assertEquals(1, DBUtils.getAllMessages().size());
	}

}
