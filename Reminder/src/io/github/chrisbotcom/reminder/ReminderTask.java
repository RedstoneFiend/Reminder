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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.bukkit.World;
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
        long now = new Date().getTime(); // get the current (long) date
        Connection db = getDb();

        if (messageFlipFlop) { // Send broadcast message.

            try {                
                // select message for all players (*)
                
                String sql = "SELECT * FROM reminders WHERE (player LIKE '*%') AND (echo <> 0) AND "
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
                    String player = resultSet.getString("player");
                    String[] worlds;
                    
                    if (player.equals("*")) {
                        
                        List<World> ww = plugin.getServer().getWorlds();
                        worlds = new String[ww.size()];
                        
                        for (int i = 0; i < ww.size(); i++) {
                            
                            worlds[i] = ww.get(i).getName();
                        }
                    }
                    else {
                        
                        worlds = player.substring(1).split(",");
                    }
                        
                    for (String world : worlds) {

                        List<Player> players = this.getPlayersInWorld(world.trim());

                        if (players != null) {
                            for (Player p : players) {

                                Map<String, Object> playerHashMap = getPlayerHashMap(p.getName());
                                Long playerLoginTime = (Long) playerHashMap.get("joined");
                                Integer playerEcho = (Integer) playerHashMap.get(id.toString());
                                if (playerEcho == null) {
                                    playerEcho = echo;
                                }

                                if ((now >= (playerLoginTime + (delay * 60000))) && (playerEcho != 0)) {

                                    // If player does not already have a reference to this message, add it.
                                    p.sendMessage(message);

                                    if (echo > 0) {
                                        playerHashMap.put(id.toString(), playerEcho - 1);
                                        putPlayerHashMap(p.getName(), playerHashMap);
                                    }
                                }
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
                plugin.getLogger().log(Level.SEVERE, e.toString());
            }

        } else { // Send player specific message.

            Player[] players = this.getPlayers();
            
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
                    plugin.getLogger().log(Level.SEVERE, e.toString());
                }
            }
        }

        isRunning = false;
    }

    private Player[] getPlayers() {
        
        Player[] players = null;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        
        try {
            
            players = scheduler.callSyncMethod(plugin, new Callable<Player[]>() {
                
                @Override
                public Player[] call() {
                    return plugin.getServer().getOnlinePlayers();
                }
            }).get();
        } 
        catch (InterruptedException | ExecutionException e) {
            
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }
        
        return players;
    }

    private List<Player> getPlayersInWorld(final String world) {
        
        List<Player> players = null;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        
        try {
            
            players = scheduler.callSyncMethod(plugin, new Callable<List<Player>>() {
                @Override
                public List<Player> call() {
                    
                    List<Player> players = new ArrayList<>();
                    
                    if (world.endsWith("*")) {
                        
                        String prefix = world.substring(0, world.length() - 2);
                        List<World> worlds = plugin.getServer().getWorlds();
                    
                        for (World w : worlds) {
                        
                            if (w.getName().startsWith(prefix)) {
                                
                                players.addAll(w.getPlayers());
                            }
                        }
                    }
                    else {
                        
                        players.addAll(plugin.getServer().getWorld(world).getPlayers());
                    }
                    
                    return players;
                }
            }).get();
        } 
        catch (InterruptedException | ExecutionException e) {
            
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }
        
        return players;
    }

    private Connection getDb() {
        
        Connection db = null;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        
        try {
            
            db = scheduler.callSyncMethod(plugin, new Callable<Connection>() {
                
                @Override
                public Connection call() {
                    return plugin.db;
                }
            }).get();
        } 
        catch (InterruptedException | ExecutionException e) {
            
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }
        return db;
    }

    private Map<String, Object> getPlayerHashMap(final String playerName) {

        Map<String, Object> playerBroadcastHashMap = new HashMap<>();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        try {
            
            playerBroadcastHashMap = scheduler.callSyncMethod(plugin, new Callable<Map<String, Object>>() {
                
                @Override
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
        } 
        catch (InterruptedException | ExecutionException e) {
            
            plugin.getLogger().log(Level.SEVERE, e.toString());
        }
        return playerBroadcastHashMap;
    }

    private void putPlayerHashMap(final String playerName, final Map<String, Object> playerHashMap) {

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        Runnable task = new Runnable() {

            @Override
            public void run() {
                
                plugin.playerHashMap.get(playerName).putAll(playerHashMap);
            }
        };

        scheduler.runTask(plugin, task);
    }
}
