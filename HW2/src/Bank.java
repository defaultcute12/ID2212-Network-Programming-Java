
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote
{
	public static final String DEFAULT_BANK_NAME = "Nordea";

	public Account newAccount(String name) throws RemoteException, RejectedException;
	
	public Account getAccount(String name) throws RemoteException;
	
	public boolean deleteAccount(String name) throws RemoteException;
	
	public String[] listAccounts() throws RemoteException;
}