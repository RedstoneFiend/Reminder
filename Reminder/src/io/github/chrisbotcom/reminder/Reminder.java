package io.github.chrisbotcom.reminder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Reminder extends JavaPlugin {
	
	@Override
    public void onEnable(){
		getLogger().info("onEnable has been invoked!");
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("onDisable has been invoked!");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("basic")){ // If the player typed /basic then do the following...
    		sender.sendMessage("&1[Reminder] &eYou typed &lbasic");
    		return true;
    	} //If this has happened the function will return true. 
            // If this hasn't happened the a value of false will be returned.
    	return false; 
    }
}
