package server;

import java.io.Serializable;

public class ServerProperties implements Serializable {

	/** The Constant serialVersionUID */
	private static final long serialVersionUID = 1L;

	/** The port */
	private int port;

	/** The number of clients */
	private int numOfClients;

	/**
	 * Default Properties
	 * 
	 */
	public ServerProperties() {
		port = 8090;
		numOfClients = 32;
	}

	/**
	 * Instantiates a new server properties
	 * 
	 * @param port
	 * @param numOfClients
	 */
	public ServerProperties(int port, int numOfClients) {
		this.port = port;
		this.numOfClients = numOfClients;
	}

	/**
	 * Gets the port
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port
	 * 
	 * @param port
	 *            the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the number of clients
	 * 
	 * @return num
	 */
	public int getNumOfClients() {
		return numOfClients;
	}

	/**
	 * Sets the number of clients
	 * 
	 * @param numOfClients
	 */
	public void setNumOfClients(int numOfClients) {
		this.numOfClients = numOfClients;
	}
}
