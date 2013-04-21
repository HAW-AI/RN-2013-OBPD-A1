package de.haw_hamburg.requests;

public class SimpleUidlRequest extends AbstractRequest {

	@Override
	public boolean isUidl() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.UIDL;
	}

}
