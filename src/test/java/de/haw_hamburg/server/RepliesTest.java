package de.haw_hamburg.server;

import static org.junit.Assert.*;

import org.junit.Test;

public class RepliesTest {

	@Test
	public void testReplyFromString() {
		assertEquals(Replies.ok(), Replies.replyFromString(OkReply.okString()));

		String message = "Somemessage";
		assertEquals(Replies.ok(message),
				Replies.replyFromString(OkReply.okString() + message));

		assertEquals(Replies.error(),
				Replies.replyFromString(ErrorReply.errorString()));

		assertEquals(Replies.error(message),
				Replies.replyFromString(ErrorReply.errorString() + message));
	}

}
