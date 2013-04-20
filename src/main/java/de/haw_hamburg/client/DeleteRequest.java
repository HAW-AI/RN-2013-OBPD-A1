package de.haw_hamburg.client;

public class DeleteRequest extends AbstractRequestWithParam {

	public DeleteRequest(String param) {
		super(param);
	}

	@Override
	public boolean isDelete() {
		return true;
	}

	@Override
	protected String name() {

		return Requests.DELE;
	}

}
