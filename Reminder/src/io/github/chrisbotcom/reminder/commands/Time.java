package io.github.chrisbotcom.reminder.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.CommandSender;

public class Time {
	public static void execute(CommandSender sender)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sender.sendMessage("Server time:" + simpleDateFormat.format(new Date()));
	}
}
