package de.haw_hamburg.client;

public class StatRequest extends AbstractRequest {

	@Override
	protected String name() {
		return Requests.STAT;
	}

	@Override
	public boolean isStat() {
		return true;
	}
}
