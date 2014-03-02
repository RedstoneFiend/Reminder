package io.github.chrisbotcom.reminder;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.Table;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reminder extends JavaPlugin {
		
	@Override
    public void onEnable() {
		getLogger().info("Reminder plugin has been enabled.");
		
		FileConfiguration config = getConfig();
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
			
        setupDatabase();
        
		CommandExecutor commandExecutor = new CommandParser(this);
		getCommand("reminder").setExecutor(commandExecutor);
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("Reminder plugin has been disabled.");
    }
    
    private void setupDatabase() 
    {
        try {
            getDatabase().find(ReminderBean.class).findRowCount();
        } 
        catch (PersistenceException ex) {
            getLogger().info(String.format("Installing table %s for first time use.", 
            		ReminderBean.class.getAnnotation(Table.class).name()));
            installDDL();
        }
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() 
    {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(ReminderBean.class);
        return list;
    }
}
