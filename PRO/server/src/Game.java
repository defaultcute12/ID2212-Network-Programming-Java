import javax.json.JsonObject;


public interface Game
{
	public static final int TICTACTOE = 0;
	
	public JsonObject setPlayers(User[] players);
	
	public boolean removePlayer(User player);
	
	public int getType();
	
	public int getMaxNoPlayers();
	
	public boolean mustBeMaxNoPlayers();

	public JsonObject getState();
	
	public JsonObject apply(User player, JsonObject move);
	
	public boolean isStarted();
	
	public boolean isOver();
	
	public User getWinner();
	
}
