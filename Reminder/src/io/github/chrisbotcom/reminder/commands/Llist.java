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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Llist {

    public static boolean execute(Reminder plugin, CommandSender sender, ReminderRecord reminder) throws Exception {
        if (reminder.getDelay() != null) {
            throw new ReminderException("Cannot specify delay.");
        }
        if (reminder.getEcho() != null) {
            throw new ReminderException("Cannot specify echo.");
        }
        if (reminder.getMessage() != null) {
            throw new ReminderException("Cannot specify message.");
        }
        if (reminder.getRate() != null) {
            throw new ReminderException("Cannot specify rate.");
        }
        if (reminder.getStart() != null) {
            throw new ReminderException("Cannot specify start.");
        }

        String sql = "SELECT * FROM reminders WHERE (? IN(id, 0)) AND (? IN(player, 'EMPTY')) AND (? IN(tag, 'EMPTY')) LIMIT ?";
        PreparedStatement preparedStatement = plugin.db.prepareStatement(sql);
        preparedStatement.setLong(1, reminder.getId() == null ? 0 : reminder.getId());
        preparedStatement.setString(2, reminder.getPlayer() == null ? "EMPTY" : reminder.getPlayer());
        preparedStatement.setString(3, reminder.getTag() == null ? "EMPTY" : reminder.getTag());
        preparedStatement.setInt(4, plugin.getConfig().getInt("maxRows"));
		//plugin.getLogger().info(preparedStatement.toString());

        ResultSet reminders = preparedStatement.executeQuery();

        String outputMessage = "";
        String formatter = "\n%6s | %-16s | %-16s | %-16s | %-16s | %8s | %9s | %6s\n\tMessage: %s";
        outputMessage += String.format(ChatColor.BOLD + formatter + ChatColor.RESET, "id", "player", "start", "last", "tag", "delay", "rate", "echo", "");

        int i = 0;
        while (reminders.next()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String start = simpleDateFormat.format(new Date(reminders.getLong("start")));
            String last = simpleDateFormat.format(new Date(reminders.getLong("last")));

            outputMessage += String.format(formatter, reminders.getInt("id"), reminders.getString("player"),
                    start, last, reminders.getString("tag"), reminders.getInt("delay"), reminders.getInt("rate"),
                    reminders.getInt("echo"), reminders.getString("message"));
            i++;
        }
        outputMessage += String.format("\n%s row(s) returned. Limit = %s.", i, plugin.getConfig().getInt("maxRows"));
        sender.sendMessage(outputMessage);

        return true;
    }
}
