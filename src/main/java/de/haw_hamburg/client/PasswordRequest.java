package de.haw_hamburg.client;

public class PasswordRequest extends AbstractRequestWithParam {

    
    public PasswordRequest(String param) {
        super(param);
    }

    @Override
    protected String name() {
        return Requests.PASS;
    }
    
    @Override
    public boolean isPass(){
        return true;
    }

}
