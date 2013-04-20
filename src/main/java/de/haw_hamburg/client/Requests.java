package de.haw_hamburg.client;

public class Requests {

	public static String USER = "USER";
	public static String PASS = "PASS";
	public static String DELE = "DELE";
	public static String UIDL = "UIDL";
	public static String QUIT = "QUIT";
	public static String LIST = "LIST";
	public static String RSET = "RSET";
	public static String NOOP = "NOOP";
	public static String RETR = "RETR";
	public static String STAT = "STAT";

	private Requests() {
	}

	public static Request user(String user) {
		return new UserRequest(user);
	}

	public static Request pass(String pass) {
		return new PasswordRequest(pass);
	}

	public static Request quit() {
		return new QuitRequest();
	}

	public static Request stat() {
		return new StatRequest();
	}

	public static Request list() {
		return new SimpleListRequest();
	}

	public static Request list(int messageNumber) {
		return new ComplexListRequest(Integer.valueOf(messageNumber).toString());
	}

	public static String message(String message) {
		// TODO: Single point has to be dealt with
		return message;
	}

	public static Request retrieve(int messageNumber) {
		return new RetrieveRequest(Integer.valueOf(messageNumber).toString());
	}

	public static Request delete(int messageNumber) {
		return new DeleteRequest(Integer.valueOf(messageNumber).toString());
	}

	public static Request noop() {
		return new NoopRequest();
	}

	public static Request reset() {
		return new ResetRequest();
	}

	public static Request uniqueIdListing() {
		return new SimpleUidlRequest();
	}

	public static Request uniqueIdListing(int messageNumber) {
		return new ComplexUidlRequest(Integer.valueOf(messageNumber).toString());
	}

	public static Request fromRawRequest(String rawRequest) {
		String cleanedRequestString = rawRequest.trim();

		if (cleanedRequestString.startsWith(Requests.USER)) {
			return user(cleanedRequestString.split("\\s")[1]);
		} else if (cleanedRequestString.startsWith(Requests.PASS)) {
			return pass(cleanedRequestString.split("\\s")[1]);
		} else if (cleanedRequestString.startsWith(Requests.DELE)) {
			return delete(Integer
					.parseInt(cleanedRequestString.split("\\s")[1]));
		} else if (cleanedRequestString.startsWith(Requests.UIDL)) {
			if (cleanedRequestString.split("\\s").length > 1) {
				return uniqueIdListing(Integer.parseInt(cleanedRequestString
						.split("\\s")[1]));
			} else {
				return uniqueIdListing();
			}
		} else if (cleanedRequestString.startsWith(Requests.QUIT)) {
			return quit();
		} else if (cleanedRequestString.startsWith(Requests.LIST)) {
			if (cleanedRequestString.split("\\s").length > 1) {
				return list(Integer
						.parseInt(cleanedRequestString.split("\\s")[1]));
			} else {
				return list();
			}
		} else if (cleanedRequestString.startsWith(Requests.RSET)) {
			return reset();
		} else if (cleanedRequestString.startsWith(Requests.NOOP)) {
			return noop();
		} else if (cleanedRequestString.startsWith(Requests.RETR)) {
			return retrieve(Integer
					.parseInt(cleanedRequestString.split("\\s")[1]));
		} else if (cleanedRequestString.startsWith(Requests.STAT)) {
			return stat();
		} else {
			// FIXME log something
			return null;
		}
	}

}
