package dataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Person;


public class DBAccessPersons 
{
	private DataBase db;
		
	DBAccessPersons(DataBase db)
	{
		this.db = db;
	}
	
	public void addPerson(Person person) throws SQLException
	{
		PreparedStatement stmt = null;
	    ResultSet keyRS = null;
		String sql = "insert into persons (descendent, personid, firstName, lastName, gender, "
				+ "father, mother, spouse)"
						+ " values (?, ?, ?, ?, ?, ?, ?, ?)";
		
		if(person.personID == null)
			person.personID = UUID.randomUUID().toString(); //unique id
		
		try
		{
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, person.descendent);
			stmt.setString(2, person.personID);
			stmt.setString(3, person.firstName);
			stmt.setString(4, person.lastName);
			stmt.setString(5, person.gender);
			stmt.setString(6, person.father);
			stmt.setString(7, person.mother);
			stmt.setString(8, person.spouse);
			
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

	
	public Person getPersonByID(String personID) throws SQLException
	{
		if (personID == null)
				return null;
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Person person = null;
		try
		{
			String sql = "select * from persons where persons.personid = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, personID);
			rs = stmt.executeQuery();
			
			while(rs.next())
			{
				person = new Person();
				person.descendent = rs.getString(1); //username of associasted descendent
			    person.personID = rs.getString(2); //unique id
			    person.firstName = rs.getString(3);
			    person.lastName = rs.getString(4);
			    person.gender = rs.getString(5);
			    person.father = rs.getString(6); //personID of father;
			    person.mother = rs.getString(7); //personID of mother;
			    person.spouse = rs.getString(8); //personOID of spouse;

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
		return person;
	}
	
	public List<Person> getUserNamesFamily(String username) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Person> persons = null;
		try
		{
			String sql = "select * from persons where persons.descendent = ?";
			stmt = db.connection.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();
			persons = new ArrayList<Person>();
			
			while(rs.next())
			{
				Person person = new Person();
				person.descendent = rs.getString(1); //username of associasted descendent
			    person.personID = rs.getString(2); //unique id
			    person.firstName = rs.getString(3);
			    person.lastName = rs.getString(4);
			    person.gender = rs.getString(5);
			    person.father = rs.getString(6); //personID of father;
			    person.mother = rs.getString(7); //personID of mother;
			    person.spouse = rs.getString(8); //personOID of spouse;
			    
			    persons.add(person);
			}					
		}
		catch(SQLException e)
		{
			throw e;
		}
		finally
		{
			if(stmt != null)
				stmt.close();
			if (rs != null)
				rs.close();
		}
		return persons;
	}
}
