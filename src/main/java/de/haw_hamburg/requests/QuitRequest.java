package de.haw_hamburg.requests;

public class QuitRequest extends AbstractRequest {

	@Override
	public boolean isQuit() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.QUIT;
	}

}
