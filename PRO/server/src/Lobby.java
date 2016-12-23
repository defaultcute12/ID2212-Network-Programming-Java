

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
		
		System.out.println("Will now inform lobby players of new player " + player.getUsername());
		informPlayers();
		
		return true;
	}
	
	public boolean removePlayer(User player)
	{
		boolean shift = false;
		for (int i = 0; i < noPlayers; i++)
		{
			if (shift)
			{
				players[i-1] = players[i];
				players[i] = null;
			}
			else if (player.equals(players[i]))
			{
				shift = true;
				players[i] = null;
			}
		}
		
		if (shift)				// found player to be removed
		{
			noPlayers--;
			informPlayers();
			return true;
		}
		
		return false;
	}
	
	private void informPlayers()
	{
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (int i = 0; i < noPlayers; i++)
		{
			arrayBuilder.add(Json.createObjectBuilder()
						.add("ID", players[i].getID())
						.add("username", players[i].getUsername())
						.add("XP", players[i].getXp()));
		}
		JsonArray playerArray = arrayBuilder.build();

		JsonObject message = Json.createObjectBuilder()
								.add("action", "lobby")
								.add("status", "update")
								.add("name", name)
								.add("type", gameType)
								.add("max", players.length)
								.add("players", playerArray)
								.build();
		
		for (int i = 0; i < noPlayers; i++) players[i].send(message);
	}
}
