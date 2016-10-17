package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPMazeServerRemoteControl extends Observable implements Observer, Runnable {

	/** The handler. */
	MazeClientHandler handler;

	/** The sender ip. */
	InetAddress senderIP;

	/** The sender port. */
	int senderPort;

	/** The server socket. */
	DatagramSocket serverSocket;

	/** The clients server. */
	MazeServer clientsServer;

	/** The properties. */
	ServerProperties properties;

	/** The executor. */
	ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Instantiates a new UDP maze server remote control.
	 * 
	 * @param properties
	 */
	public UDPMazeServerRemoteControl(ServerProperties properties) {
		this.properties = properties;
	}

	@Override
	public void run() {
		try {
			serverSocket = new DatagramSocket(properties.getPort());
		} catch (SocketException e) {

			e.printStackTrace();
		}

		waitForStartSignal();

		initiateClientsServer();

		handleClientsServer();

	}

	/** Wait for start signal */
	private void waitForStartSignal() {

		byte[] receiveData = new byte[1024];
		String input = null;
		if (clientsServer == null) {
			do {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
					input = new String(receivePacket.getData());
					senderIP = receivePacket.getAddress();
					senderPort = receivePacket.getPort();
					System.out.println("Remote client IP: " + senderIP.getHostAddress() + ", " + "client port "
							+ senderPort + " initiated server");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} while (!input.startsWith("start server"));
		}
	}

	/**
	 * Initiate clients server
	 */
	private void initiateClientsServer() {
		byte[] receiveData = new byte[1024];
		String buffer = null, input = "";
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try {
			serverSocket.receive(receivePacket);
			buffer = new String(receivePacket.getData());

			for (int i = 0; i < buffer.length(); i++)
				if (Character.isDigit(buffer.charAt(i)) || buffer.charAt(i) == ',')
					input += buffer.charAt(i);
				else
					break;

			String message = "Numebr of clients: " + input.split(",")[0] + " port to server clients "
					+ input.split(",")[1] + "\n";
			System.out.println(message);

			DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), senderIP, senderPort);
			try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			ServerProperties clientsServerProperties = new ServerProperties(Integer.parseInt(input.split(",")[1]),
					Integer.parseInt(input.split(",")[0]));
			handler = new MazeClientHandler(this);
			handler.addObserver(this);
			this.addObserver(handler);
			clientsServer = new MazeServer(clientsServerProperties, handler);
			handler.setServer(clientsServer);
			clientsServer.startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Handle clients server
	 */
	private void handleClientsServer() {
		byte[] receiveData = new byte[1024];
		String input = null;
		do {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				serverSocket.receive(receivePacket);
				input = new String(receivePacket.getData());
				senderIP = receivePacket.getAddress();
				senderPort = receivePacket.getPort();
				if (input.startsWith("exit"))
					return;
				else if (input.contains("disconnect") && !input.startsWith("stop")) {
					String buffer = input;
					input = "";
					for (int i = 0; i < buffer.length(); i++)
						if (Character.isDigit(buffer.charAt(i)) || buffer.charAt(i) == ',' || buffer.charAt(i) == '.'
								|| Character.isLetter(buffer.charAt(i)))
							input += buffer.charAt(i);
						else
							break;
					setChanged();
					notifyObservers(input);
				} else if (input.contains("stop")) {
					String message = "Server stoped" + "\n";
					DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), senderIP,
							senderPort);
					try {
						serverSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}

					executor.shutdownNow();
					clientsServer.stoppedServer();
					clientsServer = null;
					System.out.println("Server shutdwon");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} while (!input.startsWith("stop server"));
		clientsServer.stoppedServer();
		clientsServer = null;
		executor.shutdownNow();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ClientHandler) {
			String allMessages = "";
			for (String message : handler.messages) {
				allMessages += message + "\n";
			}
			DatagramPacket sendPacket = new DatagramPacket(allMessages.getBytes(), allMessages.length(), senderIP,
					senderPort);
			try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
