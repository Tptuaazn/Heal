package me.tptuaasn.plugin;

import java.util.ArrayList;
import java.util.Collection;
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

public class Cmd implements CommandExecutor, TabCompleter {

	private final Heal plugin;
	private FileConfiguration config;
	private Config cf = Heal.getCf();

	private double health;

	public Cmd(Heal plugin) {
		this.plugin = plugin;
	}

	private void reloadConfig() {
		Heal.getCf().reloadConfig();
		Heal.getCf().updateConfig();
		config = Heal.getCf().getConfig();
	}

	private boolean hasPermission(CommandSender sender, String p) {
		String noPermission = config.getString("messages.no-permission");
		if (!sender.hasPermission(p)) {
			sender.sendMessage(color(noPermission));
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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		config = Heal.getCf().getConfig();

		if (cmd.getName().equalsIgnoreCase("heal")) {
			if (!hasPermission(sender, "heal.use"))
				return true;

			if (args.length == 0) {
				if (!(sender instanceof Player)) {
					sendHelpMessage(sender);
					return true;
				}
				Player p = (Player) sender;
				p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				p.sendMessage(color(config.getString("messages.healed")));
			}

			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (!hasPermission(sender, "heal.reload"))
						return true;

					reloadConfig();
					sender.sendMessage(color(config.getString("messages.reload")));
					return true;
				}

				if (args[0].equalsIgnoreCase("*") || args[0].equalsIgnoreCase("all")) {
					if (!hasPermission(sender, "heal.all"))
						return true;

					Collection<? extends Player> online = Bukkit.getServer().getOnlinePlayers();
					byte healed = 0;

					if (online.size() <= 0) {
						sender.sendMessage(color(config.getString("messages.all.no-player")));
						return true;
					}
					for (Player players : online) {
						health = players.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

						if (players.getHealth() == health)
							continue;

						players.setHealth(health);
						healed++;
					}
					sender.sendMessage(
							color(config.getString("messages.all.finish").replace("%healed%", String.valueOf(healed))));
					return true;
				}

				if (args[0].equalsIgnoreCase("help")) {
					if (!hasPermission(sender, "heal.help"))
						return true;

					sendHelpMessage(sender);
					return true;
				}

				if (args[0].equalsIgnoreCase("toggle")) {
					if (!hasPermission(sender, "heal.toggle"))
						return true;

					if (args.length >= 2) {

						boolean joinHeal = config.getBoolean("settings.join-heal.enabled", false);
						boolean levelUp = config.getBoolean("settings.level-up.enabled", false);

						if (args[1].equalsIgnoreCase("join-heal")) {
							if (joinHeal == true) {
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
							if (levelUp == true) {
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

						sender.sendMessage("[Heal] /heal toggle <name>");
						return true;
					}
					return true;
				}

				if (args[0].equalsIgnoreCase("player")) {
					if (args.length >= 2) {
						if (!hasPermission(sender, "heal.others"))
							return true;

						Player target = Bukkit.getPlayer(args[1]);

						if (target == null) {
							sender.sendMessage(
									color(config.getString("messages.target.unknown").replace("%target%", args[1])));
							return true;
						}

						if (args.length >= 3) {
							if (!args[1].matches("\\d+")) {
								sender.sendMessage(color("&cOnly digits can be used"));
								return true;
							}

							health = Double.parseDouble(args[1]);
							double tHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

							if (target.getHealth() == tHealth) {
								sender.sendMessage(color("&cThis player is already full of health, no need to heal."));
								return true;
							}

//							if (health > (tHealth)) {
//								sender.sendMessage(
//										color("&cThe value must be less than or equal to the player's maximum health. "
//												+ tHealth));
//								return true;
//							}

							target.setHealth(health);
							target.sendMessage(color(config.getString("messages.target.value")
									.replace("%healer%", sender.getName()).replace("%value%", args[2])));
							return true;
						}

						health = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

						if (target.getHealth() == health) {
							sender.sendMessage(color("&cThis player is already full of health, no need to heal."));
							return true;
						}

						target.setHealth(health);
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
		List<String> args1 = new ArrayList<String>();

		if (command.getName().equalsIgnoreCase("heal")) {
			if (args.length == 1) {
				args0.add("*");
				args0.add("all");
				args0.add("help");
				args0.add("reload");
				args0.add("toggle");
				args0.add("player");

				return args0;
			}

			if (args[0].equalsIgnoreCase("player")) {
				if (args.length >= 2) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray(players);
					for (int i = 0; i < players.length; i++) {
						args1.add(players[i].getName());
					}
					return args1;
				}
			}
			return null;
		}
		return null;
	}

	private String color(String string) {
		// TODO Auto-generated method stub
		return ChatColor.translateAlternateColorCodes('&', string);
	}

}
