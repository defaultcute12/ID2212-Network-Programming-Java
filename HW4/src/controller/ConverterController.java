package controller;

import model.ExchangeRate;
import model.ExchangeRateDTO;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class ConverterController
{
    @PersistenceContext(unitName = "myPU")
    private EntityManager entityManager;


    public void createRate(String fromCurrency, String toCurrency, float rate)
    {
    	if (0 != getConversion(fromCurrency, toCurrency, 1))		// rate already exists for these currencies
    	{
    		Query query = entityManager.createQuery("UPDATE ExchangeRate e " +
    												"SET e.rate = :r " +
    												"WHERE e.fromCurrency LIKE :f " +
    												"AND e.toCurrency LIKE :t");
    		query.setParameter("r", rate);
    		query.setParameter("f", fromCurrency);
    		query.setParameter("t", toCurrency);
    		System.out.println("Updated " + query.executeUpdate() + " rate(s)");
    	}
    	else {
    		ExchangeRate newRate = new ExchangeRate(rate, fromCurrency, toCurrency);
    		entityManager.persist(newRate);
    	}
    }
    
    public float getConversion(String fromCurrency, String toCurrency, float value)
    {
    	if (fromCurrency.equals(toCurrency)) return value;
    	
		Query query = entityManager.createQuery("SELECT e.rate FROM ExchangeRate e " + 
												"WHERE e.fromCurrency LIKE :f " + 
												"AND e.toCurrency LIKE :t");
		query.setParameter("f", fromCurrency);
		query.setParameter("t", toCurrency);
		
		float rate;
		
		try {
			rate = (float) query.getSingleResult();
		} catch (NoResultException e) {
			rate = 0;
		}
		
		return (rate * value);
    }

}
