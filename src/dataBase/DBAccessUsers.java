package dataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import model.User;

public class DBAccessUsers 
{
	private DataBase db;
	private int AUTH_TOKEN_LENGTH = 15;
	
	DBAccessUsers(DataBase db)
	{
		this.db = db;
	}
	
	public boolean regesterUser(User user) throws SQLException
	{
		PreparedStatement stmt = null;
	    ResultSet keyRS = null;
		String sql = "insert into users (username, password, email, firstName, "
				+ "lastName, token) values (?, ?, ?, ?, ?, ?)";
		boolean success = false;
		try
		{			
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, user.username);
			stmt.setString(2, user.password);
			stmt.setString(3, user.email);
			stmt.setString(4, user.firstName);
			stmt.setString(5, user.lastName);
			stmt.setString(6, makeToken());

			
			if(stmt.executeUpdate() == 1)
			{
				success = true;
				updateUserToken(user.username);
			}
			else
			{
				throw new SQLException();
			}
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			if(stmt != null)
				stmt.close();
			if (keyRS != null) 
				keyRS.close();
		}
		
		return success;
	}
	
	public boolean authenticateUser(User user) throws SQLException
	{

		User dbUser = getUserByUserName(user.username);
		if(dbUser == null)
			return false;
		
		if(user.password.equals(dbUser.password))
		{
			//update the user in the DB with a new token
			updateUserToken(user.username);
			return true;
		}

		return false;

	}
	
	public boolean authenticateUser(String accessToken)
	{
		try 
		{
			return getUserByAccessToken(accessToken) != null;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return false;
	}

	private void updateUserToken(String userName)
	{
		PreparedStatement stmt = null;
		try
		{
			String sql = "Update users set token = ? where users.username=?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, makeToken());
			stmt.setString(2, userName);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(stmt != null)
				stmt = null;
		}
	}
	
	public User getUserByUserName(String username) throws SQLException 
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User readUser = null;	
		try
		{
			String sql = "select * from users where users.username = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();
			
			while(rs.next())
			{
				readUser = new User();
				readUser.username = rs.getString(1);
				readUser.password = rs.getString(2);
				readUser.email = rs.getString(3);
				readUser.firstName = rs.getString(4);
				readUser.lastName = rs.getString(5);
				readUser.token = rs.getString(6);
			}					
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			if(stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return readUser;
	}
	public User getUserByAccessToken(String token) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User readUser = null;	
		try
		{
			String sql = "select * from users where users.token = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, token);
			rs = stmt.executeQuery();
			
			while(rs.next())
			{
				readUser = new User();
				readUser.username = rs.getString(1);
				readUser.password = rs.getString(2);
				readUser.email = rs.getString(3);
				readUser.firstName = rs.getString(4);
				readUser.lastName = rs.getString(5);
				readUser.token = rs.getString(6);
			}					
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			if(stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return readUser;
	}
	
	private String makeToken()
	{
		return UUID.randomUUID().toString().substring(0, AUTH_TOKEN_LENGTH);
	}
	
}
