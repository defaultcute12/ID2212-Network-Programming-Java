

import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/play")
public class WebsocketServer
{
	@Inject
	private LobbyHandler lobbyHandler;
	
	@Inject
	private UserHandler userHandler;
	
	@OnOpen
	public void open(Session session) { }
	
	@OnClose
	public void close(Session session) { }
	
	@OnMessage
	public void handleMessage(String message, Session session)
	{
		try (JsonReader reader = Json.createReader(new StringReader(message)))
		{
			JsonObject jsonMessage = reader.readObject();
			
			switch (jsonMessage.getString("action"))
			{
			case "new-user":
				System.out.println("New User Action");
				userHandler.newUser(jsonMessage);
				break;
			case "login":
				System.out.println("Login Action");
				User loginUser = userHandler.login(jsonMessage, session);
				if (loginUser != null) System.out.println("User " + loginUser.getUsername() + " signed in");
				break;
			case "create-lobby":
				System.out.println("New Lobby Action");
				User lobbyCreator = userHandler.getUser(session);
				if (lobbyCreator != null)
				{
					int lobbyID = lobbyHandler.newLobby(lobbyCreator, jsonMessage);
					System.out.println("User " + lobbyCreator.getUsername() + " created lobby with ID " + lobbyID);
				}
				break;
			case "browser":
				System.out.println("Browser Action");
				User browserUser = userHandler.getUser(session);
				if (browserUser != null)
				{
					System.out.println("User " + browserUser.getUsername() + " requested a browser update");
					lobbyHandler.sendUpdateMessage(browserUser);
				}
				break;
			case "join-lobby":
				System.out.println("Lobby Action");
				User joiningUser = userHandler.getUser(session);
				if (joiningUser != null) lobbyHandler.joinLobby(joiningUser, jsonMessage);
				break;
			default:
				System.err.println("Got unknown message type");
			}
		}
	}
}
