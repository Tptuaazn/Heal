package me.tptuaasn.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.tptuaasn.plugin.config.Config;
import me.tptuaasn.plugin.config.ConfigManager;

public class Cmd implements CommandExecutor, TabCompleter {

	private final Heal plugin;
	private FileConfiguration config;
	private Config cf = Heal.getCf();

	public Cmd(Heal plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		config = Heal.getCf().getConfig();

		if (cmd.getName().equalsIgnoreCase("heal") && hasPermission(sender, "heal.use")) {
			if (args.length == 0) {
				if (!(sender instanceof Player)) {
					sendHelpMessage(sender);
					return true;
				}
				Player p = (Player) sender;
				p.setHealth(getMaxHealth(p));
				p.sendMessage(color(config.getString("messages.healed")));
				return true;
			}

			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload") && hasPermission(sender, "heal.reload")) {
					ConfigManager.reloadConfig();
					config = Heal.getCf().getConfig();
					sender.sendMessage(color(config.getString("messages.reload")));
					return true;
				}

				if ((args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all"))
						&& hasPermission(sender, "heal.all")) {
					Collection<? extends Player> online = Bukkit.getServer().getOnlinePlayers();

					if (!online.isEmpty()) {
						for (Player players : online) {
							if (isHealthFull(players))
								continue;

							players.setHealth(getMaxHealth(players));
						}

						sender.sendMessage(color(
								config.getString("messages.all.finish").replace("%online%", String.valueOf(online))));
						return true;
					}

					sender.sendMessage(color(config.getString("messages.all.no-player")));
					return true;
				}

				if (args[0].equalsIgnoreCase("help") && hasPermission(sender, "heal.help")) {
					sendHelpMessage(sender);
					return true;
				}

				if (args[0].equalsIgnoreCase("toggle") && hasPermission(sender, "heal.toggle")) {
					if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("join-heal")) {
							if (config.getBoolean("settings.join-heal.enabled")) {
								config.set("settings.join-heal.enabled", false);
								cf.saveConfig();
								sender.sendMessage(color(config.getString("messages.join-heal.false")));
							} else {
								config.set("settings.join-heal.enabled", true);
								cf.saveConfig();
								sender.sendMessage(color(config.getString("messages.join-heal.true")));
							}
							return true;
						}

						if (args[1].equalsIgnoreCase("level-up")) {
							if (config.getBoolean("settings.level-up.enabled")) {
								config.set("settings.level-up.enabled", false);
								cf.saveConfig();
								sender.sendMessage(color(config.getString("messages.level-up.false")));
							} else {
								config.set("settings.level-up.enabled", true);
								cf.saveConfig();
								sender.sendMessage(color(config.getString("messages.level-up.true")));
							}
							return true;
						}
						return true;
					}
					sender.sendMessage("[Heal] /heal toggle <name>");
					return true;
				}

				if (args[0].equalsIgnoreCase("player")) {
					if (args.length >= 2 && hasPermission(sender, "heal.others")) {
						Player target = Bukkit.getPlayer(args[1]);

						if (target == null) {
							sender.sendMessage(
									color(config.getString("messages.target.unknown").replace("%target%", args[1])));
							return true;
						}

						if (args.length >= 3 && isNumber(args[1])) {
							if (!isHealthFull(target)) {
								target.setHealth(Double.parseDouble(args[1]));
								target.sendMessage(color(config.getString("messages.target.value")
										.replace("%healer%", sender.getName()).replace("%value%", args[2])));
								return true;
							}
							return true;
						}

						if (isHealthFull(target)) {
							sender.sendMessage(color("&cThis player is already full of health, no need to heal."));
							return true;
						}

						target.setHealth(getMaxHealth(target));
						target.sendMessage(color(
								config.getString("messages.target.healed").replace("%healer%", sender.getName())));
						return true;
					}
					sender.sendMessage("[Heal] /heal player <name> <value>");
					return true;
				}
				sender.sendMessage("Invalid arguments");
				return true;
			}
			return true;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

		List<String> args0 = new ArrayList<String>();

		if (command.getName().equalsIgnoreCase("heal")) {
			if (args.length == 1) {
				if (sender.hasPermission("heal.all")) {
					args0.add("*");
					args0.add("all");
				}
				if (sender.hasPermission("heal.help"))
					args0.add("help");
				if (sender.hasPermission("heal.reload"))
					args0.add("reload");
				if (sender.hasPermission("heal.toggle"))
					args0.add("toggle");
				if (sender.hasPermission("heal.others"))
					args0.add("player");
			}

			if (args[0].equalsIgnoreCase("player")) {
				if (args.length >= 2) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray(players);
					for (int i = 0; i < players.length; i++) {
						args0.add(players[i].getName());
					}

					if (args.length >= 3)
						args0.add("<number>");
				}

			}
			Collections.sort(args0);
			return args0;
		}
		return null;
	}

	private boolean isNumber(String s) {
		return s.matches("\\d+");
	}

	private double getMaxHealth(Player p) {
		return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
	}

	private boolean isHealthFull(Player p) {
		return p.getHealth() == p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
	}

	private boolean hasPermission(CommandSender s, String p) {
		String noPermission = config.getString("messages.no-permission");

		if (!s.hasPermission(p)) {
			s.sendMessage(color(noPermission));
			return false;
		}

		return true;
	}

	private void sendHelpMessage(CommandSender sender) {
		sender.sendMessage(color("&e&l" + plugin.getName() + " - v" + plugin.getDescription().getVersion()));
		sender.sendMessage("&8&n------------------------------------------------------");
		sender.sendMessage(color("&f/heal &8- &7Heal yourself (Only player)"));
		sender.sendMessage(color("&f/heal reload &8- &7Reload plugin"));
		sender.sendMessage(color("&f/heal toggle &8- &7Reload plugin"));
		sender.sendMessage(color("&f/heal help &8- &7List command (this)"));
		sender.sendMessage(color("&f/heal player <name> <value> &8- &7Heal other players"));
		sender.sendMessage("&8&n------------------------------------------------------");
	}

	private String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

}
