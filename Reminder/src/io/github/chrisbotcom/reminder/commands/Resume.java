package io.github.chrisbotcom.reminder.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.chrisbotcom.reminder.Reminder;
import io.github.chrisbotcom.reminder.ReminderTask;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Resume {
	public static boolean execute(Reminder plugin, CommandSender sender)
	{
		if (plugin.reminderTask == null
				|| !plugin.getServer().getScheduler().isCurrentlyRunning(plugin.reminderTask.getTaskId())
				|| !plugin.getServer().getScheduler().isQueued(plugin.reminderTask.getTaskId())) {
			plugin.reminderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ReminderTask(plugin), 300L, 300L);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sender.sendMessage("Reminder resumed at " + simpleDateFormat.format(new Date()) + ".");
		} else {
			sender.sendMessage("Reminder is already running.");
		}
		
		return true;
	}
}
