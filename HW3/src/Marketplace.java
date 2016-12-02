import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


public interface Marketplace extends Remote
{
	public static final String DEFAULT_NAME = "RMI Marketplace";
	public static final int DEFAULT_PORT = 2212;
	public static final String DEFAULT_DB = "marketDB";
	
	public void sell(int itemID, Client client) throws RemoteException, RejectedException;
	
	public void buy(int ID, Client client) throws RemoteException, RejectedException;
	
	public Item[] getMarketItems(Client client) throws RemoteException, RejectedException;
	
	public void login(String username, String password, Client client) throws RemoteException, RejectedException;
	
	public void logout(Client client) throws RemoteException, RejectedException;
	
	public void newAccount(String username, String password) throws RemoteException, RejectedException;
	
	public Item[] getInventory(Client client) throws RemoteException, RejectedException;
	
	public void addItem(String name, float price, Client client) throws RemoteException, RejectedException;
	
	public void removeItem(int itemID, Client client) throws RemoteException, RejectedException;

}