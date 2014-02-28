package io.github.chrisbotcom.reminder;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class CommandParser implements CommandExecutor {
	//private JavaPlugin plugin;
	//private Config config;
	private MySQL mysql;

	public CommandParser(JavaPlugin plugin, Config config, MySQL mysql) {
		//this.plugin = plugin;
		//this.config = config;
		this.mysql = mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		boolean commandHandled = false;
		try {
			Commands commandName = Commands.lookup(args[0]);
			switch (commandName)
			{
				case add:
					if (args.length > 1)
					{
						commandAdd();
						commandHandled = true;
					}
					break;
				case list:
					//if (args.length >= 1)
					//{
						String query = "SELECT COUNT(*) FROM chrisbotcom_reminder";
						ResultSet rs = mysql.query(query);
						rs.first();
						sender.sendMessage("Record count: " + rs.getInt(1));
						commandHandled = true;
					//}
					break;
				case delete:
					if (args.length >= 1)
					{
						sender.sendMessage("Delete command issued");
						commandHandled = true;
					}
					break;
				case update:
					if (args.length >= 1)
					{
						sender.sendMessage("Update command issued");
						commandHandled = true;
					}
					break;
				case setdefault:
					if (args.length >= 1)
					{
						sender.sendMessage("SetDefault command issued");
						commandHandled = true;
					}
					break;
				case reload:
					sender.sendMessage("Reload command issued");
					commandHandled = true;
					break;
				case stop:
					sender.sendMessage("Stop command issued");
					commandHandled = true;
					break;
				case resume:
					sender.sendMessage("Resume command issued");
					commandHandled = true;
					break;
				case time:
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
					sender.sendMessage("Server time:" + simpleDateFormat.format(new Date()));
					commandHandled = true;
					break;
				default:
					sender.sendMessage(ChatColor.RED + "[Reminder] Erroneous command issued");
					break;
			}
		}
		catch (Exception e) { 
			sender.sendMessage(e.getMessage());
		}
		
		return commandHandled;
	}

	private void commandAdd() {
		// TODO Auto-generated method stub
		
	}
}
