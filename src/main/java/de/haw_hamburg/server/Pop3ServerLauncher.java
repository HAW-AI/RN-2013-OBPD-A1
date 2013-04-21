package de.haw_hamburg.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

public class Pop3ServerLauncher extends Thread {

	private final int port;
	private Logger LOG=Logger.getLogger(Pop3ServerLauncher.class.getName());
	private int serverNumber=0;
	
	
	public Pop3ServerLauncher(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			while (!isInterrupted()) {
				try {
					Socket socket = serverSocket.accept();
					LOG.info("Incoming connection");
					Pop3Server.create(socket,serverNumber).start();
					serverNumber+=1;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
