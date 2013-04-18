package de.haw_hamburg.client;

abstract class AbstractRequest implements Request {

	public boolean isList() {
		return false;
	}

	public boolean isUidl() {
		return false;
	}

	public boolean isNoop() {
		return false;
	}

	public boolean isPass() {
		return false;
	}

	public boolean isUser() {
		return false;
	}

	public boolean isUnknown() {
		return false;
	}

	public boolean isStat() {
		return false;
	}

	public boolean isRetrieve() {
		return false;
	}

	public boolean isDelete() {
		return false;
	}

	public boolean isReset() {
		return false;
	}

	public boolean isQuit() {
		return false;
	}

	public boolean hasParam() {
		return false;
	}

	protected abstract String name();

	public String param() {
		return null;
	}

	@Override
	public String toString() {
		return name() + (hasParam() ? " " + param() : "");
	}

}
