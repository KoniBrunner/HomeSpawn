package net.lapismc.HomeSpawn;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class HomeSpawnCommand implements CommandExecutor {
	private final HomeSpawn plugin;

	private Map<String, Long> tpaCooldown = new Hashtable<String, Long>();
	private Map<String, String> currentRequest = new Hashtable<String, String>();

	public HomeSpawnCommand(HomeSpawn plugin) {
		this.plugin = plugin;
	}

	public void TeleportPlayer(Player p, Location l, String r) {
		if (plugin.getConfig().getInt("TeleportTime") == 0 || p.hasPermission("homespawn.bypassdelay")) {
			p.teleport(l);
			if (r.equalsIgnoreCase("Spawn")) {
				p.sendMessage(
						ChatColor.GOLD + ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.SentToSpawn"));
			} else if (r.equalsIgnoreCase("Home")) {
				p.sendMessage(ChatColor.GOLD + ConfigSingleton.getInstance(plugin).Messages.getString("Home.SentHome"));
			}
		} else {
			String waitraw = ChatColor.GOLD + ConfigSingleton.getInstance(plugin).Messages.getString("Wait");
			String Wait = waitraw.replace("{time}",
					ChatColor.RED + plugin.getConfig().getString("TeleportTime") + ChatColor.GOLD);
			p.sendMessage(Wait);
			plugin.HomeSpawnLocations.put(p, l);
			plugin.HomeSpawnTimeLeft.put(p, plugin.getConfig().getInt("TeleportTime"));
		}

	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("sethome")) {
				if (player.hasPermission("homespawn.player")) {
					FileConfiguration getHomes = ConfigSingleton.getInstance(plugin).getPlayerConfig(player);
					if (args.length == 0) {
						createHomeLocation(player, getHomes, "home");
						player.sendMessage(ChatColor.GOLD
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.HomeSet"));
					} else if (args.length == 1) {
						if (!player.hasPermission("homespawn.vip") && !player.hasPermission("homespawn.admin")) {
							player.sendMessage(ChatColor.RED
									+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.NotVip"));
							return false;
						}
						if (player.hasPermission("homespawn.vip") && !player.hasPermission("homespawn.admin")) {
							if (getHomes.getInt("Numb") >= plugin.getConfig().getInt("VIPHomesLimit")) {
								player.sendMessage(ChatColor.RED
										+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.LimitReached"));
								return false;
							}
						}
						if (args[0].equalsIgnoreCase("spawn")) {
							player.sendMessage(ChatColor.RED
									+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.Reserved"));
							return false;
						}
						createHomeLocation(player, getHomes, getHomeName(args[0]));
						player.sendMessage(ChatColor.GOLD
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.HomeSet"));
					} else {
						player.sendMessage(
								ChatColor.RED + ConfigSingleton.getInstance(plugin).Messages.getString("Error.Args+"));
					}
					ConfigSingleton.getInstance(plugin).savePlayerConfig(player);
					return true;
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("home")) {
				if (player.hasPermission("homespawn.player")) {
					FileConfiguration getHomes = ConfigSingleton.getInstance(plugin).getPlayerConfig(player);

					if (getHomes.getInt("Numb") == 0) {
						player.sendMessage(ChatColor.DARK_RED
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.NoHomeSet"));
						return false;
					}
					if (args.length == 0) {
						sendToHomeLocation(player, getHomes, "home");
					} else if (args.length == 1) {
						if (!player.hasPermission("homespawn.vip") && !player.hasPermission("homespawn.admin")) {
							player.sendMessage(ChatColor.RED
									+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.NotVip"));
							return false;
						}
						sendToHomeLocation(player, getHomes, getHomeName(args[0]));
					}
					return true;
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("delhome")) {
				if (player.hasPermission("homespawn.vip") || player.hasPermission("homespawn.admin")) {
					FileConfiguration getHomes = ConfigSingleton.getInstance(plugin).getPlayerConfig(player);
					List<String> list = getHomes.getStringList("List");
					if (args.length == 1) {
						if (list.contains(getHomeName(args[0]))) {
							list.remove(getHomeName(args[0]));
							getHomes.set("List", list);
							int HomesNumb = getHomes.getInt("Numb");
							getHomes.set("Numb", HomesNumb - 1);
							ConfigSingleton.getInstance(plugin).savePlayerConfig(player);
							player.sendMessage(ChatColor.GOLD
									+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.HomeRemoved"));
							return true;
						} else {
							showHomeNotFound(player, getHomes, list);
						}
					} else {
						player.sendMessage(
								ChatColor.RED + ConfigSingleton.getInstance(plugin).Messages.getString("Error.Args"));
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("setspawn")) {
				if (player.hasPermission("homespawn.admin")) {
					FileConfiguration getSpawn = ConfigSingleton.getInstance(plugin).Spawn;
					if (args.length == 0) {
						getSpawn.set("spawn.SpawnSet", "Yes");
						getSpawn.set("spawn.X", player.getLocation().getBlockX());
						getSpawn.set("spawn.Y", player.getLocation().getBlockY());
						getSpawn.set("spawn.Z", player.getLocation().getBlockZ());
						getSpawn.set("spawn.World", player.getWorld().getName());
						getSpawn.set("spawn.Yaw", player.getLocation().getYaw());
						getSpawn.set("spawn.Pitch", player.getLocation().getPitch());
						player.sendMessage(ChatColor.GOLD
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.SpawnSet"));
					} else if (args[0].equalsIgnoreCase("new")) {
						getSpawn.set("spawnnew.SpawnSet", "Yes");
						getSpawn.set("spawnnew.X", player.getLocation().getBlockX());
						getSpawn.set("spawnnew.Y", player.getLocation().getBlockY());
						getSpawn.set("spawnnew.Z", player.getLocation().getBlockZ());
						getSpawn.set("spawnnew.World", player.getWorld().getName());
						getSpawn.set("spawnnew.Yaw", player.getLocation().getYaw());
						getSpawn.set("spawnnew.Pitch", player.getLocation().getPitch());
						player.sendMessage(ChatColor.GOLD
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.SpawnNewSet"));
					} else {
						if (player.hasPermission("homespawn.help"))
							help(player, "2");
						return false;
					}
					ConfigSingleton.getInstance(plugin).saveSpawn();
					return true;
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equals("spawn")) {
				if (player.hasPermission("homespawn.player")) {
					FileConfiguration getSpawn = ConfigSingleton.getInstance(plugin).Spawn;
					if (getSpawn.contains("spawn.SpawnSet")
							&& getSpawn.getString("spawn.SpawnSet").equalsIgnoreCase("yes")) {
						int x = getSpawn.getInt("spawn.X");
						int y = getSpawn.getInt("spawn.Y");
						int z = getSpawn.getInt("spawn.Z");
						float yaw = getSpawn.getInt("spawn.Yaw");
						float pitch = getSpawn.getInt("spawn.Pitch");
						String cworld = getSpawn.getString("spawn.World");
						World world = plugin.getServer().getWorld(cworld);
						Location Spawn = new Location(world, x, y, z, yaw, pitch);
						Spawn.add(0.5, 0, 0.5);
						TeleportPlayer(player, Spawn, "Spawn");
						return true;
					} else {
						player.sendMessage(
								ChatColor.RED + ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.NotSet"));
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("delspawn")) {
				if (player.hasPermission("homespawn.admin")) {
					FileConfiguration getSpawn = ConfigSingleton.getInstance(plugin).Spawn;
					if (!getSpawn.contains("spawn.SpawnSet")
							|| getSpawn.getString("spawn.SpawnSet").equalsIgnoreCase("no")) {
						player.sendMessage(
								ChatColor.RED + ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.NotSet"));
					} else if (getSpawn.getString("spawn.SpawnSet").equalsIgnoreCase("yes")) {
						getSpawn.set("spawn.SpawnSet", "No");
						player.sendMessage(ChatColor.GOLD
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Spawn.Removed"));
						ConfigSingleton.getInstance(plugin).saveSpawn();
						return true;
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("homelist")) {
				if (player.hasPermission("homespawn.player")) {
					FileConfiguration getHomes = ConfigSingleton.getInstance(plugin).getPlayerConfig(player);
					List<String> list = getHomes.getStringList("List");
					player.sendMessage(ChatColor.GOLD + "Your Current Homes Are:");
					if (getHomes.getInt("Numb") > 0) {
						if (!list.isEmpty()) {
							String list2 = list.toString();
							String list3 = list2.replace("[", " ");
							String StringList = list3.replace("]", " ");
							player.sendMessage(ChatColor.RED + StringList);
						}
						return true;
					} else {
						player.sendMessage(ChatColor.DARK_RED
								+ ConfigSingleton.getInstance(plugin).Messages.getString("Home.NoHomeSet"));
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("homespawn")) {
				if (args.length == 0) {
					player.sendMessage(ChatColor.GOLD + "---------------" + ChatColor.RED + "Homespawn" + ChatColor.GOLD
							+ "---------------");
					player.sendMessage(ChatColor.RED + "Author:" + ChatColor.GOLD + " Dart2112, Ardamedonner");
					player.sendMessage(
							ChatColor.RED + "Version: " + ChatColor.GOLD + plugin.getDescription().getVersion());
					player.sendMessage(ChatColor.RED + "Bukkit Dev:" + ChatColor.GOLD + " http://goo.gl/2Selqa");
					if (player.hasPermission("homespawn.help")) {
						player.sendMessage(ChatColor.RED + "Use /homespawn help 1 for home commands!");
						player.sendMessage(ChatColor.RED + "Use /homespawn help 2 for spawn commands!");
						player.sendMessage(ChatColor.RED + "Use /homespawn help 3 for tpa commands!");
					}
					player.sendMessage(ChatColor.GOLD + "-----------------------------------------");
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						if (player.hasPermission("homespawn.admin")) {
							try {
								plugin.reload(player);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							player.sendMessage(ChatColor.RED
									+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
						}
					} else if (args[0].equalsIgnoreCase("help")) {
						if (player.hasPermission("homespawn.help")) {
							help(player, args.length > 1 ? args[1] : "0");
						}
					}
					return true;
				} else {
					player.sendMessage("That Is Not A Recognised Command, Use /homespawn help For Commands");
				}
			} else if (cmd.getName().equalsIgnoreCase("homepassword")) {
				FileConfiguration getPasswords = ConfigSingleton.getInstance(plugin).Passwords;
				if (!plugin.getServer().getOnlineMode()) {
					String string = args[0];
					if (args.length == 3) {
						if (string.equalsIgnoreCase("set")) {
							if (args[1] == args[2]) {
								String pass = args[1];
								String passHash = null;
								try {
									passHash = PasswordHash.createHash(pass);
								} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
									e1.printStackTrace();
									player.sendMessage(ChatColor.RED + "Failed To Save Password!");
									return false;
								}
								getPasswords.set(player.getUniqueId().toString(), passHash);
								player.sendMessage("Password Set To:");
								player.sendMessage(pass);
								ConfigSingleton.getInstance(plugin).savePasswords();
							} else {
								player.sendMessage(ChatColor.RED + "Your 2 passwords didn't match!");
							}
						}
						return true;
					} else if (args.length == 1) {
						help(player, "4");
					}
				} else {
					player.sendMessage("This Command Isnt Used As This Is An Online Mode Server");
				}

			} else if (cmd.getName().equalsIgnoreCase("setglobalhome")) {
				if (player.hasPermission("homespawn.admin")) {
					if (args.length == 0) {
						player.sendMessage(ChatColor.RED + "You need to specify a name");
					} else if (args.length == 1) {
						FileConfiguration getGlobalHomes = ConfigSingleton.getInstance(plugin).GlobalHomes;
						String home = args[1];
						if (!getGlobalHomes.contains(home))
							getGlobalHomes.createSection(home);
						if (!getGlobalHomes.contains(home + ".x"))
							getGlobalHomes.createSection(home + ".x");
						if (!getGlobalHomes.contains(home + ".y"))
							getGlobalHomes.createSection(home + ".y");
						if (!getGlobalHomes.contains(home + ".z"))
							getGlobalHomes.createSection(home + ".z");
						if (!getGlobalHomes.contains(home + ".world"))
							getGlobalHomes.createSection(home + ".world");
						if (!getGlobalHomes.contains(home + ".Yaw"))
							getGlobalHomes.createSection(home + ".Yaw");
						if (!getGlobalHomes.contains(home + ".Pitch"))
							getGlobalHomes.createSection(home + ".Pitch");
						getGlobalHomes.set(home + ".x", player.getLocation().getBlockX());
						getGlobalHomes.set(home + ".y", player.getLocation().getBlockY());
						getGlobalHomes.set(home + ".z", player.getLocation().getBlockZ());
						getGlobalHomes.set(home + ".world", player.getWorld().getName());
						getGlobalHomes.set(home + ".Yaw", player.getLocation().getYaw());
						getGlobalHomes.set(home + ".Pitch", player.getLocation().getPitch());
						List<String> list = getGlobalHomes.getStringList("List");
						if (!list.contains(home)) {
							list.add(home);
							getGlobalHomes.set("List", list);
						}
						ConfigSingleton.getInstance(plugin).saveGlobalHomes();
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "To much infomation");
						player.sendMessage(ChatColor.RED + "Usage: /setglobalhome (home name)");
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED
							+ ConfigSingleton.getInstance(plugin).Messages.getString("Error.Permission"));
				}
			} else if (cmd.getName().equalsIgnoreCase("tpa")) {
				// ---------------------------------------------------
				// tpa stuff (taken from other tpa plugin)
				// ---------------------------------------------------
				if (!player.hasPermission("tpa.bypassdelay")) {
					int cooldown = plugin.getConfig().getInt("TpaCoolDown");
					if (tpaCooldown.containsKey(player.getName())) {
						long diff = (System.currentTimeMillis()
								- ((Long) tpaCooldown.get(sender.getName())).longValue()) / 1000L;
						if (diff < (long) cooldown) {
							player.sendMessage(ChatColor.RED + "TPA: You must wait " + cooldown
									+ " seconds between sending teleport requests!");
							return false;
						}
					}
				}
				if (args.length > 0) {
					long keepAlive = plugin.getConfig().getLong("TpaKeepAlive") * 20L;
					@SuppressWarnings("deprecation")
					final Player target = plugin.getServer().getPlayer(args[0]);
					if (target == null) {
						sender.sendMessage(
								ChatColor.RED + "TPA: You can only send a teleport request to online players!");
						return false;
					}
					if (target == player) {
						sender.sendMessage(ChatColor.RED + "TPA: You can't teleport to yourself!");
						return false;
					}
					sendRequest(player, target);
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						public void run() {
							killRequest(target.getName());
						}

					}, keepAlive);
					tpaCooldown.put(player.getName(), Long.valueOf(System.currentTimeMillis()));
				} else {
					player.sendMessage("To send a teleport request to a player use:");
					player.sendMessage("/tpa [playerName]");
				}
			}
			if (cmd.getName().equalsIgnoreCase("tpaccept")) {
				if (currentRequest.containsKey(player.getName())) {
					@SuppressWarnings("deprecation")
					Player heIsGoingOutOnADate = plugin.getServer()
							.getPlayer((String) currentRequest.get(player.getName()));
					currentRequest.remove(player.getName());
					if (heIsGoingOutOnADate != null) {
						heIsGoingOutOnADate.teleport(player);
						player.sendMessage(ChatColor.GRAY + "TPA: Teleporting...");
						heIsGoingOutOnADate.sendMessage(ChatColor.GRAY + "TPA: Teleporting...");
					} else {
						sender.sendMessage(ChatColor.RED + "TPA: The player you tried to teleport to is offline!");
						return false;
					}
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED)
							.append("(TPA) You don't have open tpa requests.").toString());
					return false;
				}
			}
			if (cmd.getName().equalsIgnoreCase("tpdeny")) {
				if (currentRequest.containsKey(player.getName())) {
					@SuppressWarnings("deprecation")
					Player poorRejectedGuy = plugin.getServer()
							.getPlayer((String) currentRequest.get(player.getName()));
					currentRequest.remove(player.getName());
					if (poorRejectedGuy != null) {
						poorRejectedGuy
								.sendMessage(ChatColor.RED + player.getName() + " rejected your teleport request!");
						player.sendMessage(ChatColor.GRAY + poorRejectedGuy.getName() + " was rejected!");
						return true;
					} else {
						return true;
					}
				} else {
					sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("(TPA) No open tp requests!")
							.toString());
					return false;
				}
			} else {
				plugin.logger.severe("WTF!?! Unregistered command here?");
				return false;
			}
		} else {
			sender.sendMessage("You Must Be a Player To Do That");
		}
		return false;
	}

	private void help(Player player, String pageNumber) {
		if (player != null && player.hasPermission("homespawn.help")) {
			switch (pageNumber) {
			case "1":
				player.sendMessage(ChatColor.GOLD + "---------------------" + ChatColor.RED
						+ "Homespawn home help (1/4)" + ChatColor.GOLD + "---------------------");
				player.sendMessage(ChatColor.RED + "/homelist:" + ChatColor.GOLD + " Lists all specified home");
				player.sendMessage(
						ChatColor.RED + "/sethome:" + ChatColor.GOLD + " Sets your home at your current location");
				player.sendMessage(ChatColor.RED + "/home:" + ChatColor.GOLD + " Sends you to your home");
				player.sendMessage(ChatColor.GOLD + "VIP Only:");
				player.sendMessage(ChatColor.RED + "/sethome [name]:" + ChatColor.GOLD
						+ " Sets your home at your current location");
				player.sendMessage(
						ChatColor.RED + "/home [name]:" + ChatColor.GOLD + " Sends you to the home specified");
				player.sendMessage(ChatColor.RED + "/delhome [name]:" + ChatColor.GOLD + " Removes the specified home");
				break;
			case "2":
				player.sendMessage(ChatColor.GOLD + "-----------------------" + ChatColor.RED
						+ "Homespawn spawn help (2/4)" + ChatColor.GOLD + "-----------------------");
				player.sendMessage(ChatColor.RED + "/spawn:" + ChatColor.GOLD + " Sends you to spawn");
				if (player.hasPermission("homespawn.admin")) {
					player.sendMessage(ChatColor.RED + "/setspawn:" + ChatColor.GOLD + " Sets the server spawn");
					player.sendMessage(ChatColor.RED + "/setspawn new:" + ChatColor.GOLD
							+ " All New Players Will Be Sent To This Spawn");
					player.sendMessage(ChatColor.RED + "/delspawn:" + ChatColor.GOLD + " Removes the server spawn");
					player.sendMessage(ChatColor.RED + "/homespawn:" + ChatColor.GOLD + " Displays plugin infomation");
					player.sendMessage(
							ChatColor.RED + "/homespawn reload:" + ChatColor.GOLD + " Reloads the plugin configs");
					return;
				}
				break;
			case "3":
				player.sendMessage(ChatColor.GOLD + "-----------------------" + ChatColor.RED
						+ "Homespawn tpa help (3/4)" + ChatColor.GOLD + "-----------------------");
				player.sendMessage(ChatColor.RED + "/tpa [playerName]:" + ChatColor.GOLD + " Sends a teleport request to playerName");
				player.sendMessage(ChatColor.RED + "/tpaccept:" + ChatColor.GOLD + " Accepts a teleport request");
				player.sendMessage(ChatColor.RED + "/tpdeny:" + ChatColor.GOLD + " Denies a teleport request");
				break;
			case "4":
				player.sendMessage(ChatColor.GOLD + "---------------------" + ChatColor.RED
						+ "Homespawn password help (4/4)" + ChatColor.GOLD + "---------------------");
				player.sendMessage(ChatColor.RED + "/homepassword help:" + ChatColor.GOLD + " Shows This Text");
				player.sendMessage(ChatColor.RED + "/homepassword set [password] [password]:" + ChatColor.GOLD
						+ " Sets Your Transfer Password");
				player.sendMessage(ChatColor.RED + "/homepassword transfer [old username] [password]:" + ChatColor.GOLD
						+ " Transfers Playerdata From Old Username To Current Username");
				break;
			default:
				player.sendMessage(ChatColor.GOLD + "---------------------" + ChatColor.RED + "Homespawn help"
						+ ChatColor.GOLD + "---------------------");
				player.sendMessage(ChatColor.RED + "Use /homespawn help 1 for home commands!");
				player.sendMessage(ChatColor.RED + "Use /homespawn help 2 for spawn commands!");
				player.sendMessage(ChatColor.RED + "Use /homespawn help 3 for tpa commands!");
				player.sendMessage(ChatColor.RED + "Use /homespawn help 4 for password command!");
			}
			player.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
		} else {
			return;
		}
	}

	private String getHomeName(String name) {
		return name.toLowerCase().replace('.', '_');
	}

	private void sendToHomeLocation(Player player, FileConfiguration getHomes, String homeName) {
		List<String> list = getHomes.getStringList("List");
		if (list.contains(homeName) && getHomes.contains(homeName + ".x") && getHomes.contains(homeName + ".y")
				&& getHomes.contains(homeName + ".z") && getHomes.contains(homeName + ".world")
				&& getHomes.contains(homeName + ".Yaw") && getHomes.contains(homeName + ".Pitch")) {
			int x = getHomes.getInt(homeName + ".x");
			int y = getHomes.getInt(homeName + ".y");
			int z = getHomes.getInt(homeName + ".z");
			float yaw = getHomes.getInt(homeName + ".Yaw");
			float pitch = getHomes.getInt(homeName + ".Pitch");
			String cworld = getHomes.getString(homeName + ".world");
			World world = plugin.getServer().getWorld(cworld);
			Location home = new Location(world, x, y, z, yaw, pitch);
			home.add(0.5, 0, 0.5);
			TeleportPlayer(player, home, homeName);
		} else {
			showHomeNotFound(player, getHomes, list);
		}
	}

	private void showHomeNotFound(Player player, FileConfiguration getHomes, List<String> list) {
		player.sendMessage(ChatColor.RED + ConfigSingleton.getInstance(plugin).Messages.getString("Home.NotFound"));
		if (getHomes.getInt("Numb") > 0) {
			if (!list.isEmpty()) {
				String list2 = list.toString();
				String list3 = list2.replace("[", " ");
				String StringList = list3.replace("]", " ");
				player.sendMessage(
						ChatColor.GOLD + ConfigSingleton.getInstance(plugin).Messages.getString("Home.Current"));
				player.sendMessage(ChatColor.RED + StringList);
			}
		}
	}

	private void createHomeLocation(Player player, FileConfiguration getHomes, String homeName) {
		int HomesNumb = getHomes.getInt("Numb");
		getHomes.set("HasHome", "Yes");
		getHomes.set("Numb", HomesNumb + 1);
		List<String> list = getHomes.getStringList("List");
		if (!list.contains(homeName)) {
			list.add(homeName);
			getHomes.set("List", list);
			if (!getHomes.contains(homeName + ".x"))
				getHomes.createSection(homeName + ".x");
			if (!getHomes.contains(homeName + ".y"))
				getHomes.createSection(homeName + ".y");
			if (!getHomes.contains(homeName + ".z"))
				getHomes.createSection(homeName + ".z");
			if (!getHomes.contains(homeName + ".world"))
				getHomes.createSection(homeName + ".world");
			if (!getHomes.contains(homeName + ".Yaw"))
				getHomes.createSection(homeName + ".Yaw");
			if (!getHomes.contains(homeName + ".Pitch"))
				getHomes.createSection(homeName + ".Pitch");
		}
		getHomes.set(homeName + ".x", player.getLocation().getBlockX());
		getHomes.set(homeName + ".y", player.getLocation().getBlockY());
		getHomes.set(homeName + ".z", player.getLocation().getBlockZ());
		getHomes.set(homeName + ".world", player.getWorld().getName());
		getHomes.set(homeName + ".Yaw", player.getLocation().getYaw());
		getHomes.set(homeName + ".Pitch", player.getLocation().getPitch());
	}

	public boolean killRequest(String key) {
		if (currentRequest.containsKey(key)) {
			@SuppressWarnings("deprecation")
			Player loser = plugin.getServer().getPlayer(currentRequest.get(key));
			if (loser != null)
				loser.sendMessage(ChatColor.RED + "Your teleport request timed out.");
			currentRequest.remove(key);
			return true;
		} else {
			return false;
		}
	}

	public void sendRequest(Player sender, Player recipient) {
		sender.sendMessage("Sending a teleport request to " + recipient.getName() + ".");
		String sendtpaccept = "";
		String sendtpdeny = "";
		if (recipient.hasPermission("simpletp.tpaccept"))
			sendtpaccept = "To accept the teleport request, type " + ChatColor.RED + "/tpaccept" + ChatColor.RESET
					+ ".";
		else
			sendtpaccept = "";
		if (recipient.hasPermission("simpletp.tpdeny"))
			sendtpdeny = "To deny the teleport request, type " + ChatColor.RED + "/tpdeny" + ChatColor.RESET + ".";
		else
			sendtpdeny = "";
		recipient.sendMessage(ChatColor.RED + sender.getName() + ChatColor.RESET
				+ " has sent a request to teleport to you." + sendtpaccept + sendtpdeny);
		currentRequest.put(recipient.getName(), sender.getName());
	}

}
