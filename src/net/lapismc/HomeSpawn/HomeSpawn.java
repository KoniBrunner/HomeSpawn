package net.lapismc.HomeSpawn;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mcstats.Metrics;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateCallback;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

public class HomeSpawn extends JavaPlugin implements Listener, UpdateCallback {

	public HomeSpawn plugin;
	public Permission PlayerPerm = new Permission("homespawn.player");
	public Permission AdminPerm = new Permission("homespawn.admin");
	public Permission VIPPerm = new Permission("homespawn.vip");
	public HashMap<Player, Location> HomeSpawnLocations = new HashMap<Player, Location>();
	public HashMap<Player, Integer> HomeSpawnTimeLeft = new HashMap<Player, Integer>();
	public HomeSpawnListener pl;
	public final Logger logger = this.getLogger();

	@Override
	public void onEnable() {
		Configs();
		Update();
		Enable();
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
				logger.severe("Metrics Failed To Start!");
			}
		} else {
			getLogger().info("Metrics wasnt started because it is disabled in the config!");
		}
	}

	private void Update() {
		if (getConfig().getBoolean("AutoUpdate")) {
			new Updater(this, 86785, this.getFile(), UpdateType.DEFAULT, this, true);
		} else {
			new Updater(this, 86785, this.getFile(), UpdateType.NO_DOWNLOAD, this, true);
		}
	}

	@Override
	public void onFinish(Updater updater) {
		// Called after update has finished
		FileConfiguration getUpdate = ConfigSingleton.getInstance(this).Update;
		if (updater.getResult() == UpdateResult.SUCCESS) {
			this.getLogger().info("Updated, Reload or restart to install the update!");
			getUpdate.set("Avail", "false");
		} else if (updater.getResult() == UpdateResult.NO_UPDATE) {
			this.getLogger().info("No Update Available");
			getUpdate.set("Avail", "false");
		} else if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			this.getLogger().info("An update is Available for HomeSpawn, It can be downloaded from,"
					+ " dev.bukkit.org/bukkit-plugins/homespawn");
			getUpdate.set("Avail", "true");
		} else {
			this.getLogger().severe(ChatColor.RED + "Something Went Wrong Updating!");
			getUpdate.set("Avail", "false");
		}
		ConfigSingleton.getInstance(this).saveUpdate();
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
		logger.info("Plugin Has Been Disabled!");
		HandlerList.unregisterAll();
	}

	public void Configs() {
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
		HomeSpawnCommand command = new HomeSpawnCommand(this);
		this.getCommand("home").setExecutor(command);
		this.getCommand("sethome").setExecutor(command);
		this.getCommand("delhome").setExecutor(command);
		this.getCommand("homelist").setExecutor(command);
		// this.getCommand("globalhome").setExecutor(new
		// HomeSpawnCommand(this));
		// this.getCommand("setglobalhome")
		// .setExecutor(new HomeSpawnCommand(this));
		// this.getCommand("delglobalhome")
		// .setExecutor(new HomeSpawnCommand(this));
		this.getCommand("spawn").setExecutor(command);
		this.getCommand("setspawn").setExecutor(command);
		this.getCommand("delspawn").setExecutor(command);
		this.getCommand("homespawn").setExecutor(command);
		this.getCommand("homepassword").setExecutor(command);
		this.getCommand("tpa").setExecutor(command);
		this.getCommand("tpaccept").setExecutor(command);
		this.getCommand("tpdeny").setExecutor(command);
	}

}