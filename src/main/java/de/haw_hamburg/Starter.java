package de.haw_hamburg;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.haw_hamburg.client.Pop3UpdateTask;
import de.haw_hamburg.db.AccountType;
import de.haw_hamburg.db.DBUtils;
import de.haw_hamburg.server.Pop3ServerLauncher;

public class Starter {

	public static void initializeLogging() {
		try {
			FileInputStream configFile = new FileInputStream(Starter.class
					.getClassLoader().getResource("logging.properties")
					.getPath());
			LogManager.getLogManager().readConfiguration(configFile);
		} catch (IOException ex) {
			System.out.println("WARNING: Could not open configuration file");
			System.out
					.println("WARNING: Logging not configured (console output only)");
		}
	}

	/**
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {

		initializeLogging();
		Logger LOG = Logger.getLogger(Starter.class.getName());
		LOG.info("System started");

		// Start client tasks
		Timer timer=new Timer();
		List<AccountType> accounts=DBUtils.getAccounts();
		for(AccountType account:accounts){
			timer.schedule(Pop3UpdateTask.create(account,false), 0, 30000);
		}
		
		Pop3ServerLauncher launcher = new Pop3ServerLauncher(11000);
		launcher.start();

	}

}
