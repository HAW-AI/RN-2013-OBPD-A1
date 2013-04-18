package de.haw_hamburg.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.haw_hamburg.common.Pop3Component;
import de.haw_hamburg.common.Pop3State;

public class Pop3Server extends Pop3Component {
    
    
    private Pop3Server(BufferedReader in,PrintWriter out){
        this.in=in;
        this.out=out;
        this.state=Pop3State.CONNECTED;
    }
    
    public Pop3Server create(Socket socket) throws IOException{
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        return new Pop3Server(in,out);
    }
    
    public void run(){
        while(!this.isInterrupted()){
            try {
                String rawRequest=in.readLine();
                handleRequest(rawRequest);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }    
        }
    }
    
    public void handleRequest(String rawRequest){
        
    }

}
