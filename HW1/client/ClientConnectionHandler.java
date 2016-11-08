
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientConnectionHandler
{
    Socket clientSocket;
    ObjectInputStream ois;
    PrintWriter pw;
	
    // TODO: handle discretly when the server is not there (try again later)
    // TODO: capture the answer with an event somehow
	
	public ClientConnectionHandler(String address, int portNo)
	{
        try
        {
            clientSocket = new Socket(address, portNo);
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host: " + address + ".");
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + address + ".");
        }

        try
        {
            ois = new ObjectInputStream(clientSocket.getInputStream());
            pw = new PrintWriter(clientSocket.getInputStream());
        }
        catch (IOException e)
        {
        	System.err.println("Failed to open streams to " + address + ".");
            System.err.println(e.toString());
        }
	}

	public void send(String msg)
	{
		// send guess
	    pw.write(msg);
	    pw.flush();
    }
	
	public GameState receive()
	{
		// wait for reply
	    Object o = ois.readObject();
	    return gs = (GameState)o;
	}
	
	public void disconnect()
	{
        out.close();
        in.close();
        clientSocket.close();
	}
}