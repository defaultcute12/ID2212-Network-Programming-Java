import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("serial")
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace
{
	public final String marketplaceName;
	private Bank bank;						// used to retrieve seller account
	
	private List<Client> registeredClients = new LinkedList<Client>();
	
    private Connection conn;
    private Statement statement;
    private PreparedStatement newAccountStatement;
    private PreparedStatement getAccountStatement;
    private PreparedStatement newItemStatement;
    private PreparedStatement listItemStatement;
    private PreparedStatement getNoMarketItemsStatement;
    private PreparedStatement getMarketItemsStatement;
    private PreparedStatement getNoInventoryStatement;
    private PreparedStatement getInventoryStatement;
    private PreparedStatement changeItemOwnerStatement;
    private PreparedStatement updateSoldStatement;
    private PreparedStatement updateBoughtStatement;
    private PreparedStatement getItemOwnerStatement;
	
	protected MarketplaceImpl(String marketplaceName, Bank bank) throws RemoteException
	{
		super();
		
		this.marketplaceName = marketplaceName;
		this.bank = bank;
		connect();
		try {
			createTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void connect()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
	        conn = DriverManager.getConnection("jdbc:mysql://localhost:" + Marketplace.DEFAULT_PORT + "/" +
					Marketplace.DEFAULT_DB, "root", "test");
	        statement = conn.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createTable() throws Exception
	{
        ResultSet result = conn.getMetaData().getTables(null, null, "accounts", null);
        if (result.next()) dropTable();
        result.close();
        
        System.out.println("Creating accounts");
        statement.executeUpdate("CREATE TABLE accounts (" + 
        						"username VARCHAR(255) NOT NULL PRIMARY KEY, " +
        						"password VARCHAR(255), " +
        						"no_sold INTEGER, " +
        						"no_bought INTEGER)");
       
        System.out.println("Creating items");

        statement.executeUpdate("CREATE TABLE items (" +
        						"id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT," +
        						"name VARCHAR(255), " +
        						"price FLOAT, " +
        						"ownerUsername VARCHAR(255), " +
        						"isListed BOOLEAN)");
        
        System.out.println("Preparing statements");
        newAccountStatement = 		conn.prepareStatement("INSERT INTO accounts VALUES (?, ?, ?, ?)");
        getAccountStatement = 		conn.prepareStatement("SELECT * from accounts WHERE username=?");
        newItemStatement = 			conn.prepareStatement("INSERT INTO items VALUES (?, ?, ?, ?, ?)");
        listItemStatement = 		conn.prepareStatement("UPDATE items SET isListed=? WHERE id=? AND ownerUsername=?");
        getNoMarketItemsStatement =	conn.prepareStatement("SELECT COUNT(*) FROM items WHERE isListed=TRUE");
        getMarketItemsStatement = 	conn.prepareStatement("SELECT * FROM items WHERE isListed=TRUE");
        getNoInventoryStatement =	conn.prepareStatement("SELECT COUNT(*) FROM items WHERE isListed=0 AND ownerUsername=?");
        getInventoryStatement =		conn.prepareStatement("SELECT * FROM items WHERE isListed=FALSE AND ownerUsername=?");
        changeItemOwnerStatement =	conn.prepareStatement("UPDATE items SET ownerUsername=?, isListed=FALSE WHERE id=? AND ownerUsername=?");
        updateSoldStatement =		conn.prepareStatement("UPDATE accounts SET no_sold=no_sold+1 WHERE username=?");
        updateBoughtStatement =		conn.prepareStatement("UPDATE accounts SET no_bought=no_bought+1 WHERE username=?");
        getItemOwnerStatement =		conn.prepareStatement("SELECT * FROM items WHERE id=?");
        
        System.out.println("Marketplace ready.");
    }
	
    private void dropTable() throws Exception
    {
        int NoOfAffectedRows = statement.executeUpdate("DROP TABLE accounts");
        System.out.println("Table account dropped, " + NoOfAffectedRows + " row(s) affected");
        
        NoOfAffectedRows = statement.executeUpdate("DROP TABLE items");
        System.out.println("Table items dropped, " + NoOfAffectedRows + " row(s) affected");
    }
	
	private boolean isAuthentic(Client client) throws RejectedException
	{
		if (registeredClients.contains(client)) return true;
		throw new RejectedException("Not an authenticated marketplace user");
	}

	@Override
	public void sell(int itemID, Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);
		
		if (itemID <= 0) throw new RejectedException("Item does not exist");
				
		try
		{
			listItemStatement.setBoolean(1, true);						// set isListed (on Market) to true
			listItemStatement.setInt(2, itemID);						// for item ID
			listItemStatement.setString(3, client.getName());			// owned by client
			int noOfAffectedRows = listItemStatement.executeUpdate();
			System.out.println("data inserted in " + noOfAffectedRows + " row(s).");
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public void buy(int itemID, Client buyer) throws RemoteException, RejectedException
	{
		isAuthentic(buyer);
		if (itemID <= 0) throw new RejectedException("Item does not exist");
		
		try {
			getItemOwnerStatement.setInt(1, itemID);
			ResultSet rs = getItemOwnerStatement.executeQuery();
			String seller;
			float price;
						
			if (rs.next())
			{
				seller = rs.getString("ownerUsername");
				price = rs.getFloat("price");
			}
			else {
				throw new RejectedException("Item does not exist");
			}
						
			if (buyer.getAccount().getBalance() < price) throw new RejectedException("Not enough balance");
						
			// Money transfer
			buyer.getAccount().withdraw(price);								// remove $ from buyer
			bank.getAccount(seller).deposit(price);							// add $ to seller
						
			// Item ownership transfer
			changeItemOwnerStatement.setString(1, buyer.getName());
			changeItemOwnerStatement.setInt(2, itemID);
			changeItemOwnerStatement.setString(3, seller);			
			int NoOfAffectedRows = changeItemOwnerStatement.executeUpdate();
			
	        if (NoOfAffectedRows == 0) throw new RejectedException("Item was purchased already");
	        	        
	        // Increment counters
	        updateBoughtStatement.setString(1, buyer.getName());
	        NoOfAffectedRows = updateBoughtStatement.executeUpdate();
	        System.out.println("Number of bought updated, " + NoOfAffectedRows + " row(s) affected");
	        	        
	        updateSoldStatement.setString(1, seller);
	        NoOfAffectedRows = updateSoldStatement.executeUpdate();
	        System.out.println("Number of sold updated, " + NoOfAffectedRows + " row(s) affected");
	    }
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public Item[] getMarketItems(Client client) throws RemoteException, RejectedException
	{
		try
		{
			ResultSet res = getNoMarketItemsStatement.executeQuery();
			res.next();
			int size = res.getInt(1);
			Item[] items = new Item[size];
			
			ResultSet rs = getMarketItemsStatement.executeQuery();
			
			for (int i = 0; i < size; i++)
			{
				rs.next();
				items[i] = new Item(rs.getInt("id"), rs.getString("name"), rs.getFloat("price"), rs.getString("ownerUsername"));
			}
			rs.close();
			return items;
			
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public void login(String username, String password, Client client) throws RemoteException, RejectedException
	{
		boolean isLoggedIn = false;
		
		try {
			if (isAuthentic(client)) isLoggedIn = true;
		} catch (RejectedException e) { }					// catch the thrown "Not logged in"-exception
		
		if (isLoggedIn) throw new RejectedException("Already logged in.");
		
		// User is not logged in
		
	    try
	    {
			getAccountStatement.setString(1, username);
			ResultSet rs = getAccountStatement.executeQuery();
			
			if (!rs.next() || !rs.getString("password").equals(password))
			{
				rs.close();
				throw new RejectedException("Wrong credentials");
			}
			rs.close();
			
			registeredClients.add(client);
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public void logout(Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);
		registeredClients.remove(client);
	}

	@Override
	public void newAccount(String username, String password) throws RemoteException, RejectedException
	{
		if (password.length() < 8) throw new RejectedException("Password not sufficiently long");
		
		try {			
			newAccountStatement.setString(1, username);
			newAccountStatement.setString(2, password);
			newAccountStatement.setInt(3, 0);
			newAccountStatement.setInt(4, 0);
			
			int noOfAffectedRows = newAccountStatement.executeUpdate();
			System.out.println("data inserted in " + noOfAffectedRows + " row(s).");

		} catch (SQLException e)
		{
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public Item[] getInventory(Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);
		
		try
		{
			getNoInventoryStatement.setString(1, client.getName());			
			ResultSet res = getNoInventoryStatement.executeQuery();
			res.next();
			int size = res.getInt(1);
			Item[] items = new Item[size];
						
			getInventoryStatement.setString(1, client.getName());
			ResultSet rs = getInventoryStatement.executeQuery();
						
			for (int i = 0; i < size; i++)
			{
				rs.next();
				items[i] = new Item(rs.getInt("id"), rs.getString("name"), rs.getFloat("price"), rs.getString("ownerUsername"));
			}
			rs.close();
			
			return items;
			
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public void addItem(String name, float price, Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);

		try {
			newItemStatement.setInt(1, 0);					// id is auto incremented in mysql; yet still needs a value
			newItemStatement.setString(2, name);
			newItemStatement.setFloat(3, price);
			newItemStatement.setString(4, client.getName());
			newItemStatement.setBoolean(5, false);
			
			int noOfAffectedRows = newItemStatement.executeUpdate();
			System.out.println("data inserted in " + noOfAffectedRows + " row(s).");
									
			/*if (test == 0)
			{
				throw new RejectedException("Failed to add item to DB");
			}*/
		} catch (SQLException e) {
			System.out.println("Exception");
			e.printStackTrace();
		}
	}

	@Override
	public void removeItem(int itemID, Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);

		try {
			changeItemOwnerStatement.setNull(1, java.sql.Types.VARCHAR);		// owner to null
			changeItemOwnerStatement.setInt(2, itemID);							// for item ID
			changeItemOwnerStatement.setString(3, client.getName());			// for an item the client owns

			int noOfAffectedRows = changeItemOwnerStatement.executeUpdate();
			System.out.println("data updated in " + noOfAffectedRows + " row(s).");
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}
	
	@Override
	public String stats(Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);
		
		String returnString = "";
		
		try {
			getAccountStatement.setString(1, client.getName());
			ResultSet rs = getAccountStatement.executeQuery();
			if (rs.next()) returnString = "#Bought: " + rs.getInt("no_bought") + "; #Sold: " + rs.getInt("no_sold");
			return returnString;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new RejectedException("SQL error");
		}
	}

}
