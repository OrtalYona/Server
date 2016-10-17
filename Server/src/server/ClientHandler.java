package server;

import java.net.Socket;
/**
 * interface ClientHandler
 */
public interface ClientHandler {

	void handleClient(Socket client);
}