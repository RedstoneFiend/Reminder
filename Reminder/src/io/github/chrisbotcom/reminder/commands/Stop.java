package io.github.chrisbotcom.reminder.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.CommandSender;

import io.github.chrisbotcom.reminder.Reminder;

public class Stop {
	public static boolean execute(Reminder plugin, CommandSender sender)
	{
		if (plugin.reminderTask != null &&
				(plugin.getServer().getScheduler().isCurrentlyRunning(plugin.reminderTask.getTaskId())
				|| plugin.getServer().getScheduler().isQueued(plugin.reminderTask.getTaskId()))) {
			plugin.getServer().getScheduler().cancelTask(plugin.reminderTask.getTaskId());
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sender.sendMessage("Reminder stopped at " + simpleDateFormat.format(new Date()) + ".");
		} else {
			sender.sendMessage("Reminder is already stopped.");
		}
		
		return true;
	}
}
