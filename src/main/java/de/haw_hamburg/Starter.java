package de.haw_hamburg;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
	 */
	public static void main(String[] args) {

		initializeLogging();
		Logger LOG = Logger.getLogger(Starter.class.getName());
		LOG.info("System started");

	}

}
