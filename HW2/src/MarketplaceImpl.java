import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("serial")
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace
{
	private Map<Integer, Item> items = new HashMap<>();
	private List<Wish> wishes = new LinkedList<Wish>();
	private List<Client> registeredClients = new LinkedList<Client>();

	
	public final String marketplaceName;

	protected MarketplaceImpl(String marketplaceName) throws RemoteException
	{
		super();					// Call the constructor of the extended UniCastRemoteObject; creates and exports
		this.marketplaceName = marketplaceName;
	}

	@Override
	public synchronized boolean sell(Item sellItem)
	{
		if (!registeredClients.contains(sellItem.seller)) return false;
		if (items.containsKey(sellItem)) return false;
		items.put(sellItem.ID, sellItem);
		
		Iterator<Wish> it = wishes.iterator();
		while (it.hasNext())
		{
			Wish w = it.next();
			System.out.println("Comparing " + sellItem.name + " (" + sellItem.price + ") with " + w.wish + " (" + w.price + ")");
			if (sellItem.name.equals(w.wish) &&  sellItem.price <= w.price)
			{
				System.out.println("Will now notify requester");
				try {
					w.requester.notify("Marketplace " + marketplaceName + " now has " + w.wish + " in store");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Outside if");
			}
		}
		return true;
	}

	@Override
	public synchronized Item buy(int ID, Account buyer)
	{
		Item buyingItem = items.remove(ID);
		if (buyingItem == null) return null;
		
		try {
			if (buyer.getBalance() < buyingItem.price)
			{
				items.put(buyingItem.ID, buyingItem);			// Putting the item back
				return null;
			}
			
			buyer.withdraw(buyingItem.price);								// remove $ from buyer
			buyingItem.seller.getAccount().deposit(buyingItem.price);		// add $ to seller
			buyingItem.seller.notify("Item " + buyingItem.name + " with ID " + buyingItem.ID + " has been sold");
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
	
	@Override
	public synchronized void wish(String name, float price, Client requester)
	{
		System.out.println("Marketplace adding wish");
		
		Wish w = new Wish(name, price, requester);
		wishes.add(w);
	}
	
	public class Wish
	{
		Client requester;
		float price;
		String wish;
		
		public Wish(String wish, float price, Client req)
		{
			this.wish = wish;
			this.price = price;
			this.requester = req;
		}
	}

	@Override
	public boolean register(Client client) throws RemoteException
	{
		System.out.println("Got a new registered client: " + client.getName());
		registeredClients.add(client);
		return false;
	}
	
	@Override
	public boolean unregister(Client client) throws RemoteException
	{
		System.out.println("client: " + client.getName() + " asked to unregister");
		return registeredClients.remove(client);
	}

}

