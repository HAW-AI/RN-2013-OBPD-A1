package de.haw_hamburg.requests;

public class ComplexListRequest extends AbstractRequestWithParam {

	public ComplexListRequest(String param) {
		super(param);
	}

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	protected String name() {
		return Requests.LIST;
	}

}
