package io.github.chrisbotcom.reminder.commands;

import org.bukkit.plugin.java.JavaPlugin;

public class Reload {
	public static boolean execute(JavaPlugin plugin) throws Exception 
	{
		plugin.reloadConfig();
		
		return true;
	}
}
