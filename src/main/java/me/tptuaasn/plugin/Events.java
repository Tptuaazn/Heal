package me.tptuaasn.plugin;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class Events implements Listener {

	private final Heal plugin;

	private FileConfiguration config = Heal.getCf().getConfig();

	public Events(Heal plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		boolean joinHeal = config.getBoolean("settings.join-heal.enabled");
		double health = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		long delay = config.getLong("settings.join-heal.delay");
		
		if (joinHeal == false)
			return;

		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskLater(plugin, task -> {

			if (e.getPlayer().getHealth() == health) {
				task.cancel();
				return;
			}
			e.getPlayer().setHealth(health);

		}, 20L * delay);
	}

	@EventHandler
	private void onLevelUp(PlayerLevelChangeEvent e) {
		boolean levelUp = config.getBoolean("settings.level-up.enabled");

		if (levelUp == false)
			return;

		if ((e.getNewLevel() - e.getOldLevel()) == config.getInt("settings.level-up.level"))
			e.getPlayer().setHealth(e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}
}
