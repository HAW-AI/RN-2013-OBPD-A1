package de.haw_hamburg.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

import org.omg.CORBA.Request;

public abstract class Pop3Component extends Thread {
    
    protected PrintWriter out;
    protected BufferedReader in;
    protected Pop3State state=Pop3State.IDLE;
    
    public Pop3State getPop3State(){
        return state;
    }
    
    protected void ensureCorrectState(Pop3State... expectedState) {
        if(!new HashSet<Pop3State>(Arrays.asList(expectedState)).contains(state))
            throw new IllegalStateException("Expected "
                    + Arrays.asList(expectedState) + ". was" + state.toString());
    }
    
    protected String readLine() throws IOException {
        return in.readLine();
    }

    protected void println(String line) throws IOException {
        out.println(line);
    }
    
    protected void println(Request request) throws IOException {
        out.println(request.toString());
    }
}
