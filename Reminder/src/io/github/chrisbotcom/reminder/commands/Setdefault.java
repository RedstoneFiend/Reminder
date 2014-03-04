package io.github.chrisbotcom.reminder.commands;

import io.github.chrisbotcom.reminder.ReminderBean;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Setdefault {
	public static boolean execute(JavaPlugin plugin, CommandSender sender, ReminderBean bean) throws Exception
	{
		String returnMessage = null;
		String errorMessage = null;

		// Setable defaults
		if (bean.getDelay() != null)
		{
			plugin.getConfig().set("delay", bean.getDelay());
			returnMessage = "delay";
		}
		if (bean.getRate() != null)
		{
			plugin.getConfig().set("rate", bean.getRate());
			returnMessage = returnMessage == null ? "rate" : ", rate";
		}
		if (bean.getEcho() != null)
		{
			plugin.getConfig().set("echo", bean.getEcho());
			returnMessage = returnMessage == null ? "echo" : ", echo";
		}
		if (bean.getTag() != null)
		{
			plugin.getConfig().set("tag", bean.getTag());
			returnMessage = returnMessage == null ? "tag" : ", tag";
		}
		
		// Non-setable defaults
		if (bean.getId() != null)
		{
			errorMessage = "id";
		}
		if (bean.getMessage() != null)
		{
			errorMessage = errorMessage == null ? "message" : ", message";
		}
		if (bean.getPlayer() != null)
		{
			errorMessage = errorMessage == null ? "player" : ", player";
		}

		if (returnMessage != null)
		{
			plugin.saveConfig();
			sender.sendMessage(ChatColor.GREEN + "[Reminder] Default value(s) set for " + returnMessage + "." );
		}
		if (errorMessage != null)
			sender.sendMessage(ChatColor.RED + "[Reminder] These parameter were specified but were not set: " + errorMessage);
		
		sender.sendMessage(ChatColor.WHITE + String.format("[Reminder] Current setting(s): tag='%s', delay=%s, rate=%s, echo=%s: ", 
				plugin.getConfig().get("tag"), plugin.getConfig().get("delay"), plugin.getConfig().get("rate"), plugin.getConfig().get("echo")));
		return true;
	}
}
