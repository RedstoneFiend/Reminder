package io.github.chrisbotcom.reminder.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.chrisbotcom.reminder.ReminderBean;
import io.github.chrisbotcom.reminder.ReminderException;

// Required parameters: player, message, start
// Optional parameters: tag, delay, rate, echo
// reminder add [player] <player> [message] '<message>' [start] <start-seconds>|<yyyy-mm-dd HH:mm> 
//          [tag <tag>] [delay <seconds>] [rate <seconds>] [echo <count>] 

public class Add {
	public static boolean execute(JavaPlugin plugin, CommandSender sender, ReminderBean bean) throws Exception 
	{
		if (bean.getId() != null)
		{
			throw new ReminderException("Cannot specify id for update.");
		}
		if (bean.getPlayer() == null)
			throw new ReminderException("Missing required parameter player.");
		if (bean.getMessage() == null)
			throw new ReminderException("Missing required parameter message.");
		if (bean.getStart() == null)
			throw new ReminderException("Missing required parameter start.");
		
		plugin.getDatabase().insert(bean);
		sender.sendMessage(ChatColor.GREEN + "[Reminder] Record added.");
		return true;
	}
}
