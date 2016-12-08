package model;

import java.util.Currency;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class ExchangeRate
{
	@Id
	@GeneratedValue
	private int ID;
	
	private Currency sellingCurrency;
	private Currency buyingCurrency;
	private float price;
	
	// must have a public no-argument constructor as defined by Entity
	public ExchangeRate() { }
	
	public ExchangeRate(Currency sellingCurrency, Currency buyingCurrency, float price)
	{
		this.sellingCurrency = sellingCurrency;
		this.buyingCurrency = buyingCurrency;
		this.price = price;
	}
	
	public float getPrice()
	{
		return price;
	}
	
	public void setPrice(float price)
	{
		this.price = price;
	}
	
	public Currency getSelling()
	{
		return sellingCurrency;
	}
	
	public Currency getBuying()
	{
		return buyingCurrency;
	}

}
