

import java.io.IOException;

import javax.json.JsonStructure;
import javax.websocket.Session;

public class User
{
	private transient Session session;
	private transient Lobby lobby;
	
	private final int ID;
	private final String username;
	private int EXP;
	
	public User(int ID, String username, int EXP)
	{
		this.ID = ID;
		this.username = username;
		this.EXP = EXP;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public void setLobby(Lobby lobby) {
		this.lobby = lobby;
	}
	
	public Lobby getLobby() {
		return lobby;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getEXP() {
		return EXP;
	}
	
	public boolean addEXP(int points)
	{
		if (points < 0) return false;
		
		EXP += points;
		return true;
	}
	
	public boolean send(JsonStructure message)
	{
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException ex) {
			return false;
		}
		return true;
	}
}
