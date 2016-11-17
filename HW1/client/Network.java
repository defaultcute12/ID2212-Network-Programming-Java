package se.coada.id2212.hw1.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import se.coada.id2212.hw1.server.Hangman.GameState;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class Network
{
	private String host;
	private int port;
    private Socket clientSocket;
    private ObjectInputStream in;
    private OutputStreamWriter out;
	

    @FXML
    private Button newGameButton;
    @FXML
    private Button sendButton;
    @FXML
    private TextField sendField;
    @FXML
    private Text wordText;
    @FXML
    private Text guessedText;
    @FXML
    private Label scoreLabel;
    
    @FXML
    private void connectHandler(ActionEvent event)
    {
        new ConnectService().start();
    }
    
    @FXML
    private void newGameHandler(ActionEvent event)
    {
        new SendService().start();
    	guessedText.setText("");
    }
    
    @FXML
    private void sendHandler(ActionEvent event)
    {
        new SendService().start();

    }

	
    public Network()
    {
    	host = "localhost";
    	port = 4444;
        new ConnectService().start();
    }
    
	public Network(String host, int port)
	{
		this.host = host;
		this.port = port;
        new ConnectService().start();
	}
	
	public boolean close()
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
	
	private class ConnectService extends Service<Socket>
	{
		private ConnectService()
		{	
			setOnSucceeded((WorkerStateEvent event) -> {
				clientSocket = getValue();
				if (clientSocket == null)
				{
					guessedText.setText("Failed connecting.");
					return;
				}
				guessedText.setText("Connected.");
				newGameButton.setDisable(false);

				try {
					in = new ObjectInputStream(clientSocket.getInputStream());
		            out = new OutputStreamWriter(clientSocket.getOutputStream());
				} catch (Exception e) {
					System.err.println("Network failed creating streams");
				}
			});
		}
		
		@Override
		protected Task<Socket> createTask()
		{
			return new Task<Socket>() {
				@Override
				protected Socket call()
				{
					try {
						Socket sock = new Socket(host, port);				// This takes some time; blocking
						System.out.println("ConnectionService: task created socket to server");
						return sock;
					} catch (UnknownHostException e)
                    {
			            System.err.println("Don't know about host: " + host + ".");
			        } catch (IOException e) {
			            System.err.println("Couldn't get I/O for the connection to " + host + ".");
					}
					return null;
                }
            };
        }
    }
	
    private class SendService extends Service<GameState> {
        private SendService()
        {
        	setOnScheduled((WorkerStateEvent event) -> {
        		sendButton.setDisable(true);
        		wordText.setDisable(true);
        	});
        	
            setOnSucceeded((WorkerStateEvent event) -> {
            	GameState gs = getValue();
            	
            	// if guessed a character and it was wrong (one less guess)
            	if (sendField.getText().length() == 1 && !sendButton.getText().equals("Guess (" + gs.guesses + ")"))
            	{
            		guessedText.setText(guessedText.getText() + " " + sendField.getText().toUpperCase());
            	}
            	
            	scoreLabel.setText("Score: " + gs.score);
            	wordText.setText(gs.word);
            	sendButton.setText("Guess (" + gs.guesses + ")");
        		sendButton.setDisable(false);
        		sendField.setText("");
        		sendField.setDisable(false);
        		
        		if (gs.guesses < 1)			// Won (-1) or lost game (0)
        		{
                	sendButton.setText("Guess (-)");
                	sendButton.setDisable(true);
                	sendField.setDisable(true);
        		}
            });

            setOnFailed((WorkerStateEvent event) -> {
                System.err.println("Failed sending");
        		sendButton.setDisable(false);
            });
        }

        @Override
        protected Task<GameState> createTask() {
            return new Task<GameState>() {
                @Override
                protected GameState call() throws IOException
                {
                	System.out.println("Will now send " + sendField.getText());
                	out.write(sendField.getText() + "\n");
                	System.out.println("Will now flush");
                	out.flush();
                	System.out.println("Done sending, now waiting for GS");
                	try {
						GameState gs = (GameState)in.readObject();
						return gs;
					}
                	catch (ClassNotFoundException e)
                	{
                		System.err.println("Failed to read object from server");
                		return null;
					}
                }
            };
        }
    }
}
