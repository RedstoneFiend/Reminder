package io.github.chrisbotcom.reminder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	private final JavaPlugin plugin;
	private final String fileName;
	private FileConfiguration fileConfiguration;

	public Config(JavaPlugin plugin, String fileName) {
		this.plugin = plugin;
		this.fileName = fileName;
		this.loadConfiguration();
	}

	public void loadConfiguration() {
		File file = new File(plugin.getDataFolder(), fileName);
		if (file.exists())
		{
			fileConfiguration = YamlConfiguration.loadConfiguration(file);
		}
		else
		{
			InputStream defaultsStream = plugin.getResource(fileName);
			if (defaultsStream != null) 
			{
				fileConfiguration = YamlConfiguration.loadConfiguration(defaultsStream);
			}
		}
		saveConfig();
	}

	public void saveConfig() {
		File file = new File(plugin.getDataFolder(), fileName);
		try {
			fileConfiguration.save(file);
		} catch (IOException e) {
			plugin.getLogger().severe("IOException while saving Reminder configuration file.");
			e.printStackTrace();
		}
	}
	
	public Object get(String path) {
		return fileConfiguration.get(path);
	}
	
	public void set(String path, Object object) {
		fileConfiguration.set(path, object);
	}
}
