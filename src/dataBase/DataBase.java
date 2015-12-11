package dataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBase 
{
	
	public DBAccessPersons personTable = new DBAccessPersons(this);
	public DBAccessEvents eventsTable = new DBAccessEvents(this);
	public DBAccessUsers usersTable = new DBAccessUsers(this);
	
	Connection connection;
	
	public DataBase()
	{
		loadDriver();
	}
	/**
	 * Load the driver to talk to the database
	 */
	public void loadDriver()
	{
		try
		{
			final String driver = "org.sqlite.JDBC";
			Class.forName(driver);
		}
		catch (ClassNotFoundException e)
		{
			System.out.print("Class Not found error\n");
		}
	}
	/**
	 * Open a connection with the data base
	 */
	public void openConnection()
	{
		//String dbName = "database.sqlite";
		File directory = new File("db");
		if(!directory.exists())
		{
			try
			{
				directory.mkdirs();
			}
			catch(SecurityException se)
			{
				System.out.println("Error creating the folder for the DB files! The server can not work correctly with out this!");
				return;
			}
		}
		
		String dbName = "db" + File.separator + "database.sqlite";
		String connectionURL = "jdbc:sqlite:" + dbName;
		connection = null;
		
		try
		{
			connection = DriverManager.getConnection(connectionURL);
			createIfNotExsit();
		}
		catch(SQLException e)
		{
			System.out.print("SQL error\n");
		}
		return;
		
		
	}

	public void startTransaction()
	{
		openConnection();
		try 
		{
			connection.setAutoCommit(false);
		} 
		catch (SQLException e)
		{
			System.out.print("turn off auto commit error");
			e.printStackTrace();
		}
	}
	
	public void closeTransaction(boolean commit) 
	{
		try
		{
			if(commit)
			{
				connection.commit();
			}
			else
			{
				connection.rollback();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.print("Close transaction commit error\n");
		}
		finally
		{
			try 
			{
				connection.close();
			} 
			catch (SQLException e) {
				System.out.println("Cant close connection");
				e.printStackTrace();
			}
		}
		connection = null;
	}
	
	public void createIfNotExsit() throws SQLException
	{
		String sql = 
				"CREATE TABLE IF NOT EXISTS EVENTS"+
				"("+
				"descendant varchar(64),"+
				"eventid varchar(64) primary key,"+
				"personid varchar(64),"+
				"latitude real,"+
				"longitude real,"+
				"country varchar(25),"+
				"city varchar(25),"+
				"description varchar(50),"+
				"year varchar(5)" +
				");";

		String sql2 = "CREATE TABLE IF NOT EXISTS PERSONS"+
				"("+
				"descendant varchar(64),"+
				"personid varchar(64) primary key,"+
				"firstName varchar(64),"+
				"lastName varchar(64),"+
				"gender varchar(64),"+
				"father varchar(64),"+
				"mother varchar(64),"+
				"spouse varchar(64)"+
				");";
				
			
		String sql3 = "CREATE TABLE IF NOT EXISTS USERS"+
				"("+
				"username varchar(64) primary key,"+
				"password varchar(64),"+
				"email varchar(64),"+
				"firstName varchar(64),"+
				"lastName varchar(64),"+
				"token varchar(64),"+
				"gender varchar(10),"+
				"personId varchar(64)"+
				");";

			PreparedStatement stmt5 = this.connection.prepareStatement(sql3);
			stmt5.executeUpdate();	
			PreparedStatement stmt1 = this.connection.prepareStatement(sql);
			stmt1.executeUpdate();
			PreparedStatement stmt3 = this.connection.prepareStatement(sql2);
			stmt3.executeUpdate();
			
	}
	
	public void resetDB(boolean dropUserTable) throws SQLException
	{
		String sql = "DROP TABLE IF EXISTS events; ";
		String sql4 = "DROP TABLE IF EXISTS users; ";
		String sql2 = "DROP TABLE IF EXISTS persons; ";
		
		String sql1 = 
				"CREATE TABLE EVENTS"+
				"("+
				"descendant varchar(64),"+
				"eventid varchar(64) primary key,"+
				"personid varchar(64),"+
				"latitude real,"+
				"longitude real,"+
				"country varchar(25),"+
				"city varchar(25),"+
				"description varchar(50),"+
				"year varchar(5)" +
				");";

		String sql3 = "CREATE TABLE PERSONS"+
				"("+
				"descendant varchar(64),"+
				"personid varchar(64) primary key,"+
				"firstName varchar(64),"+
				"lastName varchar(64),"+
				"gender varchar(64),"+
				"father varchar(64),"+
				"mother varchar(64),"+
				"spouse varchar(64)"+
				");";
				
			
		String sql5 = "CREATE TABLE USERS"+
				"("+
				"username varchar(64) primary key,"+
				"password varchar(64),"+
				"email varchar(64),"+
				"firstName varchar(64),"+
				"lastName varchar(64),"+
				"token varchar(64),"+
				"gender varchar(10),"+
				"personId varchar(64)"+
				");";

		if(dropUserTable)
		{
			PreparedStatement stmt4 = this.connection.prepareStatement(sql4);
			stmt4.executeUpdate();
			
			PreparedStatement stmt5 = this.connection.prepareStatement(sql5);
			stmt5.executeUpdate();
		}
		
		
			PreparedStatement stmt = this.connection.prepareStatement(sql);
			stmt.executeUpdate();
			PreparedStatement stmt2 = this.connection.prepareStatement(sql2);
			stmt2.executeUpdate();
			
			PreparedStatement stmt1 = this.connection.prepareStatement(sql1);
			stmt1.executeUpdate();
			PreparedStatement stmt3 = this.connection.prepareStatement(sql3);
			stmt3.executeUpdate();
			
	}
	public void fillReset(String username) 
	{
		try
		{
			String personTable = "Delete from persons where descendant = ?";
			String eventTable = "Delete from events where descendant = ?";
			PreparedStatement stmt = this.connection.prepareStatement(personTable);
			PreparedStatement stmt2 = this.connection.prepareStatement(eventTable);
			
			stmt.setString(1, username);
			stmt2.setString(1, username);
			
			stmt.execute();
			stmt2.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			
		}
				
	}
}
