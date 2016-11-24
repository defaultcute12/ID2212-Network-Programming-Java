
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

@SuppressWarnings("serial")
public class ClientImpl extends UnicastRemoteObject implements Client
{
	private static final String USAGE = "java bankrmi.Client <BankName> <MarketplaceName>";
	private Account account;
	Bank bankobj;
	Marketplace marketObj;
	private String bankName;
	private String marketplaceName;
	private String clientName;
	private String accountName;
	
	private Map<String, Item> items = new HashMap<>();
	
	static enum CommandName {
		newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit, help, list, newItem, listInventory, sellItem, buyItem, listMarket, wish, unregister;
	};
	
	public ClientImpl(String bankName, String marketplaceName, String clientName) throws RemoteException
	{
		this.bankName = bankName;
		this.marketplaceName = marketplaceName;
		this.clientName = clientName;
		
		try {
			bankobj = (Bank) Naming.lookup(bankName);
			marketObj = (Marketplace) Naming.lookup(marketplaceName);
		}
		catch (Exception e) {
			System.out.println("The runtime failed: " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Connected to bank: " + bankName + " and marketplace: " + marketplaceName);
		marketObj.register(this);				// register this client as potential seller
	}
	
	public ClientImpl() throws RemoteException
	{
		this(Bank.DEFAULT_BANK_NAME, Marketplace.DEFAULT_MARKETPLACE_NAME, "NoName");
	}
	
	public Account getAccount()
	{
		return account;
	}
	
	public void notify(String msg)
	{
		System.out.println("Client " + clientName + " got msg: " + msg);
	}
	
	public void run()
	{
		BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
		
		// Client must always have an account
		if (account == null)
		{
			try {
				System.out.print("Welcome to " + bankName + "! What's your name?> ");
				clientName = consoleIn.readLine();
				
				System.out.print("Create new account> ");
				String userInput = "newAccount " + consoleIn.readLine();
				execute(parse(userInput));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RejectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		while (true)
		{
			System.out.print(accountName + "@" + bankName + "> ");
			try {
				String userInput = consoleIn.readLine();
				execute(parse(userInput));
			} catch (RejectedException re) {
				System.out.println(re);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Command parse(String userInput)
	{
		if (userInput == null) return null;
		
		StringTokenizer tokenizer = new StringTokenizer(userInput);
		if (tokenizer.countTokens() == 0) return null;
		
		CommandName commandName = null;
		String name = null;					// either userName or itemName
		float amount = 0;
		int userInputTokenNo = 1;
		
		while (tokenizer.hasMoreTokens())
		{
			switch (userInputTokenNo)
			{
			case 1:
				try {
					String commandNameString = tokenizer.nextToken();
					commandName = CommandName.valueOf(CommandName.class, commandNameString);
				}
				catch (IllegalArgumentException commandDoesNotExist)
				{
					System.out.println("Illegal command");
					return null;
				}
				break;
			case 2:
				name = tokenizer.nextToken();
				break;
			case 3:
				try {
					amount = Float.parseFloat(tokenizer.nextToken());
				} catch (NumberFormatException e) {
					System.out.println("Illegal amount");
					return null;
				}
				break;
			default:
				System.out.println("Illegal command");
				return null;
			}
			userInputTokenNo++;
		}
		return new Command(commandName, name, amount);
	}
	
	void execute(Command command) throws RemoteException, RejectedException
	{
		if (command == null) return;
		
		switch (command.getCommandName())
		{
		case list:
			try
			{
				for (String accountHolder : bankobj.listAccounts()) System.out.println(accountHolder);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		case quit:
			System.exit(0);
		case help:
			for (CommandName commandName : CommandName.values()) System.out.println(commandName);
			return;
		case listInventory:
			 for (Map.Entry<String, Item> item : items.entrySet())
			 {
			 	System.out.println(item.getValue().ID + "\t" + item.getValue().name + "\t" + item.getValue().price);
			 }
			return;
		case listMarket:
			Map<Integer, Item> marketItems = marketObj.listItems();
			
			Iterator<Item> it = marketItems.values().iterator();
			while (it.hasNext())
			{
				Item item = it.next();
				System.out.println(item.ID + "\t" + item.name + "\t" + item.price);
			}
			return;
		case newItem:
			Item newItem = new Item(command.getName(), command.getAmount());
			items.put(newItem.name, newItem);
			return;
		case sellItem:
			Item sellingItem = items.remove(command.getName());
			if (sellingItem != null)
			{
				sellingItem.updateSeller(this);
				marketObj.sell(sellingItem);
				System.out.println(sellingItem.name + " was added to Marketplace " + marketplaceName);
			}
			else {
				System.out.println("Could not find item with name " + command.getName() + " in your inventory");
			}
			return;
		case buyItem:
			int ID = Integer.parseInt(command.getName());
			Item boughtItem = marketObj.buy(ID, account);
			if (boughtItem == null)
			{
				System.out.println("Failed to buy item with ID " + ID + " from Marketplace " + marketplaceName);
			}
			else {
				items.put(boughtItem.name, boughtItem);
				System.out.println("Succesfully bought item " + boughtItem.name + " for $" + boughtItem.price +
									" from Marketplace " + marketplaceName);
			}
			return;
		case wish:
			marketObj.wish(command.getName(), command.getAmount(), this);
			return;
		}
		
		switch (command.getCommandName())
		{
		case newAccount:
			if (command.getName() == null) System.out.println("Name is not specified");
			else {
				account = bankobj.newAccount(command.getName());
				accountName = command.getName();
			}
			return;
		case deleteAccount:
			if (command.getName() == null) System.out.println("Name is not specified");
			else if (command.getName().equals(accountName)) System.out.println("Cannot delete current account");
			else bankobj.deleteAccount(command.getName());
			return;
		case unregister:
			marketObj.unregister(this);
			return;
		}
		
		switch (command.getCommandName())
		{
		case getAccount:
			System.out.println(account);
			break;
		case deposit:
			if (command.getAmount() == 0)
			{
				account.deposit(Float.parseFloat(command.getName()));
			}
			account.deposit(command.getAmount());
			break;
		case withdraw:
			account.withdraw(command.getAmount());
			break;
		case balance:
			System.out.println("balance: $" + account.getBalance());
			break;
		default:
			System.out.println("Illegal command");
		}
	}
	
	private class Command
	{
		private String name;
		private float amount;
		private CommandName commandName;
		
		private String getName() {
			return name;
		}
		
		private float getAmount() {
			return amount;
		}
		
		private CommandName getCommandName() {
			return commandName;
		}
		
		private Command(ClientImpl.CommandName commandName, String name, float amount)
		{
			this.commandName = commandName;
			this.name = name;
			this.amount = amount;
		}
	}
	
	public static void main(String[] args)
	{
		if ((args.length > 3) || (args.length > 0 && args[0].equals("-h")))
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		
		try {
			String bankName, marketplaceName, clientName;
			if (args.length == 3)
			{
				bankName = args[0];
				marketplaceName = args[1];
				clientName = args[2];
				new ClientImpl(bankName, marketplaceName, clientName).run();
			} else {
				new ClientImpl().run();
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() throws RemoteException {
		return clientName;
	}
}




