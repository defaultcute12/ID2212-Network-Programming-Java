
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Connects to a server, sends a message and displays the answer.
 */
public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception 
    {
    	// ip & port
    	ClientConnectionHandler cch = new ClientConnectionHandler();
    	
    	FlowPane rootNode = new FlowPane(10, 10);
    	
    	Label scoreLabel = new Label();
    	Label wordLabel = new Label();
    	Label remainingLabel = new Label();
    	TextField guessField = new TextField();
    	Button sendButton = new Button();
    	Button newGameButton = new Button();
    	
    	sendButton.setText("Guess");
    	sendButton.setDisable(true);
    	guessField.setDisable(true);
    	newGameButton.setText("New Game");
    	
    	sendButton.setOnAction(new EventHandler<ActionEvent>() {		// Send button pressed
    		@Override
    		public void handle(ActionEvent e)
    		{
    			if (guessField.getText().length() > 0)
    			{
    	    		newGameButton.setDisable(true);
	    			sendButton.setDisable(true);
	    			guessField.setDisable(true);
	    			cch.send(guessField.getText());
    			}
    		}
    	});
    	
    	newGameButton.setOnAction(new EventHandler<ActionEvent>()		// New Game button pressed
    	{
    		@Override
    		public void handle(ActionEvent e)
    		{
	    		newGameButton.setDisable(true);
				sendButton.setDisable(true);
				guessField.setDisable(true);
	    		cch.send("");
    		}
    	});
	    	
	    	// TODO: event to capture the answer from the server
	    	// ... cch.receive();
	    	
		    // update JavaFX
	    	wordLabel.setText(gs.obfuscatedWord);
	    	remainingLabel.setText(gs.remainingGuesses);
	    	scoreLabel.setText(gs.score);
    		sendButton.setDisable(false);
			guessField.setDisable(false);
			newGameButton.setDisable(false);
    	
    	// TODO: make a nice layout for the client
    	
    	// Add all objects
    	rootNode.getChildren().add(wordLabel);
    	rootNode.getChildren().add(scoreLabel);
    	rootNode.getChildren().add(remainingLabel);
    	rootNode.getChildren().add(guessField);
    	rootNode.getChildren().add(sendButton);
    	
    	Scene scene = new Scene(rootNode, 200, 50);
    	
    	// TODO: specify where on the screen the stage will appear
    	
    	primaryStage.setTitle("Hangman Client");
    	wordLabel.setText("Loading game...");
    	primaryStage.setScene(scene);
    	primaryStage.show();
    }

    /**
     * @param args There are no command line parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }

}