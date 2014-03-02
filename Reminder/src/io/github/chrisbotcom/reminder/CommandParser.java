package io.github.chrisbotcom.reminder;

import io.github.chrisbotcom.reminder.commands.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class CommandParser implements CommandExecutor {
	// private MySQL mysql;
	private JavaPlugin plugin;

	public CommandParser(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0) return false;
		try {
			CommandsEnum commandName = null;
			try
			{
				commandName = CommandsEnum.valueOf(args[0]);
			}
			catch (Exception e)
			{
				throw new ReminderException(String.format("Erroneous command %s!", args[0]));
			}
			switch (commandName)
			{
				case add:
					if (args.length >1)
						return Add.execute(plugin, sender, parseTokens(args));
				case list:
					Llist.execute();
					break;
				case delete:
					Delete.execute();
					break;
				case update:
					Update.execute();
					break;
				case reload:
					Reload.execute(plugin);
					break;
				case stop:
					Stop.execute();
					break;
				case resume:
					Resume.execute();
					break;
				case time:
					Time.execute(sender);
					break;
				default:
					break;					
			}
		}
		catch (ReminderException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		catch (Exception e) { 
			plugin.getLogger().warning(e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	// parameters: player, message, start, tag, delay, rate, echo
	private ReminderBean parseTokens(String[] args) throws Exception
	{
		//List<String> tokens = Arrays.asList("start", "tag", "delay", "rate", "echo");
		ReminderBean bean = new ReminderBean();
		TokensEnum token;
		int i = 1;
		
		while (i < args.length)
		{
			// test for token			
			try
			{
				token = TokensEnum.valueOf(args[i]);
				i++;
				if (i == args.length)
					throw new ReminderException(String.format("Missing value for %s!", token));
				if (token.name() != "message") // force tokenParser to parse message
				{
					bean.set(token.name(), args[i]);
					i++;
					continue;
				}
			}
			catch (IllegalArgumentException iae) { }
			
			// test for message
			if (args[i].startsWith("\"") || args[i].startsWith("'"))
			{
				String message = args[i].replace("\"", "'");
				while (!message.endsWith("'"))
				{
					i++;
					if (i == args.length)
						throw new ReminderException("Missing ending speech mark in message!");
					message += " " + args[i].replace("\"", "'");
					if (message.length() > 256)
						throw new ReminderException("Message exceeds 256 characters!");
				}
				bean.set("message", message);
				i++;
				continue;
			}
			else if (args[i].startsWith("+"))
			{
				bean.set("start", args[i]);
				i++;
				continue;
			}
			else 
			{
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
				try // parse date
				{
					simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					simpleDateFormat.parse(args[i]);
					bean.set("start", args[i]);
					i++;
					continue;
				}
				catch (ParseException pe) { }
							
				try //parse time
				{
					simpleDateFormat = new SimpleDateFormat("HH:mm");
					simpleDateFormat.parse(args[i]);
					bean.set("start", args[i]);
					i++;
					continue;
				}
				catch (ParseException pe) { }

				try // parse id
				{
					Integer id = Integer.parseInt(args[i]);
					if (bean.getId() != null)
						throw new ReminderException(String.format("Erroneous parameter '%2$s' in command line at '%1$s %2$s'!", args[i-1], args[i]));
					bean.set("id", id.toString());
					i++;
					continue;
				}
				catch (NumberFormatException nfe)
				{
					// Assume it is player name.
					if (bean.getPlayer() != null)
						throw new ReminderException(String.format("Erroneous parameter '%2$s' in command line at '%1$s %2$s'!", args[i-1], args[i]));
					bean.set("player", args[i]);
					i++;
					continue;
				}
			}
		}
		return bean;
	}
}
