package io.github.chrisbotcom.reminder;

import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class CommandParser implements CommandExecutor {
	//private JavaPlugin plugin;
	//private Config config;
	private MySQL mysql;

	public CommandParser(JavaPlugin plugin, Config config, MySQL mysql) {
		//this.plugin = plugin;
		//this.config = config;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		boolean commandHandled = true;
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
					sender.sendMessage("Delete command issued");
					break;
				case update:
					sender.sendMessage("Update command issued");
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
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
					sender.sendMessage("Server time:" + simpleDateFormat.format(new Date()));
					break;
				default:
					sender.sendMessage(ChatColor.RED + "[Reminder] Erroneous command issued");
					commandHandled = false;
					break;
			}
		}
		catch (Exception e) { 
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return commandHandled;
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
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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
			catch (Exception e) {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
	}

	private void commandAdd(CommandSender sender, Command command, String[] args) {
		// reminder add <player> '<message>' <delay-seconds>|<yyyy-mm-dd HH:MM> [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <count>] 
		String player = null, message = null, tag = null, cooldown = null, frequency = null, count = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		Date date = new Date();
		int state = 0;
		int i = 1;
		
		simpleDateFormat.setLenient(false);
		
		try {
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
					case 3: // Delay seconds or date part  (<delay-seconds>|<yyyy-mm-dd HH:MM>)
						simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
						boolean isDate = false;
						try {
							date = simpleDateFormat.parse(args[i]);
							isDate = true;
						} 
						catch (ParseException e) { }
						
						if (!isDate) 
						{
							try {
								long delay = Long.parseLong(args[i]);
								long now = new Date().getTime();
								date = new Date(now + (1000 * delay));
								state++;
								}
							catch (NumberFormatException e) {
									throw new Exception("Invalid date or delay!");
								}
						}
						i++;
						state++;
						break;
					case 4: // Delay time part
						simpleDateFormat = new SimpleDateFormat("hh:mm");
						try {
							date.setTime(simpleDateFormat.parse(args[i]).getTime());
						} 
						catch (ParseException e) {
							throw new Exception("Invalid time!");
						}
						i++;
						state++;
						break;
					case 5: // [tag <tag>] [cooldown <seconds>] [frequency <seconds>] [count <count>] 
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
			String query = String.format("INSERT INTO chrisbotcom_reminder (player, message, activate, tag, cooldown, frequency, count) " + 
					"VALUES ('%s', %s, %s, %s, %s, %s, %s)",
					player, message, date.getTime(), tag == null ? tag : "'" + tag + "'", cooldown, frequency, count);
			//sender.sendMessage(ChatColor.GOLD + query);
			int rowsAdded = mysql.update(query);
			if (rowsAdded != 1)
				throw new Exception("Expected 1 row inserted but got " + rowsAdded + ".");
			else
				sender.sendMessage(ChatColor.GREEN + "Record successfully added.");
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
}
