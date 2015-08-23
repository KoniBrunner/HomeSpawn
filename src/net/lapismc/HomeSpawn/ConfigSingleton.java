package net.lapismc.HomeSpawn;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class ConfigSingleton {
	private static ConfigSingleton instance = null;
	private JavaPlugin plugin = null;
	private Logger logger = null;
	private Map<String, FileConfiguration> playerConfigs = new HashMap<String, FileConfiguration>();

	public File PlayerDataDir = null;
	public FileConfiguration Spawn = null;
	public FileConfiguration GlobalHomes = null;
	public FileConfiguration Messages = null;
	public FileConfiguration Update = null;
	
	protected ConfigSingleton(JavaPlugin plugin) {
		this.plugin = plugin;
		logger = plugin.getLogger();
		createPlayerData();
		createSpawn();
		createGlobalHomes();
		createUpdate();
		createMessages();
		createPasswords();
	}

	public static ConfigSingleton getInstance(JavaPlugin plugin) {
		if (instance == null) {
			instance = new ConfigSingleton(plugin);
		}
		return instance;
	}
	
	public FileConfiguration getPlayerConfig(Player player)
	{
		if (playerConfigs.containsKey(player.getUniqueId().toString()))
		{
			return playerConfigs.get(player.getUniqueId().toString());
		}
		else
		{
			File file = new File(plugin.getDataFolder() + File.separator + "PlayerData" + File.separator + player.getUniqueId().toString() + ".yml");
			FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
			
			if (!file.exists()) {
				logger.info("[HomeSpawn] Creating Player("+player.getUniqueId().toString()+") configuration file!");
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.severe("[HomeSpawn] Couldn't create player file!");
					e.printStackTrace();
				}
			}
			
			boolean changedSomething = false;
			if (!playerConfig.contains("Name")) {
				playerConfig.createSection("Name");
				playerConfig.set("Name", player.getName());
				changedSomething = true;
			}
			if (!playerConfig.contains("UUID")) {
				playerConfig.createSection("UUID");
				playerConfig.set("UUID", player.getUniqueId().toString());
				changedSomething = true;
			}
			if (!playerConfig.contains("HasHome")) {
				playerConfig.createSection("HasHome");
				playerConfig.set("HasHome", "No");
				changedSomething = true;
			}
			if (!playerConfig.contains("Numb")) {
				playerConfig.createSection("Numb");
				playerConfig.set("Numb", 0);
				changedSomething = true;
			}
			if (playerConfig.contains(player.getName() + ".Numb")) {
				playerConfig.set("Numb", playerConfig.getInt(player.getName() + ".Numb"));
				changedSomething = true;
			}
			if (!playerConfig.getString("Name").equals(player.getName())) {
				playerConfig.set("Name", player.getName());
				changedSomething = true;
			}

			if (changedSomething) {
				try {
					playerConfig.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			playerConfigs.put(player.getUniqueId().toString(), playerConfig);
			
			return playerConfig;
		}
	}

	private void createPlayerData() {
		PlayerDataDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "PlayerData");
		if (!PlayerDataDir.exists()) {
			logger.info("[HomeSpawn] Creating PlayerData Directory!");
			try {
				boolean done = PlayerDataDir.mkdir();
				if (!done)
					throw new IOException("Can't create directory " + PlayerDataDir.getAbsolutePath());
			} catch (Exception e) {
				logger.severe("[HomeSpawn] Couldn't create PlayerData directory!");
				e.printStackTrace();
			}
		}
	}

	private void createSpawn() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawn.yml");
		Spawn = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("[HomeSpawn] Creating Spawn configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("[HomeSpawn] Couldn't create spawn file!");
				e.printStackTrace();
			}
		}
		// Better to check independent if file exists or not. that way we can
		// also migrate old files to newer version.
		String[] sections = new String[] { "spawn.X", "spawn.Y", "spawn.Z", "spawn.World", "spawn.Yaw", "spawn.Pitch",
				"spawnnew.X", "spawnnew.Y", "spawnnew.Z", "spawnnew.World", "spawnnew.Yaw", "spawnnew.Pitch" };
		boolean changedSomething = false;
		for (int s = 0; s < sections.length; s++) {
			if (!Spawn.contains(sections[s])) {
				Spawn.createSection(sections[s]);
				changedSomething = true;
			}
		}
		if (changedSomething) {
			try {
				Spawn.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createGlobalHomes() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "GlobalHomes.yml");
		GlobalHomes = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("[HomeSpawn] Creating GlobalHomes configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("[HomeSpawn] Couldn't create GlobalHomes file!");
				e.printStackTrace();
			}
		}
	}

	private void createUpdate() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Update.yml");
		Update = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("[HomeSpawn] Creating Update configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("[HomeSpawn] Couldn't create Update file!");
				e.printStackTrace();
			}
		}
	}

	private void createMessages() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Messages.yml");
		Messages = YamlConfiguration.loadConfiguration(file);
		if (!file.exists()) {
			logger.info("[HomeSpawn] Creating Messages configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("[HomeSpawn] Couldn't create messages file!");
				e.printStackTrace();
			}
		}
		// Better to check independent if file exists or not. that way we can
		// also migrate old files to newer version.
		String[] sections = new String[] { "Home.HomeSet", "Home.SentHome", "Home.NoHomeSet", "Home.HomeRemoved",
				"Home.LimitReached", "Spawn.NotSet", "Spawn.SpawnSet", "Spawn.SpawnNewSet", "Spawn.SentToSpawn",
				"Spawn.Removed", "Wait", "Error.Args+", "Error.Args-", "Error.Args" };
		boolean changedSomething = false;
		for (int s = 0; s < sections.length; s++) {
			if (!Messages.contains(sections[s])) {
				Messages.createSection(sections[s]);
				changedSomething = true;
			}
		}

		String[][] messages = new String[][] { { "Home.HomeSet", "Home Set, You Can Now Use /home" },
				{ "Home.SentHome", "Welcome Home" }, { "Home.NoHomeSet", "You First Need To Set a Home With /sethome" },
				{ "Home.HomeRemoved", "Home Removed" },
				{ "Home.LimitReached",
						"Sorry But You have Reached The Max Limit Of Homes, please Use /delhome To Remove A Home" },
				{ "Spawn.NotSet", "You First Need To Set a Spawn With /setspawn" },
				{ "Spawn.SpawnSet", "Spawn Set, You Can Now Use /spawn" },
				{ "Spawn.SpawnNewSet", "Spawn New Set, All New Players Will Be Sent To This Location" },
				{ "Spawn.SentToSpawn", "Welcome To Spawn" }, { "Spawn.Removed", "Spawn Removed!" },
				{ "Wait",
						"You Must Wait {time} Seconds Before You Can Be Teleported, If You Move Or Get Hit By Another Player Your Teleport Will Be Canceled" },
				{ "Error.Args+", "Too Much Infomation!" }, { "Error.Args-", "Not Enough Infomation" },
				{ "Error.Args", "Too Little or Too Much Infomation" } };
		// If a message is null or empty, we set the default value
		for (int s = 0; s < messages.length; s++) {
			if (Messages.get(messages[s][0]) != null && !Messages.getString(messages[s][0]).isEmpty()) {
				Messages.set(messages[s][0], messages[s][1]);
				changedSomething = true;
			}
		}

		if (changedSomething) {
			try {
				Messages.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createPasswords() {
		File file = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "PlayerData" + File.separator
				+ "Passwords.yml");
		if (!file.exists()) {
			logger.info("[HomeSpawn] Creating Passwords configuration file!");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.severe("[HomeSpawn] Couldn't create passwords file!");
				e.printStackTrace();
			}
		}
	}

}
