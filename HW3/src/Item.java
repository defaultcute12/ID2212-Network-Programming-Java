import java.io.Serializable;


@SuppressWarnings("serial")
public class Item implements Serializable
{
	public final int ID;
	public final String name;
	public final float price;
	public final String owner;
		
	public Item(int ID, String name, float price, String owner)
	{
		this.ID = ID;
		this.name = name;
		this.price = price;
		this.owner = owner;
	}
}
