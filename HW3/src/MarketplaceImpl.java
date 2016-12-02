import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("serial")
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace
{
	public final String marketplaceName;
	
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
    private PreparedStatement removeItemStatement;
    private PreparedStatement changeItemOwnerStatement;
    private PreparedStatement updateSoldStatement;
    private PreparedStatement updateBoughtStatement;
    private PreparedStatement getItemOwnerStatement;


    private boolean isConnInitialized;
	
	protected MarketplaceImpl(String marketplaceName) throws RemoteException
	{
		super();
		
		this.marketplaceName = marketplaceName;
		connect();
	}
	
	private void connect()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
	        conn = DriverManager.getConnection("jdbc:mysql://localhost:" + Marketplace.DEFAULT_PORT + "/" +
					Marketplace.DEFAULT_DB, "root", "javajava");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createTable() throws Exception
	{
        ResultSet result = conn.getMetaData().getTables(null, null, "ACCOUNT", null);
        if (result.next()) dropTable();

        statement.executeUpdate("CREATE TABLE accounts (" + 
        						"username VARCHAR(255) NOT NULL PRIMARY KEY, " +
        						"password VARCHAR(255), " +
        						"no_sold INT, " +
        						"no_bought INT)");
       
        statement.executeUpdate("CREATE TABLE items (" +
        						"id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
        						"name VARCHAR(255), " +
        						"price FLOAT, " +
        						"ownerUsername VARCHAR(255), " +
        						"isListed BOOLEAN)");

        isConnInitialized = true;
        newAccountStatement = 		conn.prepareStatement("INSERT INTO accounts VALUES (?, ?, ?, ?)");
        getAccountStatement = 		conn.prepareStatement("SELECT * from accounts WHERE username=?");
        newItemStatement = 			conn.prepareStatement("INSERT INTO items VALUES (?, ?, ?, ?)");
        listItemStatement = 		conn.prepareStatement("UPDATE items SET isListed=? WHERE id=?");
        getNoMarketItemsStatement =	conn.prepareStatement("SELECT COUNT(*) FROM items WHERE isListed=TRUE");
        getMarketItemsStatement = 	conn.prepareStatement("SELECT * FROM items WHERE isListed=TRUE");
        getNoInventoryStatement =	conn.prepareStatement("SELECT COUNT(*) FROM items WHERE isListed=FALSE AND ownerUsername=?");
        getInventoryStatement =		conn.prepareStatement("SELECT * FROM items WHERE isListed=FALSE AND ownerUsername=?");
        removeItemStatement =		conn.prepareStatement("UPDATE items SET ownerUsername=NULL WHERE id=?");	// TODO rem?
        changeItemOwnerStatement =	conn.prepareStatement("UPDATE items SET ownerUsername=? WHERE id=?");
        updateSoldStatement =		conn.prepareStatement("UPDATE accounts SET no_sold=no_sold+1 WHERE username=?");
        updateBoughtStatement =		conn.prepareStatement("UPDATE accounts SET no_bought=no_bought+1 WHERE username=?");
        getItemOwnerStatement =		conn.prepareStatement("SELECT * FROM items WHERE id=?");

        System.out.println();
        System.out.println("tables created.");
    }
	
    private void dropTable() throws Exception
    {
        int NoOfAffectedRows = statement.executeUpdate("DROP TABLE account");
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
			listItemStatement.setBoolean(1, true);
			listItemStatement.setInt(2, itemID);
			int noOfAffectedRows = listItemStatement.executeUpdate();
			System.out.println("data inserted in " + noOfAffectedRows + " row(s).");
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public void buy(int itemID, Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);
		if (itemID <= 0) throw new RejectedException("Item does not exist");

		
		try {
			getItemOwnerStatement.setInt(1, itemID);
			ResultSet rs = getItemOwnerStatement.executeQuery();
			String seller;
			float price;
			
			if (rs.next())		// TODO make sure next is called everywhere when we fetch
			{
				seller = rs.getString("ownerUsername");
				price = rs.getFloat("price");
			}
			else {
				throw new RejectedException("Item does not exist");
			}
			
			if (client.getAccount().getBalance() < price) throw new RejectedException("Not enough balance");
			
			// Money transfer
			client.getAccount().withdraw(price);							// remove $ from buyer
			//buyingItem.owner.getAccount().deposit(buyingItem.price);		// add $ to seller	TODO: remove money sender

			// Item ownership transfer
			changeItemOwnerStatement.setString(1, client.getName());
			changeItemOwnerStatement.setInt(2, itemID);
			int NoOfAffectedRows = changeItemOwnerStatement.executeUpdate();
	        System.out.println("Ownership updated, " + NoOfAffectedRows + " row(s) affected");
	        
	        if (NoOfAffectedRows == 0) throw new RejectedException("Item does not exist");
	        
	        // Increment counters
	        updateBoughtStatement.setString(1, client.getName());
	        NoOfAffectedRows = updateBoughtStatement.executeUpdate();
	        System.out.println("Number of bought updated, " + NoOfAffectedRows + " row(s) affected");

	        updateSoldStatement.setString(1, seller);
			
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

	@Override
	public Item[] getMarketItems(Client client) throws RemoteException, RejectedException
	{
		try
		{
			int size = getNoMarketItemsStatement.executeQuery().getInt(1);
			Item[] items = new Item[size];

			ResultSet rs = getMarketItemsStatement.executeQuery();
			
			for (int i = 0; i < size; i++)
			{
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
			
			// TODO make sure new account is unique
			
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
			int size = getNoInventoryStatement.executeQuery().getInt(1);
			Item[] items = new Item[size];
			
			getInventoryStatement.setString(1, client.getName());
			ResultSet rs = getInventoryStatement.executeQuery();
			
			for (int i = 0; i < size; i++)
			{
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
			newItemStatement.setString(1, name);
			newItemStatement.setFloat(2, price);
			newItemStatement.setString(3, client.getName());
			newItemStatement.setBoolean(4, false);
			
			ResultSet rs = getAccountStatement.executeQuery();
			
			if (!rs.next())
			{
				rs.close();
				throw new RejectedException("Failed to add item to DB");
			}
			rs.close();

		} catch (SQLException e) {
			throw new RejectedException("Failed to add item to DB");
		}
	}

	@Override
	public void removeItem(int itemID, Client client) throws RemoteException, RejectedException
	{
		isAuthentic(client);

		try {
			removeItemStatement.setInt(itemID, 1);
			int noOfAffectedRows = removeItemStatement.executeUpdate();
			System.out.println("data updated in " + noOfAffectedRows + " row(s).");
		} catch (SQLException e) {
			throw new RejectedException("SQL error");
		}
	}

}
