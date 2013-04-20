package de.haw_hamburg.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.bind.JAXBException;

public class Pop3ServerLauncher extends Thread {

	private final int port;

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
					Pop3Server.create(socket);
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
