package de.haw_hamburg.client;

abstract class AbstractRequestWithParam extends AbstractRequest {

    private String param;
    
    public AbstractRequestWithParam(String param){
        this.param=param;
    }
    
    @Override
    public String param(){
        return param;
    }
    
    @Override
    public boolean hasParam(){
        return true;
    }

}
