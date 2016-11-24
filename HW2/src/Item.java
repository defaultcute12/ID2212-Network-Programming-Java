import java.io.Serializable;


@SuppressWarnings("serial")
public class Item implements Serializable
{
	public final int ID;
	public final String name;
	public final float price;
	public Account seller;
	
	private static int idFactory = 1;
	
	public Item (String name, float price)
	{
		this.ID = idFactory++;
		this.name = name;
		this.price = price;
		this.seller = seller;
	}
	
	public void updateSeller(Account seller)
	{
		this.seller = seller;
	}
}
