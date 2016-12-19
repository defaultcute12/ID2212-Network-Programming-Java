

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class Lobby
{
	private static int idFactory = 0;
	private final int ID;
	
	private final String name;
	private final String ownerName;
	private final int gameType;
	private final User[] players;
	private int noPlayers = 0;
	
	public Lobby(String name, User owner, int gameType)
	{
		ID = idFactory++;
		this.name = name;
		this.ownerName = owner.getUsername();
		this.gameType = gameType;
		players = new User[gameType];
		
		addPlayer(owner);
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public int getNoPlayers() {
		return noPlayers;
	}
	
	public int getGameType() {
		return gameType;
	}
	
	public int getMaxNoPlayers() {
		return players.length;
	}
	
	public boolean isFull() {
		return noPlayers == players.length;
	}
	
	public boolean addPlayer(User player)
	{
		if (isFull()) return false;
		
		players[noPlayers++] = player;
		
		//informPlayers();
		
		return true;
	}
	
	private void informPlayers()
	{
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (User player : players)
		{
			arrayBuilder.add(Json.createObjectBuilder()
						.add("ID", player.getID())
						.add("username", player.getUsername())
						.add("XP", player.getXp()));
		}
		JsonArray playerArray = arrayBuilder.build();

		JsonObject message = Json.createObjectBuilder()
								.add("action", "lobby")
								.add("status", "update")
								.add("name", name)
								.add("type", gameType)
								.add("players", playerArray)
								.build();

		for (User user : players) user.send(message);
	}
}
