package se.coada.id2212.hw1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import se.coada.id2212.hw1.server.Hangman.GameState;


public class ConnectionHandler implements Runnable
{
	private Socket clientSocket;
	private ObjectOutputStream out;
	private BufferedReader in;
	
	public ConnectionHandler(Socket clientSocket)
	{
		this.clientSocket = clientSocket;
	}
	
	// class & package
	boolean close()
	{
		try {
			in.close();
			out.close();
			clientSocket.close();
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void run()
	{	
		try
		{
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String request;
			
			Hangman game = new Hangman();
			GameState gs;
			
			while (true)										// Wait for player action
			{
				request = in.readLine();						// read the player request
				if (request == null) break;						// EOS; let thread finish run() to terminate

				System.out.println("Server got " + request);
				switch(request.length())
				{
				case 0: 	gs = game.newGame();				// request is empty: start new game
							System.out.println("Handler got msg of len 0; starting new game");
							break;
				case 1: 	gs = game.guess(request.charAt(0));	// request is 1 char: make guess
							System.out.println("Handler got msg of len 1; guessing char");
							break;
				default:	gs = game.guess(request);			// request is full word: make guess
							System.out.println("Handler got msg of len >1; guessing word");
							break;
				}
				System.out.println("Handler will now send back answer");
				out.writeObject(gs);							// return back to player the GameState
				System.out.println("Handler will now flush");
				out.flush();
				System.out.println("Handler is now done");
			}
			close();
		}
		catch (IOException e)
		{
			System.out.println("ConnectionHandler failed using streams");
			e.printStackTrace();
		}
	}
}