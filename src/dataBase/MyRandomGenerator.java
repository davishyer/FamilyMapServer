package dataBase;

import java.util.Random;

public class MyRandomGenerator 
{
	static Random rand = new Random();
	final static String startOptions = "abcdefghijklmnopqrstuvwxyz012345678901234567890123456789";
	final static char[] options = startOptions.toCharArray();
	public static MyRandomGenerator instance;
	
	public static MyRandomGenerator getInstance()
	{
		if (instance == null)
			instance = new MyRandomGenerator();
		
		return instance;
	}
	
	private MyRandomGenerator()
	{

	}
	
	public void setSeed(int seed)
	{
		rand.setSeed(seed);
	}
	
	public Double nextDouble()
	{
		return rand.nextDouble();
	}
	
	public int nextInt(int input)
	{
		return rand.nextInt(input);
	}
	
	public String randomUUID()
	{
		StringBuilder result = new StringBuilder();
		
		buildString(result,8);
		result.append('-');
		buildString(result,4);
		result.append('-');
		buildString(result,4);
		result.append('-');
		buildString(result,4);
		result.append('-');
		buildString(result,12);
		
		return result.toString();
		
		//8-4-4-4-12;
	}
	
	private StringBuilder buildString(StringBuilder input, int length)
	{
		for(int i = 0; i < length; i++)
		{
			input.append(options[rand.nextInt(options.length)]);
		}
		return input;
	}
}
