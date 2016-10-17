package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import algorithms.demo.Maze3dSearchableAdapter;
import algorithms.mazeGenerators.GrowingTreeGenerator;
import algorithms.mazeGenerators.Maze3d;
import algorithms.mazeGenerators.Position;
import algorithms.search.BFS;
import algorithms.search.DFS;
import algorithms.search.Solution;

public class MazeClientHandler extends Observable implements ClientHandler, Observer {

	/** The server */
	MazeServer server;

	volatile ConcurrentHashMap<String, Socket> activeConnections = new ConcurrentHashMap<String, Socket>();

	/** The messages */
	volatile ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();

	/** The remote */
	UDPMazeServerRemoteControl remote;

	/**
	 * Instantiates a new maze client handler
	 * 
	 * @param server
	 */
	public MazeClientHandler(MazeServer server) {
		this.server = server;
	}

	/**
	 * Instantiates a new maze client handler
	 * 
	 * @param remote
	 */
	public MazeClientHandler(UDPMazeServerRemoteControl remote) {
		this.remote = remote;
	}

	/**
	 * The handleClient Method it notifies what it's doing to the client
	 * throughout it's operation. it gets the command from the client,
	 * additional arguments and parameters. does as requested and sends.
	 * 
	 * @param client
	 */

	@Override
	public void handleClient(Socket client) {
		String clientIP = "127.0.0.1";
		int clientPort = 8090;
		activeConnections.put(clientIP + "," + clientPort, client);
		String message = new String(clientIP + "," + clientPort + ",connected");
		messages.add(message);
		setChanged();
		notifyObservers();
		messages.remove(message);

		try {
			String data;
			String[] params;
			BufferedReader readerFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String command = readerFromClient.readLine();
			ObjectOutputStream outputCompressedToClient = new ObjectOutputStream(client.getOutputStream());
			outputCompressedToClient.flush();

			switch (command) {

			case "generate maze":

				String generator = readerFromClient.readLine();
				data = readerFromClient.readLine();
				params = parseGenerateMazeArgument(data);
				message = clientIP + "," + clientPort + ",generating maze";
				System.out.println(message);
				messages.add(message);
				setChanged();
				notifyObservers();
				outputCompressedToClient
						.writeObject(generateMaze(params[0], params[1], params[2], params[3], generator));
				outputCompressedToClient.flush();
				setChanged();
				notifyObservers();
				messages.remove(message);

				break;

			case "solve maze":

				String solverProperties = readerFromClient.readLine();
				data = readerFromClient.readLine();
				message = clientIP + "," + clientPort + ",solving maze";
				messages.add(message);
				setChanged();
				notifyObservers();
				outputCompressedToClient.writeObject(solveMaze(data, solverProperties));
				outputCompressedToClient.flush();
				setChanged();
				notifyObservers();
				messages.remove(message);
				break;

			default:
				message = clientIP + "," + clientPort + "," + "Invalid command";
				messages.add(message);
				setChanged();
				notifyObservers();
				outputCompressedToClient.writeObject(null);
				outputCompressedToClient.flush();

			}

		} catch (Exception e1) {

		}

		activeConnections.remove(clientIP + "," + clientPort);
		String last = new String(clientIP + "," + clientPort + ",disconnected");
		System.out.println();
		messages.add(last);
		setChanged();
		notifyObservers();
		messages.remove(last);

	}

	private String[] ParseCroosMaze(String data) {

		return data.split(" ");
	}

	/**
	 * Gets the messages
	 * 
	 * @return the messages
	 */
	public ConcurrentLinkedQueue<String> getMessages() {
		return messages;
	}

	private String[] parseGenerateMazeArgument(Object arg) {

		String[] params = (((String) arg).split(","));
		return params;
	}

	/**
	 * Generate maze.
	 *
	 * @param name
	 *            the name
	 * @param x
	 *            floor
	 * @param y
	 *            rows
	 * @param z
	 *            cols
	 * @param generator
	 *            the generator
	 * @return the maze3d
	 */
	public Maze3d generateMaze(String name, String x, String y, String z, String gene) {
		try {

			if (server.nameToMaze.containsKey(name))
				return server.nameToMaze.get(name);

			Maze3d maze3d = null;

			GrowingTreeGenerator generator = new GrowingTreeGenerator();
			maze3d = generator.generate(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));

			server.nameToMaze.put(name, maze3d);
			return maze3d;

		} catch (NumberFormatException | NullPointerException n) {

		}
		return null;

	}

	/**
	 * Solve maze
	 * 
	 * @param mazeName
	 *            the maze name
	 * @param solver
	 *            the solver
	 * @return the solution
	 */
	public Solution<Position> solveMaze(String mazeName, String solver) {

		Maze3d m = server.nameToMaze.get(mazeName);

		if (m == null) {
			return null;
		}

		Solution<Position> solution = null;

		Maze3dSearchableAdapter maze3d = new Maze3dSearchableAdapter(m);

		switch (solver) {
		case "bfs":

			solution = new BFS<Position>().search(maze3d);

			break;
		case "dfs":

			solution = new DFS<Position>().search(maze3d);

			break;

		default:
			break;
		}
		server.mazeToSolution.put(m, solution);
		return solution;

	}

	/**
	 * Gets the server
	 * 
	 * @return the server
	 */
	public MazeServer getServer() {
		return server;
	}

	public void setServer(MazeServer server) {
		this.server = server;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == remote)
			if (arg.toString().contains("disconnect")) {
				Socket clientToDisconnect = activeConnections
						.get(arg.toString().substring(0, arg.toString().length() - "disconnect".length() - 1));

				try {
					clientToDisconnect.close();
				} catch (Exception e) {

				}

			}
	}

}
