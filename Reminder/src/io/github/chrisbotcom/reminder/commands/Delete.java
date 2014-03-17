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
import io.github.chrisbotcom.reminder.Reminder;
import io.github.chrisbotcom.reminder.ReminderException;
import io.github.chrisbotcom.reminder.ReminderRecord;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Delete {
	
	public static boolean execute(Reminder plugin, CommandSender sender, ReminderRecord reminder) throws Exception
	{
		if ((reminder.getId() == null) && (reminder.getPlayer() == null) && (reminder.getTag() == null))
			throw new ReminderException("Must supply id or player and/or tag.");

		if (reminder.getDelay() != null)
			throw new ReminderException("Cannot specify delay.");
		if (reminder.getEcho() != null)
			throw new ReminderException("Cannot specify echo.");
		if (reminder.getMessage() != null)
			throw new ReminderException("Cannot specify message.");
		if (reminder.getRate() != null)
			throw new ReminderException("Cannot specify rate.");
		if (reminder.getStart() != null)
			throw new ReminderException("Cannot specify start.");
		
		String sql = "DELETE FROM reminders WHERE (? IN(id, 0)) AND (? IN(player, 'EMPTY')) AND (? IN(tag, 'EMPTY'))";
		PreparedStatement preparedStatement = plugin.db.prepareStatement(sql);
		preparedStatement.setLong(1, reminder.getId() == null ? 0 : reminder.getId());
		preparedStatement.setString(2, reminder.getPlayer() == null ? "EMPTY" : reminder.getPlayer());
		preparedStatement.setString(3, reminder.getTag() == null ? "EMPTY" : reminder.getTag());

		int rows = preparedStatement.executeUpdate();
		
		sender.sendMessage(ChatColor.GREEN + String.format("%s record(s) deleted.", rows));
		
		return true;
	}
}
