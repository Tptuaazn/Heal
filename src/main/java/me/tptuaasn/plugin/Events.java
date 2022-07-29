package me.tptuaasn.plugin;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
		Player p = e.getPlayer();
		double health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

		if (!config.getBoolean("settings.join-heal.enabled"))
			return;

		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskLater(plugin, task -> {

			if (p.getHealth() == health) {
				task.cancel();
				return;
			}

			p.setHealth(health);

		}, 20L * config.getLong("settings.join-heal.delay"));
	}

	@EventHandler
	private void onLevelUp(PlayerLevelChangeEvent e) {
		Player p = e.getPlayer();
		int level = config.getInt("settings.level-up.level");
		int c = 0;

		for (int i = 0; i < level; i++) {
			int lc = e.getNewLevel() - e.getOldLevel();
			if(c >= level) break;
			c += lc;
		}

		if (config.getBoolean("settings.level-up.enabled") && !p.isDead() && c >= level)
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}
}
