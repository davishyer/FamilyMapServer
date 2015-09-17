package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;

import com.sun.net.httpserver.*;

import facade.ServerFacade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dataBase.DataBase;
import dataBase.DataImporter;


public class MainServer 
{
	private static final int MAX_WAITING_CONNECTION = 10;
	private static int MAX_GENERATIONS = 5;
	private HttpServer server;
	private static int SERVER_PORT_NUMBER;
	private ServerFacade facade;
	private Gson gson = new Gson();
	DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
	
	
	public static void main(String[] args)		
	{				
		if(args.length < 1)
		{
			System.out.println("More arguments needed. Please specify the port number and optionally"
					+ " the default number of max generations that are allowed on the fill handle."
					+ " eg: SERVER.JAR 8080 5");
		}
		else
		{
			SERVER_PORT_NUMBER = Integer.valueOf(args[0]);		
			
			if(args.length > 1)
			{
				MAX_GENERATIONS = Integer.parseInt(args[1]);
			}
			
			File htmlF = new File("HTML");
			if(!htmlF.exists()) 
			{
				System.out.println("The HTML folder is missing. The server can not run with out it. Shutting down!");
				return;
			}
			File dataF = new File("data");
			if(!dataF.exists()) 
			{
				System.out.println("The data folder is missing. The server can not run with out it. Shutting down!");
				return;
			}
			
			
			new MainServer().run();		
			System.out.println("Server started on port:" + args[0]);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void run()
	{
		try
		{
			server = HttpServer.create(
					new InetSocketAddress(SERVER_PORT_NUMBER), MAX_WAITING_CONNECTION);
			
			String probableAddress = "Couldn't find a likly choice";
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			System.out.println("This machine is attatched to the following IP addresses:");
			while(e.hasMoreElements())
			{
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    Enumeration ee = n.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) ee.nextElement();
			        String prefix = i.getHostAddress().substring(0, 3);
			        if(prefix.contains("128") || prefix.contains("192") || prefix.contains("10"))
			        	probableAddress = i.getHostAddress();

			        System.out.println("\t" + i.getHostAddress());
			    }
			}
			
			System.out.println("\n" + probableAddress + ":" + String.valueOf(SERVER_PORT_NUMBER) + 
								" <---------- Most likely choice to use from android device");
		}
		catch (IOException e)
		{
			System.out.println("Could not create HTTP server: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		server.setExecutor(null);
		
		server.createContext("/fill/", fillHandler);
		server.createContext("/person/", personHandler);
		server.createContext("/event/", eventHandler);
		server.createContext("/user/", usersHandler);

		server.createContext("/", indexHandler);
		
		facade = new ServerFacade();
		
		server.start();
	}
	
	private HttpHandler fillHandler = new HttpHandler()
	{
		@Override 
		public void handle(HttpExchange exchange)
		{
			Calendar cal = Calendar.getInstance();
			System.out.println("Fill Handle was just called at " + dateFormat.format(cal.getTime()));
			URI command=exchange.getRequestURI();
			String theCommand=command.toString();
			
			System.out.println("    Received handle: " + theCommand);

			String[] params=theCommand.split("/");
			
			
			int levels = MAX_GENERATIONS;
			
			if(params.length <= 2)
			{
				sendOutData(makeEMessage("Failed. Please specifiy a user. Example: /fill/[USERNAME]"), exchange);
				return;
			}
			
			if(params.length == 4 && isNumeric(params[3]))
			{
				levels = Integer.parseInt(params[3]);
				if(levels > MAX_GENERATIONS)
				{
					sendOutData("Too many levels. Please pick a number below " + String.valueOf(MAX_GENERATIONS) + " for the number of levels", exchange);
					return;
				}
			}
			else if(params.length >= 4 && isNumeric(params[3]))
			{
				levels = Integer.parseInt(params[3]);
				if(Boolean.valueOf(params[4]))
				{
					DataBase db = new DataBase();
					try
					{
						db.startTransaction();
						db.resetDB(true);
						db.closeTransaction(true);
					}
					catch(SQLException e)
					{
						e.printStackTrace();
						db.closeTransaction(false);
					}
				}
			}
	
			
			String report = new DataImporter().runImport(params[2], levels);							
			sendOutData(report, exchange);
		}
		
	};
	
	private HttpHandler personHandler = new HttpHandler()
	{
		@Override
		public void handle(HttpExchange exchange) throws IOException 
		{
			Calendar cal = Calendar.getInstance();
			System.out.println("Person Handle was just called at " + dateFormat.format(cal.getTime()));
			URI command=exchange.getRequestURI();
			String theCommand=command.toString();
			
			System.out.println("    Received handle: " + theCommand);

			String[] params=theCommand.split("/");
			String token = exchange.getRequestHeaders().getFirst("Authorization");
			
			System.out.println("    Auth Token: " + token);
			
			if(token == null)
				sendOutData(makeEMessage("Missing or Bad access token"),exchange);
			else if (facade.authenticateToken(token))
			{
				User user = facade.getUserByAccessToken(token);
				//see if they want a specific person or all the people
				if(params.length <= 2) //get everyone
				{
					List<Person> persons = facade.getUserNamesFamily(user.username);
					if(persons != null)
					{
						JsonObject json = new JsonObject();
						json.add("data", gson.toJsonTree(persons));
						sendOutData(json, exchange);
					}
					else
						sendOutData(makeEMessage("Error finding ancestors"), exchange);
				}
				else if (params.length == 3)
				{
					Person person = facade.getPersonByID(params[2], user.username);
					if(person == null)
						sendOutData(makeEMessage("No one here by that ID number or incorrect token "
								+ "(the token provided doesn't match the requested persons descendant"), exchange);
					else
						sendOutData(person, exchange);
				}
				else
					sendOutData(makeEMessage("Badly formed url. EG: address:port/person/"), exchange);
			}
			else
				sendOutData(makeEMessage("Not authenticated access token"), exchange);
			
		}
	};
	
	private HttpHandler eventHandler = new HttpHandler()
	{
		@Override
		public void handle(HttpExchange exchange) throws IOException 
		{
			Calendar cal = Calendar.getInstance();
			System.out.println("Event Handle was just called at " + dateFormat.format(cal.getTime()));
			URI command=exchange.getRequestURI();
			String theCommand=command.toString();

			System.out.println("    Handle was: " + theCommand);
			String[] params=theCommand.split("/");
			
			if(params.length < 2)
				sendOutData(makeEMessage("Please specifiy more info (event OR person OR fill OR users)"), exchange);
			else
			{
				String token = exchange.getRequestHeaders().getFirst("Authorization");
				System.out.println("    Auth Token: " + token);
				
				if(token == null)
					sendOutData(makeEMessage("Missing or Bad access token"),exchange);
				
				if(facade.authenticateToken(token))
				{
					User user = facade.getUserByAccessToken(token);
					if(params.length == 3)
					{
						Event event = facade.getEventByID(params[2], user.username);
						if(event == null)
							sendOutData(makeEMessage("No event found by that id number or incorrect token "
								+ "(the token provided doesn't match the requested event's descendant, a.k.a. you are tring to "
								+ "get someone else's families events.)"), exchange);
						else
							sendOutData(event, exchange);
					}
					else if (params.length > 3)
					{
						sendOutData(makeEMessage("Badly formed url. EG: address:port/event/"), exchange);
					}
					else
					{
						List<Event> events = facade.getAllEventsFromFamilyByAccessToken(token);
						if(events == null)
							sendOutData(makeEMessage("Error getting events"), exchange);
						else
						{
							JsonObject json = new JsonObject();
							json.add("data", gson.toJsonTree(events));
							sendOutData(json, exchange);
						}
					}
				}
				else
					sendOutData(makeEMessage("Access token not authenticated or missing"), exchange);
			}
		}
	};
	
	private HttpHandler usersHandler = new HttpHandler()
	{
		@Override
		public void handle(HttpExchange exchange)
		{
			Calendar cal = Calendar.getInstance();
			System.out.println("Users Handle was just called at " + dateFormat.format(cal.getTime()));
			URI command=exchange.getRequestURI();
			String theCommand=command.toString();

			System.out.println("    Command received: " + theCommand);
			String[] params=theCommand.split("/");
			
			if(params.length < 3)
				sendOutData(makeEMessage("More info needed. eg. /users/[LOGIN] OR [REGISTER]"), exchange);
			else
			{
				if(params[2].equals("login"))
				{
					try
					{
						
						InputStream body=exchange.getRequestBody();
						String bodyParts=streamToString(body);
						System.out.println("    Response Body: " + bodyParts);
						
						JsonObject json = gson.fromJson(bodyParts, JsonObject.class);
						
						User user = new User();
						if(json != null && json.has("username"))
							user.username = json.get("username").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing user name in post body"), exchange);
							return;
						}
						
						if(json != null && json.has("password"))
							user.password = json.get("password").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing password in post body"), exchange);
							return;
						}
						
						if(facade.authenticateUser(user))
						{
							user = facade.getUserByUsername(user.username);
							AuthToken token = new AuthToken();
							token.userName = user.username;
							token.Authorization = user.token;
							sendOutData(token, exchange);
						}
						else
						{
							sendOutData(makeEMessage("User name or password are wrong"), exchange);
						}
					}catch(IOException e)
					{
						e.printStackTrace();
						System.out.println("    Error with stream " + e.getMessage());
					}
				}
				else if(params[2].equals("register"))
				{
					try
					{
						InputStream body=exchange.getRequestBody();
						String bodyParts=streamToString(body);
						System.out.println("    Response Body: " + bodyParts);
						
						JsonObject json = gson.fromJson(bodyParts, JsonObject.class);
						
						User user = new User();
						if(json != null && json.has("username"))
							user.username = json.get("username").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing user name in post body"), exchange);
							return;
						}
						
						if(json != null && json.has("password"))
							user.password = json.get("password").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing password in post body"), exchange);
							return;
						}
						
						if(json != null && json.has("email"))
							user.email = json.get("email").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing email in post body"), exchange);
							return;
						}
						
						if(json != null && json.has("firstname"))		
							user.firstName = json.get("firstname").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing first name in post body"), exchange);
							return;
						}
						
						if(json != null && json.has("lastname"))	
							user.lastName = json.get("lastname").getAsString();
						else
						{
							sendOutData(makeEMessage("Missing last name in post body"), exchange);
							return;
						}
						
						if(facade.duplicateNameFound(user.username))
						{
							sendOutData(makeEMessage("User name already taken. Try a different user name"), exchange);
						}
						else if(facade.regesterUser(user))
						{
							user = facade.getUserByUsername(user.username);
							AuthToken token = new AuthToken();
							token.userName = user.username;
							token.Authorization = user.token;
							sendOutData(token, exchange);
						}
						else
							sendOutData(makeEMessage("Error registering the user"), exchange);
							
					}
					catch(IOException e)
					{
						e.printStackTrace();
						System.out.println("Error with stream. " + e.getMessage());
					}
				}
			}
			
		}
	};
	
	private HttpHandler indexHandler = new HttpHandler()
	{
		@Override
		public void handle(HttpExchange exchange) throws IOException 
		{
			Calendar cal = Calendar.getInstance();
			System.out.println("Index Handle was just called at " + dateFormat.format(cal.getTime()));
			Headers head=exchange.getResponseHeaders();
			//head.set("Content-Type", "text/html");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			
			URI command=exchange.getRequestURI();
			String theCommand=command.toString();


			System.out.println("    Command received: " + theCommand);
			String[] params=theCommand.split("/",2);
			
			String path = null;
			if(params.length <= 1 || params[1].equals(""))
			{
				path = "index.html";
				head.set("Content-Type", "text/html");
			}
			else
			{
				path = params[1];
				if(theCommand.split("/")[1].equals("css"))
				{
					head.set("Content-Type", "text/css");
				}
				else
					head.set("Content-Type", "text/html");
			}
			
			OutputStreamWriter sendBack= new OutputStreamWriter(exchange.getResponseBody());

			
			String file = "HTML/" + File.separator + path;
			Scanner scanner = null;
			try{
				
				scanner = new Scanner(new FileReader(file));
				
			}
			catch(IOException e)
			{
				String notFound = "HTML/404.html";
				scanner = new Scanner(new FileReader(notFound));
			}
			
			StringBuilder stringBuilder = new StringBuilder();
			while(scanner.hasNextLine())
			{
				stringBuilder.append(scanner.nextLine() + "\n");
				
			}
			
			scanner.close();
			sendBack.write(stringBuilder.toString());
			
			//sendBack.write("index.html"); 
			sendBack.close();
		}
	};
	
	
	
	private void sendOutData(Object obj, HttpExchange exchange)
	{
		try
		{
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			
			OutputStreamWriter sendBack= new OutputStreamWriter(exchange.getResponseBody(), Charset.forName("UTF-8"));
			String json = gson.toJson(obj);
			sendBack.write(json); 
			sendBack.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("\nError sending out the data " + e.getMessage());
		}
	}
	
	private String streamToString(InputStream in) throws IOException 
	{
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for(String line = br.readLine(); line != null; line = br.readLine())
            out.append(line);
        br.close();
        return out.toString();
    }
	
	private JsonObject makeEMessage(String message)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("emessage",message);
		return obj;
	}
	private static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	
}
