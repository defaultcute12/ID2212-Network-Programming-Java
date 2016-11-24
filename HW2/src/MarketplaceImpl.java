import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("serial")
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace
{
	private Map<Integer, Item> items = new HashMap<>();
	public final String marketplaceName;

	protected MarketplaceImpl(String marketplaceName) throws RemoteException
	{
		super();					// Call the constructor of the extended UniCastRemoteObject; creates and exports
		this.marketplaceName = marketplaceName;
	}

	@Override
	public synchronized boolean sell(Item sellItem)
	{
		if (items.containsKey(sellItem)) return false;
		
		items.put(sellItem.ID, sellItem);
		return true;
	}

	@Override
	public synchronized Item buy(int ID, Account buyer)
	{
		Item buyingItem = items.remove(ID);
		if (buyingItem == null) return null;
		
		try {
			if (buyer.getBalance() < buyingItem.price) return null;
			
			buyer.withdraw(buyingItem.price);						// remove $ from buyer
			buyingItem.seller.deposit(buyingItem.price);			// add $ to seller
		}
		catch (RemoteException e) {
			e.printStackTrace();
		} catch (RejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buyingItem;
	}
	
	@Override
	public synchronized Map<Integer, Item> listItems()
	{
		return items;
	}

}

