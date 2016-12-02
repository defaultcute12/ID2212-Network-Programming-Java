import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Client extends Remote {
	
	public Account getAccount() throws RemoteException;
	
	public void notify(String msg) throws RemoteException;
	
	public String getName() throws RemoteException;

}
