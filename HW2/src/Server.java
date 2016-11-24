
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server
{
	private static final String USAGE = "java bankrmi.Server <BankName> <MarketplaceName>";
	
	public Server(String bankName, String marketplaceName)
	{
		try {
			System.out.println("Will now create bank");
			Bank bankobj = new BankImpl(bankName);
			
			System.out.println("Will now create Market");
			Marketplace marketObj = new MarketplaceImpl(marketplaceName);
			
			System.out.println("Will register at rmiregistry");
			// Register the newly created object at rmiregistry.
			try {
				LocateRegistry.getRegistry(1099).list();
			} catch (RemoteException e) {
				LocateRegistry.createRegistry(1099);
			}
			System.out.println("Will bind at rmiregistry");

			Naming.rebind(bankName, bankobj);
			Naming.rebind(marketplaceName, marketObj);
			
			System.out.println(bankobj + " and " + marketObj + " is ready.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{		
		if (args.length > 2 || (args.length > 0 && args[0].equalsIgnoreCase("-h")))
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		
		String bankName, marketplaceName;
		if (args.length > 0) bankName = args[0];
		else bankName = Bank.DEFAULT_BANK_NAME;
		
		if (args.length > 1) marketplaceName = args[1];
		else marketplaceName = Marketplace.DEFAULT_MARKETPLACE_NAME;
		
		System.out.println("Will now create a new server object with " + bankName + ", " + marketplaceName);
		
		new Server(bankName, marketplaceName);
	}
}



