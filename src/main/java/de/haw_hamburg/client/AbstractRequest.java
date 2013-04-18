package de.haw_hamburg.client;

abstract class AbstractRequest implements Request {

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isUidl() {
        return false;
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public boolean isPass() {
        return false;
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean isUnknown() {
        return false;
    }

    @Override
    public boolean isStat() {
        return false;
    }

    @Override
    public boolean isRetrieve() {
        return false;
    }

    @Override
    public boolean isDelete() {
        return false;
    }

    @Override
    public boolean isReset() {
        return false;
    }

    @Override
    public boolean isQuit() {
        return false;
    }
    @Override
    public boolean hasParam(){
        return false;
    }
    
    protected abstract String name();
    
    @Override
    public String param(){
        return null;
    }
    
    @Override
    public String toString(){
        return name() + (hasParam() ? " "+param() : "");
    }

}
