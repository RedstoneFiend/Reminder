package io.github.chrisbotcom.reminder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class CommandParser implements CommandExecutor {
	private MySQL mysql;

	public CommandParser(JavaPlugin plugin, Config config, MySQL mysql) {
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		boolean commandHandled = args.length > 1;
		try {
			Commands commandName = Commands.lookup(args[0]);
			switch (commandName)
			{
				case add:
					commandAdd(sender, command, args);
					break;
				case list:
					commandList(sender, command, args);
					break;
				case delete:
					commandDelete(sender, command, args);
					break;
				case update:
					commandUpdate(sender, command, args);
					break;
				case reload:
					sender.sendMessage("Reload command issued");
					break;
				case stop:
					sender.sendMessage("Stop command issued");
					break;
				case resume:
					sender.sendMessage("Resume command issued");
					break;
				case time:
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					sender.sendMessage("Server time:" + simpleDateFormat.format(new Date()));
					commandHandled = true;
					break;
				default:
					sender.sendMessage(ChatColor.RED + "[Reminder] Erroneous command issued");
					break;
			}
		}
		catch (Exception e) { 
			sender.sendMessage(ChatColor.RED + "Reminder Exception:");
			e.printStackTrace();
		}
		
		return commandHandled;
	}

	private void commandDelete(CommandSender sender, Command command, String[] args) throws Exception {
		// <id>|[player <player>] [tag <text>]
		int state = 0;
		int i = 1;
		int id = -1;
		String player = null, tag = null;
		while (i < args.length)
		{
			switch (state) 
			{
				case 0:
					try {
						id = Integer.parseInt(args[i]);
						i = args.length;
					}
					catch (NumberFormatException e) {
						state++;
					}
					break;
				case 1: 
					String variable = args[i];
					i++;
					if (i == args.length)
						throw new Exception("Missing value for variable " + variable + ".");
					if (variable.equalsIgnoreCase("tag"))
						tag = args[i];
					else if (variable.equalsIgnoreCase("player"))
						player = args[i];
					i++;
					break;
			}
		}
		String query = "DELETE FROM chrisbotcom_reminder WHERE";
		if (id != -1)
			query += String.format(" (id = %s)", id);
		else {
			query += String.format("%s%s%s",
					player != null ? " (player = '" + player + "')" : "",
					player != null && tag != null ? " AND" : "",
					tag != null ? " (tag = '" + tag + "')" : "");
		}
		
		//sender.sendMessage(ChatColor.GOLD + query);
		int rowsAdded = mysql.update(query);
		if (rowsAdded == 0)
			sender.sendMessage(ChatColor.YELLOW + "No rows were deleted.");
		else
			sender.sendMessage(ChatColor.GREEN + "Record(s) successfully deleted.");
	}

	private void commandList(CommandSender sender, Command command, String[] args) {
		// reminder list [<player>] [tag <text>]
		String query = "SELECT * FROM chrisbotcom_reminder";
		boolean syntaxError = false;
		
		if (args.length > 1)
		{
			query += " WHERE";
			
			if (args.length == 2 || args.length == 4)
				query += String.format(" (player = '%s')", args[1]);
			if (args.length == 3 && args[1].equalsIgnoreCase("tag")) 
			{
				if (args[2].equalsIgnoreCase("null"))
					query += " (tag is NULL)";
				else
					query += " (tag = '%s')";
			}
			else if (args.length == 4 && args[2].equalsIgnoreCase("tag"))
			{	
				if (args[3].equalsIgnoreCase("null"))
					query += " AND (tag is NULL)";
				else
					query += " AND (tag = '%s')";
			}
			else
				if (args.length != 2)
					syntaxError = true;
		}
		//sender.sendMessage(query);
		if (syntaxError)
			sender.sendMessage(ChatColor.RED + "Syntax error!");
		else
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String formatter = "%6s | %-16s | %16s | %-16s | %8s | %9s | %6s\nMessage: %s";

			sender.sendMessage(String.format(ChatColor.BOLD + formatter, "id", "player", "activate", "tag", "cooldown", "frequency", "count", ""));
			ResultSet resultSet = mysql.query(query);
			int recordCount = 0;
			try {
				while (resultSet.next()) {
					recordCount++;
					int id = resultSet.getInt("id");
				    String player = resultSet.getString("player");
				    String activate = simpleDateFormat.format(resultSet.getBigDecimal("activate"));
				    String tag = resultSet.getString("tag");
				    int cooldown = resultSet.getInt("cooldown");
				    int frequency = resultSet.getInt("frequency");
				    int count = resultSet.getInt("count");
				    String message = resultSet.getString("message");

					sender.sendMessage(String.format(formatter, id, player, activate, tag, cooldown, frequency, count, message));
				}
				sender.sendMessage(String.format("%s record(s) returned.", recordCount));
			} 
			catch (SQLException e) {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
	}

	private void commandAdd(CommandSender sender, Command command, String[] args) throws Exception {
		// reminder add <player> '<message>' <delay-seconds>|<yyyy-mm-dd HH:mm> [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <count>] 
		String player = null, message = null, tag = null, activate = null, cooldown = null, frequency = null, count = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		int state = 0;
		int i = 1;
		
		simpleDateFormat.setLenient(false);
		
		while (i < args.length)
		{
			switch (state)
			{
				case 0: // Player name
					player = args[i];
					i++;
					state++;
					break;
				case 1: // Start of message
					if (args[i].startsWith("\"") || args[i].startsWith("'"))
						message = args[i].replace("\"", "'");
					else
						throw new Exception("Expected message!");
					
					if (message.endsWith("'"))
						state++;
					i++;
					state++;
					break;
				case 2: // End of message
					if (args[i].endsWith("\"") || args[i].endsWith("'"))
					{
						message += args[i].replace("\"", "'");
						state++;
					}
					else
						message += " " + args[i];
					if (message.length() > 256)
						throw new Exception("Message too long. 256 characters max.");
					i++;
					break;
				case 3: // Delay seconds or date part  (<delay-seconds>|<yyyy-mm-dd HH:mm>)
					simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					try {
						simpleDateFormat.parse(args[i]);
						activate = args[i];
					} 
					catch (ParseException e) { }
					
					if (activate == null) 
					{
						try {
							long delay = Long.parseLong(args[i]);
							long now = new Date().getTime();
							simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							activate = simpleDateFormat.format(now + (1000 * delay));
							i++;
						}
						catch (NumberFormatException e) {
								throw new Exception("Invalid date or delay!");
						}
					}
					else
					{
						i++;
						if (i < args.length)
						{
							simpleDateFormat = new SimpleDateFormat("HH:mm");
							try {
								simpleDateFormat.parse(args[i]);
								activate += " " + args[i];
								i++;
							} 
							catch (ParseException e) {
								throw new Exception("Invalid time!");
							}
						}
						else
							activate += " 00:00";
					}
					state++;
					break;
				case 4: // [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <count>] 
					String variable = args[i];
					i++;
					if (i == args.length)
						throw new Exception("Missing value for variable " + variable + ".");
					if (variable.equalsIgnoreCase("tag"))
						tag = args[i];
					else if (variable.equalsIgnoreCase("cooldown"))
						cooldown = args[i];
					else if (variable.equalsIgnoreCase("frequency"))
						frequency = args[i];
					else if (variable.equalsIgnoreCase("count"))
						count = args[i];
					i++;
			}
		}
		
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String query = String.format("INSERT INTO chrisbotcom_reminder (player, message, activate, tag, cooldown, frequency, count) " + 
				"VALUES ('%s', %s, %s, %s, %s, %s, %s)",
				player, message, simpleDateFormat.parse(activate).getTime(), tag == null ? tag : "'" + tag + "'", cooldown, frequency, count);
		//sender.sendMessage(ChatColor.GOLD + query);
		int rowsAdded = mysql.update(query);
		if (rowsAdded != 1)
			throw new Exception("Expected 1 row inserted but got " + rowsAdded + ".");
		else
			sender.sendMessage(ChatColor.GREEN + "Record successfully added.");
	}

	private void commandUpdate(CommandSender sender, Command command, String[] args) throws Exception {
		// reminder update <id>|<player> <tag> ['<message>'] [delay <seconds>|activate <yyyy-mm-dd HH:mm>] 
		//     [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <seconds>] 
		String player = null, message = null, tag = null, newtag = null, activate = null, cooldown = null, frequency = null, count = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		int state = 0;
		int id = -1;
		int i = 1;
		
		simpleDateFormat.setLenient(false);
		
		while (i < args.length)
		{
			switch (state)
			{
				case 0: // Test for id or Player name
					try {
						id = Integer.parseInt(args[i]);
						i++;
						state = 2;
					}
					catch (NumberFormatException e) {
						state++;
					}
					break;
				case 1: // player and tag
					player = args[i];
					i++;
					if (i == args.length)
						throw new Exception("Expected tag.");
					tag = args[i];
					i++;
					state++;
					break;
				case 2: // ['<message>'] [delay <seconds>|activate <yyyy-mm-dd HH:mm>] 
					    // [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <count>] 
					String variable = args[i];
					i++;
					if (i == args.length)
						throw new Exception("Missing value for variable " + variable + ".");
					if (variable.equalsIgnoreCase("tag"))
					{
						if (args[i].equalsIgnoreCase("null"))
							newtag = "tag = null";
						else
							newtag = String.format("tag = '%s'", args[i]);
						i++;
					}
					else if (variable.equalsIgnoreCase("cooldown"))
					{
						cooldown = String.format("cooldown = %s", args[i]);
						i++;
					}
					else if (variable.equalsIgnoreCase("frequency"))
					{
						frequency = String.format("frequency = %s", args[i]);
						i++;
					}
					else if (variable.equalsIgnoreCase("count"))
					{
						count = String.format("count = %s", args[i]);
						i++;
					}
					else if (variable.equalsIgnoreCase("delay"))
					{
						simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
						try {
							simpleDateFormat.parse(args[i]);
							activate = args[i];
						} 
						catch (ParseException e) { }
						
						if (activate == null) 
						{
							try {
								long delay = Long.parseLong(args[i]);
								long now = new Date().getTime();
								simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
								activate = simpleDateFormat.format(now + (1000 * delay));
								i++;
							}
							catch (NumberFormatException e) {
									throw new Exception("Invalid date or delay!");
							}
						}
						else
						{
							i++;
							if (i < args.length)
							{
								simpleDateFormat = new SimpleDateFormat("HH:mm");
								try {
									simpleDateFormat.parse(args[i]);
									activate += " " + args[i];
									i++;
								} 
								catch (ParseException e) {
									throw new Exception("Invalid time!");
								}
							}
							else
								activate += " 00:00";
						}
						i++;
					}
					else if (variable.startsWith("\"") || variable.startsWith("'"))
					{
						message = variable.replace("\"", "'");
						
						if (!message.endsWith("'"))
						{
							while (!args[i].endsWith("\"") && !args[i].endsWith("'"))
							{
								message += " " + args[i];
								if (message.length() > 256)
									throw new Exception("Message too long. 256 characters max.");
								i++;
							}
							message += args[i].replace("\"", "'");
							if (message.length() > 256)
								throw new Exception("Message too long. 256 characters max.");
							i++;
						}
					}
					else
					{
						// bad tag
						throw new Exception("Erroneous tag encountered.");
					}
			}
		}
		// reminder update <id>|<player> <tag> ['<message>'] [delay <seconds>|activate <yyyy-mm-dd HH:mm>] 
		//     [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <seconds>] 
		String setClause = "";
		String whereClause = "";
		String queryFormat = "UPDATE chrisbotcom_reminder SET %s WHERE %s";
		String query = null;
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		String[] parameters = {message, newtag, cooldown, frequency, count};
		for (String item : parameters) 
		{
		    if (item != null)
		    {
		    	setClause += String.format("%s%s", setClause.length() > 0 ? ", " : "", item);
		   	}		    	
		}
		if (activate != null)
		{
			setClause += String.format("%sactivate = %s", setClause.length() > 0 ? ", " : "", simpleDateFormat.parse(activate).getTime());
		}
		if (id == -1) // player and/or tag
		{
			whereClause = String.format("%s%s%s", 
					player == null ? "": "(player = '" + player + "')",
					(player == null) && (tag == null) ? "" : " AND ",
					tag == null ? "": 
						tag.equalsIgnoreCase("null") ? "(tag IS NULL)" : "(tag = '" + tag + "')" );
		}
		else // id
		{
			whereClause = String.format("(id = %s)", id);
		}
		query = String.format(queryFormat, setClause, whereClause);
		sender.sendMessage(ChatColor.GOLD + query);
		int rowsUpdated = mysql.update(query);
		if (rowsUpdated == 0)
		{
			sender.sendMessage(query);
			throw new Exception("No records updated.");
		}
		else
			sender.sendMessage(ChatColor.GREEN + String.format("%s record(s) successfully updated.", rowsUpdated));
	}
}
