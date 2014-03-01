package io.github.chrisbotcom.reminder;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reminder extends JavaPlugin {
	private Config config;
	private MySQL mysql;
	
	@Override
    public void onEnable() {
		getLogger().info("Reminder plugin has been enabled.");
		
		config = new Config(this, "config.yml");
		
		mysql = new MySQL(this, config.get("mysql_url").toString(), config.get("username").toString(), config.get("password").toString());
		mysql.openConnection();
		getLogger().info("Reminder connection to MySQL has " + (mysql.isConnected() ? "succeeded." : ChatColor.RED + "failed!"));
		
		CommandExecutor commandExecutor = new CommandParser(this, config, mysql);
		getCommand("reminder").setExecutor(commandExecutor);
		
		//getLogger().info("tag:       " + config.get("tag"));
		//getLogger().info("cooldown:  " + config.get("cooldown"));
		//getLogger().info("frequency: " + config.get("frequency"));
		//getLogger().info("resend:    " + config.get("resend"));
		//getLogger().info("prefix:    " + config.get("prefix"));
		
		// TODO: set up reminders
    }
 
    @Override
    public void onDisable() {
    	mysql.closeConnection();
    	
    	getLogger().info("Reminder plugin has been disabled.");
    	
    	//TODO: disable reminders
    }
}
