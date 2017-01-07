

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Lobby
{
	private static int idFactory = 0;
	private final int ID;
	
	private Game game;
	
	private final String name;
	private User owner;
	private final User[] players;
	private int noPlayers = 0;
	
	
	public Lobby(String name, User owner, int gameType)
	{
		ID = idFactory++;
		this.name = name;
		this.owner = owner;
		
		newGame(owner, gameType);
		
		players = new User[game.getMaxNoPlayers()];
		addPlayer(owner);
	}
	
	public boolean isStartable()
	{
		if (game == null)		return false;
		if (game.isStarted())	return false;
		if (game.mustBeMaxNoPlayers() && players.length != game.getMaxNoPlayers()) return false;
		return true;
	}
	
	public boolean newGame(User player, int gameType)
	{
		if (!owner.equals(player)) return false;		// player lacks permission
		
		switch (gameType)
		{
		case Game.TICTACTOE:
			game = new TicTacToe();
			return true;
		default:
			System.err.println("Lobby failed to create game; unknown game type");
			game = null;
			return false;
		}
	}
	
	public boolean startGame(User player)
	{
		if (!owner.equals(player))	return false;			// player lacks permission
		if (!isStartable())			return false;
		
		JsonObject gameChange = game.setPlayers(players);	// initiate all the players
		
		if (gameChange == null) return false;
		informPlayers(gameChange);
		return true;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOwnerName()
	{
		if (owner == null) return "";
		return owner.getUsername();
	}
	
	public int getNoPlayers() {
		return noPlayers;
	}
	
	public int getGameType() {
		return game.getType();
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
		informPlayers(null);
		
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
				if (i == 0) owner = players[i+1];			// next player (if any) promoted to owner
			}
		}
		
		if (!shift) return false;							// found no player to be removed
		
		noPlayers--;
		JsonObject gameChange = game.removePlayer(player);	// remove player from game
		
		if (gameChange == null) return false;
		informPlayers(gameChange);
		return true;
	}
	
	
	public boolean move(User player, JsonObject message)
	{
		if (game == null) return false;						// assert game exists
		
		JsonObject gameChange = game.apply(player, message);
		
		if (gameChange == null) return false;
		informPlayers(gameChange);
		return true;
	}
	
	private void informPlayers(JsonObject gameMessage)
	{
		// Players
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (int i = 0; i < noPlayers; i++)
		{
			arrayBuilder.add(Json.createObjectBuilder()
						.add("ID", players[i].getID())
						.add("username", players[i].getUsername())
						.add("XP", players[i].getEXP()));
		}
		JsonArray playerArray = arrayBuilder.build();
		
		// Message
		JsonObjectBuilder messageBuilder = Json.createObjectBuilder()
												.add("action", "lobby")
												.add("status", "update")
												.add("name", name)
												.add("players", playerArray)
												.add("max", players.length);
		if (gameMessage != null) messageBuilder.add("game", gameMessage);
		
		JsonObject message = messageBuilder.build();
				
		for (int i = 0; i < noPlayers; i++) players[i].send(message);
	}
}
