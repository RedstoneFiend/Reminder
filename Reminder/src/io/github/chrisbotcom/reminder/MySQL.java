package io.github.chrisbotcom.reminder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

public class MySQL {
	private final JavaPlugin plugin;
    private final String url;  // jdbc:mysql://hostname:port/database
    private final String user;
    private final String password;

    private Connection connection;

    public MySQL(JavaPlugin plugin, String url, String username, String password) {
    	this.plugin = plugin;
        this.url = url;
        this.user = username;
        this.password = password;
        this.connection = null;
    }

    public void openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // test for driver
            connection = DriverManager.getConnection(this.url, this.user, this.password);
        } 
        catch (Exception e) {
            plugin.getLogger().info("MySQL Open Connection: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void closeConnection() {
        if (isConnected()) {
            try {
                connection.close();
            } 
            catch (Exception e) {
                plugin.getLogger().info("MySQL CloseConnection: " + e.getMessage());
            }
        }
    }

    public ResultSet query(String query) {
    	ResultSet resultset = null;
 
        if (isConnected()) {
        	try {
        		Statement statement = connection.createStatement();
        		resultset = statement.executeQuery(query);
        	}
        	catch (Exception e) {
        		plugin.getLogger().info("MySQL Query: " + e.getMessage());
            }
        }
        
        return resultset;
    }

    public int update(String query) {
       	int rowsAffected = 0;
        
        if (isConnected()) {
        	try {
        		Statement statement = connection.createStatement();
        		rowsAffected = statement.executeUpdate(query);
        	}
        	catch (Exception e) {
        		plugin.getLogger().info("MySQL Update: " + e.getMessage());
            }
        }
        
        return rowsAffected;
    }
}
