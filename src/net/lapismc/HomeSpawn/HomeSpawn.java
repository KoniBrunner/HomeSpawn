package net.lapismc.HomeSpawn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mcstats.Metrics;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HomeSpawn extends JavaPlugin implements Listener {

	public HomeSpawn plugin;
	public Permission PlayerPerm = new Permission("homespawn.player");
	public Permission AdminPerm = new Permission("homespawn.admin");
	public Permission VIPPerm = new Permission("homespawn.vip");
	HashMap<Player, Location> HomeSpawnLocations = new HashMap<Player, Location>();
	HashMap<Player, Integer> HomeSpawnTimeLeft = new HashMap<Player, Integer>();
	public HomeSpawnListener pl;
	public final Logger logger = this.getLogger();

	@Override
	public void onEnable() {
		Update();
		Enable();
		try {
			Configs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Metrics();
		Commands();
		CommandDelay();
	}

	private void Metrics() {
		if (getConfig().getBoolean("Metrics")) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe("[HomeSpawn] Metrics Failed To Start!");
			}
		} else {
			getLogger().info("Metrics wasnt started because it is disabled in the config!");
		}
	}

	private void Update() {
		if (getConfig().getBoolean("AutoUpdate")) {
			Updater updater = new Updater(this, 86785, this.getFile(), UpdateType.DEFAULT, true);
			updatecheck(updater);
		} else {
			Updater updater = new Updater(this, 86785, this.getFile(), UpdateType.NO_DOWNLOAD, true);
			updatecheck(updater);
		}
	}

	private void updatecheck(Updater updater) {
		File file = new File(this.getDataFolder().getAbsolutePath() + File.separator + "Update.yml");
		FileConfiguration getUpdate = YamlConfiguration.loadConfiguration(file);
		if (updater.getResult() == UpdateResult.SUCCESS) {
			this.getLogger().info("Updated, Reload or restart to install the update!");
		} else if (updater.getResult() == UpdateResult.NO_UPDATE) {
			this.getLogger().info("No Update Available");
			if (file.exists()) {
				if (getUpdate.contains("Avail")) {
					getUpdate.set("Avail", "false");
				} else {
					getUpdate.createSection("Avail");
					try {
						getUpdate.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					getUpdate.set("Avail", "false");
				}
			} else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				getUpdate.createSection("Avail");
				try {
					getUpdate.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				getUpdate.set("Avail", "false");
				try {
					getUpdate.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else
			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE && updater.getResult() != UpdateResult.SUCCESS) {
			this.getLogger().info("An update is Available for HomeSpawn, It can be downloaded from,"
					+ " dev.bukkit.org/bukkit-plugins/homespawn");
			if (file.exists()) {
				if (!getConfig().getBoolean("AutoUpdate")) {
					if (getUpdate.contains("Avail")) {
						getUpdate.set("Avail", "true");
					} else {
						getUpdate.createSection("Avail");
						getUpdate.set("Avail", "true");
						try {
							getUpdate.save(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				getUpdate.createSection("Avail");
				getUpdate.set("Avail", "true");
				try {
					getUpdate.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			this.getLogger().severe(ChatColor.RED + "Something Went Wrong Updating!");
			getUpdate.set("Avail", "false");
		}
		try {
			getUpdate.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		Disable();
	}

	public void Enable() {
		logger.info(" V." + getDescription().getVersion() + " Has Been Enabled!");
		PluginManager pm = getServer().getPluginManager();
		pl = new HomeSpawnListener(this);
		pm.registerEvents(this.pl, this);
	}

	public void Disable() {
		logger.info("[HomeSpawn] Plugin Has Been Disabled!");
		HandlerList.unregisterAll();
	}

	public void Configs() throws IOException {
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		ConfigSingleton.getInstance(this);
	}

	public void reload(Player player) throws IOException {
		if (player != null) { // TODO no rights check to reload?
			ConfigSingleton.Reset();
			Configs();
			player.sendMessage(ChatColor.GOLD + "You have reloaded the configs for Homespawn!");
			Bukkit.broadcast(ChatColor.GOLD + "Player " + ChatColor.RED + player.getName() + ChatColor.GOLD
					+ " Has Reloaded Homespawn!", "homespawn.admin");
			this.logger.info("Player " + player.getName() + " Has Reloaded Homespawn!");
		}
	}

	public void help(Player player) {
		if (player != null) { // TODO no rights check for help?
			player.sendMessage(ChatColor.GOLD + "-----------------------" + ChatColor.RED + "Homespawn" + ChatColor.GOLD
					+ "-----------------------");
			player.sendMessage(ChatColor.RED + "[name] = VIP Only");
			player.sendMessage(ChatColor.RED + "/home [name]:" + ChatColor.GOLD + " Sends You To The Home Specified");
			player.sendMessage(
					ChatColor.RED + "/sethome [name]:" + ChatColor.GOLD + " Sets Your Home At Your Current Location");
			player.sendMessage(ChatColor.RED + "/delhome [name]:" + ChatColor.GOLD + " Removes The Specified Home");
			player.sendMessage(ChatColor.RED + "/spawn:" + ChatColor.GOLD + " Sends You To Spawn");
			if (player.hasPermission("homespawn.admin")) {
				player.sendMessage(ChatColor.RED + "/setspawn:" + ChatColor.GOLD + " Sets The Server Spawn");
				player.sendMessage(ChatColor.RED + "/setspawn new:" + ChatColor.GOLD
						+ " All New Players Will Be Sent To This Spawn");
				player.sendMessage(ChatColor.RED + "/delspawn:" + ChatColor.GOLD + " Removes The Server Spawn");
				player.sendMessage(ChatColor.RED + "/homespawn:" + ChatColor.GOLD + " Displays Plugin Infomation");
				player.sendMessage(
						ChatColor.RED + "/homespawn reload:" + ChatColor.GOLD + " Reloads The Plugin Configs");
				player.sendMessage(ChatColor.GOLD + "---------------------------------------------------------");
				return;
			} else {
				player.sendMessage(ChatColor.GOLD + "---------------------------------------------------------");
			}

		} else {
			return;
		}
	}

	public void CommandDelay() {
		if (!getConfig().contains("TeleportTime")) {
			getConfig().createSection("TeleportTime");
			saveConfig();
			getConfig().set("TeleportTime", 0);
		}
		if (!(getConfig().getInt("TeleportTime") <= 0)) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					if (!HomeSpawnTimeLeft.isEmpty()) {
						for (Player p : HomeSpawnTimeLeft.keySet()) {
							if (HomeSpawnLocations.get(p) == null) {
								HomeSpawnTimeLeft.remove(p);
								HomeSpawnLocations.remove(p);
							}
							if (HomeSpawnTimeLeft.isEmpty()) {
								return;
							}
							for (int Time : HomeSpawnTimeLeft.values()) {
								int NewTime = Time - 1;
								if (NewTime > 0) {
									HomeSpawnTimeLeft.put(p, NewTime);
								} else if (NewTime <= 0) {
									Location Tele = HomeSpawnLocations.get(p);
									if (!Tele.equals(null)) {
										p.teleport(Tele);
										p.sendMessage(ChatColor.GOLD + "Teleporting...");
										HomeSpawnTimeLeft.remove(p);
										HomeSpawnLocations.remove(p);
									} else {
										HomeSpawnTimeLeft.remove(p);
										HomeSpawnLocations.remove(p);
									}
								}
							}
						}
					}
				}
			}, 0, 20);
		} else {
			return;
		}
	}

	public void Commands() {
		this.getCommand("home").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("sethome").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("delhome").setExecutor(new HomeSpawnCommand(this));
		// this.getCommand("globalhome").setExecutor(new
		// HomeSpawnCommand(this));
		// this.getCommand("setglobalhome")
		// .setExecutor(new HomeSpawnCommand(this));
		// this.getCommand("delglobalhome")
		// .setExecutor(new HomeSpawnCommand(this));
		this.getCommand("spawn").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("setspawn").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("delspawn").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("homespawn").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("homepassword").setExecutor(new HomeSpawnCommand(this));
		this.getCommand("homeslist").setExecutor(new HomeSpawnCommand(this));
	}
}