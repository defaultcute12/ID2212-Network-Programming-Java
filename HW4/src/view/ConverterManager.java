package view;

import controller.ConverterController;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("converterManager")
@ConversationScoped
public class ConverterManager implements Serializable
{
    private static final long serialVersionUID = 16247164405L;
    @EJB
    private ConverterController converterController;
    private Exception transactionFailure;
    private boolean success;
    
    private String fromCurrency;
    private String toCurrency;
    private float value;
    private float conversion = 0;
    
    @Inject
    private Conversation conversation;

    private void startConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    private void stopConversation() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }

    private void handleException(Exception e) {
        stopConversation();
        e.printStackTrace(System.err);
        transactionFailure = e;
    }


    /**
     * @return <code>true</code> if the latest transaction succeeded, otherwise
     * <code>false</code>.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Returns the latest thrown exception.
     */
    public Exception getException() {
        return transactionFailure;
    }

    /**
     * This return value is needed because of a JSF 2.2 bug. Note 3 on page 7-10
     * of the JSF 2.2 specification states that action handling methods may be
     * void. In JSF 2.2, however, a void action handling method plus an
     * if-element that evaluates to true in the faces-config navigation case
     * causes an exception.
     *
     * @return an empty string.
     */
    private String jsf22Bugfix() {
        return "";
    }

    public String addConversion()
    {
        try {
            startConversation();
            transactionFailure = null;
            
            converterController.createRate(fromCurrency, toCurrency, value);
            success = true;
        } catch (Exception e) {
            handleException(e);
        }
        return jsf22Bugfix();
    }
    
    public String calcConversion()
    {
    	success = true;
    	conversion = converterController.getConversion(fromCurrency, toCurrency, value);
        return jsf22Bugfix();
    }
    
    // needed?
    public void setConversion()
    {
    	
    }
    
    public float getConversion()
    {
    	return conversion;
    }


    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Never used but JSF does not support write-only properties.
     */
    public Integer getValue() {
        return null;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    /**
     * Never used but JSF does not support write-only properties.
     */
    public String getToCurrency() {
        return null;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    /**
     * Never used but JSF does not support write-only properties.
     */
    public String getFromCurrency() {
        return null;
    }
}