

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

@ApplicationScoped
public class LobbyHandler
{
	private final Map<Integer, Lobby> lobbies = new HashMap<>();
	private JsonObject latestUpdateMessage;
	
	public LobbyHandler() {
		setUpdateMessage();
	}
	
	public int newLobby(User user, JsonObject message)
	{
		String lobbyName = message.getString("name");
		Lobby newLobby = new Lobby(lobbyName, user, 2);		// TODO game-type
		
		int lobbyID = newLobby.getID();
		lobbies.put(lobbyID, newLobby);
		
		setUpdateMessage();
		
		return lobbyID;
	}
	
	public boolean joinLobby(User user, JsonObject message)
	{
		int lobbyID = message.getInt("lobby-id");
		
		if (!lobbies.containsKey(lobbyID))
		{
			sendInactiveMessage(user);
			return false;
		}
		
		Lobby lobby = lobbies.get(lobbyID);
		
		if (lobby.isFull())
		{
			sendFullMessage(user);
			return false;
		}
		
		lobby.addPlayer(user);
		setUpdateMessage();				// update to reflect the change in number of players in the lobby
		return true;
	}
	
	public void sendUpdateMessage(User user) {
		System.out.println("Sending update now");
		user.send(latestUpdateMessage);
	}
	
	private void setUpdateMessage()
	{
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		lobbies.forEach((k, lobby) -> {
			arrayBuilder.add(Json.createObjectBuilder()
					.add("name", lobby.getName())
					.add("owner", lobby.getOwnerName())
					.add("gameType", lobby.getGameType())
					.add("noPlayers", lobby.getNoPlayers())
					.add("maxNoPlayers", lobby.getMaxNoPlayers())
					);
		});
		JsonArray lobbyArray = arrayBuilder.build();
		
		latestUpdateMessage = Json.createObjectBuilder()
								.add("action", "browser")
								.add("status", "update")
								.add("lobbies", lobbyArray)
								.build();
	}
	
	private void sendInactiveMessage(User user)
	{
		JsonProvider provider = JsonProvider.provider();
		JsonObject failureMessage = provider.createObjectBuilder()
											.add("action", "lobby")
											.add("status", "inactive")
											.build();
		user.send(failureMessage);
	}
	
	private void sendFullMessage(User user)
	{
		JsonProvider provider = JsonProvider.provider();
		JsonObject failureMessage = provider.createObjectBuilder()
											.add("action", "lobby")
											.add("status", "full")
											.build();
		user.send(failureMessage);
	}
}
