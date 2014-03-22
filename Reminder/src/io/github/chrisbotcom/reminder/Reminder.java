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

package io.github.chrisbotcom.reminder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Reminder extends JavaPlugin implements Listener {
	
	public Connection db = null;
	public FileConfiguration config = null;
	BukkitTask reminderTask;
	public Map <String,Long> playerLoginTime = new HashMap<String,Long>();
	@Override
	public void onLoad() {

	}

	@Override
    public void onEnable() {
		
		// Load and update config.yml
		this.config = this.getConfig();
		this.config.options().copyDefaults(true);
		this.saveConfig();		
		
		// Set up database
		try {
			// Test for JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			// Open connection
			this.db = DriverManager.getConnection(config.getString("dbUrl"), config.getString("dbUser"), config.getString("dbPassword"));
		}
		catch (ClassNotFoundException e) {
            this.getLogger().log(Level.SEVERE, "JDBC Driver not found!");
        }
		catch (SQLException e) {
			this.getLogger().log(Level.SEVERE, "Unable to open MySQL Connection!");
			e.printStackTrace();
		}

        // Set command executor
		CommandExecutor commandExecutor = new CommandParser(this);
		this.getCommand("reminder").setExecutor(commandExecutor);
		
		// Set player join listener
		getServer().getPluginManager().registerEvents(this, this);
		
		// Start asynchronous reminderTask. Runs every 15 seconds
	    reminderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ReminderTask(this), 300L, 300L);
    }
 
    @Override
    public void onDisable() {
    	
    	// Stop asynchronous reminderTask
    	reminderTask.cancel();
    	
    	// Close database connection.
    	try {
			db.close();
		} catch (SQLException e) {
			this.getLogger().log(Level.SEVERE, "Unable to close MySQL Connection!");
			e.printStackTrace();
		}
   }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    	
    	playerLoginTime.put(event.getPlayer().getName(), new Date().getTime());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    	
    	playerLoginTime.remove(event.getPlayer().getName());
    }
}


