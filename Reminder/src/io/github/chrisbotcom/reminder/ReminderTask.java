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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class ReminderTask implements Runnable {

    private final Reminder plugin;
    private boolean isRunning;
    private boolean messageFlipFlop; // Alternates massages between broadcast
    // and specific player.

    public ReminderTask(Reminder plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        if (isRunning) {
            return;
        } else {
            isRunning = true;
        }

        messageFlipFlop = !messageFlipFlop;
        Player[] players = getPlayers(); // Get player list
        long now = new Date().getTime(); // get the current (long) date
        Connection db = getDb();

        if (messageFlipFlop) { // Send broadcast message.

            try {
                // select message for all players (*)
                String sql = "SELECT * FROM reminders WHERE (player = '*') AND (echo <> 0) AND "
                        + "(start <= ?) AND ((last + (rate * 60000)) <= ?) ORDER BY last, start LIMIT 1";
                PreparedStatement preparedStatement = db.prepareStatement(sql);
                preparedStatement.setLong(1, now);
                preparedStatement.setLong(2, now);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    Integer id = resultSet.getInt("id");
                    String message = resultSet.getString("message").replace('&', '\u00A7');
                    int delay = resultSet.getInt("delay");
                    int echo = resultSet.getInt("echo");

                    for (Player player : players) {

                        Map<String, Object> playerHashMap = getPlayerHashMap(player.getName());
                        Long playerLoginTime = (Long) playerHashMap.get("joined");
                        Integer playerEcho = (Integer) playerHashMap.get(id.toString());
                        if (playerEcho == null) {
                            playerEcho = echo;
                        }

                        if ((now >= (playerLoginTime + (delay * 60000))) && (playerEcho != 0)) {

							// If player does not already have a reference to this message, add it.
                            player.sendMessage(message);

                            if (echo > 0) {
                                playerHashMap.put(id.toString(), playerEcho - 1);
                                putPlayerHashMap(player.getName(), playerHashMap);
                            }
                        }

                    }

                    // update broadcast set last to now
                    sql = "UPDATE reminders SET last = ? WHERE (id = ?)";
                    preparedStatement = db.prepareStatement(sql);
                    preparedStatement.setLong(1, now);
                    preparedStatement.setInt(2, id);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else { // Send player specific message.

            for (Player player : players) {

                Long playerLoginTime = (Long) getPlayerHashMap(player.getName()).get("joined");

                try {
					// select message for player where: echo > 0, now >= start +
                    // delay, now >= last + rate
                    // order by last ascending, start ascending
                    String sql = "SELECT * FROM reminders WHERE (player = ?) AND (echo <> 0) AND "
                            + "(start <= ?) AND ((delay * 60000) <= ?) AND ((last + (rate * 60000)) <= ?) ORDER BY last, start LIMIT 1";
                    PreparedStatement preparedStatement = db
                            .prepareStatement(sql);
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

            }
        }

        isRunning = false;
    }

    private Player[] getPlayers() {
        Player[] players = null;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        try {
            players = scheduler.callSyncMethod(plugin,
                    new Callable<Player[]>() {
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

    private Map<String, Object> getPlayerHashMap(final String playerName) {

        Map<String, Object> playerBroadcastHashMap = new HashMap<String, Object>();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        try {
            playerBroadcastHashMap = scheduler.callSyncMethod(plugin,
                    new Callable<Map<String, Object>>() {
                        public Map<String, Object> call() {
                            Map<String, Object> playerBroadcastHashMap = (Map<String, Object>) plugin.playerHashMap.get(playerName);
                            if (playerBroadcastHashMap == null) {
                                // plugin.getLogger().info(playerName);
                                Long playerLoginTime = new Date().getTime();
                                plugin.playerHashMap.put(playerName, new HashMap<String, Object>());
                                plugin.playerHashMap.get(playerName).put("joined", playerLoginTime);
                            }
                            return playerBroadcastHashMap;
                        }
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return playerBroadcastHashMap;
    }

    private void putPlayerHashMap(final String playerName, final Map<String, Object> playerHashMap) {

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        Runnable task = new Runnable() {

            public void run() {
                plugin.playerHashMap.get(playerName).putAll(playerHashMap);
            }
        };

        scheduler.runTask(plugin, task);
    }
}
