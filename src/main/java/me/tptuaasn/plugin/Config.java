package me.tptuaasn.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;
import com.tchristofferson.configupdater.ConfigUpdater;

public class Config {

	private final Heal plugin;

	private final String path, name;
	private File filePath, file;
	private FileConfiguration config;

	public Config(Heal plugin, String path, String name) {
		this.plugin = plugin;
		this.path = path;
		this.name = name;
		this.filePath = new File(plugin.getDataFolder(), path);
		this.file = new File(filePath, name);
		saveDefaultConfig();
		this.config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveDefaultConfig() {
		if (!file.exists()) {
			plugin.saveResource(path + name, false);
		}
	}
	
	public void saveConfig() {
		try {
			getConfig().save(file);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, ex);
		}
	}

	public void updateConfig() {
		try {
			ConfigUpdater.update(plugin, name, file, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reloadConfig() {
		saveDefaultConfig();
		config = YamlConfiguration.loadConfiguration(file);

		final InputStream defConfigStream = plugin.getResource(name);
		if (defConfigStream == null) {
			return;
		}

		config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
	}

	public FileConfiguration getConfig() {
		return config;
	}
}