package de.haw_hamburg.requests;

public class ResetRequest extends AbstractRequest {

	@Override
	public boolean isReset() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.RSET;
	}

}
