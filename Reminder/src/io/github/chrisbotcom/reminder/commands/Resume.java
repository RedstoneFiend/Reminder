/* 
 * Reminder - Reminder plugin for Bukkit
 * Copyright (C) 2014 Chris Courson http://www.github.com/Chrisbotcom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl-3.0.html.
 */

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
