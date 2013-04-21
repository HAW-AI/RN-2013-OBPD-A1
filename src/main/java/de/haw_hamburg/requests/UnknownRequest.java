package de.haw_hamburg.requests;

public class UnknownRequest extends AbstractRequestWithParam {

	public UnknownRequest(String param) {
		super(param);
	}

	@Override
	protected String name() {
		return "unknown request: "+param();
	}
	
	@Override
	public boolean isUnknown() {
		return true;
	}
}
