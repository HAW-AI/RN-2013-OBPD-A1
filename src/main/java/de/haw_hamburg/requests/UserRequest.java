package de.haw_hamburg.requests;

public class UserRequest extends AbstractRequestWithParam {

	public UserRequest(String param) {
		super(param);
	}

	@Override
	protected String name() {
		return Requests.USER;
	}

	@Override
	public boolean isUser() {
		return true;
	}

	public static Request fromRawRequest(String rawRequest) {
		return new UserRequest(rawRequest.trim().split("\\s")[1]);
	}

}
