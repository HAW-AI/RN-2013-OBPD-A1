package de.haw_hamburg.requests;

public interface Request {

	boolean isList();

	boolean isUidl();

	boolean isNoop();

	boolean isPass();

	boolean isUser();

	boolean isUnknown();

	boolean isStat();

	boolean isRetrieve();

	boolean isDelete();

	boolean isReset();

	boolean isQuit();

	boolean hasParam();

	String param();

}
