package de.haw_hamburg.requests;

public class RetrieveRequest extends AbstractRequestWithParam {

	public RetrieveRequest(String param) {
		super(param);
	}

	@Override
	public boolean isRetrieve() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.RETR;
	}

}
