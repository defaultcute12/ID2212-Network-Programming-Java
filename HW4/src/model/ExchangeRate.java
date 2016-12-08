package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A persistent representation of an account.
 */
@Entity
public class ExchangeRate implements ExchangeRateDTO, Serializable
{
    private static final long serialVersionUID = 16247164401L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int ID;
    private float rate;
    private String fromCurrency;
    private String toCurrency;

    /**
     * Creates a new instance of Account
     */
    public ExchangeRate() {
    }

    /**
     * Creates a new instance of Account
     */
    public ExchangeRate(float rate, String fromCurrency, String toCurrency) {
        this.rate = rate;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    @Override
    public String getToCurrency() {
        return toCurrency;
    }

    @Override
    public String getFromCurrency() {
        return fromCurrency;
    }

    @Override
    public float getRate() {
        return rate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        return new Integer(ID).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExchangeRate)) return false;
        ExchangeRate other = (ExchangeRate) object;
        return this.ID == other.ID;
    }

    @Override
    public String toString() {
        return "[id=" + ID + "]";
    }
}
