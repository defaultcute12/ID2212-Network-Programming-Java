

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;


@ApplicationScoped
public class UserHandler
{
	private Map<Session, User> loggedInUsers = new HashMap<>();
	
	public boolean newUser(JsonObject message)
	{
		String username = message.getString("username");
		String password = message.getString("password");
		
		// TODO add persistent
		return true;
	}
	
	public User login(JsonObject message, Session session)
	{
		String username = message.getString("username");
		String password = message.getString("password");
		
		User newLoggedInUser = getPersistent(username, password);
		
		if (newLoggedInUser == null)
		{
			sendLoginFailure(session);
			return null;
		}
		
		newLoggedInUser.setSession(session);			// tie the session to the user
		loggedInUsers.put(session, newLoggedInUser);	// store user in map with session as key
		sendLoginSuccess(newLoggedInUser);				// notify user of login success
		return newLoggedInUser;
	}
	
	public User getUser(Session session)
	{
		if (!loggedInUsers.containsKey(session))
		{
			sendLoginFailure(session);
			return null;
		}
		return loggedInUsers.get(session);
	}
	
	private User getPersistent(String username, String password)
	{
		// TODO fetch persist
		int ID = 0;
		int score = 0;
		
		return new User(ID, username, score);
	}
	
	private void sendLoginFailure(Session session)
	{
		JsonProvider provider = JsonProvider.provider();
		JsonObject failureMessage = provider.createObjectBuilder()
											.add("action", "login")
											.add("status", "failure")
											.build();
		try {
			session.getBasicRemote().sendText(failureMessage.toString());
		} catch (IOException ex) { }
	}
	
	private void sendLoginSuccess(User user)
	{
		JsonProvider provider = JsonProvider.provider();
		JsonObject successMessage = provider.createObjectBuilder()
											.add("action", "login")
											.add("status", "success")
											.add("ID", user.getID())
											.add("username", user.getUsername())
											.add("XP", user.getXp())
											.build();
		user.send(successMessage);
	}
}
