package me.tptuaasn.plugin.config;

import me.tptuaasn.plugin.Heal;

public class ConfigManager {

	public static void reloadConfig() {
		Heal.getCf().reloadConfig();
		Heal.getCf().updateConfig();
	}
}
