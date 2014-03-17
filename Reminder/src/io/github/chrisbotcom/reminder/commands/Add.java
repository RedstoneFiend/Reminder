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

import java.sql.PreparedStatement;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import io.github.chrisbotcom.reminder.Reminder;
import io.github.chrisbotcom.reminder.ReminderException;
import io.github.chrisbotcom.reminder.ReminderRecord;

// Required parameters: player, message, start
// Optional parameters: tag, delay, rate, echo
// reminder add [player] <player> [message] '<message>' [start] <start-seconds>|<yyyy-mm-dd HH:mm> 
//          [tag <tag>] [delay <seconds>] [rate <seconds>] [echo <count>] 

public class Add {
	
	public static boolean execute(Reminder plugin, CommandSender sender, ReminderRecord reminder) throws Exception 
	{
		if (reminder.getId() != null)
			throw new ReminderException("Cannot specify id when adding a reminder.");

		if (reminder.getPlayer() == null)
			throw new ReminderException("Missing required parameter player.");
		if (reminder.getMessage() == null)
			throw new ReminderException("Missing required parameter message.");
		if (reminder.getStart() == null)
			throw new ReminderException("Missing required parameter start.");
		
		String sql = "INSERT INTO reminders (player, message, start, tag, rate, delay, echo) VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement preparedStatement = plugin.db.prepareStatement(sql);
		preparedStatement.setString(1, reminder.getPlayer());
		preparedStatement.setString(2, reminder.getMessage());
		preparedStatement.setLong(3, reminder.getStart());
		preparedStatement.setString(4, reminder.getTag() == null ? plugin.getConfig().getString("tag") : reminder.getTag());
		preparedStatement.setInt(5, reminder.getRate() == null ? plugin.getConfig().getInt("rate") : reminder.getRate());
		preparedStatement.setInt(6, reminder.getDelay() == null ? plugin.getConfig().getInt("delay") : reminder.getDelay());
		preparedStatement.setInt(7, reminder.getEcho() == null ? plugin.getConfig().getInt("echo") : reminder.getEcho());

		int rows = preparedStatement.executeUpdate();
		
		if (rows == 1)
			sender.sendMessage(ChatColor.GREEN + "Record added.");
		else
			sender.sendMessage(ChatColor.RED + String.format("Rows returned = ", rows));
		
		return true;
	}
}
