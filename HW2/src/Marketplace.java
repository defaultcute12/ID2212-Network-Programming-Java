import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


public interface Marketplace extends Remote
{
	public static final String DEFAULT_MARKETPLACE_NAME = "ElanteMall";

	public boolean sell(Item sellItem) throws RemoteException;
	
	public Item buy(int ID, Account buyer) throws RemoteException;
	
	public Map<Integer, Item> listItems() throws RemoteException;

	public void wish(String name, float price, Client requester) throws RemoteException;
	
	public boolean register(Client client) throws RemoteException;
	
	public boolean unregister(Client client) throws RemoteException;
}