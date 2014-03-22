/*
 * Reminder - Reminder plugin for Bukkit
 * Copyright (C) 2014 Chris Courson http://www.github.com/Chrisbotcom
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package io.github.chrisbotcom.reminder;

import io.github.chrisbotcom.reminder.commands.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class CommandParser implements CommandExecutor {
	
	private Reminder plugin;
	private Pattern pattern;
	
	public CommandParser(Reminder plugin) {
		
		this.plugin = plugin;

		// Compile regex pattern
		String commands = "add|list|delete|del|update|up|reload|stop|resume|start|time|setdefault|setdefaults|set";
		String tags = "tag|delay|rate|echo";
		String cmd = String.format("(?<cmd>^%s)", commands);
		String id = "(?:\\s+)(?<id>\\d+)";
		String tag = String.format("(?:\\s+)(?<tag>%s)(?:\\s+)(?<value>\\S+)", tags);
		String date = "(?:\\s+)(?<date>\\d{1,4}-\\d{1,2}-\\d{1,2})";
		String time = "(?:\\s+)(?<time>\\d{1,2}:\\d{2})";
		String offset = "(?:\\s+)(?<offset>\\+\\d+)";
		String msg = "(?:\\s+)(?:[\\\"\\'])(?<msg>.*?)(?:[\\\"\\'])";
		String player = "(?:\\s+)(?<player>\\w+|\\*)";
		pattern = Pattern.compile(String.format("%s|%s|%s|%s|%s|%s|%s|%s", cmd, tag, date, time, offset, id, msg, player), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (args.length == 0) return false;

		String commandLine = "";
		
		// Concatenate args.
		if (args.length > 0) 
			commandLine = StringUtils.join(args, ' ');
		else
			return false;

	    Matcher matcher = pattern.matcher(commandLine);

		try {
			String commandName = null;
			ReminderRecord reminder = new ReminderRecord();
			
		    // Groups: 1 = cmd, 2 = tag, 3 = value, 4 = date, 5 = time, 6 = offset, 7 = msg, 8 = player
		    while (matcher.find())
		    { 
		    	if (matcher.group("cmd") != null)
		    			commandName = matcher.group("cmd");
		    	
		    	else if (matcher.group("id") != null)
		    		reminder.set("id", matcher.group("id"));
		    	
		    	else if (matcher.group("tag") != null)
		    	{
		    		if (reminder.get(matcher.group("tag")) != null)
		    			throw new ReminderException(String.format("'%s' is specified more than once!", matcher.group("tag")));
		    		else
		    			reminder.set(matcher.group("tag"), matcher.group("value"));
		    	}
		    	
		    	else if (matcher.group("date") != null)
		    		reminder.set("start", matcher.group("date"));

		    	else if (matcher.group("time") != null)
		    		reminder.set("start", matcher.group("time"));

		    	else if (matcher.group("offset") != null)
		    		reminder.set("start", matcher.group("offset"));

		    	else if (matcher.group("msg") != null)
		    		if (reminder.getMessage() != null)
		    			throw new ReminderException("Message is specified more than once!");
		    		else
		    			reminder.setMessage(matcher.group("msg"));
		    
			    else if (matcher.group("player") != null)
		    		if (reminder.getPlayer() != null)
		    			if (reminder.getTag() == null)
		    				reminder.setTag(matcher.group("player"));
		    			else
		    				throw new ReminderException("Player is specified more than once!");
		    		else
		    			reminder.setPlayer(matcher.group("player"));
		    }
			
		    if (commandName == null)
		    	throw new ReminderException(String.format("Command '%s' not found!", commandName));
		    
			switch (commandName.toLowerCase())
			{
				case "add":
					return Add.execute(plugin, sender, reminder);
				case "list":
					return Llist.execute(plugin, sender, reminder);
				case "delete":
				case "del":
					return Delete.execute(plugin, sender, reminder);
				case "update":
				case "up":
					return Update.execute(plugin, sender, reminder);
				case "reload":
					return Reload.execute(plugin);
				case "stop":
					return Stop.execute(plugin, sender);
				case "resume":
				case "start":
					return Resume.execute(plugin, sender);
				case "time":
					return Time.execute(sender);
				case "setdefault":
				case "setdefaults":
				case "set":
					return Setdefault.execute(plugin, sender, reminder);
				default:
					return false;					
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
}
