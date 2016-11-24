import java.io.Serializable;


@SuppressWarnings("serial")
public class Item implements Serializable
{
	public final int ID;
	public final String name;
	public final float price;
	public Client seller;
	
	private static int idFactory = 1;
	
	public Item (String name, float price)
	{
		this.ID = idFactory++;
		this.name = name;
		this.price = price;
	}
	
	public void updateSeller(Client seller)
	{
		this.seller = seller;
	}
}
