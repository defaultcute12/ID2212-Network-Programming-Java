
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;



public class SimpleConnectionHandler implements Runnable
{
	private Socket clientSocket;
	
	public SimpleConnectionHandler(Socket clientSocket)
	{
		this.clientSocket = clientSocket;
	}
	
	public void run()
	{
		BufferedInputStream in;
		BufferedOutputStream out;
		
		try
		{
			in = new BufferedInputStream(clientSocket.getInputStream());
			out = new BufferedOutputStream(clientSocket.getOutputStream());
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
			return;
		}
		
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			BufferedReader br = new InputStreamReader(clientSocket.getInputStream());
			
			Hangman game = new Hangman();
			
			String request;
			GameState gs;
			
			while (true)										// Wait for player action
			{
				request = br.readLine());						// read the player request
				
				switch(request.length())
				{
				case 0: 	gs = game.newGame();				// request is empty: start new game
							break;
				case 1: 	gs = game.guess(request.charAt(0));	// request is 1 char: make guess
							break;
				default:	gs = game.guess(request);			// request is full word: make guess
							break;
				}
				oos.writeObject(gs);							// return back to player the GameState
				oos.flush();
			}
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
		
		try
		{
			out.close();
			in.close();
			clientSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}