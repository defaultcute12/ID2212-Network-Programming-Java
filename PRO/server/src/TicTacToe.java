

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class TicTacToe implements Game
{
	public static final int POINTS = 10;
	public static final int NUM_PLAYERS = 2;
	public static final int EMPTY = 0;
	public static final int X = 1;
	public static final int O = 2;
	public static final int[] MARK = {X, O};
	
	private int[][] grid = new int[3][3];
	private User[] players;
	private boolean isOver = false;
	private User wonPlayer;
	private int playerTurn = 0;				// first player starts
	private int moveCounter = 0;			// counts the number of moves that have been made
	
	public TicTacToe()
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				grid[i][j] = EMPTY;
			}
		}
	}
	
	@Override
	public JsonObject setPlayers(User[] players)
	{
		if (isStarted()) return null;			// cannot set players if game already begun
		
		this.players = players;
		
		return getState();
	}
	
	@Override
	public JsonObject removePlayer(User player)
	{
		int playerIndex = getPlayerIndex(player);
		
		if (playerIndex == -1)	return null;					// user not part of game
		if (isOver)				return null;					// applyable
		
		isOver = true;											// if someone leaves the game is over
		int wonPlayerIndex = (playerIndex + 1) % NUM_PLAYERS;
		wonPlayer = players[wonPlayerIndex];					// non-leaver is winner
		wonPlayer.addEXP(POINTS);
		
		return getState();
	}
	
	@Override
	public int getType() {
		return Game.TICTACTOE;
	}
	
	@Override
	public int getMaxNoPlayers() {
		return NUM_PLAYERS;
	}
	
	@Override
	public boolean mustBeMaxNoPlayers() {
		return true;
	}
	
	@Override
	public JsonObject getState()
	{		
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (int i = 0; i < grid.length; i++)
		{
			JsonArrayBuilder arrayBuilder2 = Json.createArrayBuilder();
			
			for (int j = 0; j < grid.length; j++) arrayBuilder2.add(grid[i][j]);
			
			arrayBuilder.add(arrayBuilder2);
		}
		JsonArray grid = arrayBuilder.build();
		
		JsonObjectBuilder messageBuilder = Json.createObjectBuilder()
												.add("started", isStarted())
												.add("ended", isOver())
												.add("grid", grid)
												.add("turn", players[playerTurn].getUsername());
		if (wonPlayer != null)	messageBuilder.add("winner", wonPlayer.getUsername());
		
		return messageBuilder.build();
	}
	
	@Override
	public JsonObject apply(User player, JsonObject move)
	{		
		int toX = move.getInt("toX");
		int toY = move.getInt("toY");
		int playerIndex = getPlayerIndex(player);
		
		if (isOver)										return null;	// applyable
		if (playerIndex != playerTurn)					return null;	// correct player
		if (move.getInt("playerID") != player.getID()) 	return null;	// assert message has correct value
		if (grid[toX][toY] != EMPTY)					return null;	// correct move
		
		grid[toX][toY] = MARK[playerIndex];
		
		if (isWon(toX, toY))								// check if game is won based on impact of latest move
		{
			isOver = true;
			wonPlayer = player;
			wonPlayer.addEXP(POINTS);
		}
		else if (moveCounter++ == 9) isOver = true;			// tie
		else setPlayerTurn();
		
		return move;										// return same movement to be distributed
	}
	
	private int getPlayerIndex(User player)
	{
		for (int i = 0; i < players.length; i++)
		{
			if (players[i].equals(player)) return i;
		}
		return -1;
	}
	
	private boolean isWon(int x, int y)
	{
		int compareTo = grid[x][y];
		boolean won = true;
		
		for (int i = 0; i < 3; i++)			// horizontal
		{
			if (grid[x][i] != compareTo)
			{
				won = false;
				break;
			}
		}
		if (won) return won;
		won = true;
		
		for (int i = 0; i < 3; i++)			// vertical
		{
			if (grid[i][y] != compareTo)
			{
				won = false;
				break;
			}
		}
		if (won) return won;
		won = true;
		
		if (x != y) return false;			// diagonal
		for (int i = 0; i < 3; i++)
		{
			if (grid[i][i] != compareTo) return false;
		}
		return true;
	}
	
	private void setPlayerTurn() {
		playerTurn = (playerTurn + 1) % NUM_PLAYERS;
	}
	
	@Override
	public boolean isStarted() {
		return (moveCounter > 0);
	}

	@Override
	public boolean isOver() {
		return isOver;
	}

	@Override
	public User getWinner() {
		return wonPlayer;
	}

}
