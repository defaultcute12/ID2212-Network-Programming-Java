package controller;

import java.util.Currency;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import model.ExchangeRate;

/**
 * EJB that handles persistence
 */

@Stateless
public class ConverterController
{
	@PersistenceContext(unitName = "puConverter")			// as specified in persistance.xml
	private EntityManager entityManager;
	
	// Must have a default constructor
	public ConverterController() { }
	
	public void setExchangeRate(Currency sellingCurrency, Currency buyingCurrency, float value)
	{
		// TODO verify it doesn't already exist
		
		ExchangeRate exchangeRateEntity = new ExchangeRate(sellingCurrency, buyingCurrency, value);
		entityManager.persist(exchangeRateEntity);
	}
	
	public float getConvertion(Currency fromCurrency, Currency toCurrency, float value)
	{
		Query query = entityManager.createQuery("SELECT e.price FROM ExchangeRate e " + 
												"WHERE e.sellingCurrency LIKE :s " + 
												"AND e.buyingCurrency LIKE :b");
		query.setParameter("s", fromCurrency);
		query.setParameter("b", toCurrency);
		
		float exchangeRate = (float)query.getSingleResult();
		
		// TODO calc
		
		return exchangeRate;
	}
}
