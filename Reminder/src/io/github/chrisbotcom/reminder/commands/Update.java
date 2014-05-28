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

import io.github.chrisbotcom.reminder.Reminder;
import io.github.chrisbotcom.reminder.ReminderException;
import io.github.chrisbotcom.reminder.ReminderRecord;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Update {

    public static boolean execute(Reminder plugin, CommandSender sender, ReminderRecord reminder) throws Exception {
        if ((reminder.getId() == null) && (reminder.getPlayer() == null) && (reminder.getTag() == null)) {
            throw new ReminderException("Must supply either id or player and/or tag.");
        }

        if ((reminder.getId() != null) && ((reminder.getPlayer() != null) && (reminder.getTag() != null))) {
            throw new ReminderException("Must supply either id or player and/or tag.");
        }

        List<String> setClauses = new ArrayList<String>();

		// "rate", "echo", 
        // TODO Consider making <player> and <tag> updatable.
        if (reminder.getId() != null) {
            if (reminder.getPlayer() != null) {
                setClauses.add(String.format("player = '%s'", reminder.getPlayer()));
            }
            if (reminder.getTag() != null) {
                setClauses.add(String.format("tag = '%s'", reminder.getTag()));
            }
        }

        if (reminder.getStart() != null) {
            setClauses.add(String.format("start = %s", reminder.getStart()));
        }

        if (reminder.getMessage() != null) {
            setClauses.add(String.format("message = '%s'", reminder.getMessage()));
        }

        if (reminder.getDelay() != null) {
            setClauses.add(String.format("delay = %s", reminder.getDelay()));
        }

        if (reminder.getRate() != null) {
            setClauses.add(String.format("rate = %s", reminder.getRate()));
        }

        if (reminder.getEcho() != null) {
            setClauses.add(String.format("echo = %s", reminder.getEcho()));
        }

        String sql = String.format("UPDATE reminders SET %s WHERE (? IN(id, 0)) AND (? IN(player, 'EMPTY')) AND (? IN(tag, 'EMPTY'))", StringUtils.join(setClauses, ", "));
        PreparedStatement preparedStatement = plugin.db.prepareStatement(sql);
        preparedStatement.setLong(1, reminder.getId() == null ? 0 : reminder.getId());
        preparedStatement.setString(2, reminder.getId() != null || reminder.getPlayer() == null ? "EMPTY" : reminder.getPlayer());
        preparedStatement.setString(3, reminder.getId() != null || reminder.getTag() == null ? "EMPTY" : reminder.getTag());

        int rows = preparedStatement.executeUpdate();

        //sender.sendMessage(ChatColor.BLUE + String.format("%s", preparedStatement));
        sender.sendMessage(ChatColor.GREEN + String.format("%s record(s) updated.", rows));

        return true;
    }
}
