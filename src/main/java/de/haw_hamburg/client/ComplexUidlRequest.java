package de.haw_hamburg.client;

public class ComplexUidlRequest extends AbstractRequestWithParam {

    public ComplexUidlRequest(String param) {
        super(param);
    }
    
    @Override
    public boolean isUidl() {
        return true;
    }

    @Override
    protected String name() {
        return Requests.UIDL;
    }

}
