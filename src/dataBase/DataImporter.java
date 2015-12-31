package dataBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import model.Event;
import model.Person;
import model.RunImportReturnObj;
import model.User;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataImporter 
{
	Gson gson = new Gson();
	String username;
	JsonArray fNamesArray;
	JsonArray mNamesArray;
	JsonArray sNamesArray;
	JsonArray locationsArray;
	int personsAdded = 0;
	int eventsAdded = 0;
	DataBase db = new DataBase();
	MyRandomGenerator rand;
	
	public RunImportReturnObj runImport(String username, int level, Integer seed)
	{
		rand = MyRandomGenerator.getInstance();
		if(seed == null)
			rand.setSeed((int) System.nanoTime());
		else
			rand.setSeed(seed);
		
		this.username = username;
		
		String fnames = "data" + File.separator + "fnames.json";
		String locations = "data" + File.separator + "locations.json";
		String mnames = "data" + File.separator + "mnames.json";
		String snames = "data" + File.separator + "snames.json";
		
		fNamesArray = readData(fnames);
		mNamesArray = readData(mnames);
		sNamesArray = readData(snames);
		locationsArray = readData(locations);
		
		if(fNamesArray != null && mNamesArray != null && sNamesArray != null
				&& locationsArray != null)
		{
			try
			{
				db.startTransaction();
				db.fillReset(username);
				db.closeTransaction(true);
				
				db.startTransaction();
				setUpSpecialCharacters();
				
				//Make the root of the tree be the user that is logged in
				User user = db.usersTable.getUserByUserName(username);
				if(user == null) //the user isnt registered yet
				{
					db.closeTransaction(false);
					return new RunImportReturnObj("The supplied user is not yet registered. Please register the user first", false);
				}
				Person thePerson = new Person();
				thePerson.fillBasedOnUser(user);
				fillEvents(thePerson, (int)(rand.nextDouble() * 500) + 1500);
				
				//start the normal filling process (this will add the current user's person object to the DB)
				fillTree(thePerson, level);
				db.closeTransaction(true);
				return new RunImportReturnObj("Successfully added " + String.valueOf(personsAdded) + " persons and " +
					String.valueOf(eventsAdded) + " events to the database.", true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				db.closeTransaction(false);
				return new RunImportReturnObj("There was an error loading the DB. Error message: " + e.getMessage(), false);
			}
			
		}
		return new RunImportReturnObj("Failed: One of the data files for importing could not be loaded. Missing/Corrupt?", false);
	}

	
	private Person fillTree(Person child, int levelsToGo) throws SQLException
	{
		if(levelsToGo <= 0) //base case for the recursion
		{
			db.personTable.addPerson(child);
			personsAdded++;
			return null;
		}
		
		levelsToGo--;
		
		int birthYear = (int)(rand.nextDouble() * 500) + 1500;
		Person father = fillPerson(true, birthYear);
		Person mother = fillPerson(false, birthYear);

		if(father != null && mother != null)
		{
			marry(father, mother);
			child.father = father.personID;
			child.mother = mother.personID;
			father = fillTree(father, levelsToGo);
			mother = fillTree(mother, levelsToGo);
		}

		db.personTable.addPerson(child);
		personsAdded++;
		return child;
		
	}
	
	private Person fillPerson(boolean male, int birthYear) throws SQLException
	{
		Person person = new Person();
		person.descendant = username; //username of associated descendant
	    person.personID = rand.randomUUID();
	    
	    if(male && rand.nextDouble() > 0.999)
	    {
	    	if((rand.nextDouble() > 0.5 && !GWA) || ALA)
	    	{
	    		db.eventsTable.addEvent(GWBirth);
	    	    eventsAdded++;
	    	    db.eventsTable.addEvent(GWDeath);
	    	    eventsAdded++;
	    	    GWA = true;
	    	    return GW;
	    	}
	    	else if (!ALA)
	    	{
	    	    db.eventsTable.addEvent(abeBirth);
	    	    eventsAdded++;
	    	    db.eventsTable.addEvent(abeDeath);
	    	    eventsAdded++;
	    	    ALA = true;
	    	    return Abe;
	    	}
	    }
	    
	    if(male)
	    {
	    	person.firstName = (mNamesArray.get((int)(rand.nextDouble() * mNamesArray.size()))).getAsString();
	    	person.gender = "m";
	    }
	    else
	    {
	    	person.firstName = (fNamesArray.get((int)(rand.nextDouble() * fNamesArray.size()))).getAsString();
	    	person.gender = "f";  	
	    }

	    person.lastName = (sNamesArray.get((int)(rand.nextDouble() * sNamesArray.size()))).getAsString();
	    fillEvents(person, birthYear);

    	return person;
	
	}
	
	private void fillEvents(Person person, int birthYearStart) throws SQLException
	{
		int birthYear = ((int)(rand.nextDouble() * 8) + birthYearStart) - 4;
		int deathYear = birthYear + (int)(rand.nextDouble() * 75) + 20;
		
		int christening = rand.nextInt(deathYear - birthYear) + birthYear;
		int baptism = rand.nextInt(deathYear - birthYear) + birthYear;
		int cenus = rand.nextInt(deathYear - birthYear) + birthYear;
		int caughtAToad = rand.nextInt(deathYear - birthYear) + birthYear;
		int didABackFlip = rand.nextInt(deathYear - birthYear) + birthYear;
		
		if(rand.nextDouble() > (Math.abs(2020 - birthYear)/birthYear))
			makeEvent(person, "birth", birthYear);
		if(rand.nextDouble() > (Math.abs(2020 - deathYear)/deathYear))
			makeEvent(person, "death", deathYear);
		
		if(rand.nextDouble() > (Math.abs(2020 - christening)/christening)*4.0)
			makeEvent(person, "christening", christening);
		if(rand.nextDouble() > (Math.abs(2020 - baptism)/baptism)*4.0)
			makeEvent(person, "baptism", baptism);
		if(rand.nextDouble() > (Math.abs(2020 - cenus)/cenus)*4.0)
			makeEvent(person, "census", cenus);
		
		if(rand.nextDouble() > 0.999)
			makeEvent(person, "caught a toad", caughtAToad);
		if(rand.nextDouble() > 0.999)
			makeEvent(person, "did a back flip", didABackFlip);
	    
	}
	
	private void marry(Person father, Person mother) throws SQLException
	{
		List<Event> events = db.eventsTable.getEventsByPersonID(father.personID);
		if(events != null && events.size() > 0)
		{
			Collections.sort(events);
			int marriageYear = Integer.parseInt(events.get(0).year) + (int)(rand.nextDouble() * 5) + 18;
			if(events.get(0).description.contains("death")) //only event is death, and nothing else should come after
			{
				marriageYear =- 30;
			}
			
			
			Event marriage = makeEvent(father, "marriage", marriageYear);
			
			marriage.personID = mother.personID;
			marriage.eventID = rand.randomUUID();
			db.eventsTable.addEvent(marriage);
			eventsAdded++;
			
			father.spouse = mother.personID;
			mother.spouse = father.personID;
		}
		
	}
	private Event makeEvent(Person person, String describe, int year) throws SQLException
	{
		Event event = new Event();
		event.descendant = username;
		event.eventID = rand.randomUUID(); //unique ID
	    event.personID = person.personID; //personID of associated person
	    
	    JsonObject location;
	    do
	    {
		    location = locationsArray.get(
		    		(int)(rand.nextDouble() * (locationsArray.size()-1))).getAsJsonObject();
	    }while(!location.has("longitude") || !location.has("latitude") || !location.has("country")
	    		|| !location.has("city"));
	    
	    event.longitude = location.get("longitude").getAsDouble();
	    event.latitude = location.get("latitude").getAsDouble();
	    event.country = location.get("country").getAsString();
	    event.city = location.get("city").getAsString();
	    event.description = describe;
	    event.year = String.valueOf(year);
	    db.eventsTable.addEvent(event);
	    eventsAdded++;
	    
	    return event;
	}
	
	private JsonArray readData(String fnames)
	{
		try
		{
			InputStreamReader in = new InputStreamReader(new FileInputStream(fnames), "UTF8");
	        BufferedReader reader = new BufferedReader(in);
	        StringBuilder out = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
	        //System.out.println(out.toString());   //Prints the string content read from input stream
	        reader.close();

			
			try
			{
			JsonObject fnamesJson = gson.fromJson(out.toString(), JsonObject.class);
			
			return fnamesJson.getAsJsonArray("data");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	Person Abe;
	Person GW;
	Event abeBirth;
	Event abeDeath;
	Event GWBirth;
	Event GWDeath;
	boolean GWA = false;
	boolean ALA = false;
	private void setUpSpecialCharacters()
	{
		Abe = new Person();
		Abe.descendant = username;
		Abe.father = null;
		Abe.mother = null;
		Abe.gender = "m";
		Abe.lastName = "Lincoln";
		Abe.firstName = "Abe";
		Abe.spouse = null;
		Abe.personID = rand.randomUUID();
		
		abeBirth = new Event();
		abeBirth.city = "Hodgenville";
		abeBirth.country = "USA";
		abeBirth.descendant = username;
		abeBirth.description = "birth";
		abeBirth.eventID = rand.randomUUID();
		abeBirth.personID = Abe.personID;
		abeBirth.latitude = 37.567404;
		abeBirth.longitude = -85.738268;
		abeBirth.year = "1809";
		
		abeDeath = new Event();
		abeDeath.city = "DC";
		abeDeath.country = "USA";
		abeDeath.descendant = username;
		abeDeath.description = "death";
		abeDeath.eventID = rand.randomUUID();
		abeDeath.personID = Abe.personID;
		abeDeath.latitude = 38.897083;
		abeDeath.longitude = -77.025332;
		abeDeath.year = "1865";
		
		GW = new Person();
		GW.descendant = username;
		GW.father = null;
		GW.mother = null;
		GW.gender = "m";
		GW.lastName = "Washington";
		GW.firstName = "George";
		GW.spouse = null;
		GW.personID = rand.randomUUID();
		
		GWBirth = new Event();
		GWBirth.city = "Westmoreland";
		GWBirth.country = "USA";
		GWBirth.descendant = username;
		GWBirth.description = "birth";
		GWBirth.eventID = rand.randomUUID();
		GWBirth.personID = GW.personID;
		GWBirth.latitude = 38.184959;
		GWBirth.longitude = -76.920331;
		GWBirth.year = "1732";
		
		GWDeath = new Event();
		GWDeath.city = "Mount Vernon";
		GWDeath.country = "USA";
		GWDeath.descendant = username;
		GWDeath.description = "death";
		GWDeath.eventID = rand.randomUUID();
		GWDeath.personID = GW.personID;
		GWDeath.latitude = 38.708233;
		GWDeath.longitude = -77.086143;
		GWDeath.year = "1799";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
