package se.coada.id2212.hw1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
	private boolean listening;
	private ServerSocket serverSocket = null;
	public final int port;
	
	public Server(int port)
	{
		this.port = port;
	}

	public Server() {
		port = 4444;
	}

	@Override
	public void run()
	{		
		try
		{
			System.out.println("Server will now bind to port " + port);
			serverSocket = new ServerSocket(port);
			listening = true;
		}
		catch (IOException e)
		{
			System.err.println("Server could not listen on port " + port);
			System.exit(1);
		}
				
		while (listening)
		{
			Socket clientSocket;
			
			try
			{
				clientSocket = serverSocket.accept();						// wait for client to connect
				System.out.println("Server got connecting client");
				(new Thread(new ConnectionHandler(clientSocket))).start();
				System.out.println("Server finished spawning");
			}
			catch (IOException e)
			{
				System.err.println("Server could not accept a new connection");
			}
		}
	}
	
	public boolean stop()
	{
		try
		{
			serverSocket.close();
			listening = false;
			return true;
		}
		catch (IOException e)
		{
			System.err.println("Server failed to stop");
			return false;
		}
	}

	public static void main(String[] args)
	{
		if (args.length == 1) new Server(Integer.parseInt(args[0])).run();
		else new Server().run();

	}
}
