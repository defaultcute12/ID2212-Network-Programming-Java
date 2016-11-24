import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


public interface Marketplace extends Remote
{
	public static final String DEFAULT_MARKETPLACE_NAME = "ElanteMall";

	public boolean sell(Item sellItem) throws RemoteException;
	
	public Item buy(int ID, Account buyer) throws RemoteException;
	
	public Map<Integer, Item> listItems() throws RemoteException;
}