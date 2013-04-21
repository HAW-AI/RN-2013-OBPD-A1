package de.haw_hamburg.requests;

public class SimpleListRequest extends AbstractRequest {

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.LIST;
	}

}
