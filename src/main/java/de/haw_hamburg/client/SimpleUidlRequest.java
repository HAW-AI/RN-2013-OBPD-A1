package de.haw_hamburg.client;

public class SimpleUidlRequest extends AbstractRequest {

    @Override
    public boolean isUidl() {
        return true;
    }
    
    @Override
    protected String name() {
        return Requests.UIDL;
    }

}
