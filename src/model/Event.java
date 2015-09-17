package model;

public class Event implements Comparable<Event>
{
	public String eventID; //unique ID
    public String personID; //personID of associated person
    public Double latitude;
    public Double longitude;
    public String country;
    public String city;
    public String description;
	public String year;
	public String descendant;
	
	@Override
    public int compareTo(Event rhs)
    {
        if(rhs == null)
            return -1;


        return this.year.compareTo(rhs.year);
    }
}
