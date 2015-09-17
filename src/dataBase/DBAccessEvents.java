package dataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Event;
import model.Person;


public class DBAccessEvents 
{
	private DataBase db;
	
	DBAccessEvents(DataBase db)
	{
		this.db = db;
	}
	
	public void addEvent(Event event) throws SQLException
	{
		PreparedStatement stmt = null;
	    ResultSet keyRS = null;
		String sql = "insert into events (descendant, eventid, personid, latitude, longitude, country, "
				+ "city, description, year)"
						+ " values (?,?, ?, ?, ?, ?, ?, ?, ?)";
		
		try
		{
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, event.descendant);
			stmt.setString(2, event.eventID);
			stmt.setString(3, event.personID);
			stmt.setDouble(4, event.latitude);
			stmt.setDouble(5, event.longitude);
			stmt.setString(6, event.country);
			stmt.setString(7, event.city);
			stmt.setString(8, event.description);
			stmt.setString(9, event.year);

			
			if(stmt.executeUpdate() == 1)
			{

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

	}
	
	public Event getEventByID(String eventID) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Event event = null;
		try
		{
			String sql = "select * from events where events.eventid = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, eventID);
			rs = stmt.executeQuery();
			
			while(rs.next())
			{
				event = new Event();
				event.descendant = rs.getString(1);
				event.eventID = rs.getString(2); //unique ID
			    event.personID = rs.getString(3); //personID of associated person
			    event.latitude = rs.getDouble(4);
			    event.longitude = rs.getDouble(5);
			    event.country = rs.getString(6);
			    event.city = rs.getString(7);
			    event.description = rs.getString(8);
			    event.year = rs.getString(9);
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
		return event;
	}
	
	public List<Event> getEventsByPersonID(String personID) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Event> events = null;
		try
		{
			String sql = "select * from events where events.personid = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, personID);
			rs = stmt.executeQuery();
			events = new ArrayList<Event>();
			
			while(rs.next())
			{
				Event event = new Event();
				event.descendant = rs.getString(1);
				event.eventID = rs.getString(2); //unique ID
			    event.personID = rs.getString(3); //personID of associated person
			    event.latitude = rs.getDouble(4);
			    event.longitude = rs.getDouble(5);
			    event.country = rs.getString(6);
			    event.city = rs.getString(7);
			    event.description = rs.getString(8);
			    event.year = rs.getString(9);
			    
			    events.add(event);
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
		return events;
	}

	public List<Event> getAllFamilyEventsByUserName(String username) throws SQLException 
	{

		List<Person> persons = db.personTable.getUserNamesFamily(username);
		List<Event> events = new ArrayList<Event>();
		for(int i = 0; i < persons.size(); i++)
		{
			events.addAll(db.eventsTable.getEventsByPersonID(persons.get(i).personID));
		}
		return events;
		
	}
}
