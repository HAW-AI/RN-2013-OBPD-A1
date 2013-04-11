package de.haw_hamburg.client;

import static de.haw_hamburg.client.Requests.*;
import de.haw_hamburg.db.AccountType;

public class Pop3Client extends Thread {
	
	private AccountType account;
	
	private Pop3Client(AccountType account){
		this.account=account;
	}
	
	public Pop3Client create(AccountType account){
		return new Pop3Client(account);
	}
	
	private enum State {
		AUTHORIZATION, TRANSACTION, UPDATE, IDLE
	}

	// Begin in IDLE state
	private State state = State.IDLE;
	
	private void login(){
		
	}
	
	public void run(){
		
		login();
	}
	
}
