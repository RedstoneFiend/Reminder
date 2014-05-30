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
package io.github.chrisbotcom.reminder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Reminder extends JavaPlugin implements Listener {

    public Connection db = null;
    public FileConfiguration config = null;
    public BukkitTask reminderTask;
    public Map<String, Map<String, Object>> playerHashMap = new HashMap<>();

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

        // Load and update config.yml
        this.config = this.getConfig();
        this.config.options().copyDefaults(true);
        this.saveConfig();

        // Connect to database
        try {
            // Test for JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open connection
            this.db = DriverManager.getConnection(config.getString("dbUrl"), config.getString("dbUser"), config.getString("dbPassword"));
        } catch (ClassNotFoundException e) {
            this.getLogger().log(Level.SEVERE, "JDBC Driver not found!");
        } catch (SQLException e) {
            this.getLogger().log(Level.SEVERE, "Unable to open MySQL Connection!");
        }

        // Verify table
        if (this.db != null) {
            try {
                this.db.prepareStatement("SELECT COUNT(*) FROM reminders").execute();
            } catch (SQLException e) {
                // reminders table does not exist. Create it.
                try {
                    InputStream inputStream;
                    inputStream = this.getClass().getResourceAsStream("/Resource/create_table.sql");
                    Scanner scanner;
                    scanner = new Scanner(inputStream);
                    String createTableSql = scanner.useDelimiter("\\Z").next();
                    scanner.close();
                    inputStream.close();
                    this.db.prepareStatement(createTableSql).executeUpdate();
                    this.getLogger().info("Created table 'reminders'.");
                } catch (IOException e1) {
                    this.getLogger().severe("Unable to open or close /Resource/create_table.sql!");
                    this.getLogger().log(Level.SEVERE, e1.toString());
                } catch (SQLException e1) {
                    this.getLogger().severe("Unable to create table 'reminders'!");
                    this.getLogger().log(Level.SEVERE, e1.toString());
                }
            }
        }

        // Set command executor
        CommandExecutor commandExecutor = new CommandParser(this);
        this.getCommand("reminder").setExecutor(commandExecutor);

        // Set player join listener
        getServer().getPluginManager().registerEvents(this, this);

        // Register existing players
        for (Player player : this.getServer().getOnlinePlayers()) {
            
            playerHashMap.put(player.getName(), new HashMap<String, Object>());
            playerHashMap.get(player.getName()).put("joined", new Date().getTime());
        }
        
        // Start asynchronous reminderTask. Runs every taskRate seconds. 1 second = 20 ticks. Valid range 15 - 120.
        Long taskRate = this.config.getLong("taskRate");
        if (taskRate < 15L) {
            taskRate = 15L;
        }
        if (taskRate > 120L) {
            taskRate = 120L;
        }
        taskRate *= 20L;
        if ((config.getBoolean("startOnLoad") == true) && (this.db != null)) {
            reminderTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ReminderTask(this), taskRate, taskRate);
        }
        
        if (!config.getBoolean("startOnLoad")) {
            
            this.getLogger().log(Level.WARNING, "startOnLoad in config.yml set to false. Reminder will not process until set to true.");
        }
    }

    @Override
    public void onDisable() {

        // Stop asynchronous reminderTask
        if (this.reminderTask != null
                && (this.getServer().getScheduler().isCurrentlyRunning(this.reminderTask.getTaskId())
                || this.getServer().getScheduler().isQueued(this.reminderTask.getTaskId()))) {
            this.getServer().getScheduler().cancelTask(this.reminderTask.getTaskId());
        }

        // Close database connection.
        if (this.db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                this.getLogger().severe("Unable to close MySQL Connection!");
                this.getLogger().log(Level.SEVERE, e.toString());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        playerHashMap.put(event.getPlayer().getName(), new HashMap<String, Object>());
        playerHashMap.get(event.getPlayer().getName()).put("joined", new Date().getTime());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        playerHashMap.remove(event.getPlayer().getName());
    }
}
