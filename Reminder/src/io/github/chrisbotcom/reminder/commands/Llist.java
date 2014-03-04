package io.github.chrisbotcom.reminder.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.SqlRow;

public class Llist {
	public static void execute(JavaPlugin plugin, CommandSender sender)  throws Exception
	{
		String formatter = "%6s | %-16s | %16s | %-16s | %8s | %9s | %6s\nMessage: %s";
		sender.sendMessage(String.format(ChatColor.BOLD + formatter, "id", "player", "start", "tag", "delay", "rate", "echo", ""));
	
		List<SqlRow> reminders = plugin.getDatabase().createSqlQuery("select * from reminders").findList();
		 //EntityManager entityManager;
		 //List<ReminderBean> reminders = Ebean..find(ReminderBean.class).fetch(arg0).findList();
		 
		for (SqlRow reminder : reminders) { 
			 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			 String start = simpleDateFormat.format(new Date(reminder.getLong("start")));

			 sender.sendMessage(String.format(formatter, reminder.get("id"), reminder.get("player"), 
					 start, reminder.get("tag"), reminder.get("delay"), reminder.get("rate"), 
					 reminder.get("echo"), reminder.get("message")));
		 }
	}
}
