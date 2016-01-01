package facade;

import java.sql.SQLException;
import java.util.List;

import dataBase.DataBase;
import dataBase.DataImporter;
import model.Event;
import model.Person;
import model.User;

public class ServerFacade 
{
	
	private static int MAX_GENERATIONS;

	
	public ServerFacade(int max_gen) {MAX_GENERATIONS = max_gen;}
	
	
	public List<Person> getUserNamesFamily(String username)
	{
		DataBase db = new DataBase();
		List<Person> persons = null;
		db.startTransaction();
		try 
		{
			//if(db.usersTable.authenticateUser(user))
				persons = db.personTable.getUserNamesFamily(username);
			db.closeTransaction(true);
		} catch (SQLException e) 
		{
			db.closeTransaction(false);
			e.printStackTrace();
		}
		
		return persons;
	}
	
	public List<Event> getAllEventsFromFamilyByUser(String username)
	{
		DataBase db = new DataBase();
		List<Event> events = null;
		db.startTransaction();
		try
		{
			events = db.eventsTable.getAllFamilyEventsByUserName(username);
			db.closeTransaction(true);
		}catch(SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return events;
		
	}
	
	public List<Event> getEventsByPerson(String personID)
	{
		DataBase db = new DataBase();
		List<Event> events = null;
		db.startTransaction();
		try
		{
			events = db.eventsTable.getEventsByPersonID(personID);
			db.closeTransaction(true);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return events;
	}
	
	public Event getEventByID(String eventID, String username)
	{
		DataBase db = new DataBase();
		Event event = null;
		db.startTransaction();
		try
		{
			event = db.eventsTable.getEventByID(eventID);
			if(event == null || !event.descendant.equals(username))
				event = null;
			db.closeTransaction(true);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return event;
	}
	
	public Person getPersonByID(String personID, String username)
	{
		DataBase db = new DataBase();
		Person person = null;
		db.startTransaction();
		try
		{
			person = db.personTable.getPersonByID(personID);
			if(person == null || !person.descendant.equals(username))
				person = null;
			db.closeTransaction(true);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return person;
	}
	
	public boolean regesterUser(User user)
	{
		DataBase db = new DataBase();
		boolean success = false;
		db.startTransaction();
		try
		{
			//Add the registered user to the database, thus getting a person ID
			Person person = new Person();
			person.fillBasedOnUser(user);//here is where the personId is assigned
			user.personId = person.personID;
			
			success = db.usersTable.regesterUser(user); 
			//now that user is in the user table and has a personId
			db.closeTransaction(true);
			
			//transactions are automatically handled by the DataImporter
			success = new DataImporter().runImport(user.username, MAX_GENERATIONS, null).status;

		}
		catch(SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		return success;
	}
	
	public boolean authenticateUser(User user)
	{
		DataBase db = new DataBase();
		boolean success = false;
		db.startTransaction();
		try
		{
			success = db.usersTable.authenticateUser(user);
			db.closeTransaction(true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return success;
	}
	
	public User getUserByUsername(String username) 
	{
		DataBase db = new DataBase();
		User user = null;
		db.startTransaction();
		try
		{
			user = db.usersTable.getUserByUserName(username);
			db.closeTransaction(true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			db.closeTransaction(false);
		}
		
		return user;
	}

	public boolean authenticateToken(String token) 
	{
		DataBase db = new DataBase();
		db.startTransaction();
		boolean result = db.usersTable.authenticateUser(token);
		db.closeTransaction(false);

		
		return result;
	}

	public User getUserByAccessToken(String token) 
	{
		User user = null;
		try
		{
			DataBase db = new DataBase();
			db.startTransaction();
			if(db.usersTable.authenticateUser(token))
			{
				user = db.usersTable.getUserByAccessToken(token);
			}
			db.closeTransaction(false);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return user;
	}

	public List<Event> getAllEventsFromFamilyByAccessToken(String token) 
	{
		if(authenticateToken(token))
		{
			DataBase db = new DataBase();
			try
			{
				db.startTransaction();
				User user = db.usersTable.getUserByAccessToken(token);
				if(user != null)
				{
					return db.eventsTable.getAllFamilyEventsByUserName(user.username);
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				db.closeTransaction(false);
			}
		}

		return null;
	}

	public boolean duplicateNameFound(String username) 
	{
		DataBase db = new DataBase();
		try
		{
			db.startTransaction();
			if(db.usersTable.getUserByUserName(username) == null)
				return false;
			else
				return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			db.closeTransaction(false);
		}
		return true;
	}

}
