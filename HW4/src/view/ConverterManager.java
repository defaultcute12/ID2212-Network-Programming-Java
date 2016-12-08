package view;

import java.io.Serializable;
import java.util.Currency;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import controller.ConverterController;

/**
 * This is the Backing Bean (Managed Bean) that JSF
 * uses to store/fetch data to be combined with HTML.
 * 
 * This Bean is tied to the session.
 */
@SuppressWarnings("serial")
@ConversationScoped
@LocalBean
@Named("converterManager")
public class ConverterManager implements Serializable
{
	private String fromCurrency;
	private String toCurrency;
	private float value;
	
	@Inject
    private Conversation conversation;
	
	@EJB
	ConverterController converter;
	
	// Must have a default constructor
	public ConverterManager() { }
	
	private void startConversation() {
		if (conversation.isTransient()) conversation.begin();
	}
	
	private void stopConversation() {
		if (!conversation.isTransient()) conversation.end();
	}
	
	public float getConvertion()
	{
		Currency from = Currency.getInstance(fromCurrency);
		Currency to = Currency.getInstance(toCurrency);
		return converter.getConvertion(from, to, value);
	}
	
	public void setConvertion()
	{
		Currency from = Currency.getInstance(fromCurrency);
		Currency to = Currency.getInstance(toCurrency);
		converter.setExchangeRate(from, to, value);
	}
	
}
