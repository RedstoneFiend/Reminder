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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class ReminderTask implements Runnable {
	
    private final Reminder plugin;
    private boolean isRunning;
    
    public ReminderTask(Reminder plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void run() {
    	
    	if (isRunning) {
			// ReminderTask is busy.
			return;
		}
 
    	isRunning = true;

    	// Get player list with synchronous call to main thread.
		Player[] players = getPlayers();

		// get the current date and convert it to long
		long now = new Date().getTime();
		
		Connection db = getDb();
		
		Integer broadcastId = null;
		String broadcastMessage = null;
		Integer broadcastDelay = null;
		// Do broadcasts
		try {
			// select message for all players (*)
			String sql = "SELECT id, message, delay FROM reminders WHERE (player = '*') AND (echo <> 0) AND " +
					"(start <= ?) AND ((last + (rate * 60000)) <= ?) ORDER BY last, start LIMIT 1";
			PreparedStatement preparedStatement = db.prepareStatement(sql);
			preparedStatement.setLong(1, now);
			preparedStatement.setLong(2, now);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				broadcastId = resultSet.getInt("id");
				broadcastMessage = resultSet.getString("message").replace('&', '\u00A7');
				broadcastDelay = resultSet.getInt("delay");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}			

		// iterate playrList
		for(Player player : players) {
			
			Long playerLoginTime = getPlayerLoginTime(player.getName());
			
			try {
				// select message for player where: echo > 0, now >= start + delay, now >= last + rate
				// order by last ascending, start ascending
				String sql = "SELECT id, message FROM reminders WHERE (player = ?) AND (echo <> 0) AND " +
						"(start <= ?) AND ((delay * 60000) <= ?) AND ((last + (rate * 60000)) <= ?) ORDER BY last, start LIMIT 1";
				PreparedStatement preparedStatement = db.prepareStatement(sql);
				preparedStatement.setString(1, player.getName());
				preparedStatement.setLong(2, now);
				preparedStatement.setLong(3, now - playerLoginTime);
				preparedStatement.setLong(4, now);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				// send message to player
				if (resultSet.next()) {
					player.sendMessage(resultSet.getString("message").replace('&', '\u00A7'));
				
					// update reminder set last to now, decrement echo
					sql = "UPDATE reminders SET last = ? WHERE (id = ?)";
					preparedStatement = db.prepareStatement(sql);
					preparedStatement.setLong(1, now);
					preparedStatement.setInt(2, resultSet.getInt("id"));
					preparedStatement.executeUpdate();
					sql = "UPDATE reminders SET echo = echo - 1 WHERE (id = ?) AND (echo > 0)";
					preparedStatement = db.prepareStatement(sql);
					preparedStatement.setInt(1, resultSet.getInt("id"));
					preparedStatement.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if ((broadcastMessage != null) && (now >= (playerLoginTime + broadcastDelay))) {
				
				player.sendMessage(broadcastMessage);
			}
		}
		
		// update broadcast set last to now, decrement echo
		if (broadcastId != null)
			try {
				String sql = "UPDATE reminders SET last = ? WHERE (id = ?)";
				PreparedStatement preparedStatement = db.prepareStatement(sql);
				preparedStatement.setLong(1, now);
				preparedStatement.setInt(2, broadcastId);
				preparedStatement.executeUpdate();
				sql = "UPDATE reminders SET echo = echo - 1 WHERE (id = ?) AND (echo > 0)";
				preparedStatement = db.prepareStatement(sql);
				preparedStatement.setInt(1, broadcastId);
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		
		isRunning = false;    	
    }

	private Player[] getPlayers() {
		Player[] players = null;
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
	    try {
			players = scheduler.callSyncMethod(plugin, new Callable<Player[]>() {
			    public Player[] call() {
			        return plugin.getServer().getOnlinePlayers();
			    }
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return players;
	}


	private Connection getDb() {
		Connection db = null;
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
	    try {
			db = scheduler.callSyncMethod(plugin, new Callable<Connection>() {
			    public Connection call() {
			        return plugin.db;
			    }
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return db;
	}

	private Long getPlayerLoginTime(final String playerName) {
		Long playerLoginTime = null;
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
	    try {
	    	playerLoginTime = scheduler.callSyncMethod(plugin, new Callable<Long>() {
			    public Long call() {
			    	Long playerLoginTime = plugin.playerLoginTime.get(playerName);
			    	if (playerLoginTime == null) {
			    		//plugin.getLogger().info(playerName);
			    		playerLoginTime = new Date().getTime();
			    		plugin.playerLoginTime.put(playerName, playerLoginTime);
			    	}
			        return playerLoginTime;
			    }
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return playerLoginTime;
	}
}
