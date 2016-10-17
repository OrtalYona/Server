package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import algorithms.mazeGenerators.Maze3d;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import io.MyCompressorOutputStream;
import io.MyDecompressorInputStream;

public class MazeServer extends MyTcpIpServer implements Runnable {
	/** The name to maze. */
	public ConcurrentHashMap<String, Maze3d> nameToMaze;

	/** The name to file name. */
	public ConcurrentHashMap<String, String> nameToFileName;

	/** The name to solution. */
	public ConcurrentHashMap<String, Solution<Position>> nameToSolution;

	/** The maze to solution. */
	public ConcurrentHashMap<Maze3d, Solution<Position>> mazeToSolution;

	/** The my compressor. */
	MyCompressorOutputStream myCompressor;

	/** The my decompressor */
	MyDecompressorInputStream myDecompressor;

	/**
	 * Instantiates a new maze server
	 *
	 * @param serverProperties
	 *            the server properties
	 * @param clientHandler
	 *            the client handler
	 */
	public MazeServer(ServerProperties serverProperties, MazeClientHandler clientHandler) {
		super(serverProperties, clientHandler);
		this.nameToFileName = new ConcurrentHashMap<String, String>();
		this.nameToMaze = new ConcurrentHashMap<String, Maze3d>();
		this.mazeToSolution = new ConcurrentHashMap<Maze3d, Solution<Position>>();
		this.nameToSolution = new ConcurrentHashMap<String, Solution<Position>>();
		load();
		if (mazeToSolution == null) {
			mazeToSolution = new ConcurrentHashMap<Maze3d, Solution<Position>>();
		}
	}

	@Override
	public void stoppedServer() {

		try {
			FileOutputStream fos = new FileOutputStream("name_to_model.zip");
			GZIPOutputStream gzos = new GZIPOutputStream(fos);
			ObjectOutputStream out = new ObjectOutputStream(gzos);
			out.writeObject(mazeToSolution);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.getStackTrace();
		}
		super.stoppedServer();

	}

	/**
	 * Load
	 */
	@SuppressWarnings("unchecked")
	private void load() {
		try {
			FileInputStream fos = new FileInputStream("name_to_model.zip");
			GZIPInputStream gzos = new GZIPInputStream(fos);
			ObjectInputStream out = new ObjectInputStream(gzos);
			mazeToSolution = (ConcurrentHashMap<Maze3d, Solution<Position>>) out.readObject();
			out.close();
		} catch (IOException e) {
			e.getStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		startServer();
	}

}
