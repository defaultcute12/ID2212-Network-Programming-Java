
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class Client
{
	private static final String USAGE = "java bankrmi.Client <BankName> <MarketplaceName>";
	Account account;
	Bank bankobj;
	Marketplace marketObj;
	private String bankname;
	private String marketplaceName;
	String clientname;
	
	private Map<String, Item> items = new HashMap<>();
	
	static enum CommandName {
		newAccount, getAccount, deleteAccount, deposit, withdraw, balance, quit, help, list, newItem, listInventory, sellItem, buyItem, listMarket;
	};
	
	public Client(String bankName, String marketplaceName)
	{
		this.bankname = bankName;
		this.marketplaceName = marketplaceName;
		
		try {
			bankobj = (Bank) Naming.lookup(bankname);
			marketObj = (Marketplace) Naming.lookup(marketplaceName);
		}
		catch (Exception e) {
			System.out.println("The runtime failed: " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Connected to bank: " + bankname + " and marketplace: " + marketplaceName);
	}
	
	public Client()
	{
		this(Bank.DEFAULT_BANK_NAME, Marketplace.DEFAULT_MARKETPLACE_NAME);
	}
	
	public void run()
	{
		BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
		
		while (true)
		{
			System.out.print(clientname + "@" + bankname + ">");
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
				sellingItem.updateSeller(account);
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
		}
		
		// all further commands require a name to be specified
		String userName = command.getName();
		if (userName == null)
		{
			System.out.println("name is not specified");
			return;
		}
		
		switch (command.getCommandName())
		{
		case newAccount:
			clientname = userName;
			bankobj.newAccount(userName);
			return;
		case deleteAccount:
			clientname = userName;
			bankobj.deleteAccount(userName);
			return;
		}
		
		// all further commands require a Account reference
		Account acc = bankobj.getAccount(userName);
		if (acc == null)
		{
			System.out.println("No account for " + userName);
			return;
		} else {
			account = acc;
			clientname = userName;
		}
		
		switch (command.getCommandName())
		{
		case getAccount:
			System.out.println(account);
			break;
		case deposit:
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
		
		private Command(Client.CommandName commandName, String name, float amount)
		{
			this.commandName = commandName;
			this.name = name;
			this.amount = amount;
		}
	}
	
	public static void main(String[] args)
	{
		if ((args.length > 2) || (args.length > 0 && args[0].equals("-h")))
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		
		String bankName, marketplaceName;
		if (args.length > 1)
		{
			bankName = args[0];
			marketplaceName = args[1];
			new Client(bankName, marketplaceName).run();
		} else {
			new Client().run();
		}
	}
}




