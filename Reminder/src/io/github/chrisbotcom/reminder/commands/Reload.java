package io.github.chrisbotcom.reminder.commands;

import org.bukkit.plugin.java.JavaPlugin;

public class Reload {
	public static void execute(JavaPlugin plugin) throws Exception 
	{
		plugin.reloadConfig();
	}
}
