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
			throw new ReminderException("Cannot specify id when adding a reminder.");
		}
		if (bean.getPlayer() == null)
			throw new ReminderException("Missing required parameter player.");
		if (bean.getMessage() == null)
			throw new ReminderException("Missing required parameter message.");
		if (bean.getStart() == null)
			throw new ReminderException("Missing required parameter start.");
		
		if (bean.getTag() == null)
			bean.setTag(plugin.getConfig().getString("tag"));
		if (bean.getRate() == null)
			bean.setRate(plugin.getConfig().getInt("rate"));
		if (bean.getDelay() == null)
			bean.setDelay(plugin.getConfig().getInt("delay"));
		if (bean.getEcho() == null)
			bean.setEcho(plugin.getConfig().getInt("echo"));
		
		plugin.getDatabase().insert(bean);
		sender.sendMessage(ChatColor.GREEN + "[Reminder] Record added.");
		return true;
	}
}
