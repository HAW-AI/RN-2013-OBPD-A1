package de.haw_hamburg.client;

public class NoopRequest extends AbstractRequest {

    @Override
    protected String name() {
        return Requests.NOOP;
    }

    @Override
    public boolean isNoop() {
        return true;
    }
}
