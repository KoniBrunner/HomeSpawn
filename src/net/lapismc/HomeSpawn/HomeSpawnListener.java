package net.lapismc.HomeSpawn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HomeSpawnListener implements Listener {
	private final HomeSpawn plugin;

	public HomeSpawnListener(HomeSpawn plugin) {
		this.plugin = plugin;
	}

	List<Player> Players = new ArrayList<Player>();

	@EventHandler(priority = EventPriority.HIGH)
	void PlayerJoinEvent(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		ConfigSingleton.getInstance(plugin).getPlayerConfig(player);
		if (player.hasPermission("homespawn.admin")) {
			if (ConfigSingleton.getInstance(plugin).Update.contains("Avail")
					&& ConfigSingleton.getInstance(plugin).Update.getString("Avail").equalsIgnoreCase("true")) {
				if (!plugin.getConfig().getBoolean("AutoUpdate")
						&& plugin.getConfig().getBoolean("UpdateNotification")) {
					player.sendMessage(ChatColor.GOLD + "[HomeSpawn] An update is available on Bukkit Dev");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void OnPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location From = e.getFrom();
		Location To = e.getTo();
		List<Integer> To1 = new ArrayList<Integer>();
		List<Integer> From1 = new ArrayList<Integer>();
		if (plugin.HomeSpawnLocations.containsKey(p)) {
			if (plugin.HomeSpawnTimeLeft.containsKey(p)) {
				To1.add(To.getBlockX());
				To1.add(To.getBlockY());
				To1.add(To.getBlockZ());
				From1.add(From.getBlockX());
				From1.add(From.getBlockY());
				From1.add(From.getBlockZ());
				if (From1.equals(To1)) {
					return;
				} else {
					if (!Players.contains(p)) {
						plugin.HomeSpawnLocations.put(p, null);
						plugin.HomeSpawnTimeLeft.remove(p);
						p.sendMessage(ChatColor.GOLD + "Teleport Canceled Because You Moved!");
					} else {
						e.setCancelled(true);
						plugin.HomeSpawnTimeLeft.put(p, 1);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void OnPlayerDamage(EntityDamageByEntityEvent e) {
		Entity Hitter = e.getDamager();
		Entity Hit = e.getEntity();
		if (Hit instanceof Player) {
			Player p = ((Player) Hit);
			if (plugin.HomeSpawnTimeLeft.containsKey(p)) {
				if (Hitter instanceof Arrow) {
					final Arrow arrow = (Arrow) Hitter;
					if (arrow.getShooter() instanceof Player) {
						plugin.HomeSpawnLocations.put(p, null);
						p.sendMessage(ChatColor.GOLD + "Teleport Canceled Because You Were Hit!");
					} else if (arrow.getShooter() instanceof Skeleton) {
						Players.add(p);
						e.setCancelled(true);
					}
				}
				if (Hitter instanceof Player || Hitter instanceof Wolf) {
					plugin.HomeSpawnLocations.put(p, null);
					p.sendMessage(ChatColor.GOLD + "Teleport Canceled Because You Were Hit!");
				} else {
					Players.add(p);
					e.setCancelled(true);
				}
				if (Hitter instanceof Player || Hitter instanceof Wolf || Hitter instanceof Arrow) {
					plugin.HomeSpawnLocations.put(p, null);
					plugin.HomeSpawnTimeLeft.remove(p);
					p.sendMessage(ChatColor.GOLD + "Teleport Canceled Because You Were Hit!");
				} else {
					e.setCancelled(false);
				}
			}
		}
	}
}
