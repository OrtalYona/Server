package boot;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import server.MazeClientHandler;
import server.MazeServer;
import server.MyTcpIpServer;
import server.ServerProperties;
import server.UDPMazeServerRemoteControl;

public class RunServer {

	public static void main(String[] args) throws IOException {

		Display display = new Display();
		Shell shell = new Shell(display);
		
		MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.Activate);
		messageBox.setText("Maze Generations");
		messageBox.setMessage("Server is Operating");
		messageBox.open();
		
		ServerProperties properties = new ServerProperties();
		MazeServer server = new MazeServer(properties, null);
		MazeClientHandler clientHandler = new MazeClientHandler(server);
		server.setClientHandler(clientHandler);
		server.startServer();
	}

}
