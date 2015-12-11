package model;

//This is just to wrap the contents of the returning information from running the DataImporter.
//Some methods that call DataImporter want the message back, some want a boolean back.
public class RunImportReturnObj 
{
	public boolean status;
	public String message;
	
	public RunImportReturnObj(String message, boolean status)
	{
		this.status = status;
		this.message = message;
	}
}
