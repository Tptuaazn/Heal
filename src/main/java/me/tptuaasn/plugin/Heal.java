package me.tptuaasn.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import me.tptuaasn.plugin.config.Config;

public class Heal extends JavaPlugin {

	private static Config cf;
	private static Heal plugin;

	@Override
	public void onEnable() {
		plugin = this;
		cf = new Config(this, "", "config.yml");
		cf.updateConfig();

		getServer().getPluginManager().registerEvents(new Events(this), this);
		PluginCommand cmd = Bukkit.getPluginCommand("heal");
		cmd.setExecutor(new Cmd(this));
		cmd.setTabCompleter(new Cmd(this));
	}

	public static Heal getPlugin() {
		return plugin;
	}

	public static Config getCf() {
		return cf;
	}
}
