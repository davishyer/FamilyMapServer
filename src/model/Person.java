package model;


import dataBase.MyRandomGenerator;

public class Person 
{
	public String descendant; //user name of associated descendant
    public String personID; //unique id
    public String firstName;
    public String lastName;
    public String gender;
    public String father; //personID of father;
    public String mother; //personID of mother;
    public String spouse; //personOID of spouse;
	
    public void fillBasedOnUser(User user) 
    {
    	descendant = user.username; //user name of associated descendant
    	MyRandomGenerator rand = MyRandomGenerator.getInstance();
	    personID = user.personId == null ? rand.randomUUID() : user.personId;
	    firstName = user.firstName;
	    lastName = user.lastName;
		gender = user.gender;
		
	}

}
