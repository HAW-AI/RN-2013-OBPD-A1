package de.haw_hamburg.client;

public class UserRequest extends AbstractRequestWithParam {

    public UserRequest(String param) {
        super(param);
    }

    @Override
    protected String name() {
        return Requests.USER;
    }
    
    @Override
    public boolean isUser(){
        return true;
    }

    

}
