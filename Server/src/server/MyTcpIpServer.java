package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * class MyTcpIpServer
 *
 */
public class MyTcpIpServer {

	ServerProperties serverProperties;
	ClientHandler clientHandler;
	private volatile boolean stopped;

	public MyTcpIpServer(ServerProperties serverProperties, ClientHandler clientHandler) {
		this.serverProperties = serverProperties;
		stopped = false;
		this.clientHandler = clientHandler;
	}

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public void startServer() {
		ServerSocket server;
		try {
			server = new ServerSocket(serverProperties.getPort());
			System.out.println("Server is now listeing on port " + serverProperties.getPort());
			server.setSoTimeout(500);
			while (!stopped) {
				try {
					final Socket someClient = server.accept();
					System.out.println("New client" + " port: " + someClient.getPort() + " IP: "
							+ someClient.getInetAddress().getHostAddress());
					try {
						InputStream inputFromClient = someClient.getInputStream();
						OutputStream outputToClient = someClient.getOutputStream();
						clientHandler.handleClient(someClient);
						inputFromClient.close();
						outputToClient.close();
						someClient.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				} catch (SocketTimeoutException e) {
				}
			}

			server.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/** Stopped server */
	public void stoppedServer() {
		stopped = true;
	}
}
