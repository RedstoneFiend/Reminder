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

package io.github.chrisbotcom.reminder.commands;

import io.github.chrisbotcom.reminder.Reminder;
import io.github.chrisbotcom.reminder.ReminderRecord;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Setdefault {
	
	public static boolean execute(Reminder plugin, CommandSender sender, ReminderRecord reminder) throws Exception
	{
		String returnMessage = null;
		String errorMessage = null;

		// Setable defaults
		if (reminder.getDelay() != null)
		{
			plugin.getConfig().set("delay", reminder.getDelay());
			returnMessage = "delay";
		}
		if (reminder.getRate() != null)
		{
			plugin.getConfig().set("rate", reminder.getRate());
			returnMessage = returnMessage == null ? "rate" : ", rate";
		}
		if (reminder.getEcho() != null)
		{
			plugin.getConfig().set("echo", reminder.getEcho());
			returnMessage = returnMessage == null ? "echo" : ", echo";
		}
		if (reminder.getTag() != null)
		{
			plugin.getConfig().set("tag", reminder.getTag());
			returnMessage = returnMessage == null ? "tag" : ", tag";
		}
		
		// Non-setable defaults
		if (reminder.getId() != null)
		{
			errorMessage = "id";
		}
		if (reminder.getMessage() != null)
		{
			errorMessage = errorMessage == null ? "message" : ", message";
		}
		if (reminder.getPlayer() != null)
		{
			errorMessage = errorMessage == null ? "player" : ", player";
		}

		if (returnMessage != null)
		{
			plugin.saveConfig();
			sender.sendMessage(ChatColor.GREEN + "Default value(s) set for " + returnMessage + "." );
		}
		if (errorMessage != null)
			sender.sendMessage(ChatColor.RED + "These parameter were specified but were not set: " + errorMessage);
		
		sender.sendMessage(ChatColor.WHITE + String.format("Current setting(s): tag='%s', delay=%s, rate=%s, echo=%s: ", 
				plugin.getConfig().get("tag"), plugin.getConfig().get("delay"), plugin.getConfig().get("rate"), plugin.getConfig().get("echo")));
		return true;
	}
}
