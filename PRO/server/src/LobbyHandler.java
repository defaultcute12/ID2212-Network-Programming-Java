

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
	private final Map<Integer, Lobby> listedLobbies = new HashMap<>();
	private final Map<Integer, Lobby> runningLobbies = new HashMap<>();
	private JsonObject latestUpdateMessage;
	
	public LobbyHandler() {
		setUpdateMessage();
	}
	
	public int newLobby(User user, JsonObject message)
	{
		if (user.getLobby() != null)						// user already tied to a lobby
		{
			sendProblemMessage(user, message.getString("action"), "occupied");
			return -1;
		}
		
		String lobbyName = message.getString("name");
		Lobby newLobby = new Lobby(lobbyName, user, 2);		// TODO game-type
		user.setLobby(newLobby);							// tie lobby to user
		listedLobbies.put(newLobby.getID(), newLobby);		// store lobby
		
		setUpdateMessage();									// introduction of new lobby
		return newLobby.getID();
	}
	
	public boolean joinLobby(User user, JsonObject message)
	{
		/* TODO if user tied to lobby. Commented to allow same user to join same lobby multiple times (for testing)
		if (user.getLobby() != null)
		{
			sendNonuniqueMessage(user);
			return false;
		}
		*/
		
		int lobbyID = message.getInt("id");
		
		if (!listedLobbies.containsKey(lobbyID))
		{
			sendProblemMessage(user, message.getString("action"), "inactive");
			return false;
		}
		
		Lobby lobby = listedLobbies.get(lobbyID);
		
		if (lobby.isFull())
		{
			sendProblemMessage(user, message.getString("action"), "full");
			return false;
		}
		
		user.setLobby(lobby);				// tie lobby to user
		lobby.addPlayer(user);
		
		setUpdateMessage();					// update to reflect the change in number of players in the lobby
		return true;
	}
	
	public boolean leaveLobby(User user)
	{
		Lobby lobby = user.getLobby();
		if (lobby == null) return false;	// user not linked with any lobby
		if (!lobby.removePlayer(user))		// user linked with lobby, but not as a player. Should not happen?
		{
			System.err.println("User " + user.getUsername() + " leaving linked lobby not recognized as player");
			user.setLobby(null);
			return false;
		}
		
		user.setLobby(null);
		
		if (lobby.getNoPlayers() == 0) listedLobbies.remove(lobby.getID());
		
		setUpdateMessage();					// change in number of players in the lobby, or removal of lobby
		return true;
	}
	
	public boolean startGame(User user)
	{
		Lobby lobby = user.getLobby();
		if (lobby == null) 										return false;	// user not linked with any lobby		
		if (!lobby.getOwnerName().equals(user.getUsername())) 	return false;	// this user is not the owner
		if (!lobby.isStartable())								return false;	// not enough players
		
		runningLobbies.put(lobby.getID(), lobby);
		listedLobbies.remove(lobby.getID());									// remove listing of lobby
		setUpdateMessage();														// update to reflect removal of lobby
		
		lobby.startGame(user);
		return true;
	}
	
	public boolean move(User user, JsonObject message)
	{
		Lobby lobby = user.getLobby();
		if (lobby == null) return false;	// user not linked with any lobby
		
		return lobby.move(user, message);
	}
	
	public void sendUpdateMessage(User user) {
		user.send(latestUpdateMessage);
	}
	
	private void setUpdateMessage()
	{
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		listedLobbies.forEach((k, lobby) -> {
			arrayBuilder.add(Json.createObjectBuilder()
					.add("id", lobby.getID())
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
	
	private static void sendProblemMessage(User user, String action, String status)
	{
		JsonProvider provider = JsonProvider.provider();
		JsonObject message = provider.createObjectBuilder()
									.add("action", action)
									.add("status", status)
									.build();
		user.send(message);
	}
}
