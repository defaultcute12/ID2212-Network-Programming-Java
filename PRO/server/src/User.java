

import java.io.IOException;

import javax.json.JsonStructure;
import javax.websocket.Session;

public class User
{
	private transient Session session;
	
	private final int ID;
	private final String username;
	private int xp;
	
	public User(int ID, String username, int xp)
	{
		this.ID = ID;
		this.username = username;
		this.xp = xp;
	}
	
	public void setSession(Session session)
	{
		this.session = session;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getXp() {
		return xp;
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
