import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;


public class ClientImpl implements Client, Runnable
{
	private static final String USAGE = "java Client <BankName> <MarketplaceName>";
	
	private Scanner scan;
	private Account account;
	
	private Marketplace marketplace;
	private Bank bank;
	
	public ClientImpl(String bankName, String marketplaceName)
	{
		try {
			bank = (Bank) Naming.lookup(bankName);
			marketplace = (Marketplace) Naming.lookup(marketplaceName);
		}
		catch (Exception e) {
			System.out.println("The runtime failed: " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Connected to bank: " + bankName + " and marketplace: " + marketplaceName);
	}
	
	@Override
	public void run()
	{		
		scan = new Scanner(System.in);
		try {
			mainMenu();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void mainMenu() throws RemoteException
	{
		while(true)
		{
			String username, password;

			System.out.print("1. Marketplace Login\n2. New Account\n3. Exit\n> ");			
			switch (scan.nextInt())
			{
			case 1:
				System.out.print("Username> ");
				username = scan.nextLine();
				System.out.print("Password> ");
				password = scan.nextLine();
				
				try {
					marketplace.login(username, password, this);
				} catch (RejectedException e1) {
					System.err.println("Failed to login.");
					break;
				}
				
				account = bank.getAccount(username);
				
				try {
					loggedInMenu();
				} catch (RejectedException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				System.out.print("Username> ");
				username = scan.nextLine();
				System.out.print("Password> ");
				password = scan.nextLine();
				try {
					marketplace.newAccount(username, password);
					bank.newAccount(username);
				} catch (RejectedException e) {
					System.err.println("Failed to create new account.");
				}
				break;
			case 3:
				return;
			}
		}
	}
	
	private void loggedInMenu() throws RemoteException, RejectedException
	{
		while(true)
		{
			System.out.println("1. Bank\n2. Inventory\n3. Market\n4. Logout\n> ");
			switch (scan.nextInt())
			{
			case 1:
				bankMenu();
				break;
			case 2:
				inventoryMenu();
				break;
			case 3:
				marketMenu();
				break;
			case 4:
				marketplace.logout(this);
				account = null;
				return;
			}
		}
	}
	
	private void bankMenu() throws RemoteException
	{
		while(true)
		{
			System.out.print("1. Balance\n2. Deposit\n3. Withdraw\n4. Back\n> ");			
			switch (scan.nextInt())
			{
			case 1:
				System.out.println("$" + account.getBalance());
				break;
			case 2:
				try {
					account.deposit(scan.nextFloat());
				} catch (RejectedException e) {
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					account.withdraw(scan.nextFloat());
				} catch (RejectedException e) {
					e.printStackTrace();
				}
				break;
			case 4:
				return;
			}
		}
	}
	
	private void inventoryMenu() throws RemoteException, RejectedException
	{
		while (true)
		{
			System.out.print("1. List Items\n2. New Item\n3. Delete Item\n4. Back\n> ");
			switch (scan.nextInt())
			{
			case 1:
				Item[] items = marketplace.getInventory(this);
				for (int i = 0; i < items.length; i++)
					System.out.println(items[i].ID + ".\t" + items[i].name +"\t$" + items[i].price);
				break;
			case 2:
				System.out.print("Item name> ");
				String name = scan.nextLine();
				System.out.print("Item price ($)> ");
				float price = scan.nextFloat();
				
				marketplace.addItem(name, price, this);
				break;
			case 3:
				System.out.print("Item ID> ");
				marketplace.removeItem(scan.nextInt(), this);
				break;
			case 4:
				return;
			}
		}
	}
	
	private void marketMenu() throws RemoteException, RejectedException
	{
		while (true)
		{
			// TODO: add wishes
			System.out.print("1. Show Items\n2. Buy Item\n3. Sell Item\n4. Back\n> ");
			switch (scan.nextInt())
			{
			case 1:
				Item[] items = marketplace.getMarketItems(this);
				for (Item item : items) System.out.println(item.ID + "\t" + item.name + "\t$" + item.price);
				break;
			case 2:
				System.out.print("Item ID> ");
				marketplace.buy(scan.nextInt(), this);
				break;
			case 3:
				System.out.print("Inventory Item ID> ");
				marketplace.sell(scan.nextInt(), this);
				break;
			case 4:
				return;
			}
		}
	}
	
	@Override
	public Account getAccount() throws RemoteException {
		return account;
	}

	@Override
	public void notify(String msg) throws RemoteException {
		System.out.println(msg);
		
	}

	@Override
	public String getName() throws RemoteException {
		if (account == null) return "";
		return account.getName();
	}
	

	public static void main(String[] args)
	{
		if ((args.length > 3) || (args.length > 0 && args[0].equals("-h")))
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		
		String bankName = Bank.DEFAULT_NAME;
		String marketplaceName = Marketplace.DEFAULT_NAME;
		
		if (args.length >= 1) bankName = args[0];
		if (args.length >= 1) marketplaceName = args[1];
		
		new ClientImpl(bankName, marketplaceName).run();
	}
}
